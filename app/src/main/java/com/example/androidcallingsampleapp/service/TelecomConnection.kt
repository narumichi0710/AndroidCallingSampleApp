package com.example.androidcallingsampleapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import com.example.androidcallingsampleapp.service.CallingMessagingService.Companion.channel
import com.example.androidcallingsampleapp.view.IncomingCallActivity
import com.example.androidcallingsampleapp.view.tag


interface ConnectionStateChangedListener {
    fun onStateChanged(state: Int, connection: TelecomConnection)
}

class TelecomConnection(
    private val context: Context,
    private val stateChangedListeners: MutableList<ConnectionStateChangedListener>
) : Connection() {

    init {
        connectionProperties = connectionProperties or PROPERTY_SELF_MANAGED
        connectionCapabilities = connectionCapabilities or CAPABILITY_MUTE or CAPABILITY_SUPPORT_HOLD or CAPABILITY_HOLD
        audioModeIsVoip = true
        setInitializing()
    }

    override fun onStateChanged(state: Int) {
        Log.d(tag, "onStateChanged")
        stateChangedListeners.map { listener ->
            listener.onStateChanged(state, this)
        }
    }

    override fun onAnswer() {
        Log.d(tag, "onAnswer")
        setActive()
    }

    override fun onReject() {
        Log.d(tag, "onReject")
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
    }

    override fun onDisconnect() {
        Log.d(tag, "onDisconnect")
        setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
    }

    // カスタムのUIを表示する
    override fun onShowIncomingCallUi() {
        // 通知チャンネルにカスタムサウンドを設定
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        channel.setSound(ringtoneUri, AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )

        Log.d(tag, "onShowIncomingCallUi")

        // 画面を表示
        val intent = Intent(context, IncomingCallActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

}