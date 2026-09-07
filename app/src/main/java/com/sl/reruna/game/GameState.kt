package com.sl.reruna.game

data class GameState(
    val player: Cell,
    val reruns: List<Rerun>,
    val recording: List<Direction>,
    val recordingOrigin: Cell,
    val sparks: Set<Cell>,
    val score: Int,
    val bestScore: Int,
    val combo: Int,
    val turnsWithoutSpark: Int,
    val entropy: Int,
    val resonance: Int,
    val resonanceTurnsRemaining: Int,
    val turn: Int,
    val rngState: Long,
    val lastSyncCount: Int,
    val rerunCreatedThisTurn: Boolean,
    val resonanceStartedThisTurn: Boolean,
    val gameOver: Boolean,
) {
    val cycle: Int
        get() = turn / GameRules.RECORDING_LENGTH

    val stepInCycle: Int
        get() = recording.size
}
