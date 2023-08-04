package com.example.androidcallingsampleapp.di

import android.content.Context
import android.telecom.TelecomManager
import com.example.androidcallingsampleapp.service.TelecomConnectionService
import com.example.androidcallingsampleapp.service.TelecomUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideTelecomUseCase(
        @ApplicationContext context: Context
    ): TelecomUseCase {
        return TelecomUseCase(
            context,
            context.getSystemService(TelecomManager::class.java)
        )
    }
}