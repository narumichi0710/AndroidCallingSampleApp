package com.example.androidcallingsampleapp.view


import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter
import com.example.androidcallingsampleapp.CallingApplication
import com.example.androidcallingsampleapp.service.TelecomUseCase
import com.example.androidcallingsampleapp.viewModel.CallingViewModel


/*
* 1. Push通知受け取った際に、useCaseを通じてtelecomManagerに対してaddNewIncomingCall()を実行し、新しい着信をTelecomサブシステムに通知する
* 2. TelecomサブシステムはTelecomConnectionServiceの実装にバインドしonCreateIncomingConnection() メソッドを使用して、新しい着信をConnectionクラスにリクエストする
* 3. TelecomサブシステムがonShowIncomingCallUi()メソッドを使用して、アプリに対し、通話応答画面を表示する必要があることを通知する
* 4. アプリが関連する全画面インテントを持つ通知を使用して着信 UI を表示します。詳しくは onShowIncomingCallUi() をご覧ください
* 5. ユーザーが着信に応答した場合は setActive() メソッドを呼び出し、着信を拒否した場合はsetDisconnected()を呼び出した後、destroy()を呼び出す
*/

class IncomingCallActivity : ComponentActivity() {

    private val viewModel: CallingViewModel by viewModels {
        ViewModelFactory(CallingApplication.instance.useCase)
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter("https://source.unsplash.com/random"),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "hogehoge",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.activateCall()
                        // TODO: 通話画面に遷移させる
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "応答")
                }

                Button(
                    onClick = {
                        viewModel.rejectCall()
                        onFinish()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(text = "拒否")
                }
            }
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
