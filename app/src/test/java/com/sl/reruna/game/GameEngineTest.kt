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
    fun newGameStartsWithFourSparks() {
        val state = GameEngine.newGame(seed = 43L)

        assertEquals(4, state.sparks.size)
    }

    @Test
    fun firstTurnUsesGentlerEntropyGain() {
        val state = GameEngine.newGame(seed = 47L).copy(
            sparks = emptySet(),
        )

        val next = GameEngine.step(state, Direction.LEFT)

        assertEquals(GameRules.STARTING_ENTROPY_GAIN, next.entropy)
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
        assertEquals(1, state.rerunsCreated)
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
        var state = closedRouteState(seed = 19L)

        state = GameEngine.step(state.copy(entropy = 0), Direction.RIGHT)

        assertEquals(2, state.lastSyncCount)
        assertEquals(setOf(state.player), state.lastSyncCells)
        assertEquals(1, state.syncEvents)
        assertEquals(24, state.resonance)
        assertTrue(state.score >= 600)
    }

    @Test
    fun sustainedOverlapDoesNotFarmSyncRewardsEveryTurn() {
        var state = closedRouteState(seed = 53L)

        state = GameEngine.step(state.copy(entropy = 0), Direction.RIGHT)
        val scoreAfterConvergence = state.score
        val resonanceAfterConvergence = state.resonance

        state = GameEngine.step(state.copy(entropy = 0), Direction.LEFT)

        assertEquals(1, state.syncEvents)
        assertEquals(0, state.lastSyncCount)
        assertEquals(resonanceAfterConvergence, state.resonance)
        assertTrue(state.score >= scoreAfterConvergence)
    }

    @Test
    fun separationAllowsSameParticipantsToSyncAgain() {
        var state = closedRouteState(seed = 59L)

        state = GameEngine.step(state.copy(entropy = 0), Direction.RIGHT)
        state = GameEngine.step(state.copy(entropy = 0), Direction.UP)
        state = GameEngine.step(state.copy(entropy = 0), Direction.LEFT)

        assertEquals(1, state.syncEvents)

        state = GameEngine.step(state.copy(entropy = 0), Direction.DOWN)

        assertTrue(state.syncEvents >= 1)
    }

    @Test
    fun sparkCollectionIsExposedAsDeterministicTurnEvent() {
        val start = GameEngine.newGame(seed = 31L)
        val target = start.player.moved(Direction.RIGHT)
        val state = start.copy(
            sparks = setOf(target),
            entropy = 50,
        )

        val next = GameEngine.step(state, Direction.RIGHT)

        assertEquals(1, next.sparksCollectedThisTurn)
        assertEquals(1, next.totalSparksCollected)
        assertEquals(1, next.maxCombo)
        assertTrue(next.score >= GameRules.SPARK_BASE_SCORE)
        assertTrue(next.entropy < state.entropy)
    }

    @Test
    fun turnWithoutCollectionClearsSparkEvent() {
        val state = GameEngine.newGame(seed = 37L).copy(sparks = emptySet())

        val next = GameEngine.step(state, Direction.LEFT)

        assertEquals(0, next.sparksCollectedThisTurn)
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

    private fun closedRouteState(seed: Long): GameState {
        var state = GameEngine.newGame(seed = seed)
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
        return state
    }
}
