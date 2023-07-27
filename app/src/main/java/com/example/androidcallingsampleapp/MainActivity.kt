package com.example.androidcallingsampleapp

import android.content.Context
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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