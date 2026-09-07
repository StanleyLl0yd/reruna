package com.sl.reruna.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    @Test
    fun movementWrapsAroundToroidalGrid() {
        val topLeft = Cell(0, 0)

        assertEquals(Cell(GameRules.GRID_WIDTH - 1, 0), topLeft.moved(Direction.LEFT))
        assertEquals(Cell(0, GameRules.GRID_HEIGHT - 1), topLeft.moved(Direction.UP))
    }

    @Test
    fun newGameIsDeterministicForSameSeed() {
        val first = GameEngine.newGame(seed = 42L)
        val second = GameEngine.newGame(seed = 42L)

        assertEquals(first.player, second.player)
        assertEquals(first.sparks, second.sparks)
        assertEquals(first.rngState, second.rngState)
    }

    @Test
    fun eightMovesCreateRerunAndStartFreshRecording() {
        var state = GameEngine.newGame(seed = 7L)

        repeat(GameRules.RECORDING_LENGTH) {
            state = GameEngine.step(state.copy(entropy = 0), Direction.RIGHT)
        }

        assertEquals(1, state.reruns.size)
        assertEquals(GameRules.RECORDING_LENGTH, state.reruns.single().moves.size)
        assertTrue(state.recording.isEmpty())
        assertTrue(state.rerunCreatedThisTurn)
    }

    @Test
    fun rerunReplaysRecordedMoveOnNextCycle() {
        var state = GameEngine.newGame(seed = 7L)

        repeat(GameRules.RECORDING_LENGTH) {
            state = GameEngine.step(state.copy(entropy = 0), Direction.RIGHT)
        }

        val origin = state.reruns.single().origin
        state = GameEngine.step(state.copy(entropy = 0), Direction.RIGHT)

        assertEquals(origin.moved(Direction.RIGHT), state.reruns.single().position)
    }

    @Test
    fun onlySixMostRecentRerunsRemainActive() {
        var state = GameEngine.newGame(seed = 11L)

        repeat(GameRules.RECORDING_LENGTH * 7) {
            state = GameEngine.step(
                state.copy(
                    entropy = 0,
                    gameOver = false,
                ),
                Direction.RIGHT,
            )
        }

        assertEquals(GameRules.MAX_RERUNS, state.reruns.size)
        assertEquals(listOf(2, 3, 4, 5, 6, 7), state.reruns.map { it.id })
    }

    @Test
    fun matchingClosedRouteCreatesSync() {
        var state = GameEngine.newGame(seed = 19L)
        val closedRoute = listOf(
            Direction.RIGHT,
            Direction.LEFT,
            Direction.RIGHT,
            Direction.LEFT,
            Direction.RIGHT,
            Direction.LEFT,
            Direction.RIGHT,
            Direction.LEFT,
        )

        closedRoute.forEach { direction ->
            state = GameEngine.step(state.copy(entropy = 0), direction)
        }

        state = GameEngine.step(state.copy(entropy = 0), Direction.RIGHT)

        assertEquals(2, state.lastSyncCount)
        assertTrue(state.score >= 1000)
    }

    @Test
    fun entropyAtLimitEndsRun() {
        val state = GameEngine.newGame(seed = 23L).copy(
            entropy = GameRules.ENTROPY_MAX - 1,
            sparks = emptySet(),
        )

        val ended = GameEngine.step(state, Direction.UP)

        assertTrue(ended.gameOver)
        assertEquals(GameRules.ENTROPY_MAX, ended.entropy)
    }

    @Test
    fun movesAfterGameOverAreIgnored() {
        val ended = GameEngine.newGame(seed = 29L).copy(gameOver = true)

        val next = GameEngine.step(ended, Direction.DOWN)

        assertEquals(ended, next)
        assertFalse(next.rerunCreatedThisTurn)
    }
}
