package com.example.androidcallingsampleapp.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
        override fun onStateChanged(state: Int, connection: TelecomConnection) {
            when (state) {
                STATE_INITIALIZING -> {
                    Log.d(tag, "onStateChanged INITIALIZING")
                }
                STATE_NEW -> {
                    Log.d(tag, "onStateChanged NEW")
                }
                STATE_RINGING -> {
                    Log.d(tag, "onStateChanged RINGING")
                }
                STATE_DIALING -> {
                    Log.d(tag, "onStateChanged DIALING")
                }
                STATE_ACTIVE -> {
                    Log.d(tag, "onStateChanged ACTIVE")
                }
                STATE_HOLDING -> {
                    Log.d(tag, "onStateChanged HOLDING")
                }
                STATE_DISCONNECTED -> {
                    Log.d(tag, "onStateChanged DISCONNECTED")
                }
                STATE_PULLING_CALL -> {
                    Log.d(tag, "onStateChanged PULLING_CALL")
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
    }
}