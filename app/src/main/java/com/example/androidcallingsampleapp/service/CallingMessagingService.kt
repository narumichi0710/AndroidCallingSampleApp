package com.example.androidcallingsampleapp.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.androidcallingsampleapp.MainActivity
import com.example.androidcallingsampleapp.R
import com.example.androidcallingsampleapp.tag
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val CHANNEL_ID = "channel_0"
const val NOTIFICATION_ID = 0

class CallingMessagingService : FirebaseMessagingService() {

    override fun onCreate() {
        Log.d(tag, "onCreate ")
        super.onCreate()
    }
    override fun onNewToken(token: String) {
        Log.d(tag, "Refreshed token: $token")
        super.onNewToken(token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            Log.d(tag, "title: " + it.title)
            Log.d(tag, "body: " + it.body)
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                action = Intent.ACTION_VIEW
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE)

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

    override fun onDeletedMessages() {
        Log.d(tag, "onDeletedMessages")
        super.onDeletedMessages()
    }
}