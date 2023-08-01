package com.example.androidcallingsampleapp.service

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import com.example.androidcallingsampleapp.view.tag


/// 通話開始をOSに伝えるためのサービスクラス
open class TelecomConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d(tag, "onCreateIncomingConnection")
        val bundle = request?.extras?.getBundle(TelecomManager.EXTRA_INCOMING_CALL_EXTRAS)
        val name = bundle?.getString("name")
        val connection = TelecomConnection(this, stateChangedListeners).apply {
            setCallerDisplayName(name, TelecomManager.PRESENTATION_ALLOWED)
            setRinging()
            Log.d(tag, "setCallerDisplayName, setRinging")
        }
        return connection
    }

    companion object {
        private val stateChangedListeners = mutableListOf<ConnectionStateChangedListener>()
        fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
            stateChangedListeners.add(listener)
        }

        fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
            stateChangedListeners.remove(listener)
        }
    }
}