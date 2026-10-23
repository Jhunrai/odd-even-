package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.math.abs

@JsonClass(generateAdapter = true)
data class ApiGuessOddResponse(
    @Json(name = "status") val status: Int? = null,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "data") val data: List<ApiDrawItem>? = null
)

@JsonClass(generateAdapter = true)
data class ApiDrawItem(
    // Specific Game 24 API fields
    @Json(name = "id") val id: Any? = null,
    @Json(name = "add_time") val addTime: String? = null,
    @Json(name = "game_state") val gameState: Any? = null,
    @Json(name = "game_type") val gameType: Any? = null,
    @Json(name = "game_guess") val gameGuess: String? = null,
    @Json(name = "game_lottery_num") val gameLotteryNum: String? = null,
    @Json(name = "game_time") val gameTime: String? = null,
    @Json(name = "game_open_time") val gameOpenTime: String? = null,
    @Json(name = "_game_state") val _gameState: String? = null,
    @Json(name = "_game_guess") val _gameGuess: String? = null,
    @Json(name = "_game_time") val _gameTime: String? = null,
    @Json(name = "_game_open_time") val _gameOpenTime: String? = null,

    // Alternative legacy / generic fields
    @Json(name = "time") val time: String? = null,
    @Json(name = "serial") val serial: Any? = null,
    @Json(name = "number") val number: Any? = null,
    @Json(name = "result_value") val resultValue: Int? = null,
    @Json(name = "result") val result: String? = null
) {
    fun parseSerial(): Long {
        if (id != null) {
            when (id) {
                is Number -> return id.toLong()
                is String -> id.toLongOrNull()?.let { return it }
            }
        }
        return when (serial) {
            is Number -> serial.toLong()
            is String -> serial.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    fun parseTimestamp(): String {
        return (gameTime ?: _gameTime ?: addTime ?: time ?: "").trim().ifEmpty { "N/A" }
    }

    fun parseOpenTime(): String {
        return (gameOpenTime ?: _gameOpenTime ?: "").trim().ifEmpty { "N/A" }
    }

    fun parseLotteryNum(): String {
        return gameLotteryNum?.trim().orEmpty()
    }

    fun parseUserGuess(): String {
        return (_gameGuess ?: gameGuess ?: "").trim()
    }

    fun parseGameStateString(): String {
        return (_gameState ?: gameState?.toString() ?: "").trim().ifEmpty { "Settled" }
    }

    fun isSettled(): Boolean {
        val state = parseGameStateString()
        if (state.contains("Waiting", ignoreCase = true) ||
            state.contains("Pending", ignoreCase = true) ||
            state.contains("Unsettled", ignoreCase = true)
        ) {
            return false
        }
        val lotNum = parseLotteryNum()
        if (lotNum.isEmpty() && number == null && resultValue == null && result.isNullOrEmpty()) {
            return false
        }
        return true
    }

    fun parseNumber(parityRule: String = "LAST_DIGIT"): Int {
        // 1. Direct number if provided
        val directNum = when (number) {
            is Number -> number.toInt()
            is String -> number.toIntOrNull()
            else -> null
        }
        if (directNum != null) return directNum

        // 2. Direct resultValue
        if (resultValue != null) return resultValue

        // 3. Fallback from game_lottery_num
        val lotNum = parseLotteryNum()
        if (lotNum.isNotEmpty()) {
            when (parityRule) {
                "DIGIT_SUM" -> {
                    val sum = lotNum.filter { it.isDigit() }.sumOf { it.digitToInt() }
                    return sum
                }
                "LAST_DIGIT", "DIRECT_API" -> {
                    val digits = lotNum.filter { it.isDigit() }
                    if (digits.isNotEmpty()) {
                        return digits.last().digitToInt()
                    }
                }
                else -> {
                    val digits = lotNum.filter { it.isDigit() }
                    if (digits.isNotEmpty()) {
                        return digits.takeLast(2).toIntOrNull() ?: digits.last().digitToInt()
                    }
                }
            }
        }

        // 4. Fallback: parse from result string e.g. "Odd&65"
        result?.let { str ->
            val match = Regex("\\d+").find(str)
            if (match != null) {
                return match.value.toIntOrNull() ?: 0
            }
        }

        return 0
    }

    fun parseResultType(num: Int, settled: Boolean): String {
        if (!settled) return "PENDING"
        return if (num % 2 == 0) "EVEN" else "ODD"
    }

    fun parseResultValue(num: Int, settled: Boolean): Int {
        if (!settled) return -1
        return if (num % 2 == 0) 0 else 1
    }

    fun toNormalized(parityRule: String = "LAST_DIGIT", rawJson: String = ""): NormalizedDraw {
        val parsedSerial = parseSerial()
        val parsedTime = parseTimestamp()
        val parsedOpenTime = parseOpenTime()
        val lotNum = parseLotteryNum()
        val guess = parseUserGuess()
        val stateStr = parseGameStateString()
        val settled = isSettled()
        val parsedNum = parseNumber(parityRule)
        val res = parseResultType(parsedNum, settled)
        val resVal = parseResultValue(parsedNum, settled)
        val compositeId = if (parsedSerial > 0) "${parsedSerial}_${parsedTime}" else "${System.currentTimeMillis()}_${parsedTime}"

        return NormalizedDraw(
            id = compositeId,
            timestamp = parsedTime,
            openTime = parsedOpenTime,
            serial = parsedSerial,
            number = parsedNum,
            lotteryNum = lotNum,
            userGuess = guess,
            gameState = stateStr,
            isSettled = settled,
            result = res,
            resultValue = resVal,
            rawData = rawJson.ifEmpty { "time=$parsedTime, serial=$parsedSerial, lotNum=$lotNum, guess=$guess, state=$stateStr, res=$res" }
        )
    }
}

data class NormalizedDraw(
    val id: String,
    val timestamp: String,
    val openTime: String = "",
    val serial: Long,
    val number: Int,
    val lotteryNum: String = "",
    val userGuess: String = "",
    val gameState: String = "Settled",
    val isSettled: Boolean = true,
    val result: String, // "ODD", "EVEN", or "PENDING"
    val resultValue: Int, // 1 for ODD, 0 for EVEN, -1 for PENDING
    val rawData: String
)

enum class AnalysisWindow(val count: Int, val label: String) {
    LAST_10(10, "10"),
    LAST_20(20, "20"),
    LAST_50(50, "50"),
    LAST_100(100, "100"),
    LAST_720(720, "720"),
    ALL(Int.MAX_VALUE, "All")
}

data class WindowStatistics(
    val window: AnalysisWindow,
    val totalCount: Int,
    val settledCount: Int = totalCount,
    val pendingCount: Int = 0,
    val oddCount: Int,
    val evenCount: Int,
    val oddPercentage: Float,
    val evenPercentage: Float,
    val currentStreakCount: Int,
    val currentStreakType: String, // "ODD", "EVEN", "PENDING", "NONE"
    val maxOddStreak: Int,
    val maxEvenStreak: Int,
    val maxChopStreak: Int,
    val markovOddGivenOdd: Float,
    val markovEvenGivenOdd: Float,
    val markovOddGivenEven: Float,
    val markovEvenGivenEven: Float,
    val oddToOddCount: Int = 0,
    val oddToEvenCount: Int = 0,
    val evenToOddCount: Int = 0,
    val evenToEvenCount: Int = 0
)

enum class ForecastModelType(val displayName: String, val shortName: String, val description: String) {
    BASELINE("Uniform Baseline", "Baseline", "Fixed 50/50 uniform probability prior."),
    HISTORICAL_FREQ("Historical Frequency", "Historical", "Marginal probability calculated over all verified records."),
    ROLLING_FREQ("Rolling Window", "Rolling", "Local sample probability across recent verified outcomes."),
    MARKOV_TRANSITION("1st-Order Markov", "Markov", "Conditional state transition probability based on previous draw."),
    COMBINED_SMOOTHED("Laplace Smoothed", "Combined", "Bayesian shrinkage combining rolling & Markov with Laplace damping.")
}

data class ProbabilityForecast(
    val modelType: ForecastModelType,
    val estimatedOddProb: Float, // 0.0 .. 1.0
    val estimatedEvenProb: Float, // 0.0 .. 1.0
    val mostLikelyCategory: String, // "ODD" or "EVEN"
    val confidence: Float, // Evidence-based deviation from 0.50 (e.g. 54.5%)
    val dataWindow: Int, // Number of verified records evaluated
    val lastDataTimestamp: String,
    val isSufficientEvidence: Boolean,
    val insufficientReason: String? = null,
    val backtestedAccuracy: Float? = null,
    val backtestedBrierScore: Float? = null,
    val backtestedLogLoss: Float? = null,
    val calibrationScore: Float? = null,
    val limitations: String = "Past distribution does not guarantee future results. Out-of-sample randomness governs fair draws."
)

data class ConfusionMatrix(
    val trueOddPredOdd: Int = 0,
    val trueOddPredEven: Int = 0,
    val trueEvenPredOdd: Int = 0,
    val trueEvenPredEven: Int = 0
) {
    val total: Int get() = trueOddPredOdd + trueOddPredEven + trueEvenPredOdd + trueEvenPredEven
    val accuracy: Float get() = if (total > 0) ((trueOddPredOdd + trueEvenPredEven).toFloat() / total) * 100f else 50f
    val oddRecall: Float get() {
        val totalOdd = trueOddPredOdd + trueOddPredEven
        return if (totalOdd > 0) (trueOddPredOdd.toFloat() / totalOdd) * 100f else 0f
    }
    val evenRecall: Float get() {
        val totalEven = trueEvenPredEven + trueEvenPredOdd
        return if (totalEven > 0) (trueEvenPredEven.toFloat() / totalEven) * 100f else 0f
    }
}

data class BacktestResult(
    val modelType: ForecastModelType,
    val trainingWindowSize: Int,
    val evaluatedCount: Int,
    val correctCount: Int,
    val actualAccuracy: Float,
    val baselineAccuracy: Float = 50.0f,
    val brierScore: Float, // Mean squared error: 0.25 is random 50/50 baseline
    val logLoss: Float, // Mean cross-entropy: ~0.693 is random 50/50 baseline
    val confusionMatrix: ConfusionMatrix,
    val cumulativeAccuracyTimeline: List<Float> = emptyList(),
    val inSampleSize: Int,
    val outOfSampleSize: Int,
    val disclaimer: String = "Walk-forward validation avoids data leakage. Measured edge reflects past variance and does not guarantee future outcomes."
)

data class GameEvaluationRecord(
    val gameNumber: Int,
    val serial: Long,
    val timestamp: String,
    val openTime: String = "",
    val hitNumber: Int, // The drawn winning number ("hit num")
    val actualParity: String, // "ODD" or "EVEN"
    val predictedParity: String, // "ODD" or "EVEN"
    val isWin: Boolean,
    val confidence: Int,
    val cumulativeAccuracy: Float,
    val cumulativeWins: Int,
    val cumulativeLosses: Int,
    val rationale: String = ""
)

data class Last50AccuracyReport(
    val games: List<GameEvaluationRecord> = emptyList(),
    val totalEvaluated: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val winRate: Float = 0f,
    val baselineAccuracy: Float = 50.0f,
    val edge: Float = 0f,
    val currentStreak: Int = 0,
    val isCurrentStreakWin: Boolean = true,
    val maxWinStreak: Int = 0,
    val maxLossStreak: Int = 0,
    val oddWins: Int = 0,
    val oddLosses: Int = 0,
    val evenWins: Int = 0,
    val evenLosses: Int = 0,
    val accuracyTimeline: List<Float> = emptyList(),
    val topHitNumbers: List<Pair<Int, Int>> = emptyList()
)

data class SignalResult(
    val id: Long = 0,
    val forSerial: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val signalType: String, // "ODD" or "EVEN"
    val confidence: Int, // e.g. 54
    val modelStatus: String = "ACTIVE",
    val rationale: String,
    val predictedNumbers: List<Int> = emptyList(),
    val strategyUsed: String = "COMBINED_SMOOTHED",
    val isInverted: Boolean = false,
    val patternRhythm: String = "Normal",
    val targetZone: String = "",
    val targetHighLow: String = "BIG (41-80)",
    val sequenceTrajectory: String = "",
    val dominantTails: List<Int> = emptyList(),
    val coldExclusions: List<Int> = emptyList(),
    val primaryBull: Int? = null,
    val lossProtectionActive: Boolean = false,
    val consecutiveLossCount: Int = 0,
    val isPassMuna: Boolean = false,
    val passMunaReason: String = "",
    val isConfluenceLocked: Boolean = false,
    val confluenceMessage: String = "",
    val recoveryStakeAdvice: String = "",
    val recoveryMultiplier: Int = 1,
    val safetyHedgeAdvice: String = "",
    val paSureModeActive: Boolean = true,
    val actualResult: String? = null,
    val actualNumber: Int? = null,
    val isCorrect: Boolean? = null,
    val isNumberHit: Boolean? = null
)

data class NumberCandidate(
    val number: Int,
    val role: String,
    val score: Float
)

data class NumberSequenceAnalysis(
    val lastNumber: Int,
    val averageDelta: Float,
    val trajectory: String,
    val activeZone: String,
    val targetHighLow: String = "BIG (41-80)",
    val dominantTails: List<Int>,
    val candidates: List<NumberCandidate>,
    val coldExclusions: List<Int> = emptyList(),
    val recentDeltas: List<Int>,
    val recentNumbers: List<Int>
)

data class AccuracySummary(
    val totalTracked: Int = 0,
    val resolvedCount: Int = 0,
    val hitCount: Int = 0,
    val missCount: Int = 0,
    val accuracyRate: Float = 0f,
    val numberHitCount: Int = 0,
    val tailHitCount: Int = 0,
    val zoneHitCount: Int = 0,
    val currentStreak: Int = 0,
    val isCurrentStreakWin: Boolean = true
)

enum class ApiConnectionStatus {
    CONNECTED,
    CONNECTING,
    OFFLINE,
    ERROR
}
