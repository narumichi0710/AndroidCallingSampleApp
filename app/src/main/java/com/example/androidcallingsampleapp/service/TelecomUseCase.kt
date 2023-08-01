package com.example.androidcallingsampleapp.service

import android.content.ComponentName
import android.content.Context
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import com.example.androidcallingsampleapp.view.tag

interface ITelecomUseCase {
    fun initPhoneAccount(
        callerId: String?,
        callerName: String?,
        callerIdType: String?,
        hasVideo: String?
    ): PhoneAccount?
    fun startIncoming(phoneAccount: PhoneAccount)
    fun activate()
    fun reject()
    fun hold()
    fun disconnect()
    fun firstConnectionOrNull(): TelecomConnection?
    fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener)
    fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener)
}

class TelecomUseCase(
    private val context: Context,
    private val telecomManager: TelecomManager
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
        TelecomConnectionService.addConnectionStateChangedListener(listener)
    }

    override fun initPhoneAccount(
        callerId: String?,
        callerName: String?,
        callerIdType: String?,
        hasVideo: String?
    ): PhoneAccount {
        val accountHandle = PhoneAccountHandle(
            ComponentName(context, TelecomConnectionService::class.java),
            context.packageName
        )
        val account = PhoneAccount.builder(accountHandle, callerName ?: "")
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

    override fun firstConnectionOrNull(): TelecomConnection? = null

    override fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        TelecomConnectionService.addConnectionStateChangedListener(listener)
    }

    override fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        TelecomConnectionService.removeConnectionStateChangedListener(listener)
    }

    private fun startConnection(connection: TelecomConnection) {
        connections.add(connection)
    }

    private fun endConnection(connection: TelecomConnection) {
        connections.remove(connection)
        connection.setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
        connection.destroy()
    }
}