package com.example.androidcallingsampleapp.service


import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.telecom.Connection
import android.telecom.DisconnectCause
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

interface ConnectionStateChangedListener {
    fun onStateChanged(state: ConnectionState, connection: TelecomConnection)
}

class TelecomConnection(
    private val context: Context,
    private val stateChangedListeners: MutableList<ConnectionStateChangedListener>
) : Connection() {
    // 着信音
    private val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private val ringtone: Ringtone = RingtoneManager.getRingtone(context, ringtoneUri)

    // バイブレーション
    private val vibrator: Vibrator? = context.getSystemService(Vibrator::class.java)
    private val pattern = longArrayOf(0, 400, 200, 400)

    init {
        connectionProperties = PROPERTY_SELF_MANAGED
        setInitializing()
    }

    override fun onStateChanged(state: Int) {
        val platformCallState = getPlatformCallState(state)
        stateChangedListeners.map { listener ->
            listener.onStateChanged(platformCallState, this)
        }
    }

    override fun onAnswer() {
        Log.d(tag,"onAnswer")
        setActive()
        vibrator?.cancel()
        ringtone.stop()
    }

    override fun onReject() {
        Log.d(tag,"onReject")
        vibrator?.cancel()
        ringtone.stop()
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
    }

    override fun onDisconnect() {
        Log.d(tag,"onDisconnect")
        setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
    }

    override fun onShowIncomingCallUi() {
        Log.d(tag,"onShowIncomingCallUi, $extras")
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        ringtone.play()
        val intent = Intent(context, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            action = Intent.ACTION_VIEW
        }
        context.startActivity(intent)
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
        Log.d(tag,"onChanged platformCallState: $platformCallState")
        return platformCallState
    }
}