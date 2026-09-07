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

    fun move(direction: Direction) {
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

    private companion object {
        const val PREFERENCES_NAME = "reruna"
        const val KEY_BEST_SCORE = "best_score"
    }
}
