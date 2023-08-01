package com.example.androidcallingsampleapp

import android.app.Application
import android.telecom.PhoneAccount
import android.telecom.TelecomManager
import com.example.androidcallingsampleapp.service.TelecomConnectionService
import com.example.androidcallingsampleapp.service.TelecomUseCase

class CallingApplication : Application() {
    companion object { var instance: CallingApplication? = null }
    lateinit var useCase: TelecomUseCase
    lateinit var account: PhoneAccount


    override fun onCreate() {
        super.onCreate()
        instance = this
        useCase = TelecomUseCase(
            this,
            getSystemService(TELECOM_SERVICE) as TelecomManager,
            TelecomConnectionService()
        )
        account = useCase.initPhoneAccount()!!
    }
}
