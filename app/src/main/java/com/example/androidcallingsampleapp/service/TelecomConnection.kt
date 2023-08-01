package com.example.androidcallingsampleapp.service

import android.content.Context
import android.content.Intent
import android.telecom.Connection
import android.telecom.DisconnectCause
import com.example.androidcallingsampleapp.view.IncomingCallActivity

interface ConnectionStateChangedListener {
    fun onStateChanged(state: Int, connection: TelecomConnection)
}

class TelecomConnection(
    private val context: Context,
    private val stateChangedListeners: MutableList<ConnectionStateChangedListener> = mutableListOf()
) : Connection() {

    init {
        audioModeIsVoip = true
        connectionProperties = PROPERTY_SELF_MANAGED
        setInitializing()
    }

    override fun onStateChanged(state: Int) {
        stateChangedListeners.map { listener ->
            listener.onStateChanged(state, this)
        }
    }

    override fun onAnswer() {
        setActive()
    }

    override fun onReject() {
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
    }

    override fun onDisconnect() {
        setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
    }

    // カスタムのUIを表示する
    override fun onShowIncomingCallUi() {
        val intent = Intent(context, IncomingCallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
}