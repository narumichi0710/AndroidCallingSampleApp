package com.example.androidcallingsampleapp.service

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.androidcallingsampleapp.store.CallControlStore
import com.example.androidcallingsampleapp.view.tag
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CallControlUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    val store: CallControlStore
) {
    private val telecomManager: TelecomManager = context.getSystemService(
        TelecomManager::class.java
    )

    init {
        resetAllPhoneAccounts()
    }

    // 着信をリクエストする
    fun startIncoming(data: CallRequestData) {
        val account = createOrFindPhoneAccount(data.uuid, false)
        val extras = TelecomConnection.getBundle(data, account.accountHandle)
        telecomManager.addNewIncomingCall(account.accountHandle, extras)
        Log.d(tag,"start incoming: ${data.title}")
    }

    // 発信をリクエストする
    fun startOutgoing(data: CallRequestData) {
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "permission error CALL_PHONE")
            return
        }
        if (store.hasConnections) {
            Log.d(tag,"start outgoing: already has connection")
            return
        }

        val account = createOrFindPhoneAccount(data.uuid, true)
        val extras = TelecomConnection.getBundle(data, account.accountHandle)
        try {
            telecomManager.placeCall(account.address, extras)
        } catch (e: Exception) {
            Log.e(tag, "exception: $e")
        }
    }

    fun onAnswer(id: String?) {
        store.fetchConnection(id)?.onAnswer()
    }

    fun onReject(id: String?) {
        store.fetchConnection(id)?.onReject()
    }

    private fun createOrFindPhoneAccount(id: String?, isOutgoing: Boolean): PhoneAccount {
        val handle = PhoneAccountHandle(
            ComponentName(context, TelecomConnectionService::class.java),
            id ?: "unknownId"
        )
        if (isExistingPhoneAccount(handle).not()) {
            registerPhoneAccount(handle, isOutgoing)
        }

        return telecomManager.getPhoneAccount(handle)
    }

    private fun registerPhoneAccount(handle: PhoneAccountHandle, isOutgoing: Boolean) {
        val label = if (isOutgoing) "outgoing" else "incoming"
        val newAccount = TelecomConnection.getNewAccount(label, handle)
        try {
            checkVisibleCallState(newAccount)
            telecomManager.registerPhoneAccount(newAccount)
            Log.d(tag, "phone account created: ${newAccount.label} ${newAccount.accountHandle.id}")
        } catch (e: Exception) {
            Log.e(tag,"exception: $e")
        }
    }

    private fun isExistingPhoneAccount(accountHandle: PhoneAccountHandle): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "permission error READ_PHONE_STATE")
            return false
        }
        val existingAccounts = telecomManager.selfManagedPhoneAccounts
        val existingAccountHandle = existingAccounts.find { it.id == accountHandle.id }
        return existingAccountHandle != null
    }

    private fun resetAllPhoneAccounts() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "permission error READ_PHONE_STATE")
            return
        }

        telecomManager.selfManagedPhoneAccounts
            ?.map { telecomManager.getPhoneAccount(it) }
            ?.forEach {
                telecomManager.unregisterPhoneAccount(it.accountHandle)
                Log.d(tag, "unregistered phone account: ${it.label} ${it.accountHandle.id}")
            }
        telecomManager.selfManagedPhoneAccounts?.map {
            Log.d(tag, "available phone account: $it")
        }
    }

    private fun checkIsInCall() {
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Log.d(tag, "connection is activate: ${telecomManager.isInCall}")
    }

    private fun checkVisibleCallState(account: PhoneAccount) {
        val isVisibleIncoming = telecomManager.isIncomingCallPermitted(
            account.accountHandle
        )
        val isVisibleOutgoing = telecomManager.isOutgoingCallPermitted(
            account.accountHandle
        )
        Log.d(tag, "isVisible phone account. incoming: $isVisibleIncoming, outgoing: $isVisibleOutgoing")
    }
}
