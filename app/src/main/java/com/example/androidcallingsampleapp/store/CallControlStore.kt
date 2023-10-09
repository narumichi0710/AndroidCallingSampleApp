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

    private val mutableConnections: MutableStateFlow<List<TelecomConnection>> = MutableStateFlow(listOf())
    val connections get() = mutableConnections.asStateFlow()
    val currentConnection get() = mutableConnections.value.firstOrNull { it.callState == ConnectionState.ACTIVE }

    val isIncoming get() = currentConnection?.requestData?.isOutgoing == false
    val hasConnections get() = mutableConnections.value.isNotEmpty()


    fun fetchConnection(id: String?): TelecomConnection? {
        return connections.value.firstOrNull { it.requestData?.uuid == id }
    }

    fun addConnection(connection: TelecomConnection) {
        mutableConnections.update { connections.value + connection }
        Log.d(tag, "add connection. count: ${connections.value.count()}")
    }

    fun deleteConnection(id: String?) {
        val applyData = connections.value.filter { it.requestData?.uuid != id }
        mutableConnections.update { applyData }
    }
}
