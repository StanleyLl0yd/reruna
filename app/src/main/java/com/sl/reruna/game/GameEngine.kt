package com.sl.reruna.game

import kotlin.math.max
import kotlin.math.min

object GameEngine {
    private const val DEFAULT_SEED = 0x524552554E41L
    private const val RNG_MULTIPLIER = 6364136223846793005L
    private const val RNG_INCREMENT = 1442695040888963407L

    fun newGame(
        seed: Long = DEFAULT_SEED,
        bestScore: Int = 0,
    ): GameState {
        val player = Cell(
            x = GameRules.GRID_WIDTH / 2,
            y = GameRules.GRID_HEIGHT / 2,
        )
        val seeded = spawnUntil(
            sparks = emptySet(),
            targetCount = GameRules.STARTING_SPARKS,
            occupied = setOf(player),
            rngState = if (seed == 0L) DEFAULT_SEED else seed,
        )
        return GameState(
            player = player,
            reruns = emptyList(),
            recording = emptyList(),
            recordingOrigin = player,
            sparks = seeded.sparks,
            score = 0,
            bestScore = bestScore,
            combo = 0,
            turnsWithoutSpark = 0,
            entropy = 0,
            resonance = 0,
            resonanceTurnsRemaining = 0,
            turn = 0,
            rngState = seeded.rngState,
            lastSyncCount = 0,
            rerunCreatedThisTurn = false,
            resonanceStartedThisTurn = false,
            gameOver = false,
        )
    }

    fun step(
        state: GameState,
        direction: Direction,
    ): GameState {
        if (state.gameOver) return state

        val stepIndex = state.recording.size
        val nextPlayer = state.player.moved(direction)
        val movedReruns = state.reruns.map { rerun ->
            rerun.copy(position = rerun.position.moved(rerun.moves[stepIndex]))
        }

        val activePositions = buildList {
            add(nextPlayer)
            movedReruns.forEach { add(it.position) }
        }
        val occupiedAfterMove = activePositions.toSet()
        val collectedCells = state.sparks.intersect(occupiedAfterMove)
        val collectedCount = collectedCells.size

        val syncCount = activePositions
            .groupingBy { it }
            .eachCount()
            .values
            .maxOrNull()
            ?.takeIf { it >= 2 }
            ?: 0

        val activeResonance = state.resonanceTurnsRemaining > 0
        val scoreMultiplier = if (activeResonance) 2 else 1
        val sparkScore = sparkScore(
            collectedCount = collectedCount,
            comboBefore = state.combo,
        )
        val syncScore = if (syncCount >= 2) {
            250 * syncCount * syncCount
        } else {
            0
        }
        val nextScore = state.score + (sparkScore + syncScore) * scoreMultiplier

        val turnsWithoutSpark = if (collectedCount > 0) {
            0
        } else {
            state.turnsWithoutSpark + 1
        }
        val nextCombo = when {
            collectedCount > 0 -> state.combo + collectedCount
            turnsWithoutSpark >= GameRules.COMBO_GRACE_TURNS -> 0
            else -> state.combo
        }

        val rawResonance = if (!activeResonance && syncCount >= 2) {
            state.resonance + syncCount * 12
        } else {
            state.resonance
        }
        val resonanceTriggered = !activeResonance && rawResonance >= GameRules.RESONANCE_MAX
        val nextResonanceTurns = when {
            resonanceTriggered -> GameRules.RESONANCE_TURNS
            activeResonance -> max(0, state.resonanceTurnsRemaining - 1)
            else -> 0
        }
        val nextResonance = when {
            resonanceTriggered -> 0
            activeResonance -> state.resonance
            else -> min(GameRules.RESONANCE_MAX, rawResonance)
        }

        val baseEntropyGain = 2 + state.turn / 64 + state.reruns.size / 2
        val entropyGain = if (activeResonance) max(1, baseEntropyGain - 1) else baseEntropyGain
        val entropyReduction = collectedCount * 7 + if (syncCount >= 2) syncCount * 4 else 0
        val nextEntropy = (state.entropy + entropyGain - entropyReduction)
            .coerceIn(0, GameRules.ENTROPY_MAX)

        val recordedMoves = state.recording + direction
        val completedCycle = recordedMoves.size == GameRules.RECORDING_LENGTH

        val nextReruns: List<Rerun>
        val nextRecording: List<Direction>
        val nextRecordingOrigin: Cell
        if (completedCycle) {
            val newRerun = Rerun(
                id = state.turn / GameRules.RECORDING_LENGTH + 1,
                origin = state.recordingOrigin,
                moves = recordedMoves,
                position = state.recordingOrigin,
            )
            nextReruns = (movedReruns + newRerun)
                .takeLast(GameRules.MAX_RERUNS)
                .map { it.copy(position = it.origin) }
            nextRecording = emptyList()
            nextRecordingOrigin = nextPlayer
        } else {
            nextReruns = movedReruns
            nextRecording = recordedMoves
            nextRecordingOrigin = state.recordingOrigin
        }

        val remainingSparks = state.sparks - collectedCells
        val targetSparkCount = min(
            GameRules.MAX_SPARKS,
            GameRules.STARTING_SPARKS + state.turn / 48,
        )
        val displayOccupied = buildSet {
            add(nextPlayer)
            nextReruns.forEach { add(it.position) }
        }
        val spawned = spawnUntil(
            sparks = remainingSparks,
            targetCount = targetSparkCount,
            occupied = displayOccupied,
            rngState = state.rngState,
        )

        val isGameOver = nextEntropy >= GameRules.ENTROPY_MAX
        val nextBestScore = max(state.bestScore, nextScore)

        return state.copy(
            player = nextPlayer,
            reruns = nextReruns,
            recording = nextRecording,
            recordingOrigin = nextRecordingOrigin,
            sparks = spawned.sparks,
            score = nextScore,
            bestScore = nextBestScore,
            combo = nextCombo,
            turnsWithoutSpark = turnsWithoutSpark,
            entropy = nextEntropy,
            resonance = nextResonance,
            resonanceTurnsRemaining = nextResonanceTurns,
            turn = state.turn + 1,
            rngState = spawned.rngState,
            lastSyncCount = syncCount,
            rerunCreatedThisTurn = completedCycle,
            resonanceStartedThisTurn = resonanceTriggered,
            gameOver = isGameOver,
        )
    }

    private fun sparkScore(
        collectedCount: Int,
        comboBefore: Int,
    ): Int {
        var total = 0
        repeat(collectedCount) { index ->
            val comboNumber = comboBefore + index + 1
            val comboMultiplier = 1 + (comboNumber - 1) / 5
            total += 100 * comboMultiplier
        }
        return total
    }

    private fun spawnUntil(
        sparks: Set<Cell>,
        targetCount: Int,
        occupied: Set<Cell>,
        rngState: Long,
    ): SpawnResult {
        var result = sparks
        var random = rngState
        var attempts = 0

        while (result.size < targetCount && attempts < 256) {
            val xResult = nextInt(random, GameRules.GRID_WIDTH)
            random = xResult.rngState
            val yResult = nextInt(random, GameRules.GRID_HEIGHT)
            random = yResult.rngState
            val candidate = Cell(xResult.value, yResult.value)
            if (candidate !in occupied) {
                result = result + candidate
            }
            attempts++
        }

        return SpawnResult(result, random)
    }

    private fun nextInt(
        rngState: Long,
        bound: Int,
    ): RandomResult {
        val nextState = rngState * RNG_MULTIPLIER + RNG_INCREMENT
        val value = ((nextState ushr 1) % bound.toLong()).toInt()
        return RandomResult(value, nextState)
    }

    private data class RandomResult(
        val value: Int,
        val rngState: Long,
    )

    private data class SpawnResult(
        val sparks: Set<Cell>,
        val rngState: Long,
    )
}
