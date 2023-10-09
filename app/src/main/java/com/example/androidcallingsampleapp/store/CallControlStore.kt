package com.example.androidcallingsampleapp.store

import android.util.Log
import com.example.androidcallingsampleapp.service.ConnectionState
import com.example.androidcallingsampleapp.service.TelecomConnection
import com.example.androidcallingsampleapp.view.tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallControlStore @Inject constructor() {

    private val _connections: MutableStateFlow<List<TelecomConnection>> = MutableStateFlow(listOf())
    val connections get() = _connections.asStateFlow()
    val hasConnections get() = _connections.value.isNotEmpty()

    fun fetchConnection(id: String?): TelecomConnection? {
        return connections.value.firstOrNull { it.requestData?.uuid == id }
    }

    fun addConnection(connection: TelecomConnection) {
        _connections.update { connections.value + connection }
        Log.d(tag, "add connection. count: ${connections.value.count()}")
    }

    fun deleteConnection(id: String?) {
        val applyData = connections.value.filter { it.requestData?.uuid != id }
        _connections.update { applyData }

        // 終了した通話とIDが一致する場合削除する
        if (currentConnectionId.value == id) {
            updateCurrentConnectionId(null)
        }
    }

    private val _currentConnectionId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val currentConnectionId = _currentConnectionId.asStateFlow()
    val currentConnection get() = _connections.value.firstOrNull { it.requestData?.uuid == _currentConnectionId.value }
    val isIncoming get() = currentConnection?.requestData?.isOutgoing == false

    fun updateCurrentConnectionId(id: String?) {
        _currentConnectionId.update { id }
    }
}
