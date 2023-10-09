package com.example.androidcallingsampleapp.view


import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidcallingsampleapp.service.CallControlUseCase
import com.example.androidcallingsampleapp.service.getRequestData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    @Inject
    lateinit var useCase: CallControlUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate IncomingCallActivity")

        setIncomingCallSetting()
        val requestData = intent.getRequestData()

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            action = Intent.ACTION_VIEW
        }
        setContent {
            IncomingCallScreen(
                incomingName = requestData?.title,
                onActivate = {
                    useCase.onAnswer(requestData?.uuid)
                    startActivity(intent)
                },
                onReject = {
                    useCase.onReject(requestData?.uuid)
                    startActivity(intent)
                }
            )
        }
    }

    private fun setIncomingCallSetting() {
        // 画面がロックされていても画面をオンにするように設定
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        } else {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        // キーガード（ロック画面）を非表示にする
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    }
}

@Composable
fun IncomingCallScreen(
    incomingName: String?,
    onActivate: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(start = 16.dp, top = 64.dp, end = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = incomingName ?: "着信",
            fontSize = 32.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Text(
            "Calling Sample App",
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.padding(46.dp)) {
            IconButton(
                text = "拒否",
                image = Icons.Filled.CallEnd,
                color = Color.Red,
                onClick = onReject
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                text = "応答",
                image = Icons.Filled.Call,
                color = Color.Green,
                onClick = onActivate
            )
        }
    }
}

@Composable
private fun IconButton(
    text: String,
    image: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color, shape = RoundedCornerShape(50))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(40.dp),
                painter = rememberVectorPainter(image = image),
                contentDescription = "",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, color = Color.White)
    }
}
