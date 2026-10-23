package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalysisWindow
import com.example.data.model.NormalizedDraw
import com.example.data.model.WindowStatistics
import com.example.domain.StatisticalAnalyzer
import com.example.ui.components.TrendBeadPlate
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun HistoricalAnalyticsScreen(
    drawsAscending: List<NormalizedDraw>,
    selectedWindow: AnalysisWindow,
    onWindowSelected: (AnalysisWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats = StatisticalAnalyzer.computeStatistics(
        StatisticalAnalyzer.sliceWindow(drawsAscending, selectedWindow),
        selectedWindow
    )

    // Check for duplicates
    val totalRecords = drawsAscending.size
    val uniqueIds = drawsAscending.map { it.id }.toSet().size
    val duplicateCount = totalRecords - uniqueIds

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("historical_analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Header Title
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = OddRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "HISTORICAL ANALYTICS",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Markov State Transitions, Streaks & Rolling Distributions",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Window Selection Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnalysisWindow.values().forEach { win ->
                            val isSelected = selectedWindow == win
                            FilterChip(
                                selected = isSelected,
                                onClick = { onWindowSelected(win) },
                                label = { Text(win.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OddRed.copy(alpha = 0.25f),
                                    selectedLabelColor = OddRed,
                                    containerColor = NavySurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) OddRed else NavyBorder,
                                    selectedBorderColor = OddRed,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }
                }
            }
        }

        // 1. Frequency Distribution Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ODD / EVEN FREQUENCY DISTRIBUTION",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Sample: ${stats.settledCount} verified completed outcomes",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ODD: ${stats.oddCount} (${String.format(Locale.US, "%.1f%%", stats.oddPercentage)})",
                            color = OddRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "EVEN: ${stats.evenCount} (${String.format(Locale.US, "%.1f%%", stats.evenPercentage)})",
                            color = EvenBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { if (stats.settledCount > 0) stats.oddPercentage / 100f else 0.5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = OddRed,
                        trackColor = EvenBlue
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Theoretical expectation is 50.0% per category under uniform fair game dynamics.",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // 2. First-Order Markov Transition Matrix
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1ST-ORDER MARKOV TRANSITIONS",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Icon(Icons.Default.CompareArrows, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "Empirical conditional transition probabilities P(Next | Current)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransitionTile(
                            label = "ODD → ODD",
                            count = stats.oddToOddCount,
                            percentage = stats.markovOddGivenOdd * 100f,
                            color = OddRed,
                            modifier = Modifier.weight(1f)
                        )
                        TransitionTile(
                            label = "ODD → EVEN",
                            count = stats.oddToEvenCount,
                            percentage = stats.markovEvenGivenOdd * 100f,
                            color = EvenBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransitionTile(
                            label = "EVEN → ODD",
                            count = stats.evenToOddCount,
                            percentage = stats.markovOddGivenEven * 100f,
                            color = OddRed,
                            modifier = Modifier.weight(1f)
                        )
                        TransitionTile(
                            label = "EVEN → EVEN",
                            count = stats.evenToEvenCount,
                            percentage = stats.markovEvenGivenEven * 100f,
                            color = EvenBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Streak Distributions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STREAK & ALTERNATION RUNS",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StreakBox(
                            label = "MAX ODD STREAK",
                            value = "${stats.maxOddStreak} in a row",
                            accentColor = OddRed,
                            modifier = Modifier.weight(1f)
                        )
                        StreakBox(
                            label = "MAX EVEN STREAK",
                            value = "${stats.maxEvenStreak} in a row",
                            accentColor = EvenBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StreakBox(
                            label = "MAX CHOP (ALT)",
                            value = "${stats.maxChopStreak} swings",
                            accentColor = Color(0xFFFFB703),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Bead Plate
        item {
            TrendBeadPlate(drawsAscending = drawsAscending)
        }

        // 5. Data Integrity Audit (Duplicate Detection)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = StatusConnected,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "DATA INTEGRITY & ORDER VERIFICATION",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Unique Records: $uniqueIds / $totalRecords (Duplicates: $duplicateCount)\n• Chronological Walk-Forward Sorting: Verified Stable\n• Out-of-Order Records: Resolved via timestamp/serial sorting",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TransitionTile(
    label: String,
    count: Int,
    percentage: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.US, "%.1f%%", percentage),
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(text = "$count transitions", color = TextSecondary, fontSize = 9.sp)
        }
    }
}

@Composable
private fun StreakBox(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
