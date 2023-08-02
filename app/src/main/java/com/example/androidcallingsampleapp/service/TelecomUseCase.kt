package com.example.androidcallingsampleapp.service

import android.content.ComponentName
import android.content.Context
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.util.Log
import com.example.androidcallingsampleapp.view.tag
import dagger.hilt.android.qualifiers.ApplicationContext


interface ITelecomUseCase {
    // 初期化
    fun initPhoneAccount()
    // 着信
    fun startIncoming(inComingData: InComingData)
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
    @ApplicationContext private val context: Context,
    private val connectionService: TelecomConnectionService,
) : ITelecomUseCase {
    private val connections = mutableListOf<TelecomConnection>()
    var account: PhoneAccount? = null

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
        connectionService.addConnectionStateChangedListener(listener)
    }

    override fun initPhoneAccount() {
        account = findExistingAccount(context)
        if (account == null) { account = createAccount() }
    }

    private fun createAccount(): PhoneAccount {
        val accountHandle = PhoneAccountHandle(
            ComponentName(context, TelecomConnectionService::class.java),
            context.packageName
        )
        val account = PhoneAccount.builder(accountHandle, "AndroidCallingSampleApp")
            .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
            .setSupportedUriSchemes(listOf(PhoneAccount.SCHEME_SIP))
            .build()
        connectionService.registerPhoneAccount(account)
        Log.d(tag, "createAccount: $account")
        return account
    }

    private fun findExistingAccount(context: Context): PhoneAccount? {
        try {
            var account: PhoneAccount? = null
            val phoneAccountHandleList = connectionService.getSelfManagedPhoneAccounts(context) ?: return null
            val connectionService = ComponentName(context, TelecomConnectionService::class.java)
            for (phoneAccountHandle in phoneAccountHandleList) {
                if (phoneAccountHandle.componentName == connectionService) {
                    Log.d(tag,"Found existing phone account: ${this.account}")
                    account = this.account
                    break
                }
            }
            if (account == null) {
                Log.d(tag,"Existing phone account not found")
            }
            return account
        } catch (se: SecurityException) {
            Log.d(tag,"Can't check phone accounts: $se")
        }
        return null
    }

    override fun startIncoming(inComingData: InComingData) {
        if (account == null) {
            Log.d(tag,"startIncoming: Account not found. so called initPhoneAccount()")
            initPhoneAccount()
        }
        connectionService.addNewIncomingCall(account, inComingData)
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
        connectionService.addConnectionStateChangedListener(listener)
    }

    override fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        Log.d(tag, "removeConnectionStateChangedListener: $listener")
        connectionService.removeConnectionStateChangedListener(listener)
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