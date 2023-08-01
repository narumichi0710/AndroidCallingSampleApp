package com.example.androidcallingsampleapp.service

import android.content.ComponentName
import android.content.Context
import android.provider.Settings.Global.getString
import android.telecom.Connection
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import com.example.androidcallingsampleapp.view.tag


interface ITelecomUseCase {
    // 初期化
    fun initPhoneAccount(): PhoneAccount?
    // 着信
    fun startIncoming(phoneAccount: PhoneAccount)
    // 通話開始
    fun activate()
    // 通話拒否
    fun reject()
    // 保留中
    fun hold()
    // 通話終了
    fun disconnect()
    // Connectionの状態（通話状態）を監視
    fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener)
    // Connectionの状態（通話状態）を監視
    fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener)
}

class TelecomUseCase(
    private val context: Context,
    private val telecomManager: TelecomManager,
    private val telecomConnectionService: TelecomConnectionService,
) : ITelecomUseCase {
    private val connections = mutableListOf<TelecomConnection>()

    init {
        // Connectionのaddやremoveをするためにリスナーをセット
        val listener = object : ConnectionStateChangedListener {
            override fun onStateChanged(
                state: Int,
                connection: TelecomConnection
            ) {
                when (state) {
                    Connection.STATE_RINGING -> {
                        startConnection(connection)
                    }
                    Connection.STATE_DIALING -> {
                        startConnection(connection)
                    }
                    Connection.STATE_DISCONNECTED -> {
                        endConnection(connection)
                    }
                }
            }
        }
        telecomConnectionService.addConnectionStateChangedListener(listener)
    }

    override fun initPhoneAccount(): PhoneAccount? {

        val accountHandle = PhoneAccountHandle(
            ComponentName(context, TelecomConnectionService::class.java),
            context.packageName
        )

        val account = PhoneAccount.builder(accountHandle, "AndroidCallingSampleApp")
            .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
            .setSupportedUriSchemes(listOf(PhoneAccount.SCHEME_SIP))
            .build()

        telecomManager.registerPhoneAccount(account)
        Log.d(tag, "initPhoneAccount: $account")
        return account
    }


    override fun startIncoming(phoneAccount: PhoneAccount) {
        telecomManager.addNewIncomingCall(
            phoneAccount.accountHandle,
            phoneAccount.extras
        )
    }

    override fun activate() {
        connections.lastOrNull {
            it.state == Connection.STATE_DIALING
                    || it.state == Connection.STATE_RINGING
                    || it.state == Connection.STATE_HOLDING
        }?.setActive()
    }

    override fun reject() {
        connections.lastOrNull()?.onReject()
    }

    override fun hold() {
        connections.lastOrNull {
            it.state == Connection.STATE_ACTIVE
        }?.setOnHold()
    }

    override fun disconnect() {
        connections.lastOrNull {
            it.state == Connection.STATE_DIALING
                    || it.state == Connection.STATE_RINGING
                    || it.state == Connection.STATE_ACTIVE
                    || it.state == Connection.STATE_HOLDING
        }?.let {
            endConnection(it)
        }
    }

    override fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        Log.d(tag, "addConnectionStateChangedListener: $listener")
        telecomConnectionService.addConnectionStateChangedListener(listener)
    }

    override fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        Log.d(tag, "removeConnectionStateChangedListener: $listener")
        telecomConnectionService.removeConnectionStateChangedListener(listener)
    }

    private fun startConnection(connection: TelecomConnection) {
        Log.d(tag, "startConnection: $connection")
        connections.add(connection)
    }

    private fun endConnection(connection: TelecomConnection) {
        Log.d(tag, "endConnection: $connection")
        connections.remove(connection)
        connection.setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
        connection.destroy()
    }
}