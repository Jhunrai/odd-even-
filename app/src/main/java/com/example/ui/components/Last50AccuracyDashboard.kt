package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameEvaluationRecord
import com.example.data.model.Last50AccuracyReport
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * Recharts-based Visual Dashboard for Historical Accuracy over the Last 50 Games.
 * Displays:
 * 1. High-level KPI Scorecards (Win Rate, Total Wins, Total Losses, Streaks, Parity breakdown).
 * 2. Recharts-inspired Interactive Spline Accuracy Chart with 50% baseline reference line.
 * 3. Top Drawn Hit Numbers in the 50 games.
 * 4. Filterable List of each game detailing:
 *    - Win (Panalo) / Loss (Talo)
 *    - Hit Number ("hit num" rendered with 3D lottery ball)
 *    - Prediction made vs actual parity
 *    - Round / Serial & timestamp
 */
@Composable
fun Last50AccuracyDashboard(
    report: Last50AccuracyReport,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "WIN", "LOSS"

    val filteredGames = remember(report.games, selectedFilter) {
        when (selectedFilter) {
            "WIN" -> report.games.filter { it.isWin }
            "LOSS" -> report.games.filter { !it.isWin }
            else -> report.games
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("last_50_accuracy_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. KPI Scorecards Overview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("last_50_kpi_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (report.winRate >= 50f) StatusConnected.copy(alpha = 0.2f) else StatusOffline.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (report.winRate >= 50f) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (report.winRate >= 50f) StatusConnected else StatusOffline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "50-GAME HISTORICAL ACCURACY",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Out-of-sample walk-forward predictions (${report.totalEvaluated} games)",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Win rate pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (report.winRate >= 50f) StatusConnected.copy(alpha = 0.15f) else StatusOffline.copy(alpha = 0.15f))
                            .border(1.dp, if (report.winRate >= 50f) StatusConnected else StatusOffline, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f%%", report.winRate),
                            color = if (report.winRate >= 50f) StatusConnected else StatusOffline,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Scoreboard: Win Rate, Wins, Losses, Streaks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Win Rate Metric Box
                    MetricScorecard(
                        label = "PANALO (WINS)",
                        mainValue = "${report.totalWins}",
                        subValue = String.format(Locale.US, "%.1f%% ng laro", report.winRate),
                        accentColor = StatusConnected,
                        modifier = Modifier.weight(1f)
                    )

                    // Losses Metric Box
                    val lossPct = if (report.totalEvaluated > 0) (report.totalLosses.toFloat() / report.totalEvaluated) * 100f else 0f
                    MetricScorecard(
                        label = "TALO (LOSSES)",
                        mainValue = "${report.totalLosses}",
                        subValue = String.format(Locale.US, "%.1f%% ng laro", lossPct),
                        accentColor = StatusOffline,
                        modifier = Modifier.weight(1f)
                    )

                    // Edge vs 50% Baseline
                    MetricScorecard(
                        label = "EDGE VS 50%",
                        mainValue = String.format(Locale.US, "%+.1f%%", report.edge),
                        subValue = if (report.edge >= 0) "Positibong Edge" else "Walang Edge",
                        accentColor = if (report.edge >= 0) StatusConnected else StatusOffline,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar: Win vs Loss distribution
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${report.totalWins} Panalo",
                            color = StatusConnected,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${report.totalLosses} Talo",
                            color = StatusOffline,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val winFraction = if (report.totalEvaluated > 0) (report.totalWins.toFloat() / report.totalEvaluated) else 0.5f
                    LinearProgressIndicator(
                        progress = { winFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StatusConnected,
                        trackColor = StatusOffline.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Streak & Parity Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavySurfaceVariant.copy(alpha = 0.6f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current Streak
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "CURRENT STREAK", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${report.currentStreak}x ${if (report.isCurrentStreakWin) "WIN" else "LOSS"}",
                            color = if (report.isCurrentStreakWin) StatusConnected else StatusOffline,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(NavyBorder))

                    // Max Streaks
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "MAX WIN STREAK", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${report.maxWinStreak}x Sunod",
                            color = StatusConnected,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(NavyBorder))

                    // Odd vs Even Breakdown
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "ODD / EVEN WINS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${report.oddWins} Odd • ${report.evenWins} Even",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Interactive Recharts Visual Accuracy Chart
        RechartsAccuracyChart(
            timeline = report.accuracyTimeline,
            games = report.games
        )

        // 3. Top Hit Numbers in the 50 Games
        if (report.topHitNumbers.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("top_hit_numbers_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "TOP HIT NUMBERS (LAST 50 GAMES)",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        report.topHitNumbers.forEach { (number, count) ->
                            val isOdd = (number % 2 != 0)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavySurfaceVariant)
                                    .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LotteryBall(
                                    number = number,
                                    isOdd = isOdd,
                                    size = 28.dp,
                                    fontSize = 11.sp
                                )
                                Column {
                                    Text(
                                        text = if (isOdd) "ODD" else "EVEN",
                                        color = if (isOdd) OddRed else EvenBlue,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${count}x lumabas",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Game-by-Game Breakdown List: Win, Loss, and Hit Number
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("game_by_game_list_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header with Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LISTA NG MGA LARO (LAST 50)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Pinapakita ang Win, Loss, at Hit Number",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Total shown count
                    Text(
                        text = "${filteredGames.size} ng ${report.totalEvaluated}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Lahat (${report.totalEvaluated})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavySurfaceVariant,
                            selectedLabelColor = TextPrimary,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedFilter == "ALL") TextPrimary else NavyBorder,
                            selectedBorderColor = TextPrimary,
                            enabled = true,
                            selected = selectedFilter == "ALL"
                        ),
                        modifier = Modifier.testTag("filter_all_games")
                    )

                    FilterChip(
                        selected = selectedFilter == "WIN",
                        onClick = { selectedFilter = "WIN" },
                        label = { Text("Panalo (${report.totalWins})", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StatusConnected,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusConnected.copy(alpha = 0.2f),
                            selectedLabelColor = StatusConnected,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedFilter == "WIN") StatusConnected else NavyBorder,
                            selectedBorderColor = StatusConnected,
                            enabled = true,
                            selected = selectedFilter == "WIN"
                        ),
                        modifier = Modifier.testTag("filter_win_games")
                    )

                    FilterChip(
                        selected = selectedFilter == "LOSS",
                        onClick = { selectedFilter = "LOSS" },
                        label = { Text("Talo (${report.totalLosses})", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = StatusOffline,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusOffline.copy(alpha = 0.2f),
                            selectedLabelColor = StatusOffline,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedFilter == "LOSS") StatusOffline else NavyBorder,
                            selectedBorderColor = StatusOffline,
                            enabled = true,
                            selected = selectedFilter == "LOSS"
                        ),
                        modifier = Modifier.testTag("filter_loss_games")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Game Items List
                if (filteredGames.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Walang nahanap na record para sa filter na ito.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredGames.forEach { record ->
                            GameEvaluationRowItem(record = record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricScorecard(
    label: String,
    mainValue: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NavySurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = mainValue,
                color = accentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                color = TextSecondary,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GameEvaluationRowItem(
    record: GameEvaluationRecord,
    modifier: Modifier = Modifier
) {
    val isOdd = (record.hitNumber % 2 != 0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (record.isWin) StatusConnected.copy(alpha = 0.05f) else StatusOffline.copy(alpha = 0.05f))
            .border(
                1.dp,
                if (record.isWin) StatusConnected.copy(alpha = 0.25f) else StatusOffline.copy(alpha = 0.25f),
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
            .testTag("game_item_${record.gameNumber}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Section: Game Number + Lottery Ball (Hit Num) + Actual Parity
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Game Number indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#${record.gameNumber}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Lottery Ball for Hit Number
            LotteryBall(
                number = record.hitNumber,
                isOdd = isOdd,
                size = 36.dp,
                fontSize = 13.sp
            )

            // Hit Number and Round details
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Hit Num: ${record.hitNumber}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${if (isOdd) "ODD" else "EVEN"})",
                        color = if (isOdd) OddRed else EvenBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "Taya: ${record.predictedParity} (${record.confidence}%)",
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                if (record.openTime.isNotEmpty() || record.timestamp.isNotEmpty()) {
                    Text(
                        text = "Oras: ${record.openTime.ifEmpty { record.timestamp }} • Round ${record.serial}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Right Section: Win/Loss Status Badge & Cumulative Acc
        Column(horizontalAlignment = Alignment.End) {
            // Status Badge: PANALO (WIN) vs TALO (LOSS)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (record.isWin) StatusConnected.copy(alpha = 0.2f) else StatusOffline.copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        if (record.isWin) StatusConnected.copy(alpha = 0.6f) else StatusOffline.copy(alpha = 0.6f),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (record.isWin) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (record.isWin) StatusConnected else StatusOffline,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (record.isWin) "PANALO" else "TALO",
                        color = if (record.isWin) StatusConnected else StatusOffline,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Cumulative Accuracy
            Text(
                text = "Acc: ${record.cumulativeAccuracy}%",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
