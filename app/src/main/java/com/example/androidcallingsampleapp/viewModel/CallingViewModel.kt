package com.example.androidcallingsampleapp.viewModel


import android.telecom.Connection.STATE_ACTIVE
import android.telecom.Connection.STATE_DIALING
import android.telecom.Connection.STATE_DISCONNECTED
import android.telecom.Connection.STATE_HOLDING
import android.telecom.Connection.STATE_INITIALIZING
import android.telecom.Connection.STATE_NEW
import android.telecom.Connection.STATE_PULLING_CALL
import android.telecom.Connection.STATE_RINGING
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.androidcallingsampleapp.service.ConnectionStateChangedListener
import com.example.androidcallingsampleapp.service.TelecomConnection
import com.example.androidcallingsampleapp.service.TelecomUseCase
import com.example.androidcallingsampleapp.view.tag


class CallingViewModel(
    private val telecomUseCase: TelecomUseCase
): ViewModel() {

    private val listener = object : ConnectionStateChangedListener {
        override fun onStateChanged(
            state: Int,
            connection: TelecomConnection
        ) {
            when(state) {
                STATE_INITIALIZING -> {
                    Log.d(tag, "STATE_INITIALIZING")
                }
                STATE_NEW -> {
                    Log.d(tag, "STATE_NEW")
                }
                STATE_RINGING -> {
                    Log.d(tag, "STATE_RINGING")
                }
                STATE_DIALING -> {
                    Log.d(tag, "STATE_DIALING")
                }
                STATE_ACTIVE -> {
                    Log.d(tag, "STATE_ACTIVE")
                }
                STATE_HOLDING -> {
                    Log.d(tag, "STATE_HOLDING")
                }
                STATE_DISCONNECTED -> {
                    Log.d(tag, "STATE_DISCONNECTED")
                }
                STATE_PULLING_CALL -> {
                    Log.d(tag, "STATE_PULLING_CALL")
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

    fun holdCall() {
        telecomUseCase.hold()
    }

    fun disconnectCall() {
        telecomUseCase.disconnect()
    }

    override fun onCleared() {
        telecomUseCase.removeConnectionStateChangedListener(listener)
        super.onCleared()
    }
}