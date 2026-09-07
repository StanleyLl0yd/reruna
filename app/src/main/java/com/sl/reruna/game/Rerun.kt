package com.sl.reruna.game

data class Rerun(
    val id: Int,
    val origin: Cell,
    val moves: List<Direction>,
    val position: Cell,
)
