package com.example.androidcallingsampleapp.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.telecom.Connection
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.androidcallingsampleapp.view.MainActivity
import com.example.androidcallingsampleapp.R
import com.example.androidcallingsampleapp.view.IncomingCallActivity
import com.example.androidcallingsampleapp.view.tag
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject


class CallingMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var telecomUseCase: TelecomUseCase

    override fun onCreate() {
        Log.d(tag, "onCreate CallingMessagingService")
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        Log.d(tag, "Refreshed token: $token")
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(tag, "onMessageReceived ${remoteMessage.data}")
        if (remoteMessage.data.isEmpty()) { return }
        startIncoming(remoteMessage)
    }

    override fun onDeletedMessages() {
        Log.d(tag, "onDeletedMessages")
        super.onDeletedMessages()
    }

    private fun startIncoming(remoteMessage: RemoteMessage) {
        // 着信をリクエスト
        telecomUseCase.startIncoming(IncomingData("0123456789", "hoge", "1", "false"))
        Thread.sleep(3000)
        // TODO: 通話状態が可能であることを受け取ることが出来た場合にIntentさせる必要がある
        if (telecomUseCase.currentState == Connection.STATE_RINGING) {
            Log.d(tag, "通話応答画面を表示")
            val intent = Intent(this, IncomingCallActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                action = Intent.ACTION_VIEW
            }
            this.startActivity(intent)
        }
    }

    private fun sendMessage(remoteMessage: RemoteMessage) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        remoteMessage.notification?.let {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                action = Intent.ACTION_VIEW
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(it.title)
                .setContentText(it.body)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "channel_0"
        const val NOTIFICATION_ID = 0
        val channel = NotificationChannel(
            CHANNEL_ID,
            "着信通話", NotificationManager.IMPORTANCE_HIGH
        )
    }
}
