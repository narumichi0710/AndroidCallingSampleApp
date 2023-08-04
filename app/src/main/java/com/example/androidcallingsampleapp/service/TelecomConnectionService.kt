package com.example.androidcallingsampleapp.service

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.util.Log
import com.example.androidcallingsampleapp.view.tag

class TelecomConnectionService: ConnectionService() {

    // 通話が可能な場合に呼び出される
    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d(tag,"onCreateIncomingConnection $connectionManagerPhoneAccount, $request")
        return TelecomConnection(stateChangedListeners).apply { setRinging() }
    }

    // 通話が不可能な場合に呼び出される
    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Log.d(tag,"onCreateIncomingConnectionFailed $connectionManagerPhoneAccount, $request")
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
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
