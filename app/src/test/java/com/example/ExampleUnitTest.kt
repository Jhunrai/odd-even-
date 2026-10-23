package com.example

import com.example.data.model.AnalysisWindow
import com.example.data.model.ApiDrawItem
import com.example.data.model.ForecastModelType
import com.example.data.model.NormalizedDraw
import com.example.data.preferences.AppSettings
import com.example.domain.BacktestingEngine
import com.example.domain.ForecastEngine
import com.example.domain.StatisticalAnalyzer
import com.example.ui.viewmodel.AnalyzerUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticalAnalyzerTest {

    @Test
    fun testOddEvenDetermination() {
        val oddItem = ApiDrawItem(time = "09-30 11:40", serial = 8260930170L, number = 65)
        val normalizedOdd = oddItem.toNormalized()
        assertEquals("ODD", normalizedOdd.result)
        assertEquals(1, normalizedOdd.resultValue)
        assertEquals(65, normalizedOdd.number)
        assertEquals("8260930170_09-30 11:40", normalizedOdd.id)

        val evenItem = ApiDrawItem(time = "09-30 11:36", serial = 8260930168L, number = 40)
        val normalizedEven = evenItem.toNormalized()
        assertEquals("EVEN", normalizedEven.result)
        assertEquals(0, normalizedEven.resultValue)
        assertEquals(40, normalizedEven.number)
    }

    @Test
    fun testGame24ApiSchemaParsingAndPendingDetection() {
        // Settled round
        val settledItem = ApiDrawItem(
            id = 123456L,
            addTime = "2026-10-22 14:40:50",
            gameState = 1,
            gameGuess = "0",
            gameLotteryNum = "8261022261",
            gameTime = "2026-10-22 14:40:50",
            gameOpenTime = "2026-10-22 14:42:00",
            _gameState = "Settled",
            _gameGuess = "Even"
        )
        val normSettled = settledItem.toNormalized(parityRule = "LAST_DIGIT")
        assertTrue(normSettled.isSettled)
        assertEquals(1, normSettled.number) // last digit of 8261022261 is 1
        assertEquals("ODD", normSettled.result)
        assertEquals("Even", normSettled.userGuess)
        assertEquals("8261022261", normSettled.lotteryNum)

        // Pending round: "Waiting for draw"
        val pendingItem = ApiDrawItem(
            id = 123457L,
            addTime = "2026-10-22 14:42:50",
            gameState = 0,
            gameGuess = "1",
            gameLotteryNum = "",
            gameTime = "2026-10-22 14:42:50",
            gameOpenTime = "2026-10-22 14:44:00",
            _gameState = "Waiting for draw",
            _gameGuess = "Odd"
        )
        val normPending = pendingItem.toNormalized(parityRule = "LAST_DIGIT")
        assertFalse(normPending.isSettled)
        assertEquals("PENDING", normPending.result)
        assertEquals(-1, normPending.resultValue)
        assertEquals("Waiting for draw", normPending.gameState)
    }

    @Test
    fun testStatisticsCalculation() {
        val draws = listOf(
            NormalizedDraw("1", "10:00", "", 1, 11, "", "", "Settled", true, "ODD", 1, ""),
            NormalizedDraw("2", "10:02", "", 2, 12, "", "", "Settled", true, "EVEN", 0, ""),
            NormalizedDraw("3", "10:04", "", 3, 13, "", "", "Settled", true, "ODD", 1, ""),
            NormalizedDraw("4", "10:06", "", 4, 15, "", "", "Settled", true, "ODD", 1, ""),
            NormalizedDraw("5", "10:08", "", 5, 20, "", "", "Settled", true, "EVEN", 0, "")
        )

        val stats = StatisticalAnalyzer.computeStatistics(draws, AnalysisWindow.LAST_20)
        assertEquals(5, stats.totalCount)
        assertEquals(5, stats.settledCount)
        assertEquals(0, stats.pendingCount)
        assertEquals(3, stats.oddCount)
        assertEquals(2, stats.evenCount)
        assertEquals(60.0f, stats.oddPercentage, 0.01f)
        assertEquals(40.0f, stats.evenPercentage, 0.01f)
        assertEquals("EVEN", stats.currentStreakType)
        assertEquals(1, stats.currentStreakCount)
        assertEquals(2, stats.maxOddStreak)
    }

    @Test
    fun testForecastEngineBaselineAndInsufficientEvidence() {
        // Less than 10 draws should flag insufficient evidence
        val shortDraws = listOf(
            NormalizedDraw("1", "10:00", "", 1, 11, "", "", "Settled", true, "ODD", 1, ""),
            NormalizedDraw("2", "10:02", "", 2, 12, "", "", "Settled", true, "EVEN", 0, "")
        )
        val shortForecast = ForecastEngine.generateForecast(shortDraws, ForecastModelType.COMBINED_SMOOTHED)
        assertFalse(shortForecast.isSufficientEvidence)
        assertTrue(shortForecast.insufficientReason!!.contains("INSUFFICIENT EVIDENCE"))

        // Build 20 verified draws
        val validDraws = mutableListOf<NormalizedDraw>()
        for (i in 1..20) {
            val isOdd = i % 2 != 0
            validDraws.add(
                NormalizedDraw(
                    id = "$i",
                    timestamp = "10:$i",
                    serial = i.toLong(),
                    number = if (isOdd) 23 else 24,
                    result = if (isOdd) "ODD" else "EVEN",
                    resultValue = if (isOdd) 1 else 0,
                    isSettled = true,
                    rawData = ""
                )
            )
        }

        val forecast = ForecastEngine.generateForecast(validDraws, ForecastModelType.COMBINED_SMOOTHED)
        assertTrue(forecast.isSufficientEvidence)
        assertTrue(forecast.estimatedOddProb in 0.1f..0.9f)
        assertTrue(forecast.estimatedEvenProb in 0.1f..0.9f)
        assertEquals(1.0f, forecast.estimatedOddProb + forecast.estimatedEvenProb, 0.01f)
    }

    @Test
    fun testWalkForwardBacktestingEngine() {
        val validDraws = mutableListOf<NormalizedDraw>()
        for (i in 1..50) {
            val isOdd = (i % 3 != 0) // structured pattern
            validDraws.add(
                NormalizedDraw(
                    id = "$i",
                    timestamp = "10:$i",
                    serial = i.toLong(),
                    number = if (isOdd) 21 else 22,
                    result = if (isOdd) "ODD" else "EVEN",
                    resultValue = if (isOdd) 1 else 0,
                    isSettled = true,
                    rawData = ""
                )
            )
        }

        val backtest = BacktestingEngine.runWalkForwardBacktest(
            drawsAscending = validDraws,
            modelType = ForecastModelType.COMBINED_SMOOTHED,
            trainingWindow = 20
        )

        assertEquals(30, backtest.evaluatedCount) // 50 - 20 = 30 out-of-sample evaluations
        assertTrue(backtest.brierScore in 0.0f..1.0f)
        assertTrue(backtest.logLoss > 0f)
        assertEquals(30, backtest.confusionMatrix.total)
        assertTrue(backtest.actualAccuracy in 0f..100f)
    }

    @Test
    fun testDragonMomentumFollowsStreak() {
        val draws = mutableListOf<NormalizedDraw>()
        for (i in 1..20) {
            val num = if (i >= 17) 23 else (if (i % 2 == 0) 10 else 11)
            val res = if (num % 2 == 0) "EVEN" else "ODD"
            draws.add(NormalizedDraw("$i", "10:$i", "", i.toLong(), num, "", "", "Settled", true, res, if (res == "ODD") 1 else 0, ""))
        }

        val signal = StatisticalAnalyzer.generateNextSignal(
            drawsAscending = draws,
            strategy = AppSettings.STRATEGY_TREND_DRAGON
        )
        assertNotNull(signal)
        assertEquals("ODD", signal!!.signalType)
        assertTrue(signal.confidence in 55..95)
    }

    @Test
    fun testAntiLossInvertMode() {
        val draws = mutableListOf<NormalizedDraw>()
        for (i in 1..20) {
            val num = if (i >= 17) 23 else (if (i % 2 == 0) 10 else 11)
            val res = if (num % 2 == 0) "EVEN" else "ODD"
            draws.add(NormalizedDraw("$i", "10:$i", "", i.toLong(), num, "", "", "Settled", true, res, if (res == "ODD") 1 else 0, ""))
        }

        val normalSignal = StatisticalAnalyzer.generateNextSignal(draws, invertSignal = false)
        val invertedSignal = StatisticalAnalyzer.generateNextSignal(draws, invertSignal = true)

        assertNotNull(normalSignal)
        assertNotNull(invertedSignal)
        val expectedInverted = if (normalSignal!!.signalType == "ODD") "EVEN" else "ODD"
        assertEquals(expectedInverted, invertedSignal!!.signalType)
        assertTrue(invertedSignal.isInverted)
    }

    @Test
    fun testDynamicNumberDomainDetectionAndMatchingBulls() {
        // Draws in 0..9 range (like Game 24 single digit parity)
        val singleDigitDraws = mutableListOf<NormalizedDraw>()
        for (i in 1..25) {
            val num = (i * 3 + 1) % 10 // strictly in 0..9
            val res = if (num % 2 == 0) "EVEN" else "ODD"
            singleDigitDraws.add(
                NormalizedDraw(
                    id = "$i",
                    timestamp = "10:$i",
                    serial = i.toLong(),
                    number = num,
                    result = res,
                    resultValue = if (res == "ODD") 1 else 0,
                    isSettled = true,
                    rawData = ""
                )
            )
        }

        val domain = StatisticalAnalyzer.detectNumberDomain(singleDigitDraws)
        assertEquals(0, domain.first)
        assertEquals(9, domain.second)

        // When generating bulls for ODD in 0..9 domain, candidate numbers must be in {1, 3, 5, 7, 9}
        val analysisOdd = StatisticalAnalyzer.analyzeNumberSequence(singleDigitDraws, "ODD", count = 5)
        for (candidate in analysisOdd.candidates) {
            assertTrue("Candidate ${candidate.number} must be in 0..9", candidate.number in 0..9)
            assertEquals("Candidate ${candidate.number} must be ODD", 1, candidate.number % 2)
        }

        // When generating bulls for EVEN in 0..9 domain, candidate numbers must be in {0, 2, 4, 6, 8}
        val analysisEven = StatisticalAnalyzer.analyzeNumberSequence(singleDigitDraws, "EVEN", count = 5)
        for (candidate in analysisEven.candidates) {
            assertTrue("Candidate ${candidate.number} must be in 0..9", candidate.number in 0..9)
            assertEquals("Candidate ${candidate.number} must be EVEN", 0, candidate.number % 2)
        }
    }

    @Test
    fun testPaSureModePassMunaAndBawiTaloRecovery() {
        val mixedDraws = mutableListOf<NormalizedDraw>()
        // Alternate ODD / EVEN equally with no streak
        for (i in 1..20) {
            val res = if (i % 2 == 0) "EVEN" else "ODD"
            mixedDraws.add(
                NormalizedDraw(
                    id = "$i",
                    timestamp = "10:$i",
                    serial = i.toLong(),
                    number = if (res == "ODD") 1 else 2,
                    result = res,
                    resultValue = if (res == "ODD") 1 else 0,
                    isSettled = true,
                    rawData = ""
                )
            )
        }

        // Confluence lock during clean Dragon streak (3 in a row)
        val dragonDraws = mixedDraws.toMutableList()
        dragonDraws.add(NormalizedDraw(id = "21", timestamp = "10:21", serial = 21L, number = 1, result = "ODD", resultValue = 1, isSettled = true, rawData = ""))
        dragonDraws.add(NormalizedDraw(id = "22", timestamp = "10:22", serial = 22L, number = 3, result = "ODD", resultValue = 1, isSettled = true, rawData = ""))
        dragonDraws.add(NormalizedDraw(id = "23", timestamp = "10:23", serial = 23L, number = 5, result = "ODD", resultValue = 1, isSettled = true, rawData = ""))

        val dragonSignal = StatisticalAnalyzer.generateNextSignal(dragonDraws, paSureModeActive = true)
        assertNotNull(dragonSignal)
        assertTrue("Dragon run should lock confluence", dragonSignal!!.isConfluenceLocked)
        assertFalse("Dragon run should not PASS MUNA", dragonSignal.isPassMuna)

        // Test Agad-Agad na Bawi-Talo recovery multiplier
        val recovery1 = StatisticalAnalyzer.generateNextSignal(dragonDraws, consecutiveLossCount = 1)
        assertNotNull(recovery1)
        assertEquals(2, recovery1!!.recoveryMultiplier) // 2x recovery stake
        assertTrue(recovery1.recoveryStakeAdvice.contains("2x"))

        val recovery2 = StatisticalAnalyzer.generateNextSignal(dragonDraws, consecutiveLossCount = 2)
        assertNotNull(recovery2)
        assertEquals(3, recovery2!!.recoveryMultiplier) // 3x recovery stake
        assertTrue(recovery2.recoveryStakeAdvice.contains("3x"))
    }

    @Test
    fun testSignalParityAndBullsAlignmentNeverConflict() {
        val testDraws = mutableListOf<NormalizedDraw>()
        for (i in 1..25) {
            val num = (i * 3) % 10 // generates 0..9 digits
            val parity = if (num % 2 == 0) "EVEN" else "ODD"
            testDraws.add(
                NormalizedDraw(
                    id = "$i",
                    timestamp = "10:$i",
                    serial = i.toLong(),
                    number = num,
                    result = parity,
                    resultValue = if (parity == "ODD") 1 else 0,
                    isSettled = true,
                    rawData = ""
                )
            )
        }

        val signal = StatisticalAnalyzer.generateNextSignal(testDraws)
        assertNotNull(signal)
        val targetParity = signal!!.signalType
        assertTrue(targetParity == "ODD" || targetParity == "EVEN")

        // Strictly verify that EVERY predicted candidate number matches targetParity!
        signal.predictedNumbers.forEach { ballNum ->
            val isBallOdd = (ballNum % 2 != 0)
            if (targetParity == "ODD") {
                assertTrue("Ball $ballNum must be ODD when signal is ODD", isBallOdd)
            } else {
                assertFalse("Ball $ballNum must be EVEN when signal is EVEN", isBallOdd)
            }
        }
    }

    @Test
    fun test35SecondsAnalysisWindowAndEarlyRevealLogic() {
        // 1. When countdown is 60s (> 35s), system MUST be in analyzing phase and not flex prematurely
        val stateAt60 = AnalyzerUiState(upcomingCountdownSeconds = 60, forceRevealEarly = false)
        assertTrue("When countdown > 35s, analyzing phase must be active", stateAt60.isAnalyzingPhase)

        val stateAt36 = AnalyzerUiState(upcomingCountdownSeconds = 36, forceRevealEarly = false)
        assertTrue("At 36s (> 35s), analysis must still be running", stateAt36.isAnalyzingPhase)

        // 2. When countdown hits 35s (or below), analysis finishes and verified prediction is locked & revealed
        val stateAt35 = AnalyzerUiState(upcomingCountdownSeconds = 35, forceRevealEarly = false)
        assertFalse("At 35s, analysis must finish and prediction must be locked & revealed", stateAt35.isAnalyzingPhase)

        val stateAt15 = AnalyzerUiState(upcomingCountdownSeconds = 15, forceRevealEarly = false)
        assertFalse("At 15s, signal must remain locked & revealed", stateAt15.isAnalyzingPhase)

        val stateAtZero = AnalyzerUiState(upcomingCountdownSeconds = 0, forceRevealEarly = false)
        assertFalse("At 0s (draw ongoing/imminent), signal must not be blocked", stateAtZero.isAnalyzingPhase)

        // 3. User manual override: if user taps 'I-flex Agad', forceRevealEarly becomes true
        val stateForcedEarly = AnalyzerUiState(upcomingCountdownSeconds = 55, forceRevealEarly = true)
        assertFalse("When user taps reveal early, analyzing phase must end immediately", stateForcedEarly.isAnalyzingPhase)
    }
}

