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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
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
import com.example.data.model.AnalysisWindow
import com.example.data.model.WindowStatistics
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun StatisticsOverview(
    statistics: WindowStatistics,
    selectedWindow: AnalysisWindow,
    onWindowSelected: (AnalysisWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("statistics_overview_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Title & Window Filter Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = OddRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "HISTORICAL DISTRIBUTION",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "${statistics.totalCount} draws analyzed",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multiple Analysis Windows Selector (Horizontal Chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalysisWindow.values().forEach { window ->
                    val isSelected = window == selectedWindow
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) OddRed else NavySurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) OddRed else NavyBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onWindowSelected(window) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("window_chip_${window.name.lowercase()}")
                    ) {
                        Text(
                            text = window.label,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Proportional Distribution Bar (ODD RED vs BLUE EVEN)
            val oddWeight = if (statistics.totalCount > 0) statistics.oddPercentage.coerceAtLeast(1f) else 50f
            val evenWeight = if (statistics.totalCount > 0) statistics.evenPercentage.coerceAtLeast(1f) else 50f

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ODD (RED) ${String.format(Locale.US, "%.1f", statistics.oddPercentage)}%",
                        color = OddRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "EVEN (BLUE) ${String.format(Locale.US, "%.1f", statistics.evenPercentage)}%",
                        color = EvenBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(oddWeight)
                            .height(10.dp)
                            .background(OddRed)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .weight(evenWeight)
                            .height(10.dp)
                            .background(EvenBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Grid Layout:
            // TOTAL 50, ODD (RED) 27, EVEN (BLUE) 23 | ODD 54%, EVEN 46%
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavySurfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // TOTAL
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOTAL",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${statistics.totalCount}",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // ODD (RED)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ODD (RED)",
                        color = OddRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${statistics.oddCount}",
                        color = OddRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f", statistics.oddPercentage)}%",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // EVEN (BLUE)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EVEN (BLUE)",
                        color = EvenBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${statistics.evenCount}",
                        color = EvenBlue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f", statistics.evenPercentage)}%",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Streaks Card Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current Streak Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavySurfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "CURRENT STREAK",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val streakColor = if (statistics.currentStreakType == "ODD") OddRed else EvenBlue
                        Text(
                            text = "${statistics.currentStreakCount}x ${statistics.currentStreakType}",
                            color = streakColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Max ODD Streak (RED)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavySurfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "MAX ODD (RED)",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${statistics.maxOddStreak} draws",
                            color = OddRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Max EVEN Streak (BLUE)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavySurfaceVariant.copy(alpha = 0.35f))
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "MAX EVEN (BLUE)",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${statistics.maxEvenStreak} draws",
                            color = EvenBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
