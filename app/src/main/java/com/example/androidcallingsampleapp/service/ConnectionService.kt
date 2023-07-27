package com.example.androidcallingsampleapp.service

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager


/// 通話開始をOSに伝えるためのサービスクラス
open class DemoConnectionService : ConnectionService() {

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        // TelecomManager.placeCall()を呼ぶとこのメソッドが呼ばれます
        // ↓また、placeCall()の引数に渡したbundleから値を受け取れます
        val name = request?.extras?.getString("name")
        val connection = DemoConnection(stateChangedListeners).apply {
            // 発信者名をセットしています
            setCallerDisplayName(name, TelecomManager.PRESENTATION_ALLOWED)
            // Connectionのstateをdialingに設定しています
            setDialing()
        }
        return connection
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val bundle = request?.extras?.getBundle(TelecomManager.EXTRA_INCOMING_CALL_EXTRAS)
        val name = bundle?.getString("name")
        val connection = DemoConnection(stateChangedListeners).apply {
            setCallerDisplayName(name, TelecomManager.PRESENTATION_ALLOWED)
            setRinging()
        }
        return connection
    }

    companion object {
        // リスナーが複数セットされるため
        private val stateChangedListeners = mutableListOf<ConnectionStateChangedListener>()
        fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
            stateChangedListeners.add(listener)
        }

        fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
            stateChangedListeners.remove(listener)
        }
    }
 }

interface ConnectionStateChangedListener {
    fun onStateChanged(state: Int, connection: DemoConnection)
}

class DemoConnection(
    private val stateChangedListeners: MutableList<ConnectionStateChangedListener> = mutableListOf()
) : Connection() {
    init {
        audioModeIsVoip = true
        connectionProperties = PROPERTY_SELF_MANAGED
        setInitializing()
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)

        stateChangedListeners.map { listener ->
            listener.onStateChanged(state, this)
        }
    }

    /// OS由来のポップアップに対して電話に出るという類のアクションをすると呼び出される
    override fun onAnswer() {
        super.onAnswer()
        setActive()
    }

    /// OS由来のポップアップに対して拒否するという類のアクションをすると呼び出される
    override fun onReject() {
        super.onReject()
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
    }

    // OS由来のポップアップに対して切電するという類のアクションをすると呼び出される
    override fun onDisconnect() {
        super.onDisconnect()
        setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
    }

}