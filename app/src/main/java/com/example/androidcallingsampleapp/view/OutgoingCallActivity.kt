package com.example.androidcallingsampleapp.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidcallingsampleapp.service.CallControlUseCase
import com.example.androidcallingsampleapp.service.CallRequestData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
class OutgoingCallActivity : ComponentActivity() {

    @Inject
    lateinit var useCase: CallControlUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            action = Intent.ACTION_VIEW
        }

        val requestData = CallRequestData(
            UUID
                .randomUUID()
                .toString(),
            "outgoing call",
            "content",
            true,
            "appLinkString"
        )
        useCase.startOutgoing(requestData)

        setContent {
            OutgoingCallScreen(
                onConnected = {
                    startActivity(intent)
                },
                onDisconnected = {
                    useCase.onDisconnect(requestData?.uuid)
                    startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun OutgoingCallScreen(
    onConnected: () -> Unit = {},
    onDisconnected: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onConnected()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(start = 16.dp, top = 64.dp, end = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Calling...",
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
        IconButton(
            text = "終了",
            image = Icons.Filled.CallEnd,
            color = Color.Red,
            onClick = onDisconnected
        )
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
