package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccuracySummary
import com.example.data.model.SignalResult
import com.example.ui.components.LotteryBall
import com.example.ui.theme.AccentGold
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SignalsTrackerScreen(
    accuracySummary: AccuracySummary,
    signals: List<SignalResult>,
    modifier: Modifier = Modifier
) {
    var filterMode by remember { mutableStateOf("ALL") } // "ALL", "HITS", "MISSES"

    val displayedSignals = remember(signals, filterMode) {
        when (filterMode) {
            "HITS" -> signals.filter { it.isCorrect == true }
            "MISSES" -> signals.filter { it.isCorrect == false }
            else -> signals
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("signals_tracker_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Title Row
        item {
            Column {
                Text(
                    text = "SIGNAL & BULLS TRACKER",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Empirical Verification: Red (ODD) / Blue (EVEN) Parity, Zone & Bulls",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Accuracy Metrics Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("accuracy_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = null,
                                tint = OddRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "HISTORICAL HIT RATE",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "${accuracySummary.resolvedCount} Resolved",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Hit Rate Text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f%%", accuracySummary.accuracyRate),
                            color = if (accuracySummary.accuracyRate >= 50f) StatusConnected else EvenBlue,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${accuracySummary.hitCount} Hits / ${accuracySummary.missCount} Misses",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (accuracySummary.currentStreak > 0) {
                                Text(
                                    text = "Streak: ${accuracySummary.currentStreak} ${if (accuracySummary.isCurrentStreakWin) "Wins 🔥" else "Losses ⚠️"}",
                                    color = if (accuracySummary.isCurrentStreakWin) StatusConnected else OddRed,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { if (accuracySummary.resolvedCount > 0) (accuracySummary.accuracyRate / 100f) else 0.5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StatusConnected,
                        trackColor = NavySurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Dimensional Metrics Strip (Zone & Bulls Hit)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ZONE HITS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${accuracySummary.zoneHitCount} Hits", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(NavyBorder))
                        Column {
                            Text("TAIL HITS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${accuracySummary.tailHitCount} Hits", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(NavyBorder))
                        Column {
                            Text("BULLSEYE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${accuracySummary.numberHitCount} Hits", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Statistical Disclaimer Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavySurfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Statistical signals and candidate bulls represent mathematical sequence modeling only. Independent random draw events carry inherent variance without guaranteed outcomes.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Filter Bar & Signals Count Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRACKED SIGNALS (${displayedSignals.size})",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL", "HITS", "MISSES").forEach { mode ->
                            val isSelected = (filterMode == mode)
                            FilterChip(
                                selected = isSelected,
                                onClick = { filterMode = mode },
                                label = { Text(mode, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (mode == "HITS") StatusConnected.copy(alpha = 0.25f) else (if (mode == "MISSES") OddRed.copy(alpha = 0.25f) else NavySurfaceVariant),
                                    selectedLabelColor = if (mode == "HITS") StatusConnected else (if (mode == "MISSES") OddRed else TextPrimary),
                                    containerColor = NavyCard,
                                    labelColor = TextMuted
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) StatusConnected else NavyBorder,
                                    selectedBorderColor = if (mode == "MISSES") OddRed else StatusConnected,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }
            }
        }

        if (displayedSignals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (signals.isEmpty()) "No signals generated yet. Connect to API to begin tracking." else "No signals matching filter '$filterMode'.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(displayedSignals, key = { it.id }) { item ->
                SignalHistoryCard(item)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SignalHistoryCard(
    signal: SignalResult,
    modifier: Modifier = Modifier
) {
    val isOdd = signal.signalType == "ODD"
    val signalColor = if (isOdd) OddRed else EvenBlue

    val (statusLabel, statusColor, statusIcon) = when {
        signal.actualResult == null -> Triple("PENDING", TextSecondary, Icons.Default.HourglassTop)
        signal.isCorrect == true -> Triple("HIT", StatusConnected, Icons.Default.CheckCircle)
        else -> Triple("MISS", StatusOffline, Icons.Default.RemoveCircle)
    }

    val timeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(signal.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_card_${signal.forSerial}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Serial & Generated time
                Column {
                    Text(
                        text = "Draw #${signal.forSerial}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Bullseye Hit badge if matched number
                    if (signal.isNumberHit == true) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGold.copy(alpha = 0.2f))
                                .border(1.dp, AccentGold, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CrisisAlert,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "BULL HIT!",
                                    color = AccentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Parity Status Badge (HIT / MISS / PENDING)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = statusLabel,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Predicted Balls / Bulls row
            if (signal.predictedNumbers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Predicted Bulls:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        signal.predictedNumbers.forEach { bullNum ->
                            val isDrawnHit = (signal.actualNumber == bullNum)
                            LotteryBall(
                                number = bullNum,
                                isOdd = (bullNum % 2 != 0),
                                size = 28.dp,
                                fontSize = 10.sp,
                                isHighlighted = isDrawnHit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Signal vs Actual Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Signal:", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = signal.signalType,
                        color = signalColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "(${signal.confidence}%)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Actual:", color = TextSecondary, fontSize = 12.sp)
                    val actualColor = when (signal.actualResult) {
                        "ODD" -> OddRed
                        "EVEN" -> EvenBlue
                        else -> TextMuted
                    }
                    Text(
                        text = if (signal.actualResult != null && signal.actualNumber != null) {
                            "${signal.actualResult} (#${signal.actualNumber})"
                        } else {
                            signal.actualResult ?: "Awaiting..."
                        },
                        color = actualColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (signal.rationale.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = signal.rationale,
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
