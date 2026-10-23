package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ForecastModelType
import com.example.ui.screens.BacktestingScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DrawHistoryScreen
import com.example.ui.screens.FieldInspectorScreen
import com.example.ui.screens.ForecastComparisonScreen
import com.example.ui.screens.HistoricalAnalyticsScreen
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.AnalyzerViewModel

sealed class AppDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Dashboard : AppDestination("dashboard", "Tayaan", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard")
    object Analytics : AppDestination("analytics", "Trends", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_analytics")
    object Forecast : AppDestination("forecast", "Signals", Icons.Filled.Functions, Icons.Outlined.Functions, "nav_forecast")
    object Backtest : AppDestination("backtest", "Backtest", Icons.Filled.Timeline, Icons.Outlined.Timeline, "nav_backtest")
    object Inspector : AppDestination("inspector", "Settings", Icons.Filled.Code, Icons.Outlined.Code, "nav_inspector")
}

@Composable
fun MainScreen(
    viewModel: AnalyzerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawsDesc by viewModel.drawsDesc.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val destinations = listOf(
        AppDestination.Dashboard,
        AppDestination.Analytics,
        AppDestination.Forecast,
        AppDestination.Backtest,
        AppDestination.Inspector
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark),
        containerColor = NavyDark,
        bottomBar = {
            NavigationBar(
                containerColor = NavyCard,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                destinations.forEachIndexed { index, destination ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OddRed,
                            selectedTextColor = OddRed,
                            indicatorColor = NavySurfaceVariant,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> DashboardScreen(
                    uiState = uiState,
                    drawsDesc = drawsDesc,
                    onRefreshClick = { viewModel.refreshData() },
                    onWindowSelected = { viewModel.selectWindow(it) },
                    onNavigateToHistory = { selectedTabIndex = 4 },
                    onToggleInvertSignal = { viewModel.toggleInvertSignal() },
                    onSelectStrategy = { viewModel.setPatternStrategy(it) },
                    onSelectBallCount = { viewModel.selectBallDisplayCount(it) },
                    onTogglePaSureMode = { viewModel.togglePaSureMode() },
                    onRevealSignalEarly = { viewModel.revealSignalEarly() },
                    simpleMode = settings.simpleMode,
                    onToggleSimpleMode = { viewModel.toggleSimpleMode() },
                    colorMappingVerified = settings.colorMappingVerified
                )
                1 -> HistoricalAnalyticsScreen(
                    drawsAscending = drawsDesc.reversed(),
                    selectedWindow = uiState.selectedWindow,
                    onWindowSelected = { viewModel.selectWindow(it) }
                )
                2 -> ForecastComparisonScreen(
                    forecasts = uiState.allForecastModels,
                    activeModel = when (settings.activeForecastModel) {
                        com.example.data.preferences.AppSettings.MODEL_BASELINE -> ForecastModelType.BASELINE
                        com.example.data.preferences.AppSettings.MODEL_HISTORICAL_FREQ -> ForecastModelType.HISTORICAL_FREQ
                        com.example.data.preferences.AppSettings.MODEL_ROLLING_FREQ -> ForecastModelType.ROLLING_FREQ
                        com.example.data.preferences.AppSettings.MODEL_MARKOV_TRANSITION -> ForecastModelType.MARKOV_TRANSITION
                        else -> ForecastModelType.COMBINED_SMOOTHED
                    },
                    onSelectModel = { viewModel.setActiveForecastModel(it) }
                )
                3 -> BacktestingScreen(
                    backtestResult = uiState.backtestResult,
                    isRunning = uiState.isBacktestingRunning,
                    onRunBacktest = { model, window ->
                        viewModel.runWalkForwardBacktest(model, window)
                    }
                )
                4 -> FieldInspectorScreen(
                    latestDraw = drawsDesc.firstOrNull(),
                    totalLoadedCount = uiState.totalCachedCount,
                    settledCount = uiState.settledCount,
                    pendingCount = uiState.pendingCount,
                    currentSettings = settings,
                    onGenerateCsv = { viewModel.generateCsvExport() },
                    onUpdateParityRule = { viewModel.setParityRule(it) },
                    onUpdateColorVerified = { viewModel.setColorMappingVerified(it) },
                    onSaveApiSettings = { url, page, limit, type ->
                        viewModel.updateApiSettings(url, page, limit, type)
                    },
                    onUpdateAutoRefresh = { enabled, interval ->
                        viewModel.updateAutoRefreshSettings(enabled, interval)
                    },
                    onClearCache = { viewModel.clearLocalCache() }
                )
            }
        }
    }
}
