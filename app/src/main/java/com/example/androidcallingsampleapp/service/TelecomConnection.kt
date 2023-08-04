package com.example.androidcallingsampleapp.service


import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import com.example.androidcallingsampleapp.view.tag


interface ConnectionStateChangedListener {
    fun onStateChanged(state: Int, connection: TelecomConnection)
}

class TelecomConnection(
    private val stateChangedListeners: MutableList<ConnectionStateChangedListener>
) : Connection() {

    init {
        connectionProperties = PROPERTY_SELF_MANAGED
        setInitializing()
    }

    override fun onStateChanged(state: Int) {
        logToConsole(state)
        stateChangedListeners.map { listener ->
            listener.onStateChanged(state, this)
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

    private fun logToConsole(state: Int) {
        val platformCallState = when(state) {
            STATE_INITIALIZING -> "INITIALIZING"
            STATE_NEW -> "NEW"
            STATE_DIALING -> "DIALING"
            STATE_RINGING -> "RINGING"
            STATE_ACTIVE -> "ACTIVE"
            STATE_HOLDING -> "HOLDING"
            STATE_DISCONNECTED -> "DISCONNECTED"
            else -> "UNKNOWN"
        }
        Log.d(tag,"onChanged platformCallState: $platformCallState")

    }
}