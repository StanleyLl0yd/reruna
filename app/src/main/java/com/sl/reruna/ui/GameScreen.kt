package com.sl.reruna.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    paused: Boolean,
    showTutorial: Boolean,
    onMove: (Direction) -> Unit,
    onRestart: () -> Unit,
    onDismissTutorial: () -> Unit,
) {
    val rerunPulse = remember { Animatable(0f) }
    val syncPulse = remember { Animatable(0f) }
    val resonancePulse = remember { Animatable(0f) }

    LaunchedEffect(state.turn, state.rerunCreatedThisTurn) {
        if (state.rerunCreatedThisTurn) {
            rerunPulse.snapTo(1f)
            rerunPulse.animateTo(0f, tween(durationMillis = 480))
        }
    }
    LaunchedEffect(state.turn, state.lastSyncCount) {
        if (state.lastSyncCount >= 2) {
            syncPulse.snapTo(1f)
            syncPulse.animateTo(0f, tween(durationMillis = 620))
        }
    }
    LaunchedEffect(state.turn, state.resonanceStartedThisTurn) {
        if (state.resonanceStartedThisTurn) {
            resonancePulse.snapTo(1f)
            resonancePulse.animateTo(0f, tween(durationMillis = 760))
        }
    }

    GameFeedback(
        state = state,
        enabled = !paused,
    )

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
                enabled = !paused,
                rerunPulse = rerunPulse.value,
                syncPulse = syncPulse.value,
                resonancePulse = resonancePulse.value,
                onMove = onMove,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            if (showTutorial && !state.gameOver) {
                Spacer(Modifier.height(6.dp))
                TutorialHint(
                    state = state,
                    onDismiss = onDismissTutorial,
                )
            }

            Spacer(Modifier.height(6.dp))
            DirectionPad(
                enabled = !paused && !state.gameOver,
                onMove = onMove,
            )
            Spacer(Modifier.height(6.dp))
            Footer(state)
        }

        if (state.gameOver) {
            GameOverOverlay(
                state = state,
                onRestart = onRestart,
                modifier = Modifier.align(Alignment.Center),
            )
        } else if (paused) {
            PauseOverlay(
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
        state.rerunCreatedThisTurn -> "RERUN ONLINE"
        state.sparksCollectedThisTurn > 0 -> "SPARK ×" + state.sparksCollectedThisTurn
        else -> ""
    }
    val color = when {
        state.resonanceStartedThisTurn -> RerunaViolet
        state.lastSyncCount >= 2 -> RerunaCyan
        state.rerunCreatedThisTurn -> RerunaViolet
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
    enabled: Boolean,
    rerunPulse: Float,
    syncPulse: Float,
    resonancePulse: Float,
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
                contentDescription =
                    "RERUNA board. Swipe up, down, left, or right, or use the direction buttons."
            }
            .pointerInput(enabled, state.gameOver, thresholdPx) {
                if (!enabled || state.gameOver) return@pointerInput
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
        val boardSize = Size(boardWidth, boardHeight)
        val boardCorner = CornerRadius(cellSize * 0.2f)

        drawRoundRect(
            color = if (state.resonanceTurnsRemaining > 0) {
                RerunaViolet.copy(alpha = 0.09f)
            } else {
                RerunaSurface
            },
            topLeft = origin,
            size = boardSize,
            cornerRadius = boardCorner,
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

        if (state.lastSyncCount >= 2) {
            state.lastSyncCells.forEach { syncCell ->
                drawCircle(
                    color = RerunaCyan.copy(alpha = 0.30f + syncPulse * 0.55f),
                    radius = cellSize * (0.28f + syncPulse * 0.14f),
                    center = cellCenter(syncCell, origin, cellSize),
                    style = Stroke(width = cellSize * 0.055f),
                )
            }
        }

        val playerCenter = cellCenter(state.player, origin, cellSize)
        val resonanceBoost = if (state.resonanceTurnsRemaining > 0) 0.08f else 0f
        drawCircle(
            color = if (state.resonanceTurnsRemaining > 0) {
                RerunaViolet.copy(alpha = 0.22f + resonancePulse * 0.18f)
            } else {
                RerunaCyan.copy(alpha = 0.16f)
            },
            radius = cellSize * (0.34f + resonanceBoost),
            center = playerCenter,
        )
        drawCircle(
            color = RerunaText,
            radius = cellSize * 0.13f,
            center = playerCenter,
        )
        drawCircle(
            color = if (state.resonanceTurnsRemaining > 0) RerunaViolet else RerunaCyan,
            radius = cellSize * 0.13f,
            center = playerCenter,
            style = Stroke(width = cellSize * 0.035f),
        )

        if (state.resonanceTurnsRemaining > 0) {
            drawRoundRect(
                color = RerunaViolet.copy(alpha = 0.34f + resonancePulse * 0.40f),
                topLeft = origin,
                size = boardSize,
                cornerRadius = boardCorner,
                style = Stroke(width = cellSize * 0.04f),
            )
        }

        if (rerunPulse > 0f) {
            drawRoundRect(
                color = RerunaViolet.copy(alpha = rerunPulse * 0.72f),
                topLeft = origin,
                size = boardSize,
                cornerRadius = boardCorner,
                style = Stroke(width = cellSize * (0.03f + rerunPulse * 0.04f)),
            )
        }
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
private fun TutorialHint(
    state: GameState,
    onDismiss: () -> Unit,
) {
    val text = when {
        state.lastSyncCount >= 2 ->
            "SYNC: overlap your selves to score, cut Entropy, and charge Resonance."
        state.reruns.isNotEmpty() ->
            "Your Rerun repeats its 8 moves. Shape the next route to meet it on purpose."
        state.turn == 0 ->
            "Swipe or tap a direction. Time advances only when you move."
        else ->
            "Your first 8 moves are being recorded. The route will become your first Rerun."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RerunaSurface,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = RerunaMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontFamily = FontFamily.Monospace,
            )
            TextButton(onClick = onDismiss) {
                Text(
                    text = "GOT IT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DirectionPad(
    enabled: Boolean,
    onMove: (Direction) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DirectionButton("←", "Move left", Direction.LEFT, enabled, onMove)
        DirectionButton("↑", "Move up", Direction.UP, enabled, onMove)
        DirectionButton("↓", "Move down", Direction.DOWN, enabled, onMove)
        DirectionButton("→", "Move right", Direction.RIGHT, enabled, onMove)
    }
}

@Composable
private fun DirectionButton(
    label: String,
    description: String,
    direction: Direction,
    enabled: Boolean,
    onMove: (Direction) -> Unit,
) {
    OutlinedButton(
        onClick = { onMove(direction) },
        enabled = enabled,
        modifier = Modifier
            .size(44.dp)
            .semantics {
                contentDescription = description
            },
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

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
            text = if (state.combo > 0) "COMBO ×" + state.combo else "SWIPE OR TAP",
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
private fun PauseOverlay(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(28.dp),
        color = RerunaSurface.copy(alpha = 0.98f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 12.dp,
    ) {
        Text(
            text = "PAUSED",
            modifier = Modifier.padding(horizontal = 34.dp, vertical = 22.dp),
            color = RerunaText,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
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
