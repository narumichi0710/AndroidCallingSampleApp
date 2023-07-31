package com.example.androidcallingsampleapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidcallingsampleapp.service.CHANNEL_ID
import com.example.androidcallingsampleapp.service.TelecomHelper
import com.example.androidcallingsampleapp.service.TelecomHelperImpl
import com.example.androidcallingsampleapp.ui.theme.AndroidCallingSampleAppTheme
import com.example.androidcallingsampleapp.viewModel.CallingViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CallingViewModel by viewModels {
        ViewModelFactory(TelecomHelperImpl(this, getSystemService(Context.TELECOM_SERVICE) as TelecomManager))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        askAllPermissions()

        setContent {
            AndroidCallingSampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CallingView(this, viewModel)
                }
            }
        }
    }

    private fun askAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // プッシュ通知が許可されている場合
                Log.d(tag,"granted")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // 一度、拒否されて「今後表示しない」が選択されていない場合
                Log.d(tag,"shouldShowRequestPermissionRationale")
            } else {
                // パーミッションダイアログを表示
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_OWN_CALLS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.MANAGE_OWN_CALLS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    // パーミッションダイアログを表示
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(tag,"permission granted")
        } else {
            Log.d(tag, "permission denied")
        }
    }

    // 通知チャンネルを作成
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "sampleName", NotificationManager.IMPORTANCE_HIGH)
        with(NotificationManagerCompat.from(this)) {
            createNotificationChannel(channel)
        }
    }
}


class ViewModelFactory(private val telecomHelper: TelecomHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallingViewModel(telecomHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

const val tag = "AppLog"
