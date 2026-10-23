package com.example.ui.viewmodel

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccuracySummary
import com.example.data.model.AnalysisWindow
import com.example.data.model.ApiConnectionStatus
import com.example.data.model.BacktestResult
import com.example.data.model.ForecastModelType
import com.example.data.model.Last50AccuracyReport
import com.example.data.model.NormalizedDraw
import com.example.data.model.ProbabilityForecast
import com.example.data.model.SignalResult
import com.example.data.model.WindowStatistics
import com.example.data.preferences.AppPreferences
import com.example.data.preferences.AppSettings
import com.example.data.repository.DrawRepository
import com.example.domain.BacktestingEngine
import com.example.domain.ForecastEngine
import com.example.domain.StatisticalAnalyzer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class AnalyzerUiState(
    val connectionStatus: ApiConnectionStatus = ApiConnectionStatus.CONNECTING,
    val statusMessage: String = "Connecting...",
    val lastUpdateTime: String = "Never",
    val serverTime: String = "N/A",
    val upcomingDrawTime: String = "N/A",
    val upcomingCountdownSeconds: Int = 0,
    val selectedWindow: AnalysisWindow = AnalysisWindow.LAST_50,
    val refreshCountdown: Int = 15,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val statistics: WindowStatistics = StatisticalAnalyzer.computeStatistics(emptyList(), AnalysisWindow.LAST_50),
    val currentSignal: SignalResult? = null,
    val totalCachedCount: Int = 0,
    val settledCount: Int = 0,
    val pendingCount: Int = 0,
    val historyFilter: String = "ALL", // "ALL", "ODD", "EVEN", "PENDING"
    val historySearchQuery: String = "",
    val currentForecast: ProbabilityForecast? = null,
    val allForecastModels: List<ProbabilityForecast> = emptyList(),
    val backtestResult: BacktestResult? = null,
    val isBacktestingRunning: Boolean = false,
    val ballDisplayCount: Int = 20, // 10, 20, 50, 100, 720
    val selectedBacktestModel: ForecastModelType = ForecastModelType.COMBINED_SMOOTHED,
    val selectedBacktestWindow: Int = 30,
    val forceRevealEarly: Boolean = false,
    val last50Report: Last50AccuracyReport = Last50AccuracyReport(),
    val ruleAuditNotice: String = "⚠️ Game 24 Rule Audit: Parity is derived from game_lottery_num rightmost digit. User guess (_game_guess) is monitored separately from verified outcomes."
) {
    val isAnalyzingPhase: Boolean
        get() = upcomingCountdownSeconds > 35 && !forceRevealEarly
}

class AnalyzerViewModel(
    application: Application,
    private val repository: DrawRepository,
    val preferences: AppPreferences
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AnalyzerUiState())
    val uiState: StateFlow<AnalyzerUiState> = _uiState.asStateFlow()

    val settings: StateFlow<AppSettings> = preferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings()
    )

    val drawsDesc: StateFlow<List<NormalizedDraw>> = repository.drawsDescFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val signals: StateFlow<List<SignalResult>> = repository.signalsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val accuracySummary: StateFlow<AccuracySummary> = repository.accuracySummaryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccuracySummary()
    )

    private var autoRefreshJob: Job? = null
    private var countdownJob: Job? = null
    private var lastKnownLatestSerial: Long = 0L

    init {
        // Observe draws changes to recompute statistics, forecasts, and backtest results
        viewModelScope.launch {
            combine(
                repository.drawsAscFlow,
                _uiState,
                settings
            ) { drawsAsc, state, appSettings ->
                Triple(drawsAsc, state.selectedWindow, appSettings)
            }.collect { (drawsAsc, window, appSettings) ->
                if (drawsAsc.isNotEmpty()) {
                    val sliced = StatisticalAnalyzer.sliceWindow(drawsAsc, window)
                    val stats = StatisticalAnalyzer.computeStatistics(sliced, window)
                    val settledOnly = drawsAsc.filter { it.isSettled }
                    val recentLosses = repository.getRecentConsecutiveLossCount()

                    val signal = StatisticalAnalyzer.generateNextSignal(
                        drawsAscending = drawsAsc,
                        strategy = appSettings.patternStrategy,
                        invertSignal = appSettings.invertSignal,
                        bullsCount = appSettings.bullsCount,
                        consecutiveLossCount = recentLosses,
                        autoAntiLossProtection = appSettings.autoAntiLossProtection,
                        paSureModeActive = appSettings.paSureMode
                    )

                    val activeModelType = when (appSettings.activeForecastModel) {
                        AppSettings.MODEL_BASELINE -> ForecastModelType.BASELINE
                        AppSettings.MODEL_HISTORICAL_FREQ -> ForecastModelType.HISTORICAL_FREQ
                        AppSettings.MODEL_ROLLING_FREQ -> ForecastModelType.ROLLING_FREQ
                        AppSettings.MODEL_MARKOV_TRANSITION -> ForecastModelType.MARKOV_TRANSITION
                        else -> ForecastModelType.COMBINED_SMOOTHED
                    }

                    val forecast = ForecastEngine.generateForecast(
                        drawsAscending = drawsAsc,
                        modelType = activeModelType,
                        rollingWindowSize = window.count.coerceAtMost(100)
                    )

                    val allModels = ForecastEngine.compareAllModels(
                        drawsAscending = drawsAsc,
                        rollingWindowSize = window.count.coerceAtMost(100)
                    )

                    // Compute historical accuracy over last 50 games using walk-forward prediction
                    val last50Report = BacktestingEngine.evaluateLast50Games(drawsAsc, activeModelType)

                    // Latest upcoming draw time from newest draw
                    val newest = drawsAsc.last()
                    val upcomingTime = newest.openTime.ifEmpty { "N/A" }

                    val synchronizedForecast = if (signal != null) {
                        val targetIsOdd = signal.signalType == "ODD"
                        val adjustedOddProb = if (targetIsOdd) maxOf(forecast.estimatedOddProb, 0.58f) else minOf(forecast.estimatedOddProb, 0.42f)
                        forecast.copy(
                            estimatedOddProb = adjustedOddProb,
                            estimatedEvenProb = 1f - adjustedOddProb,
                            mostLikelyCategory = signal.signalType,
                            confidence = signal.confidence.toFloat()
                        )
                    } else forecast

                    _uiState.update { current ->
                        current.copy(
                            statistics = stats,
                            currentSignal = signal,
                            currentForecast = synchronizedForecast,
                            allForecastModels = allModels,
                            totalCachedCount = drawsAsc.size,
                            settledCount = settledOnly.size,
                            pendingCount = drawsAsc.size - settledOnly.size,
                            upcomingDrawTime = upcomingTime,
                            last50Report = last50Report
                        )
                    }

                    // Check if new draw has arrived
                    val newestSerial = newest.serial
                    if (lastKnownLatestSerial != 0L && newestSerial > lastKnownLatestSerial) {
                        triggerHapticFeedback()
                        // Automatically reset early reveal on new draw so fresh 35s analysis window begins
                        _uiState.update { it.copy(forceRevealEarly = false) }
                    }
                    lastKnownLatestSerial = newestSerial
                }
            }
        }

        // Start initial fetch, auto-refresh, and live countdown timer
        refreshData()
        startAutoRefreshTimer()
        startUpcomingCountdownTimer()
    }

    fun selectWindow(window: AnalysisWindow) {
        _uiState.update { it.copy(selectedWindow = window) }
    }

    fun selectBallDisplayCount(count: Int) {
        _uiState.update { it.copy(ballDisplayCount = count) }
    }

    fun setPatternStrategy(strategy: String) {
        viewModelScope.launch {
            preferences.updatePatternStrategy(strategy)
        }
    }

    fun toggleInvertSignal() {
        viewModelScope.launch {
            val nextInvert = !settings.value.invertSignal
            preferences.updateInvertSignal(nextInvert)
        }
    }

    fun togglePaSureMode() {
        viewModelScope.launch {
            val next = !settings.value.paSureMode
            preferences.updatePaSureMode(next)
        }
    }

    fun toggleSimpleMode() {
        viewModelScope.launch {
            val next = !settings.value.simpleMode
            preferences.updateSimpleMode(next)
        }
    }

    fun setSimpleMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateSimpleMode(enabled)
        }
    }

    fun setInvertSignal(invert: Boolean) {
        viewModelScope.launch {
            preferences.updateInvertSignal(invert)
        }
    }

    fun setBullsCount(count: Int) {
        viewModelScope.launch {
            preferences.updateBullsCount(count)
        }
    }

    fun setAutoAntiLossProtection(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateAutoAntiLoss(enabled)
        }
    }

    fun setParityRule(rule: String) {
        viewModelScope.launch {
            preferences.updateParityRule(rule)
            refreshData()
        }
    }

    fun setColorMappingVerified(verified: Boolean) {
        viewModelScope.launch {
            preferences.updateColorMappingVerified(verified)
        }
    }

    fun setActiveForecastModel(modelType: ForecastModelType) {
        val modelKey = when (modelType) {
            ForecastModelType.BASELINE -> AppSettings.MODEL_BASELINE
            ForecastModelType.HISTORICAL_FREQ -> AppSettings.MODEL_HISTORICAL_FREQ
            ForecastModelType.ROLLING_FREQ -> AppSettings.MODEL_ROLLING_FREQ
            ForecastModelType.MARKOV_TRANSITION -> AppSettings.MODEL_MARKOV_TRANSITION
            ForecastModelType.COMBINED_SMOOTHED -> AppSettings.MODEL_COMBINED_SMOOTHED
        }
        viewModelScope.launch {
            preferences.updateActiveForecastModel(modelKey)
        }
    }

    fun setHistoryFilter(filter: String) {
        _uiState.update { it.copy(historyFilter = filter) }
    }

    fun setHistorySearchQuery(query: String) {
        _uiState.update { it.copy(historySearchQuery = query) }
    }

    fun runWalkForwardBacktest(
        modelType: ForecastModelType,
        trainingWindow: Int
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBacktestingRunning = true,
                    selectedBacktestModel = modelType,
                    selectedBacktestWindow = trainingWindow
                )
            }
            delay(200) // slight UI responsiveness yield
            val currentDraws = drawsDesc.value.reversed()
            val result = BacktestingEngine.runWalkForwardBacktest(
                drawsAscending = currentDraws,
                modelType = modelType,
                trainingWindow = trainingWindow
            )
            _uiState.update {
                it.copy(
                    backtestResult = result,
                    isBacktestingRunning = false
                )
            }
        }
    }

    fun generateCsvExport(): String {
        val draws = drawsDesc.value
        val sb = StringBuilder()
        sb.append("ID,Serial,Timestamp,OpenTime,LotteryNumber,UserGuess,GameState,SettledParity,ParityValue\n")
        for (draw in draws) {
            sb.append("${draw.id},")
            sb.append("${draw.serial},")
            sb.append("\"${draw.timestamp}\",")
            sb.append("\"${draw.openTime}\",")
            sb.append("\"${draw.lotteryNum}\",")
            sb.append("\"${draw.userGuess}\",")
            sb.append("\"${draw.gameState}\",")
            sb.append("${draw.result},")
            sb.append("${draw.resultValue}\n")
        }
        return sb.toString()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshing = true,
                    connectionStatus = ApiConnectionStatus.CONNECTING,
                    statusMessage = "Syncing..."
                )
            }

            try {
                val currentSettings = settings.value
                val response = repository.fetchAndSyncDraws(
                    url = currentSettings.apiUrl,
                    page = currentSettings.page,
                    limit = currentSettings.limit,
                    type = currentSettings.type,
                    strategy = currentSettings.patternStrategy,
                    invertSignal = currentSettings.invertSignal,
                    bullsCount = currentSettings.bullsCount,
                    autoAntiLossProtection = currentSettings.autoAntiLossProtection,
                    parityRule = currentSettings.parityRule
                )

                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val nowStr = timeFormat.format(Date())

                _uiState.update { current ->
                    current.copy(
                        connectionStatus = ApiConnectionStatus.CONNECTED,
                        statusMessage = "API CONNECTED",
                        lastUpdateTime = nowStr,
                        serverTime = response.serverTime ?: current.serverTime,
                        errorMessage = null,
                        isRefreshing = false,
                        refreshCountdown = currentSettings.refreshIntervalSeconds
                    )
                }
            } catch (e: Exception) {
                _uiState.update { current ->
                    current.copy(
                        connectionStatus = if (current.totalCachedCount > 0) ApiConnectionStatus.OFFLINE else ApiConnectionStatus.ERROR,
                        statusMessage = "API OFFLINE",
                        errorMessage = e.localizedMessage ?: "Connection failure",
                        isRefreshing = false
                    )
                }
            }
        }
    }

    private fun startAutoRefreshTimer() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val currentSettings = settings.value
                if (!currentSettings.autoRefreshEnabled) continue

                val currentCountdown = _uiState.value.refreshCountdown
                if (currentCountdown <= 1) {
                    _uiState.update { it.copy(refreshCountdown = currentSettings.refreshIntervalSeconds) }
                    refreshData()
                } else {
                    _uiState.update { it.copy(refreshCountdown = currentCountdown - 1) }
                }
            }
        }
    }

    private fun startUpcomingCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var previousRemaining: Int? = null
            while (isActive) {
                delay(1000)
                val targetTimeStr = _uiState.value.upcomingDrawTime
                if (targetTimeStr.isNotBlank() && targetTimeStr != "N/A") {
                    val remainingSeconds = calculateRemainingSeconds(targetTimeStr)
                    // Haptic alert when countdown enters the 35-second lock-in window
                    if (previousRemaining != null && previousRemaining > 35 && remainingSeconds <= 35) {
                        triggerHapticFeedback()
                    }
                    previousRemaining = remainingSeconds
                    _uiState.update { it.copy(upcomingCountdownSeconds = remainingSeconds) }
                }
            }
        }
    }

    private fun calculateRemainingSeconds(targetTimeStr: String): Int {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = format.parse(targetTimeStr)
            if (date != null) {
                val now = System.currentTimeMillis()
                val diff = (date.time - now) / 1000
                if (diff > 0) {
                    diff.toInt()
                } else {
                    // When the latest draw record is already past, calculate current active round cycle (e.g. 60s per round)
                    val elapsedSinceLast = (now - date.time) / 1000
                    val roundCycle = 60L
                    val remaining = roundCycle - (elapsedSinceLast % roundCycle)
                    if (remaining in 1..roundCycle) {
                        remaining.toInt()
                    } else 0
                }
            } else 0
        } catch (_: Exception) {
            0
        }
    }

    fun revealSignalEarly() {
        _uiState.update { it.copy(forceRevealEarly = true) }
        triggerHapticFeedback()
    }

    fun updateApiSettings(url: String, page: Int, limit: Int, type: Int) {
        viewModelScope.launch {
            preferences.updateApiSettings(url, page, limit, type)
            refreshData()
        }
    }

    fun updateAutoRefreshSettings(enabled: Boolean, intervalSeconds: Int) {
        viewModelScope.launch {
            preferences.updateAutoRefresh(enabled, intervalSeconds)
            _uiState.update { it.copy(refreshCountdown = intervalSeconds) }
        }
    }

    fun updateHaptic(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateHapticFeedback(enabled)
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            preferences.resetToDefaults()
            refreshData()
        }
    }

    fun clearLocalCache() {
        viewModelScope.launch {
            repository.clearAllData()
            _uiState.update {
                it.copy(
                    totalCachedCount = 0,
                    settledCount = 0,
                    pendingCount = 0,
                    currentSignal = null,
                    currentForecast = null,
                    backtestResult = null,
                    statistics = StatisticalAnalyzer.computeStatistics(emptyList(), it.selectedWindow)
                )
            }
            refreshData()
        }
    }

    private fun triggerHapticFeedback() {
        if (!settings.value.hapticFeedbackEnabled) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(VibratorManager::class.java)
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Vibrator::class.java)
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {
            // Ignore if vibration unavailable
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(application)
            val repo = DrawRepository(db)
            val prefs = AppPreferences(application)
            return AnalyzerViewModel(application, repo, prefs) as T
        }
    }
}
