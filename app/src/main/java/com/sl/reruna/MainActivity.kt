package com.sl.reruna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.sl.reruna.ui.RerunaApp
import com.sl.reruna.ui.theme.RerunaTheme

class MainActivity : ComponentActivity() {
    private lateinit var rerunaViewModel: RerunaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rerunaViewModel = ViewModelProvider(this)[RerunaViewModel::class.java]
        enableEdgeToEdge()
        setContent {
            RerunaTheme {
                RerunaApp(viewModel = rerunaViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::rerunaViewModel.isInitialized) {
            rerunaViewModel.setPaused(false)
        }
    }

    override fun onPause() {
        if (::rerunaViewModel.isInitialized) {
            rerunaViewModel.setPaused(true)
        }
        super.onPause()
    }
}
