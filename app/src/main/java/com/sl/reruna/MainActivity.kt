package com.sl.reruna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sl.reruna.ui.RerunaApp
import com.sl.reruna.ui.theme.RerunaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RerunaTheme {
                RerunaApp()
            }
        }
    }
}
