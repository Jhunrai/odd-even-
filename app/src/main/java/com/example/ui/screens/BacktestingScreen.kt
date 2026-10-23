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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BacktestResult
import com.example.data.model.ConfusionMatrix
import com.example.data.model.ForecastModelType
import com.example.data.model.Last50AccuracyReport
import com.example.ui.components.Last50AccuracyDashboard
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

@Composable
fun BacktestingScreen(
    backtestResult: BacktestResult?,
    last50Report: Last50AccuracyReport? = null,
    isRunning: Boolean,
    onRunBacktest: (ForecastModelType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = 50-Game Dashboard, 1 = Custom Matrix Tuner
    var selectedModel by remember { mutableStateOf(ForecastModelType.COMBINED_SMOOTHED) }
    var selectedWindow by remember { mutableIntStateOf(30) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("backtesting_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // TAB SWITCHER: 50-GAME DASHBOARD vs WALK-FORWARD TUNER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavySurfaceVariant)
                    .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 0) StatusConnected.copy(alpha = 0.25f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (activeTab == 0) StatusConnected else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { activeTab = 0 }
                        .padding(vertical = 8.dp)
                        .testTag("tab_last_50_dashboard"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📊 50-Game Accuracy",
                        color = if (activeTab == 0) StatusConnected else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (activeTab == 1) OddRed.copy(alpha = 0.25f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (activeTab == 1) OddRed else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { activeTab = 1 }
                        .padding(vertical = 8.dp)
                        .testTag("tab_matrix_tuner"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙️ Matrix Tuner",
                        color = if (activeTab == 1) OddRed else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeTab == 0) {
            // 50-GAME RECHARTS VISUAL DASHBOARD & GAME-BY-GAME BREAKDOWN
            item {
                Last50AccuracyDashboard(
                    report = last50Report ?: Last50AccuracyReport()
                )
            }
        } else {
            // Header Card: Backtest Controller
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backtest_controls_card"),
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
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = OddRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "WALK-FORWARD BACKTESTING",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Chronological Out-of-Sample Statistical Validation",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Model Selection
                    Text(
                        text = "1. SELECT FORECAST MODEL",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ForecastModelType.values().forEach { model ->
                            val isSelected = selectedModel == model
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedModel = model },
                                label = { Text(model.shortName, fontSize = 11.sp) },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Training Window Selection
                    Text(
                        text = "2. TRAINING WINDOW (IN-SAMPLE DRAWS)",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10, 20, 30, 50, 100).forEach { windowSize ->
                            val isSelected = selectedWindow == windowSize
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedWindow = windowSize },
                                label = { Text("$windowSize draws", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EvenBlue.copy(alpha = 0.25f),
                                    selectedLabelColor = EvenBlue,
                                    containerColor = NavySurfaceVariant,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) EvenBlue else NavyBorder,
                                    selectedBorderColor = EvenBlue,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Run Action Button
                    Button(
                        onClick = { onRunBacktest(selectedModel, selectedWindow) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("run_backtest_button"),
                        enabled = !isRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = OddRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulating Walk-Forward...", fontSize = 13.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Run Walk-Forward Validation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Backtest Results Section
        if (backtestResult != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backtest_results_card"),
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
                                text = "OUT-OF-SAMPLE PERFORMANCE",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${backtestResult.evaluatedCount} rounds evaluated",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big Accuracy Comparison
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavySurfaceVariant.copy(alpha = 0.6f))
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "MODEL ACCURACY", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", backtestResult.actualAccuracy),
                                    color = if (backtestResult.actualAccuracy >= 50f) StatusConnected else StatusOffline,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${backtestResult.correctCount} / ${backtestResult.evaluatedCount} hits",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(44.dp)
                                    .background(NavyBorder)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "BASELINE (50/50)", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "50.0%",
                                    color = TextSecondary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                                val diff = backtestResult.actualAccuracy - 50.0f
                                Text(
                                    text = String.format(Locale.US, "%+.1f%% edge", diff),
                                    color = if (diff >= 0) StatusConnected else StatusOffline,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Scoring Metrics: Brier Score & Log Loss
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricBox(
                                label = "BRIER SCORE",
                                value = String.format(Locale.US, "%.3f", backtestResult.brierScore),
                                subtitle = "Lower is better (Random = 0.250)",
                                isPositive = backtestResult.brierScore <= 0.250f,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                label = "LOG LOSS",
                                value = String.format(Locale.US, "%.3f", backtestResult.logLoss),
                                subtitle = "Cross-entropy (Random = 0.693)",
                                isPositive = backtestResult.logLoss <= 0.693f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confusion Matrix 2x2
                        Text(
                            text = "CONFUSION MATRIX (2x2)",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ConfusionMatrixGrid(matrix = backtestResult.confusionMatrix)

                        // Disclaimer Box
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavySurfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, NavyBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = backtestResult.disclaimer,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ready to Run Walk-Forward Validation",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select model & training window above and tap Run to evaluate out-of-sample edge without future data leakage.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    subtitle: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(
                text = value,
                color = if (isPositive) StatusConnected else StatusOffline,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(text = subtitle, color = TextSecondary, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ConfusionMatrixGrid(matrix: ConfusionMatrix) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Actual \\ Predicted", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
            Text(text = "Pred ODD", color = OddRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(text = "Pred EVEN", color = EvenBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }

        // Row 1: True ODD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "True ODD", color = OddRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(OddRed.copy(alpha = 0.2f))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${matrix.trueOddPredOdd} (Hit)",
                    color = OddRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyCard)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${matrix.trueOddPredEven} (Miss)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Row 2: True EVEN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "True EVEN", color = EvenBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NavyCard)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${matrix.trueEvenPredOdd} (Miss)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(EvenBlue.copy(alpha = 0.2f))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${matrix.trueEvenPredEven} (Hit)",
                    color = EvenBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Summary Recall Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format(Locale.US, "ODD Recall: %.1f%%", matrix.oddRecall),
                color = OddRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = String.format(Locale.US, "EVEN Recall: %.1f%%", matrix.evenRecall),
                color = EvenBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
