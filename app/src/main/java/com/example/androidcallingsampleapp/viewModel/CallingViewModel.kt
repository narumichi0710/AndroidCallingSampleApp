package com.example.androidcallingsampleapp.viewModel


import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.androidcallingsampleapp.service.ConnectionState
import com.example.androidcallingsampleapp.service.ConnectionStateChangedListener
import com.example.androidcallingsampleapp.service.TelecomConnection
import com.example.androidcallingsampleapp.service.TelecomUseCase
import com.example.androidcallingsampleapp.view.tag


class CallingViewModel(
    private val telecomUseCase: TelecomUseCase
): ViewModel() {

    private val listener = object : ConnectionStateChangedListener {
        override fun onStateChanged(
            state: ConnectionState,
            connection: TelecomConnection
        ) {
            when(state) {
                ConnectionState.INITIALIZING -> {
                    Log.d(tag, "STATE_INITIALIZING")
                }
                ConnectionState.NEW -> {
                    Log.d(tag, "STATE_NEW")
                }
                ConnectionState.RINGING -> {
                    Log.d(tag, "STATE_RINGING")
                }
                ConnectionState.DIALING -> {
                    Log.d(tag, "STATE_DIALING")
                }
                ConnectionState.ACTIVE -> {
                    Log.d(tag, "STATE_ACTIVE")
                }
                ConnectionState.HOLDING -> {
                    Log.d(tag, "STATE_HOLDING")
                }
                ConnectionState.DISCONNECTED -> {
                    Log.d(tag, "STATE_DISCONNECTED")
                }
                ConnectionState.UNKNOWN -> {
                    Log.d(tag, "UNKNOWN")
                }
            }
        }
    }

    init {
        telecomUseCase.addConnectionStateChangedListener(listener)
    }

    fun activateCall() {
        telecomUseCase.activate()
    }

    fun rejectCall() {
        telecomUseCase.reject()
    }

    fun disconnectCall() {
        telecomUseCase.disconnect()
    }

    override fun onCleared() {
        telecomUseCase.removeConnectionStateChangedListener(listener)
        super.onCleared()
    }
}