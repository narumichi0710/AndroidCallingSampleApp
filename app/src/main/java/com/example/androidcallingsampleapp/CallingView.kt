package com.example.androidcallingsampleapp

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.androidcallingsampleapp.viewModel.CallingViewModel


@Composable
fun CallingView(
    context: Context,
    viewModel: CallingViewModel
) {

    Column {

        Button(onClick = {
            viewModel.startOutgoingCall(context, "+1234567890", "John Doe")
        }) {
            Text("Start Outgoing Call")
        }

        Button(onClick = {
            viewModel.startIncomingCall(context, "John Doe")
        }) {
            Text("Start Incoming Call")
        }

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