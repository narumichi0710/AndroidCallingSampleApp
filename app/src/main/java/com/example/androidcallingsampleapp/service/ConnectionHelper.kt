package com.example.androidcallingsampleapp.service

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.annotation.RequiresPermission

interface TelecomHelper {
    // 発着信の際に必要なため
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun initPhoneAccount(): PhoneAccount?

    @RequiresPermission(Manifest.permission.CALL_PHONE)
    fun startOutgoing(number: String, name: String, accountHandle: PhoneAccountHandle)
    fun startIncoming(name: String, accountHandle: PhoneAccountHandle)
    fun activate()
    fun hold()
    fun disconnect()

    fun firstConnectionOrNull(): DemoConnection?
    fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener)
    fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener)
}

class TelecomHelperImpl(
    private val context: Context,
    private val telecomManager: TelecomManager
) : TelecomHelper {

    init {
        // Connectionのaddやremoveをするためにリスナーをセット
        val listener = object : ConnectionStateChangedListener {
            override fun onStateChanged(
                state: Int,
                connection: DemoConnection
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
        DemoConnectionService.addConnectionStateChangedListener(listener)
    }

    private val connections = mutableListOf<DemoConnection>()

    @RequiresPermission(Manifest.permission.CALL_PHONE)
    override fun startOutgoing(number: String, name: String, accountHandle: PhoneAccountHandle) {
        telecomManager.placeCall(
            Uri.fromParts("tel", number, null),
            Bundle().apply {
                putParcelable(
                    TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS,
                    Bundle().apply {
                        putString("name", name)
                    }
                )
                putParcelable(
                    TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
                    accountHandle,
                )
            }
        )
    }

    override fun startIncoming(name: String, accountHandle: PhoneAccountHandle) {
        telecomManager.addNewIncomingCall(
            accountHandle,
            Bundle().apply {
                putParcelable(
                    TelecomManager.EXTRA_INCOMING_CALL_EXTRAS,
                    Bundle().apply {
                        putString("name", name)
                    }
                )
            }
        )
    }

    override fun activate() {
        connections.lastOrNull {
            it.state == Connection.STATE_DIALING
                    || it.state == Connection.STATE_RINGING
                    || it.state == Connection.STATE_HOLDING
        }?.setActive()
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

    override fun firstConnectionOrNull(): DemoConnection? = null

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override fun initPhoneAccount(): PhoneAccount {
        return findExistingAccount(context) ?: return createAccount(context)
    }

    private fun createAccount(context: Context): PhoneAccount {
        val accountHandle = PhoneAccountHandle(
            ComponentName(context, DemoConnectionService::class.java),
            context.packageName
        )
        val account = PhoneAccount.builder(accountHandle, "test")
            .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
            .setSupportedUriSchemes(listOf(PhoneAccount.SCHEME_SIP))
            .build()

        telecomManager.registerPhoneAccount(account)
        return account
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private fun findExistingAccount(context: Context): PhoneAccount? {
        val connectionService = ComponentName(
            context,
            DemoConnectionService::class.java
        )

        val targetPhoneAccountHandle =
            telecomManager.selfManagedPhoneAccounts.firstOrNull { phoneAccountHandle ->
                phoneAccountHandle.componentName == connectionService
            }
        return telecomManager.getPhoneAccount(targetPhoneAccountHandle)
    }

    override fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        DemoConnectionService.addConnectionStateChangedListener(listener)
    }

    override fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        DemoConnectionService.removeConnectionStateChangedListener(listener)
    }

    private fun startConnection(connection: DemoConnection) {
        connections.add(connection)
    }

    private fun endConnection(connection: DemoConnection) {
        connections.remove(connection)
        connection.setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
        connection.destroy()
    }
}