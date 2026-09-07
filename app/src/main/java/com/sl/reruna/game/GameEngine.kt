package com.sl.reruna.game

import kotlin.math.max
import kotlin.math.min

object GameEngine {
    private const val DEFAULT_SEED = 0x524552554E41L
    private const val RNG_MULTIPLIER = 6364136223846793005L
    private const val RNG_INCREMENT = 1442695040888963407L
    private const val PLAYER_PARTICIPANT_ID = 0

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
            totalSparksCollected = 0,
            syncEvents = 0,
            resonanceActivations = 0,
            rerunsCreated = 0,
            maxCombo = 0,
            activeSyncGroups = emptySet(),
            sparksCollectedThisTurn = 0,
            lastSyncCount = 0,
            lastSyncCells = emptySet(),
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

        val activePositions = buildSet {
            add(nextPlayer)
            movedReruns.forEach { add(it.position) }
        }
        val collectedCells = state.sparks.intersect(activePositions)
        val collectedCount = collectedCells.size

        val syncGroupsAtMove = syncGroupsByCell(
            player = nextPlayer,
            reruns = movedReruns,
        )
        val newSyncs = syncGroupsAtMove.filterValues { group ->
            group !in state.activeSyncGroups
        }
        val syncCells = newSyncs.keys
        val syncCount = newSyncs.values
            .maxOfOrNull { it.size }
            ?: 0
        val syncParticipantTotal = newSyncs.values.sumOf { it.size }

        val activeResonance = state.resonanceTurnsRemaining > 0
        val scoreMultiplier = if (activeResonance) {
            GameRules.RESONANCE_SCORE_MULTIPLIER
        } else {
            1
        }
        val sparkScore = sparkScore(
            collectedCount = collectedCount,
            comboBefore = state.combo,
        )
        val syncScore = newSyncs.values.sumOf { group ->
            GameRules.SYNC_BASE_SCORE * group.size * group.size
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

        val rawResonance = if (!activeResonance && syncParticipantTotal > 0) {
            state.resonance +
                syncParticipantTotal * GameRules.RESONANCE_CHARGE_PER_PARTICIPANT
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

        val baseEntropyGain = GameRules.STARTING_ENTROPY_GAIN +
            state.turn / GameRules.ENTROPY_TURN_RAMP_INTERVAL +
            state.reruns.size / GameRules.ENTROPY_RERUN_RAMP_DIVISOR
        val entropyGain = if (activeResonance) {
            max(1, baseEntropyGain - GameRules.RESONANCE_ENTROPY_RELIEF)
        } else {
            baseEntropyGain
        }
        val entropyReduction =
            collectedCount * GameRules.SPARK_ENTROPY_REDUCTION +
                syncParticipantTotal * GameRules.SYNC_ENTROPY_REDUCTION_PER_PARTICIPANT
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
        val displayOccupied = buildSet {
            add(nextPlayer)
            nextReruns.forEach { add(it.position) }
        }
        val spawned = spawnUntil(
            sparks = remainingSparks,
            targetCount = GameRules.MAX_SPARKS,
            occupied = displayOccupied,
            rngState = state.rngState,
        )

        val isGameOver = nextEntropy >= GameRules.ENTROPY_MAX
        val nextBestScore = max(state.bestScore, nextScore)
        val nextActiveSyncGroups = syncGroupsByCell(
            player = nextPlayer,
            reruns = nextReruns,
        ).values.toSet()

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
            totalSparksCollected = state.totalSparksCollected + collectedCount,
            syncEvents = state.syncEvents + newSyncs.size,
            resonanceActivations = state.resonanceActivations + if (resonanceTriggered) 1 else 0,
            rerunsCreated = state.rerunsCreated + if (completedCycle) 1 else 0,
            maxCombo = max(state.maxCombo, nextCombo),
            activeSyncGroups = nextActiveSyncGroups,
            sparksCollectedThisTurn = collectedCount,
            lastSyncCount = syncCount,
            lastSyncCells = syncCells,
            rerunCreatedThisTurn = completedCycle,
            resonanceStartedThisTurn = resonanceTriggered,
            gameOver = isGameOver,
        )
    }

    private fun syncGroupsByCell(
        player: Cell,
        reruns: List<Rerun>,
    ): Map<Cell, Set<Int>> {
        val participantsByCell = mutableMapOf<Cell, MutableSet<Int>>()
        participantsByCell.getOrPut(player) { mutableSetOf() }
            .add(PLAYER_PARTICIPANT_ID)
        reruns.forEach { rerun ->
            participantsByCell.getOrPut(rerun.position) { mutableSetOf() }
                .add(rerun.id)
        }
        return participantsByCell
            .filterValues { it.size >= 2 }
            .mapValues { (_, participants) -> participants.toSet() }
    }

    private fun sparkScore(
        collectedCount: Int,
        comboBefore: Int,
    ): Int {
        var total = 0
        repeat(collectedCount) { index ->
            val comboNumber = comboBefore + index + 1
            val comboMultiplier = 1 + (comboNumber - 1) / GameRules.COMBO_SCORE_STEP
            total += GameRules.SPARK_BASE_SCORE * comboMultiplier
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
