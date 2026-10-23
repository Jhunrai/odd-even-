package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NormalizedDraw
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun NumberSequenceTracker(
    drawsAscending: List<NormalizedDraw>,
    modifier: Modifier = Modifier
) {
    val validDraws = drawsAscending.filter { it.number in 1..80 }
    val recentDraws = validDraws.takeLast(10)

    if (recentDraws.size < 2) return

    val numbers = recentDraws.map { it.number }
    val deltas = mutableListOf<Int>()
    for (i in 1 until numbers.size) {
        deltas.add(numbers[i] - numbers[i - 1])
    }
    val avgDelta = if (deltas.isNotEmpty()) deltas.takeLast(5).average().toFloat() else 0f

    // Sector counts for last up to 30 draws
    val sectorSample = validDraws.takeLast(30)
    var s1 = 0; var s2 = 0; var s3 = 0; var s4 = 0
    sectorSample.forEach { d ->
        when (d.number) {
            in 1..20 -> s1++
            in 21..40 -> s2++
            in 41..60 -> s3++
            in 61..80 -> s4++
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("number_sequence_tracker_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Velocity Direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = null,
                        tint = OddRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "NUMBERING SEQUENCE FLOW",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Trajectory Pill
                val (trendIcon, trendText, trendColor) = when {
                    avgDelta > 5f -> Triple(Icons.Default.TrendingUp, "Climbing (+${avgDelta.roundToInt()})", OddRed)
                    avgDelta < -5f -> Triple(Icons.Default.TrendingDown, "Falling (${avgDelta.roundToInt()})", EvenBlue)
                    else -> Triple(Icons.Default.TrendingFlat, "Stable (±${abs(avgDelta).roundToInt()})", TextSecondary)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(trendColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = trendText,
                        color = trendColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Number Sequence Chain
            Text(
                text = "Recent Draw Progression (Earliest → Latest)",
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recentDraws.forEachIndexed { index, draw ->
                    val isOdd = (draw.number % 2 != 0)
                    val isLatest = (index == recentDraws.lastIndex)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LotteryBall(
                            number = draw.number,
                            isOdd = isOdd,
                            size = if (isLatest) 40.dp else 34.dp,
                            fontSize = if (isLatest) 14.sp else 12.sp,
                            isHighlighted = isLatest
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isLatest) "NOW" else "#${draw.serial % 100}",
                            color = if (isLatest) (if (isOdd) OddRed else EvenBlue) else TextMuted,
                            fontSize = 8.sp,
                            fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (index < recentDraws.lastIndex) {
                        val delta = recentDraws[index + 1].number - draw.number
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = if (delta >= 0) "+$delta" else "$delta",
                                color = if (delta >= 0) OddRed.copy(alpha = 0.8f) else EvenBlue.copy(alpha = 0.8f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sector Distribution (1-20, 21-40, 41-60, 61-80)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sector Clustering (Last ${sectorSample.size} draws)",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sectors = listOf(
                    "01-20" to s1,
                    "21-40" to s2,
                    "41-60" to s3,
                    "61-80" to s4
                )
                val maxSectorVal = sectors.maxOfOrNull { it.second } ?: 1

                sectors.forEach { (label, count) ->
                    val isDominant = (count == maxSectorVal && count > 0)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavySurfaceVariant)
                            .border(
                                1.dp,
                                if (isDominant) OddRed.copy(alpha = 0.7f) else NavyBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                color = if (isDominant) OddRed else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isDominant) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "$count hits",
                                color = if (isDominant) TextPrimary else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
