package com.example.androidcallingsampleapp.view

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.androidcallingsampleapp.service.CallingMessagingService.Companion.CHANNEL_ID
import com.example.androidcallingsampleapp.ui.theme.AndroidCallingSampleAppTheme


class MainActivity : ComponentActivity() {

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
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "応答待ち", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    private fun requestAllPermissions() {
        var permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.MANAGE_OWN_CALLS,
            Manifest.permission.VIBRATE,
            Manifest.permission.USE_FULL_SCREEN_INTENT,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
            )
        getNotificationPermission()?.let { permissions += it }
        requestPermissionsLauncher.launch(permissions)
    }

    // 通知チャンネルを作成
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "sampleName", NotificationManager.IMPORTANCE_HIGH)
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