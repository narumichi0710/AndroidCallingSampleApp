package com.example.androidcallingsampleapp.service


import android.content.Context
import android.content.Intent
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
    }

    override fun onReject() {
        Log.d(tag,"onReject")
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
    }

    override fun onDisconnect() {
        Log.d(tag,"onDisconnect")
        setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
    }

    override fun onShowIncomingCallUi() {
        Log.d(tag,"onShowIncomingCallUi, $extras")
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