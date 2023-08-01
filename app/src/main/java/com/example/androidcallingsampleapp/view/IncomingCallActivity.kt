package com.example.androidcallingsampleapp.view

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidcallingsampleapp.service.TelecomUseCase
import com.example.androidcallingsampleapp.viewModel.CallingViewModel


class IncomingCallActivity : ComponentActivity() {

    private val viewModel: CallingViewModel by viewModels {
        ViewModelFactory(TelecomUseCase(this, getSystemService(TELECOM_SERVICE) as TelecomManager))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate IncomingCallActivity")
        // 画面がロックされていても表示するように設定
        setShowWhenLocked(true)
        // 画面をオンにするように設定
        setTurnScreenOn(true)
        // キーガード（ロック画面）を非表示にするリクエスト
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        setContent {
            IncomingCallScreen(viewModel) {
                finishAffinity()
            }
        }
    }
}

@Composable
fun IncomingCallScreen(
    viewModel: CallingViewModel,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Button(onClick = {
                viewModel.activateCall()
                // TODO: 通話画面に遷移させる
            }) {
                Text(text = "応答")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                viewModel.rejectCall()
                onFinish()
            }) {
                Text(text = "拒否")
            }
        }
    }
}

@Composable
fun CallingScreen(
    context: Context,
    viewModel: CallingViewModel
) {

    Column {
        Button(onClick = {
            viewModel.activateCall()
        }) {
            Text("Activate Call")
        }

        Button(onClick = {
            viewModel.holdCall()
        }) {
            Text("Hold Call")
        }

        Button(onClick = {
            viewModel.disconnectCall()
        }) {
            Text("Disconnect Call")
        }
    }
}


class ViewModelFactory(private val telecomUseCase: TelecomUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallingViewModel(telecomUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
