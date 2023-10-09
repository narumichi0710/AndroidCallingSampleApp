package com.example.androidcallingsampleapp.service

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.util.Log
import com.example.androidcallingsampleapp.store.CallControlStore
import com.example.androidcallingsampleapp.view.tag
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TelecomConnectionService: ConnectionService() {

    @Inject
    lateinit var store: CallControlStore

    // 着信リクエストが成功した際に呼び出される
    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d(tag, "onCreateIncomingConnection $connectionManagerPhoneAccount, $request")

        return TelecomConnection(this, ::eventHandler).apply {
            requestData = request?.extras?.getRequestData()
            setRinging()
        }
    }

    // 着信リクエストが失敗した際に呼び出される
    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Log.d(tag, "onCreateIncomingConnectionFailed $connectionManagerPhoneAccount, $request")
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    // 発信リクエストが成功した際に呼び出される
    override fun onCreateOutgoingConnection(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d(
            tag,
            "onCreate Outgoing Connection: ${phoneAccount?.componentName}, ${request?.extras}"
        )
        return TelecomConnection(this, ::eventHandler).apply {
            requestData = request?.extras?.getRequestData()
            setRinging()
            setActive()
        }
    }

    // 発信リクエストが失敗した際に呼び出される
    override fun onCreateOutgoingConnectionFailed(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Log.d(tag, "onCreate Outgoing Connection Failed: ${phoneAccount?.componentName}")
        super.onCreateOutgoingConnectionFailed(phoneAccount, request)
    }

    private fun eventHandler(type: TelecomConnection.Handler) {
        when (type) {
            is TelecomConnection.Handler.State -> {
                val state = type.state
                val connection = type.connection
                val uuid = connection.requestData?.uuid

                when (state) {
                    ConnectionState.RINGING -> {
                        store.addConnection(connection)
                    }
                    ConnectionState.ACTIVE -> {
                        store.updateCurrentConnectionId(uuid)

                        // 割り込み着信に応答した場合、他の通話を終了させる
                        if (store.connections.value.count() > 1) {
                            store.connections.value
                                .filter { it.requestData?.uuid != uuid }
                                .forEach { it.onDisconnect() }
                        }
                    }
                    ConnectionState.DISCONNECTED -> {
                        store.deleteConnection(uuid)
                    }
                    else -> return
                }
            }
        }
    }
}
