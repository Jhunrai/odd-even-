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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.DrawRowItem
import com.example.ui.components.HeaderStatusCard
import com.example.ui.components.PredictionCard
import com.example.ui.components.RoundedBallGrid
import com.example.ui.components.SimpleBettingCard
import com.example.ui.components.StatisticsOverview
import com.example.ui.components.TrendBeadPlate
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Refresh
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnalyzerUiState
import java.util.Locale

@Composable
fun DashboardScreen(
    uiState: AnalyzerUiState,
    drawsDesc: List<NormalizedDraw>,
    onRefreshClick: () -> Unit,
    onWindowSelected: (AnalysisWindow) -> Unit,
    onNavigateToHistory: () -> Unit,
    onToggleInvertSignal: () -> Unit = {},
    onSelectStrategy: (String) -> Unit = {},
    onSelectBallCount: (Int) -> Unit = {},
    onTogglePaSureMode: () -> Unit = {},
    onRevealSignalEarly: () -> Unit = {},
    simpleMode: Boolean = true,
    onToggleSimpleMode: () -> Unit = {},
    colorMappingVerified: Boolean = false,
    modifier: Modifier = Modifier
) {
    val drawsAsc = drawsDesc.reversed()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // TOP MODE SWITCHER: SIMPLE (DIRETSO TAYA) vs ADVANCED (DATA & STATS)
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
                        .background(if (simpleMode) OddRed else Color.Transparent)
                        .clickable { if (!simpleMode) onToggleSimpleMode() }
                        .padding(vertical = 8.dp)
                        .testTag("simple_mode_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎯 SIMPLE (Diretso Taya)",
                        color = if (simpleMode) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!simpleMode) NavyCard else Color.Transparent)
                        .clickable { if (simpleMode) onToggleSimpleMode() }
                        .padding(vertical = 8.dp)
                        .testTag("advanced_mode_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔬 ADVANCED (Data & Stats)",
                        color = if (!simpleMode) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (simpleMode) {
            // ==========================================
            // ZERO-CONFUSION SIMPLE BETTING INTERFACE
            // ==========================================
            item {
                SimpleBettingCard(
                    forecast = uiState.currentForecast,
                    signal = uiState.currentSignal,
                    upcomingCountdownSeconds = uiState.upcomingCountdownSeconds,
                    recentDraws = drawsDesc,
                    onTogglePaSure = onTogglePaSureMode,
                    forceRevealEarly = uiState.forceRevealEarly,
                    onRevealEarly = onRevealSignalEarly
                )
            }

            // Quick Refresh Button
            item {
                OutlinedButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isRefreshing) "Kinukuha ang bagong bola..." else "I-refresh ang Resulta",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            // ==========================================
            // ADVANCED MODE (FULL MATHEMATICAL SUITE)
            // ==========================================
            // 1. Top Header Status Card with Server Time, Upcoming Draw Countdown & Sync
            item {
                HeaderStatusCard(
                    connectionStatus = uiState.connectionStatus,
                    statusMessage = uiState.statusMessage,
                    lastUpdateTime = uiState.lastUpdateTime,
                    serverTime = uiState.serverTime,
                    cachedCount = uiState.totalCachedCount,
                    settledCount = uiState.settledCount,
                    pendingCount = uiState.pendingCount,
                    upcomingDrawTime = uiState.upcomingDrawTime,
                    upcomingCountdownSeconds = uiState.upcomingCountdownSeconds,
                    refreshCountdown = uiState.refreshCountdown,
                    isRefreshing = uiState.isRefreshing,
                    onRefreshClick = onRefreshClick
                )
            }

        // 2. Settlement Verification Notice
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Official game rules dictate outcomes. User guesses (_game_guess) are tracked separately from verified results.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // 3. Summary Cards (Section 4B): Verified Completed Results, Odd%, Even%, Streak, API Health
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_metrics_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "METRICS OVERVIEW",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Total Loaded: ${uiState.totalCachedCount} (Limit 720)",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryPill(
                            label = "ODD HITS",
                            value = "${uiState.statistics.oddCount}",
                            sub = String.format(Locale.US, "%.1f%%", uiState.statistics.oddPercentage),
                            accentColor = OddRed,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryPill(
                            label = "EVEN HITS",
                            value = "${uiState.statistics.evenCount}",
                            sub = String.format(Locale.US, "%.1f%%", uiState.statistics.evenPercentage),
                            accentColor = EvenBlue,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryPill(
                            label = "CURRENT STREAK",
                            value = "${uiState.statistics.currentStreakCount}x",
                            sub = uiState.statistics.currentStreakType,
                            accentColor = if (uiState.statistics.currentStreakType == "ODD") OddRed else EvenBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Prediction Card with exact Section 5 format (Probabilities, Brier Score, Model Used)
        item {
            PredictionCard(
                forecast = uiState.currentForecast,
                signal = uiState.currentSignal,
                upcomingCountdownSeconds = uiState.upcomingCountdownSeconds,
                forceRevealEarly = uiState.forceRevealEarly,
                onRevealEarly = onRevealSignalEarly,
                onToggleInvert = onToggleInvertSignal,
                onSelectStrategy = onSelectStrategy,
                onTogglePaSureMode = onTogglePaSureMode
            )
        }

        // 5. Rounded Ball Display (Section 4C) with 10, 20, 50, 100, 720 count filters & interactive modal
        item {
            RoundedBallGrid(
                drawsAscending = drawsAsc,
                selectedCount = uiState.ballDisplayCount,
                onSelectCount = onSelectBallCount,
                colorMappingVerified = colorMappingVerified
            )
        }

        // 6. Historical Statistics & Windows Overview
        item {
            StatisticsOverview(
                statistics = uiState.statistics,
                selectedWindow = uiState.selectedWindow,
                onWindowSelected = onWindowSelected
            )
        }

        // 7. Bead Plate Trend Grid
        item {
            TrendBeadPlate(
                drawsAscending = drawsAsc
            )
        }

        // 8. Latest Verified Draws Preview
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = OddRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LATEST VERIFIED DRAWS",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToHistory,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OddRed),
                    modifier = Modifier.testTag("view_all_history_button")
                ) {
                    Text("View Table", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.size(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        val previewDraws = drawsDesc.take(5)
        if (previewDraws.isEmpty()) {
            item {
                Text(
                    text = "No draws loaded yet. Checking connection...",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(previewDraws, key = { it.id }) { draw ->
                DrawRowItem(draw = draw)
            }
        }
    }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryPill(
    label: String,
    value: String,
    sub: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, NavyBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(text = label, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(
                text = value,
                color = accentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = sub,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
