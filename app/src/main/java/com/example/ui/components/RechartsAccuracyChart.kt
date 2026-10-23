package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameEvaluationRecord
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Recharts-inspired Interactive Visual Dashboard Chart for Jetpack Compose.
 * Features:
 * - Smooth cubic spline accuracy curve
 * - Area gradient fill beneath curve
 * - 50% Baseline dashed reference line
 * - Subtle Cartesian grid
 * - Interactive scrubber / touch tooltip displaying Game #, Hit Num, Win/Loss, and Running Accuracy
 */
@Composable
fun RechartsAccuracyChart(
    timeline: List<Float>,
    games: List<GameEvaluationRecord>,
    modifier: Modifier = Modifier
) {
    if (timeline.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("recharts_empty_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Walang sapat na settled draws para sa 50-game chart.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        return
    }

    // Chronological games matching timeline (timeline is index 0..N-1, games is reversed for list,
    // so we get chronological list for chart scrub lookup)
    val chronologicalGames = remember(games) { games.reversed() }

    // Selected game index for Recharts tooltip (null when not scrubbing)
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val latestAccuracy = timeline.lastOrNull() ?: 50f
    val isOverallWin = latestAccuracy >= 50f
    val primaryColor = if (isOverallWin) StatusConnected else StatusOffline

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_accuracy_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Chart Header with Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RECHARTS ACCURACY CURVE",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Last ${timeline.size} Games Walk-Forward Accuracy Timeline",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                // Interactive Hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NavySurfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (selectedIndex == null) "I-tap o i-drag" else "Game #${(selectedIndex ?: 0) + 1}",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recharts Legend Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Win Rate Line Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(primaryColor)
                    )
                    Text(
                        text = "Model Win Rate",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Baseline Reference Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(2.dp)
                            .background(Color(0xFF64748B))
                    )
                    Text(
                        text = "50% Baseline",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Tooltip Card (Recharts floating card style)
            AnimatedVisibility(
                visible = selectedIndex != null && selectedIndex!! in chronologicalGames.indices,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val record = selectedIndex?.let { chronologicalGames.getOrNull(it) }
                if (record != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurfaceVariant)
                            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("recharts_tooltip_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "GAME #${record.gameNumber}",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "• Round ${record.serial}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "Taya: ${record.predictedParity} (${record.confidence}%) ➔ Hit: ${record.actualParity} ${record.hitNumber}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Hit number lottery ball tag
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (record.actualParity == "ODD") OddRed else EvenBlue)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${record.hitNumber}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                // Win/Loss pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (record.isWin) StatusConnected.copy(alpha = 0.2f) else StatusOffline.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (record.isWin) "WIN (${record.cumulativeAccuracy}%)" else "LOSS (${record.cumulativeAccuracy}%)",
                                        color = if (record.isWin) StatusConnected else StatusOffline,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Canvas Chart Area with touch gestures
            val yMin = 30f
            val yMax = 90f
            val yRange = yMax - yMin

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C1322))
                    .pointerInput(timeline.size) {
                        detectTapGestures(
                            onTap = { offset ->
                                val count = timeline.size
                                if (count > 1) {
                                    val usableWidth = size.width - 48.dp.toPx()
                                    val xLeft = 40.dp.toPx()
                                    val relX = (offset.x - xLeft).coerceIn(0f, usableWidth)
                                    val step = usableWidth / (count - 1)
                                    val idx = (relX / step).roundToInt().coerceIn(0, count - 1)
                                    selectedIndex = if (selectedIndex == idx) null else idx
                                }
                            }
                        )
                    }
                    .pointerInput(timeline.size) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val count = timeline.size
                                if (count > 1) {
                                    val usableWidth = size.width - 48.dp.toPx()
                                    val xLeft = 40.dp.toPx()
                                    val relX = (offset.x - xLeft).coerceIn(0f, usableWidth)
                                    val step = usableWidth / (count - 1)
                                    selectedIndex = (relX / step).roundToInt().coerceIn(0, count - 1)
                                }
                            },
                            onDrag = { change, _ ->
                                val count = timeline.size
                                if (count > 1) {
                                    val usableWidth = size.width - 48.dp.toPx()
                                    val xLeft = 40.dp.toPx()
                                    val relX = (change.position.x - xLeft).coerceIn(0f, usableWidth)
                                    val step = usableWidth / (count - 1)
                                    selectedIndex = (relX / step).roundToInt().coerceIn(0, count - 1)
                                }
                            }
                        )
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val leftPadding = 40.dp.toPx()
                    val rightPadding = 12.dp.toPx()
                    val topPadding = 16.dp.toPx()
                    val bottomPadding = 24.dp.toPx()

                    val chartWidth = w - leftPadding - rightPadding
                    val chartHeight = h - topPadding - bottomPadding

                    fun mapY(valPercent: Float): Float {
                        val clamped = valPercent.coerceIn(yMin, yMax)
                        val ratio = (clamped - yMin) / yRange
                        return (topPadding + chartHeight) - (ratio * chartHeight)
                    }

                    // 1. Horizontal Gridlines at 80%, 70%, 60%, 50%, 40%
                    val gridLevels = listOf(80f, 70f, 60f, 50f, 40f)
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    for (level in gridLevels) {
                        val y = mapY(level)
                        val isBaseline = (level == 50f)

                        drawLine(
                            color = if (isBaseline) Color(0xFF64748B) else NavyBorder.copy(alpha = 0.5f),
                            start = Offset(leftPadding, y),
                            end = Offset(w - rightPadding, y),
                            strokeWidth = if (isBaseline) 1.5.dp.toPx() else 1.dp.toPx(),
                            pathEffect = dashedEffect
                        )
                    }

                    // 2. Build Points
                    val n = timeline.size
                    if (n > 1) {
                        val stepX = chartWidth / (n - 1)
                        val points = timeline.mapIndexed { idx, acc ->
                            Offset(leftPadding + idx * stepX, mapY(acc))
                        }

                        // 3. Smooth Bezier Curve Path & Area Path
                        val curvePath = Path()
                        val areaPath = Path()

                        curvePath.moveTo(points.first().x, points.first().y)
                        areaPath.moveTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlX1 = p0.x + (p1.x - p0.x) / 2f
                            val controlY1 = p0.y
                            val controlX2 = p0.x + (p1.x - p0.x) / 2f
                            val controlY2 = p1.y

                            curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                            areaPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                        }

                        // Close area down to bottom
                        val bottomY = topPadding + chartHeight
                        areaPath.lineTo(points.last().x, bottomY)
                        areaPath.lineTo(points.first().x, bottomY)
                        areaPath.close()

                        // Draw Area Gradient Fill
                        val areaBrush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.35f),
                                primaryColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = topPadding,
                            endY = bottomY
                        )
                        drawPath(path = areaPath, brush = areaBrush)

                        // Draw Smooth Line
                        drawPath(
                            path = curvePath,
                            color = primaryColor,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // 4. Draw Selected Scrubber Line & Active Point Glow (Recharts active dot style)
                        val sel = selectedIndex
                        if (sel != null && sel in points.indices) {
                            val activePt = points[sel]

                            // Vertical dashed scrubber cursor
                            drawLine(
                                color = TextSecondary.copy(alpha = 0.6f),
                                start = Offset(activePt.x, topPadding),
                                end = Offset(activePt.x, bottomY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashedEffect
                            )

                            // Outer translucent glow ring
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.25f),
                                radius = 9.dp.toPx(),
                                center = activePt
                            )

                            // Inner active point
                            drawCircle(
                                color = primaryColor,
                                radius = 4.5.dp.toPx(),
                                center = activePt
                            )

                            // White center dot
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = activePt
                            )
                        } else {
                            // Draw latest point dot
                            val lastPt = points.last()
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = lastPt
                            )
                        }
                    }
                }

                // Y-Axis Labels overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp, top = 10.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "80%", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "70%", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "60%", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "50%", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(text = "40%", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }

                // X-Axis Game Ticks overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 44.dp, end = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val count = timeline.size
                    Text(text = "G1", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    if (count >= 20) {
                        Text(text = "G${count / 4}", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                    if (count >= 10) {
                        Text(text = "G${count / 2}", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                    if (count >= 30) {
                        Text(text = "G${(count * 3) / 4}", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(text = "G$count", color = primaryColor, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Footnote Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "50% = Fair Coin Baseline",
                    color = TextMuted,
                    fontSize = 9.sp
                )
                Text(
                    text = String.format(Locale.US, "Current Win Rate: %.1f%%", latestAccuracy),
                    color = primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
