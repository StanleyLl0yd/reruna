package com.sl.reruna.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import com.sl.reruna.game.GameState

private enum class FeedbackCue(
    val tone: Int,
    val durationMs: Int,
    val haptic: Int,
) {
    SPARK(
        tone = ToneGenerator.TONE_DTMF_2,
        durationMs = 45,
        haptic = HapticFeedbackConstants.CLOCK_TICK,
    ),
    RERUN(
        tone = ToneGenerator.TONE_DTMF_5,
        durationMs = 65,
        haptic = HapticFeedbackConstants.VIRTUAL_KEY,
    ),
    SYNC(
        tone = ToneGenerator.TONE_DTMF_8,
        durationMs = 90,
        haptic = HapticFeedbackConstants.CONTEXT_CLICK,
    ),
    RESONANCE(
        tone = ToneGenerator.TONE_DTMF_0,
        durationMs = 120,
        haptic = HapticFeedbackConstants.LONG_PRESS,
    ),
    GAME_OVER(
        tone = ToneGenerator.TONE_PROP_NACK,
        durationMs = 150,
        haptic = HapticFeedbackConstants.LONG_PRESS,
    ),
}

@Composable
fun GameFeedback(
    state: GameState,
    enabled: Boolean,
) {
    val view = LocalView.current
    val toneGenerator = remember {
        runCatching {
            ToneGenerator(AudioManager.STREAM_MUSIC, 24)
        }.getOrNull()
    }

    DisposableEffect(toneGenerator) {
        onDispose {
            toneGenerator?.release()
        }
    }

    LaunchedEffect(state.turn, state.gameOver, enabled) {
        if (!enabled) return@LaunchedEffect

        val cue = feedbackCue(state) ?: return@LaunchedEffect
        toneGenerator?.startTone(cue.tone, cue.durationMs)
        view.performHapticFeedback(cue.haptic)
    }
}

private fun feedbackCue(state: GameState): FeedbackCue? = when {
    state.gameOver -> FeedbackCue.GAME_OVER
    state.resonanceStartedThisTurn -> FeedbackCue.RESONANCE
    state.lastSyncCount >= 2 -> FeedbackCue.SYNC
    state.rerunCreatedThisTurn -> FeedbackCue.RERUN
    state.sparksCollectedThisTurn > 0 -> FeedbackCue.SPARK
    else -> null
}
