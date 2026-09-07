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
    val paused by viewModel.paused
    val showTutorial by viewModel.showTutorial

    GameScreen(
        state = state,
        paused = paused,
        showTutorial = showTutorial,
        onMove = viewModel::move,
        onRestart = viewModel::restart,
        onDismissTutorial = viewModel::dismissTutorial,
    )
}
