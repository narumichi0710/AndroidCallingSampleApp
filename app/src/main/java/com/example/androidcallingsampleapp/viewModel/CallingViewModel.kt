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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import com.example.androidcallingsampleapp.service.ConnectionStateChangedListener
import com.example.androidcallingsampleapp.service.DemoConnection
import com.example.androidcallingsampleapp.service.TelecomHelper


class CallingViewModel(
    private val telecomHelper: TelecomHelper
): ViewModel() {

    private val listener = object : ConnectionStateChangedListener {
        override fun onStateChanged(
            state: Int,
            connection: DemoConnection
        ) {
            when (state) {
                STATE_INITIALIZING -> {}
                STATE_NEW -> {}
                STATE_RINGING -> {}
                STATE_DIALING -> {}
                STATE_ACTIVE -> {}
                STATE_HOLDING -> {}
                STATE_DISCONNECTED -> {}
                STATE_PULLING_CALL -> {}
            }
        }
    }

    init {
        telecomHelper.addConnectionStateChangedListener(listener)
    }

    fun startOutgoingCall(
        context: Context,
        number: String,
        name: String
    ) {
      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

          val accountHandle = telecomHelper.initPhoneAccount()
          telecomHelper.initPhoneAccount()
          accountHandle?.let {
              telecomHelper.startOutgoing(number, name, it.accountHandle)
          }
            return
        }

    }

    fun startIncomingCall(
        context: Context,
        name: String
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            val accountHandle = telecomHelper.initPhoneAccount()
            accountHandle?.let {
                telecomHelper.startIncoming(name, it.accountHandle)
            }
        }
    }

    fun activateCall() {
        telecomHelper.activate()
    }

    fun holdCall() {
        telecomHelper.hold()
    }

    fun disconnectCall() {
        telecomHelper.disconnect()
    }

    override fun onCleared() {
        // メモリリークしないように
        telecomHelper.removeConnectionStateChangedListener(listener)
        super.onCleared()
    }
}