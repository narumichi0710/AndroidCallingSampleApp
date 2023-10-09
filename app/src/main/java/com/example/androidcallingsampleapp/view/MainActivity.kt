package com.example.androidcallingsampleapp.view

import android.Manifest
import android.app.Notification
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.androidcallingsampleapp.service.CallControlUseCase
import com.example.androidcallingsampleapp.service.CallRequestData
import com.example.androidcallingsampleapp.service.CallingMessagingService
import com.example.androidcallingsampleapp.ui.theme.AndroidCallingSampleAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var useCase: CallControlUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestAllPermissions()
        setContent {
            AndroidCallingSampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    useCase.startIncoming(
                                        CallRequestData(
                                            UUID.randomUUID().toString(),
                                            "incoming call",
                                            "content",
                                            false
                                        )
                                    )
                                }
                        ) {
                            Text(text = "Incoming Call")
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    useCase.startOutgoing(
                                        CallRequestData(
                                            UUID.randomUUID().toString(),
                                            "outgoing call",
                                            "content",
                                            true
                                        )
                                    )
                                }
                        ) {
                            Text(text = "OutGoing Call")
                        }
                    }
                }
            }
        }
    }

    private fun requestAllPermissions() {
        var permissions = arrayOf(
            Manifest.permission.MANAGE_OWN_CALLS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.READ_PHONE_STATE
        )
        getNotificationPermission()?.let { permissions += it }
        requestPermissionsLauncher.launch(permissions)
    }

    // 通知チャンネルを作成
    private fun createNotificationChannel() {
        val channel = CallingMessagingService.channel
        channel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        with(NotificationManagerCompat.from(this)) {
            createNotificationChannel(channel)
        }
    }

    /// push通知の権限コードを取得
    private fun getNotificationPermission(): String? {
        // OS バージョン確認
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Android 13 未満は通知権限不要
            return null
        }
        // 通知権限が許可されているか確認
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // 権限許可済
            return null
        }
        // 通知権限を必要とする理由をアプリが提示する必要があるか確認
        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            // 必要がある場合は、その理由を説明するためのUIを明示的に表示
            return null
        }

        return Manifest.permission.POST_NOTIFICATIONS
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grantStates: Map<String, Boolean> ->
        for ((permission, granted) in grantStates) {
            Log.d(tag,"$permission - $granted")
        }
    }
}

const val tag = "AppLog"
