package com.sl.reruna.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sl.reruna.RerunaViewModel

@Composable
fun RerunaApp(
    viewModel: RerunaViewModel = viewModel(),
) {
    val state by viewModel.state

    GameScreen(
        state = state,
        onMove = viewModel::move,
        onRestart = viewModel::restart,
    )
}
