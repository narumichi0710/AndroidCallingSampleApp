package com.example.androidcallingsampleapp.service


import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import android.util.Log
import com.example.androidcallingsampleapp.view.IncomingCallActivity
import com.example.androidcallingsampleapp.view.tag


enum class ConnectionState {
    INITIALIZING,
    NEW,
    DIALING,
    RINGING,
    ACTIVE,
    HOLDING,
    DISCONNECTED,
    UNKNOWN
}

object ConnectionObject {
    const val uuid = "uuid"
    const val title = "title"
    const val content = "content"
    const val isOutgoing = "isOutgoing"
    const val appLinkString = "appLinkString"
    const val startMillis = "startMillis"
}

data class CallRequestData(
    val uuid: String? = null,
    val title: String? = null,
    val content: String? = null,
    val isOutgoing: Boolean,
    val appLinkString: String? = null,
    val startMillis: Long = System.currentTimeMillis()
)

class TelecomConnection(
    private val context: Context,
    private val eventHandler: (Handler) -> Unit
) : Connection() {

    var callState = ConnectionState.INITIALIZING
    var requestData: CallRequestData? = null

    private val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)

    private val vibrator = context.getSystemService(Vibrator::class.java)
    private val pattern = longArrayOf(0, 400, 200, 400)

    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

    init {
        setInitializing()
        connectionProperties = PROPERTY_SELF_MANAGED
        audioModeIsVoip = true
    }

    override fun onStateChanged(state: Int) {
        val callState = getPlatformCallState(state)
        this.callState = callState
        eventHandler(Handler.State(this, callState))
    }

    override fun onAnswer() {
        Log.d(tag, "onAnswer")
        stopRingtoneAndVibrate()
        setActive()
    }

    override fun onReject() {
        Log.d(tag, "onReject")
        stopRingtoneAndVibrate()
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
        destroy()
    }

    fun onMissed() {
        stopRingtoneAndVibrate()
        setDisconnected(DisconnectCause(DisconnectCause.MISSED))
        destroy()
    }

    override fun onDisconnect() {
        Log.d(tag, "onDisconnect")
        setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
        destroy()
    }

    override fun onShowIncomingCallUi() {
        Log.d(tag, "onShowIncomingCallUi, $extras")

        if (keyguardManager.isKeyguardLocked.not()) {
            // TODO: show fullScreenIntent instead of Incoming Screen
        }

        val intent = Intent(context, IncomingCallActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            requestData?.let { putRequestData(it) }
        }

        // TODO: add fadeInOut animation options
        context.startActivity(intent)
        startRingtoneAndVibrate()
    }

    private fun startRingtoneAndVibrate() {
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        ringtone?.play()
    }

    private fun stopRingtoneAndVibrate() {
        vibrator?.cancel()
        ringtone?.stop()
    }

    private fun getPlatformCallState(state: Int): ConnectionState {
        val platformCallState = when (state) {
            STATE_INITIALIZING -> ConnectionState.INITIALIZING
            STATE_NEW -> ConnectionState.NEW
            STATE_DIALING -> ConnectionState.DIALING
            STATE_RINGING -> ConnectionState.RINGING
            STATE_ACTIVE -> ConnectionState.ACTIVE
            STATE_HOLDING -> ConnectionState.HOLDING
            STATE_DISCONNECTED -> ConnectionState.DISCONNECTED
            else -> ConnectionState.UNKNOWN
        }
        Log.d(tag, "onChanged platformCallState: $platformCallState")
        return platformCallState
    }

    // for other events
    sealed class Handler {
        abstract val connection: TelecomConnection

        data class State(
            override val connection: TelecomConnection,
            val state: ConnectionState
        ) : Handler()
    }


    companion object {
        fun getBundle(
            requestData: CallRequestData,
            accountHandle: PhoneAccountHandle
        ): Bundle {
            return Bundle().apply {
                putParcelable(
                    TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
                    accountHandle
                )
                putInt(
                    TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE,
                    VideoProfile.STATE_BIDIRECTIONAL
                )
                putRequestData(requestData)
            }
        }

        fun getNewAccount(
            address: Uri,
            accountHandle: PhoneAccountHandle
        ): PhoneAccount {
            return PhoneAccount.builder(
                accountHandle,
                address.toString()
            ).apply {
                setAddress(address)
                setSupportedUriSchemes(listOf(PhoneAccount.SCHEME_SIP))
                setCapabilities(
                    PhoneAccount.CAPABILITY_SELF_MANAGED or
                            PhoneAccount.CAPABILITY_VIDEO_CALLING or
                            PhoneAccount.CAPABILITY_SUPPORTS_VIDEO_CALLING
                )
            }.build()
        }
    }
}


fun Intent.putRequestData(requestData: CallRequestData): Intent {
    return this.apply {
        putExtra(ConnectionObject.uuid, requestData.uuid)
        putExtra(ConnectionObject.title, requestData.title)
        putExtra(ConnectionObject.content, requestData.content)
        putExtra(ConnectionObject.isOutgoing, requestData.isOutgoing)
        putExtra(ConnectionObject.appLinkString, requestData.appLinkString)
        putExtra(ConnectionObject.startMillis, requestData.startMillis)
    }
}

fun Intent.getRequestData(): CallRequestData? {
    return CallRequestData(
        uuid = getStringExtra(ConnectionObject.uuid),
        title = getStringExtra(ConnectionObject.title),
        content = getStringExtra(ConnectionObject.content),
        isOutgoing = getBooleanExtra(ConnectionObject.isOutgoing, false),
        appLinkString = getStringExtra(ConnectionObject.appLinkString),
        startMillis = getLongExtra(ConnectionObject.startMillis, 0)
    )
}

fun Bundle.putRequestData(requestData: CallRequestData): Bundle {
    return this.apply {
        putString(ConnectionObject.uuid, requestData.uuid)
        putString(ConnectionObject.title, requestData.title)
        putString(ConnectionObject.content, requestData.content)
        putBoolean(ConnectionObject.isOutgoing, requestData.isOutgoing)
        putString(ConnectionObject.appLinkString, requestData.appLinkString)
        putLong(ConnectionObject.startMillis, requestData.startMillis)
    }
}


fun Bundle.getRequestData(): CallRequestData? {
    return CallRequestData(
        uuid = getString(ConnectionObject.uuid),
        title = getString(ConnectionObject.title),
        content = getString(ConnectionObject.content),
        isOutgoing = getBoolean(ConnectionObject.isOutgoing, false),
        appLinkString = getString(ConnectionObject.appLinkString),
        startMillis = getLong(ConnectionObject.startMillis, 0)
    )
}

