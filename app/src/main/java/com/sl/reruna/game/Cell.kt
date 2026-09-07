package com.sl.reruna.game

data class Cell(
    val x: Int,
    val y: Int,
) {
    fun moved(
        direction: Direction,
        width: Int = GameRules.GRID_WIDTH,
        height: Int = GameRules.GRID_HEIGHT,
    ): Cell {
        val nextX = when (direction) {
            Direction.LEFT -> x - 1
            Direction.RIGHT -> x + 1
            else -> x
        }
        val nextY = when (direction) {
            Direction.UP -> y - 1
            Direction.DOWN -> y + 1
            else -> y
        }
        return Cell(nextX.wrap(width), nextY.wrap(height))
    }
}

private fun Int.wrap(size: Int): Int = ((this % size) + size) % size
