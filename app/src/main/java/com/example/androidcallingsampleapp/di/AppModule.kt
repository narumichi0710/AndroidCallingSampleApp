package com.example.androidcallingsampleapp.di

import android.content.Context
import com.example.androidcallingsampleapp.service.CallControlUseCase
import com.example.androidcallingsampleapp.store.CallControlStore
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
    fun provideCallControlUseCase(
        @ApplicationContext context: Context,
        callControlStore: CallControlStore
    ): CallControlUseCase {
        return CallControlUseCase(
            context,
            callControlStore
        )
    }
}