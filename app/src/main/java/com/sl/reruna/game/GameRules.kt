package com.sl.reruna.game

object GameRules {
    const val GRID_WIDTH = 7
    const val GRID_HEIGHT = 9
    const val RECORDING_LENGTH = 8
    const val MAX_RERUNS = 6

    const val STARTING_SPARKS = 4
    const val MAX_SPARKS = 4

    const val ENTROPY_MAX = 100
    const val STARTING_ENTROPY_GAIN = 1
    const val ENTROPY_TURN_RAMP_INTERVAL = 96
    const val ENTROPY_RERUN_RAMP_DIVISOR = 4
    const val RESONANCE_ENTROPY_RELIEF = 1
    const val SPARK_ENTROPY_REDUCTION = 7
    const val SYNC_ENTROPY_REDUCTION_PER_PARTICIPANT = 4

    const val RESONANCE_MAX = 100
    const val RESONANCE_CHARGE_PER_PARTICIPANT = 12
    const val RESONANCE_TURNS = 8
    const val RESONANCE_SCORE_MULTIPLIER = 2

    const val SPARK_BASE_SCORE = 100
    const val SYNC_BASE_SCORE = 150
    const val COMBO_GRACE_TURNS = 4
    const val COMBO_SCORE_STEP = 5
}
