package com.example.androidcallingsampleapp.service

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.androidcallingsampleapp.view.tag
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class IncomingData(
    val callerId: String,
    val callerName: String,
    val callerIdType: String,
    val hasVideo: String
)

interface InComingCallInterFace {
    // 初期化
    fun initPhoneAccount()

    // 着信
    fun startIncoming(incomingData: IncomingData)

    // 通話開始
    fun activate()

    // 通話拒否
    fun reject()

    // 通話終了
    fun disconnect()

    // Connectionの状態（通話状態）を監視
    fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener)

    // Connectionの状態（通話状態）を監視
    fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener)
}

class TelecomUseCase(
    @ApplicationContext private val context: Context,
    private val telecomManager: TelecomManager
) : InComingCallInterFace {
    private val mutableConnections = MutableStateFlow(listOf<TelecomConnection>())
    private val connections get() = mutableConnections.asStateFlow()

    private val mutableState = MutableStateFlow(ConnectionState.INITIALIZING)
    val state get() = mutableState.asStateFlow()

    private val accountHandle = PhoneAccountHandle(
        ComponentName(context, TelecomConnectionService::class.java),
        context.packageName
    )

    init {
        setConnectionObserver()
    }

    override fun initPhoneAccount() {
        unregisterPhoneAccounts()
        if (isExistingAccounts() == true) { return }
        createPhoneAccount()
    }

    override fun startIncoming(incomingData: IncomingData) {
        val existingAccount = telecomManager.getPhoneAccount(accountHandle)
        if (existingAccount == null) {
            Log.d(tag, "account not found. so called initPhoneAccount()")
            initPhoneAccount()
        }
        val extras = Bundle().apply {
        }
        Log.d(tag, "start incoming: ${accountHandle.componentName}, $extras")
        telecomManager.addNewIncomingCall(accountHandle, extras)
    }

    override fun activate() {
        Log.d(tag, "activate: count ${connections.value.count()}")
        connections.value.lastOrNull()?.onAnswer()
        checkIsInCall()
    }

    override fun reject() {
        Log.d(tag, "reject: count ${connections.value.count()}")
        connections.value.lastOrNull()?.onReject()
    }

    override fun disconnect() {
        Log.d(tag, "disconnect: count ${connections.value.count()}")
        connections.value.lastOrNull()?.let { endConnection(it) }
    }

    override fun addConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        Log.d(tag,"addConnectionStateChangedListener: $listener")
        TelecomConnectionService.addConnectionStateChangedListener(listener)
    }

    override fun removeConnectionStateChangedListener(listener: ConnectionStateChangedListener) {
        Log.d(tag,"removeConnectionStateChangedListener: $listener")
        TelecomConnectionService.removeConnectionStateChangedListener(listener)
    }

    private fun createPhoneAccount() {
        Log.d(tag,"createAccount")
        val accountHandle = PhoneAccountHandle(
            ComponentName(context, TelecomConnectionService::class.java),
            context.packageName
        )
        val account = PhoneAccount.builder(accountHandle, "AndroidCallingSampleApp")
            .setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
            .setSupportedUriSchemes(listOf(PhoneAccount.SCHEME_SIP))
            .build()

        telecomManager.registerPhoneAccount(account)
        Log.d(tag,"initPhoneAccount: $account")
    }

    private fun startConnection(connection: TelecomConnection) {
        // 同じ接続情報がある場合は追加しない
        if (connections.value.any { it.address == connection.address }) {
            return
        }
        Log.d(tag,"startConnection: $connection")
        mutableConnections.update { it.plus(connection) }
    }


    private fun endConnection(connection: TelecomConnection) {
        Log.d(tag, "endConnection: $connection")
        if (connections.value.isNotEmpty()) {
            mutableConnections.update { it.dropLast(1) }
        }
        connection.setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
        connection.destroy()
    }

    private fun setConnectionObserver() {
        val listener = object : ConnectionStateChangedListener {
            override fun onStateChanged(
                state: ConnectionState,
                connection: TelecomConnection
            ) {
                when (state) {
                    ConnectionState.RINGING -> {
                        startConnection(connection)
                    }
                    ConnectionState.ACTIVE -> {
                        startConnection(connection)
                    }
                    ConnectionState.DISCONNECTED -> {
                        endConnection(connection)
                    }
                    else -> {}
                }
                mutableState.update { state }
            }
        }
        TelecomConnectionService.addConnectionStateChangedListener(listener)
    }

    private fun isExistingAccounts(): Boolean? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(tag,"permission error READ_PHONE_STATE")
            return null
        }
        // テレコムに登録されている全ての自己管理型のアカウントを取得
        val existingAccounts = telecomManager.selfManagedPhoneAccounts
        // 自分のアプリのアカウントを抽出
        val existingAccountHandle = existingAccounts.find {
            it.componentName.packageName == accountHandle.componentName.packageName
        }
        return existingAccountHandle != null
    }

    // 【通話登録のデバッグ用】アプリで作成したアカウントの登録を解除する
    // 他のアプリがOSに通話状態の終了を報告していない場合、常に割り込み着信になってしまうため、そのアプリのアンインストールが必要
    private fun unregisterPhoneAccounts() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(tag, "permission error READ_PHONE_STATE")
            return
        }
        telecomManager.callCapablePhoneAccounts.onEach {
            Log.d(tag, "capable phone accounts: ${it.componentName.packageName} ${it.userHandle}")
        }
        val existingAccounts = telecomManager.selfManagedPhoneAccounts
        val synQAccounts = existingAccounts.filter {
            Log.d(tag, "existing accounts: ${it.componentName.packageName}")
            it.componentName.packageName == accountHandle.componentName.packageName
        }
        synQAccounts.forEach {
            Log.d(tag, "unregistered phone account: ${it.componentName.packageName}")
            telecomManager.unregisterPhoneAccount(it)
        }
    }

    // 【通話制御の確認用】OSに通話状態が共有されている場合はtrueを出力
    private fun checkIsInCall() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        Log.d(tag,"activate: ${telecomManager.isInCall}")
    }
}
