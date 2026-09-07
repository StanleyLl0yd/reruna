package com.sl.reruna.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sl.reruna.game.Cell
import com.sl.reruna.game.Direction
import com.sl.reruna.game.GameRules
import com.sl.reruna.game.GameState
import com.sl.reruna.ui.theme.RerunaBackground
import com.sl.reruna.ui.theme.RerunaCyan
import com.sl.reruna.ui.theme.RerunaDanger
import com.sl.reruna.ui.theme.RerunaGrid
import com.sl.reruna.ui.theme.RerunaMuted
import com.sl.reruna.ui.theme.RerunaSurface
import com.sl.reruna.ui.theme.RerunaText
import com.sl.reruna.ui.theme.RerunaViolet
import kotlin.math.abs
import kotlin.math.min

@Composable
fun GameScreen(
    state: GameState,
    onMove: (Direction) -> Unit,
    onRestart: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RerunaBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Header(state)
            Spacer(Modifier.height(10.dp))
            ProgressPanel(state)
            Spacer(Modifier.height(8.dp))
            EventLine(state)
            Spacer(Modifier.height(4.dp))

            GameBoard(
                state = state,
                onMove = onMove,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Spacer(Modifier.height(8.dp))
            Footer(state)
        }

        if (state.gameOver) {
            GameOverOverlay(
                state = state,
                onRestart = onRestart,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun Header(state: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                text = "RERUNA",
                color = RerunaText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
            Text(
                text = "BUILD YOUR PAST.",
                color = RerunaMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Stat(label = "SCORE", value = state.score.toString())
            Stat(label = "BEST", value = state.bestScore.toString())
        }
    }
}

@Composable
private fun Stat(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            color = RerunaMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
        )
        Text(
            text = value,
            color = RerunaText,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ProgressPanel(state: GameState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Meter(
            label = "ENTROPY",
            value = state.entropy / GameRules.ENTROPY_MAX.toFloat(),
            valueText = state.entropy.toString() + "%",
            indicatorColor = RerunaDanger,
        )
        Meter(
            label = if (state.resonanceTurnsRemaining > 0) "RESONANCE ACTIVE" else "RESONANCE",
            value = if (state.resonanceTurnsRemaining > 0) {
                state.resonanceTurnsRemaining / GameRules.RESONANCE_TURNS.toFloat()
            } else {
                state.resonance / GameRules.RESONANCE_MAX.toFloat()
            },
            valueText = if (state.resonanceTurnsRemaining > 0) {
                state.resonanceTurnsRemaining.toString() + "T"
            } else {
                state.resonance.toString() + "%"
            },
            indicatorColor = RerunaViolet,
        )
    }
}

@Composable
private fun Meter(
    label: String,
    value: Float,
    valueText: String,
    indicatorColor: Color,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                color = RerunaMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = valueText,
                color = RerunaMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = indicatorColor,
            trackColor = RerunaGrid,
        )
    }
}

@Composable
private fun EventLine(state: GameState) {
    val text = when {
        state.resonanceStartedThisTurn -> "RESONANCE"
        state.lastSyncCount >= 2 -> "SYNC ×" + state.lastSyncCount
        state.rerunCreatedThisTurn -> "RERUN"
        else -> ""
    }
    val color = when {
        state.resonanceStartedThisTurn -> RerunaViolet
        state.lastSyncCount >= 2 -> RerunaCyan
        else -> RerunaMuted
    }

    Text(
        text = text,
        modifier = Modifier.height(22.dp),
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp,
    )
}

@Composable
private fun GameBoard(
    state: GameState,
    onMove: (Direction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val thresholdPx = with(LocalDensity.current) { 28.dp.toPx() }
    val pathCells = remember(state.recordingOrigin, state.recording) {
        buildList {
            var cell = state.recordingOrigin
            add(cell)
            state.recording.forEach { direction ->
                cell = cell.moved(direction)
                add(cell)
            }
        }
    }

    Canvas(
        modifier = modifier
            .semantics {
                contentDescription = "RERUNA board. Swipe up, down, left, or right to move."
            }
            .pointerInput(state.gameOver, thresholdPx) {
                if (state.gameOver) return@pointerInput
                var drag = Offset.Zero
                detectDragGestures(
                    onDragStart = { drag = Offset.Zero },
                    onDragCancel = { drag = Offset.Zero },
                    onDragEnd = {
                        if (drag.getDistance() >= thresholdPx) {
                            val direction = if (abs(drag.x) > abs(drag.y)) {
                                if (drag.x > 0f) Direction.RIGHT else Direction.LEFT
                            } else {
                                if (drag.y > 0f) Direction.DOWN else Direction.UP
                            }
                            onMove(direction)
                        }
                        drag = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        drag += dragAmount
                    },
                )
            },
    ) {
        val cellSize = min(
            size.width / GameRules.GRID_WIDTH,
            size.height / GameRules.GRID_HEIGHT,
        )
        val boardWidth = cellSize * GameRules.GRID_WIDTH
        val boardHeight = cellSize * GameRules.GRID_HEIGHT
        val origin = Offset(
            x = (size.width - boardWidth) / 2f,
            y = (size.height - boardHeight) / 2f,
        )

        drawRoundRect(
            color = RerunaSurface,
            topLeft = origin,
            size = Size(boardWidth, boardHeight),
            cornerRadius = CornerRadius(cellSize * 0.2f),
        )

        for (x in 0..GameRules.GRID_WIDTH) {
            val lineX = origin.x + x * cellSize
            drawLine(
                color = RerunaGrid.copy(alpha = 0.55f),
                start = Offset(lineX, origin.y),
                end = Offset(lineX, origin.y + boardHeight),
                strokeWidth = 1f,
            )
        }
        for (y in 0..GameRules.GRID_HEIGHT) {
            val lineY = origin.y + y * cellSize
            drawLine(
                color = RerunaGrid.copy(alpha = 0.55f),
                start = Offset(origin.x, lineY),
                end = Offset(origin.x + boardWidth, lineY),
                strokeWidth = 1f,
            )
        }

        for (index in 0 until pathCells.lastIndex) {
            val fromCell = pathCells[index]
            val toCell = pathCells[index + 1]
            val wraps = abs(fromCell.x - toCell.x) > 1 || abs(fromCell.y - toCell.y) > 1
            if (!wraps) {
                drawLine(
                    color = RerunaCyan.copy(alpha = 0.18f),
                    start = cellCenter(fromCell, origin, cellSize),
                    end = cellCenter(toCell, origin, cellSize),
                    strokeWidth = cellSize * 0.045f,
                )
            }
        }

        state.sparks.forEach { spark ->
            val center = cellCenter(spark, origin, cellSize)
            drawCircle(
                color = RerunaCyan.copy(alpha = 0.12f),
                radius = cellSize * 0.30f,
                center = center,
            )
            drawCircle(
                color = RerunaCyan,
                radius = cellSize * 0.10f,
                center = center,
            )
        }

        state.reruns.forEachIndexed { index, rerun ->
            val center = cellCenter(rerun.position, origin, cellSize)
            val alpha = (0.62f - index * 0.06f).coerceAtLeast(0.24f)
            drawCircle(
                color = RerunaViolet.copy(alpha = alpha * 0.20f),
                radius = cellSize * 0.30f,
                center = center,
            )
            drawCircle(
                color = RerunaViolet.copy(alpha = alpha),
                radius = cellSize * 0.16f,
                center = center,
                style = Stroke(width = cellSize * 0.035f),
            )
            drawCircle(
                color = RerunaViolet.copy(alpha = alpha * 0.55f),
                radius = cellSize * 0.055f,
                center = center,
            )
        }

        val playerCenter = cellCenter(state.player, origin, cellSize)
        drawCircle(
            color = RerunaCyan.copy(alpha = 0.16f),
            radius = cellSize * 0.34f,
            center = playerCenter,
        )
        drawCircle(
            color = RerunaText,
            radius = cellSize * 0.13f,
            center = playerCenter,
        )
        drawCircle(
            color = RerunaCyan,
            radius = cellSize * 0.13f,
            center = playerCenter,
            style = Stroke(width = cellSize * 0.035f),
        )
    }
}

private fun cellCenter(
    cell: Cell,
    origin: Offset,
    cellSize: Float,
): Offset = Offset(
    x = origin.x + (cell.x + 0.5f) * cellSize,
    y = origin.y + (cell.y + 0.5f) * cellSize,
)

@Composable
private fun Footer(state: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "RERUN " + state.reruns.size + "/" + GameRules.MAX_RERUNS,
            color = RerunaMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = if (state.combo > 0) "COMBO ×" + state.combo else "SWIPE TO MOVE",
            color = if (state.combo > 0) RerunaCyan else RerunaMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (state.combo > 0) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = "STEP " + state.stepInCycle + "/" + GameRules.RECORDING_LENGTH,
            color = RerunaMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun GameOverOverlay(
    state: GameState,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(28.dp),
        color = RerunaSurface.copy(alpha = 0.98f),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "RUN ENDED",
                color = RerunaDanger,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = state.score.toString(),
                color = RerunaText,
                fontSize = 40.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "BEST " + state.bestScore,
                color = RerunaMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
            )
            Spacer(Modifier.size(22.dp))
            Button(onClick = onRestart) {
                Text(
                    text = "AGAIN",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                )
            }
        }
    }
}
