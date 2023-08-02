package com.example.androidcallingsampleapp

import android.app.Application
import android.telecom.TelecomManager
import com.example.androidcallingsampleapp.service.TelecomConnectionService
import com.example.androidcallingsampleapp.service.TelecomUseCase

class CallingApplication : Application() {
    companion object { var instance = CallingApplication() }
    lateinit var useCase: TelecomUseCase

    override fun onCreate() {
        super.onCreate()
        instance = this
        useCase = TelecomUseCase(
            this,
            TelecomConnectionService(getSystemService(TELECOM_SERVICE) as TelecomManager)
        )
        useCase.initPhoneAccount()
    }
}
