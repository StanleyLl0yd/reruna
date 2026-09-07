package com.sl.reruna

import android.app.Application
import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.sl.reruna.game.Direction
import com.sl.reruna.game.GameEngine
import com.sl.reruna.game.GameState

class RerunaViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private val _state = mutableStateOf(
        GameEngine.newGame(
            seed = System.nanoTime(),
            bestScore = preferences.getInt(KEY_BEST_SCORE, 0),
        ),
    )
    val state: State<GameState>
        get() = _state

    private val _paused = mutableStateOf(false)
    val paused: State<Boolean>
        get() = _paused

    private val _showTutorial = mutableStateOf(
        !preferences.getBoolean(KEY_TUTORIAL_SEEN, false),
    )
    val showTutorial: State<Boolean>
        get() = _showTutorial

    fun move(direction: Direction) {
        if (_paused.value) return

        val next = GameEngine.step(_state.value, direction)
        _state.value = next

        val storedBest = preferences.getInt(KEY_BEST_SCORE, 0)
        if (next.bestScore > storedBest) {
            preferences.edit()
                .putInt(KEY_BEST_SCORE, next.bestScore)
                .apply()
        }
    }

    fun restart() {
        _state.value = GameEngine.newGame(
            seed = System.nanoTime(),
            bestScore = preferences.getInt(KEY_BEST_SCORE, 0),
        )
    }

    fun setPaused(paused: Boolean) {
        _paused.value = paused
    }

    fun dismissTutorial() {
        if (!_showTutorial.value) return

        _showTutorial.value = false
        preferences.edit()
            .putBoolean(KEY_TUTORIAL_SEEN, true)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "reruna"
        const val KEY_BEST_SCORE = "best_score"
        const val KEY_TUTORIAL_SEEN = "tutorial_seen"
    }
}
