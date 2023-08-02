package com.example.androidcallingsampleapp.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.androidcallingsampleapp.view.tag

/// 通話開始をOSに伝えるためのサービスクラス
class TelecomConnectionService(
    private val telecomManager: TelecomManager
): ConnectionService() {
    private val stateChangedListeners = mutableListOf<ConnectionStateChangedListener>()

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d(tag, "onCreateIncomingConnection $connectionManagerPhoneAccount, $request")
        val bundle = request?.extras?.getBundle(TelecomManager.EXTRA_INCOMING_CALL_EXTRAS)
        val name = bundle?.getString("name")
        val connection = TelecomConnection(this, stateChangedListeners).apply {
            setCallerDisplayName(name, TelecomManager.PRESENTATION_ALLOWED)
            setRinging()
            Log.d(tag, "setCallerDisplayName, setRinging")
        }
        return connection
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Log.d(tag, "onCreateIncomingConnectionFailed $connectionManagerPhoneAccount, $request")
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        stateChangedListeners.add(listener)
    }

    fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        stateChangedListeners.remove(listener)
    }

    fun addNewIncomingCall(account: PhoneAccount?, inComingData: InComingData) {
        Log.d(tag, "addNewIncomingCall: $account, $inComingData")
        telecomManager.addNewIncomingCall(
            account?.accountHandle,
            account?.extras
        )
    }

    fun registerPhoneAccount(account: PhoneAccount?) {
        Log.d(tag, "addNewIncomingCall: $account")
        telecomManager.registerPhoneAccount(account)
    }

    fun getSelfManagedPhoneAccounts(context: Context): List<PhoneAccountHandle>? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return telecomManager.selfManagedPhoneAccounts
    }
}