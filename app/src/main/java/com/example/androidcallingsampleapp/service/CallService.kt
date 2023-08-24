package com.example.androidcallingsampleapp.service

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.example.androidcallingsampleapp.view.tag

class CallService: InCallService() {

    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)
        Log.d(tag, "onCallAdded: $call")
    }

    override fun onCallRemoved(call: Call?) {
        super.onCallRemoved(call)
        Log.d(tag, "onCallRemoved: $call")
    }
}