package com.example.domain

import com.example.data.model.BacktestResult
import com.example.data.model.ConfusionMatrix
import com.example.data.model.ForecastModelType
import com.example.data.model.GameEvaluationRecord
import com.example.data.model.Last50AccuracyReport
import com.example.data.model.NormalizedDraw
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.roundToInt

object BacktestingEngine {

    /**
     * Executes chronological, walk-forward out-of-sample backtesting.
     * At each step t, only information prior to t is used to predict draw t.
     */
    fun runWalkForwardBacktest(
        drawsAscending: List<NormalizedDraw>,
        modelType: ForecastModelType = ForecastModelType.COMBINED_SMOOTHED,
        trainingWindow: Int = 30
    ): BacktestResult {
        val settled = drawsAscending.filter { it.isSettled && (it.result == "ODD" || it.result == "EVEN") }

        if (settled.size <= trainingWindow) {
            return BacktestResult(
                modelType = modelType,
                trainingWindowSize = trainingWindow,
                evaluatedCount = 0,
                correctCount = 0,
                actualAccuracy = 50.0f,
                baselineAccuracy = 50.0f,
                brierScore = 0.25f,
                logLoss = 0.693f,
                confusionMatrix = ConfusionMatrix(),
                cumulativeAccuracyTimeline = emptyList(),
                inSampleSize = settled.size,
                outOfSampleSize = 0,
                disclaimer = "Insufficient history for walk-forward validation (minimum ${trainingWindow + 5} settled draws needed)."
            )
        }

        var trueOddPredOdd = 0
        var trueOddPredEven = 0
        var trueEvenPredOdd = 0
        var trueEvenPredEven = 0

        var sumBrier = 0.0
        var sumLogLoss = 0.0
        var correctCount = 0
        var totalEvaluated = 0

        val cumulativeTimeline = mutableListOf<Float>()

        for (i in trainingWindow until settled.size) {
            val history = settled.subList(0, i)
            val target = settled[i]
            val actualIsOdd = target.result == "ODD"
            val actualY = if (actualIsOdd) 1.0 else 0.0

            // Estimate probability using solely history up to i-1
            val pOdd = predictProbabilityAtStep(history, modelType).toDouble()
            val clampedP = pOdd.coerceIn(0.001, 0.999)

            // Category assignment
            val predIsOdd = pOdd >= 0.50
            val isCorrect = (predIsOdd == actualIsOdd)

            if (isCorrect) correctCount++
            totalEvaluated++

            // Confusion matrix counts
            if (actualIsOdd) {
                if (predIsOdd) trueOddPredOdd++ else trueOddPredEven++
            } else {
                if (predIsOdd) trueEvenPredOdd++ else trueEvenPredEven++
            }

            // Brier score: (p - y)^2
            val brierDiff = clampedP - actualY
            sumBrier += (brierDiff * brierDiff)

            // Cross-entropy Log Loss: -[y * ln(p) + (1-y) * ln(1-p)]
            val logLossStep = -(actualY * ln(clampedP) + (1.0 - actualY) * ln(1.0 - clampedP))
            sumLogLoss += logLossStep

            // Record rolling cumulative accuracy
            val currentAcc = (correctCount.toFloat() / totalEvaluated) * 100f
            cumulativeTimeline.add((currentAcc * 10).roundToInt() / 10f)
        }

        val actualAccuracy = if (totalEvaluated > 0) (correctCount.toFloat() / totalEvaluated) * 100f else 50f
        val meanBrier = if (totalEvaluated > 0) (sumBrier / totalEvaluated).toFloat() else 0.25f
        val meanLogLoss = if (totalEvaluated > 0) (sumLogLoss / totalEvaluated).toFloat() else 0.693f

        return BacktestResult(
            modelType = modelType,
            trainingWindowSize = trainingWindow,
            evaluatedCount = totalEvaluated,
            correctCount = correctCount,
            actualAccuracy = (actualAccuracy * 10).roundToInt() / 10f,
            baselineAccuracy = 50.0f,
            brierScore = (meanBrier * 1000).roundToInt() / 1000f,
            logLoss = (meanLogLoss * 1000).roundToInt() / 1000f,
            confusionMatrix = ConfusionMatrix(
                trueOddPredOdd = trueOddPredOdd,
                trueOddPredEven = trueOddPredEven,
                trueEvenPredOdd = trueEvenPredOdd,
                trueEvenPredEven = trueEvenPredEven
            ),
            cumulativeAccuracyTimeline = cumulativeTimeline,
            inSampleSize = trainingWindow,
            outOfSampleSize = totalEvaluated,
            disclaimer = "Chronological walk-forward test over $totalEvaluated out-of-sample predictions. Past statistical distribution does not guarantee future results."
        )
    }

    /**
     * Evaluates the last up to 50 settled games in chronological order using walk-forward prediction.
     * Computes game-by-game results (Win, Loss, Hit Number, Parity), cumulative win rate timeline,
     * and statistical metrics.
     */
    fun evaluateLast50Games(
        drawsAscending: List<NormalizedDraw>,
        modelType: ForecastModelType = ForecastModelType.COMBINED_SMOOTHED
    ): Last50AccuracyReport {
        val settled = drawsAscending.filter { it.isSettled && (it.result == "ODD" || it.result == "EVEN") }
        if (settled.isEmpty()) {
            return Last50AccuracyReport()
        }

        val targetCount = min(50, settled.size)
        // If settled size is > 50, use the last 50 draws with preceding history as in-sample
        val startIndex = (settled.size - targetCount).coerceAtLeast(0)

        val evaluatedChronological = mutableListOf<GameEvaluationRecord>()
        val timeline = mutableListOf<Float>()
        var runningWins = 0
        var runningLosses = 0
        var oddWins = 0
        var oddLosses = 0
        var evenWins = 0
        var evenLosses = 0

        val hitNumberCounts = mutableMapOf<Int, Int>()

        for (i in startIndex until settled.size) {
            val target = settled[i]
            val history = settled.subList(0, i)

            val pOdd = if (history.isEmpty()) 0.50f else predictProbabilityAtStep(history, modelType)
            val predictedParity = if (pOdd >= 0.50f) "ODD" else "EVEN"
            val actualParity = target.result
            val hitNum = target.number
            val isWin = (predictedParity.equals(actualParity, ignoreCase = true))

            hitNumberCounts[hitNum] = (hitNumberCounts[hitNum] ?: 0) + 1

            if (isWin) {
                runningWins++
                if (actualParity == "ODD") oddWins++ else evenWins++
            } else {
                runningLosses++
                if (actualParity == "ODD") oddLosses++ else evenLosses++
            }

            val totalSoFar = runningWins + runningLosses
            val runningAcc = (runningWins.toFloat() / totalSoFar) * 100f
            val roundedAcc = (runningAcc * 10).roundToInt() / 10f
            timeline.add(roundedAcc)

            val conf = (abs(pOdd - 0.50f) * 100f + 50f).roundToInt().coerceIn(51, 95)
            val gameNum = totalSoFar

            val record = GameEvaluationRecord(
                gameNumber = gameNum,
                serial = target.serial,
                timestamp = target.timestamp,
                openTime = target.openTime,
                hitNumber = hitNum,
                actualParity = actualParity,
                predictedParity = predictedParity,
                isWin = isWin,
                confidence = conf,
                cumulativeAccuracy = roundedAcc,
                cumulativeWins = runningWins,
                cumulativeLosses = runningLosses,
                rationale = "Walk-Forward model: P(ODD) = ${(pOdd * 100).roundToInt()}%"
            )
            evaluatedChronological.add(record)
        }

        // Calculate streaks
        var currentStreak = 0
        val isCurrentStreakWin = evaluatedChronological.lastOrNull()?.isWin ?: true
        for (rec in evaluatedChronological.reversed()) {
            if (rec.isWin == isCurrentStreakWin) {
                currentStreak++
            } else {
                break
            }
        }

        var maxWinStreak = 0
        var maxLossStreak = 0
        var tempWinStreak = 0
        var tempLossStreak = 0
        for (rec in evaluatedChronological) {
            if (rec.isWin) {
                tempWinStreak++
                tempLossStreak = 0
                if (tempWinStreak > maxWinStreak) maxWinStreak = tempWinStreak
            } else {
                tempLossStreak++
                tempWinStreak = 0
                if (tempLossStreak > maxLossStreak) maxLossStreak = tempLossStreak
            }
        }

        val totalEvaluated = evaluatedChronological.size
        val finalWinRate = if (totalEvaluated > 0) {
            ((runningWins.toFloat() / totalEvaluated) * 1000).roundToInt() / 10f
        } else 0f

        val topNumbers = hitNumberCounts.entries
            .sortedByDescending { it.value }
            .take(8)
            .map { Pair(it.key, it.value) }

        return Last50AccuracyReport(
            games = evaluatedChronological.reversed(), // Newest game first for list view
            totalEvaluated = totalEvaluated,
            totalWins = runningWins,
            totalLosses = runningLosses,
            winRate = finalWinRate,
            baselineAccuracy = 50.0f,
            edge = ((finalWinRate - 50.0f) * 10).roundToInt() / 10f,
            currentStreak = currentStreak,
            isCurrentStreakWin = isCurrentStreakWin,
            maxWinStreak = maxWinStreak,
            maxLossStreak = maxLossStreak,
            oddWins = oddWins,
            oddLosses = oddLosses,
            evenWins = evenWins,
            evenLosses = evenLosses,
            accuracyTimeline = timeline,
            topHitNumbers = topNumbers
        )
    }

    private fun predictProbabilityAtStep(
        history: List<NormalizedDraw>,
        modelType: ForecastModelType
    ): Float {
        val n = history.size
        if (n == 0) return 0.50f

        val lastResult = history.last().result

        return when (modelType) {
            ForecastModelType.BASELINE -> 0.50f

            ForecastModelType.HISTORICAL_FREQ -> {
                val oddCount = history.count { it.result == "ODD" }
                (oddCount.toFloat() / n).coerceIn(0.10f, 0.90f)
            }

            ForecastModelType.ROLLING_FREQ -> {
                val windowSize = min(30, n)
                val window = history.takeLast(windowSize)
                val oddCount = window.count { it.result == "ODD" }
                (oddCount.toFloat() / windowSize).coerceIn(0.10f, 0.90f)
            }

            ForecastModelType.MARKOV_TRANSITION -> {
                var oddToOdd = 0
                var oddToEven = 0
                var evenToOdd = 0
                var evenToEven = 0

                for (j in 0 until n - 1) {
                    val curr = history[j].result
                    val next = history[j + 1].result
                    if (curr == "ODD") {
                        if (next == "ODD") oddToOdd++ else oddToEven++
                    } else {
                        if (next == "ODD") evenToOdd++ else evenToEven++
                    }
                }

                val pOddGivenOdd = (oddToOdd + 1f) / (oddToOdd + oddToEven + 2f)
                val pOddGivenEven = (evenToOdd + 1f) / (evenToOdd + evenToEven + 2f)

                if (lastResult == "ODD") pOddGivenOdd else pOddGivenEven
            }

            ForecastModelType.COMBINED_SMOOTHED -> {
                val histOdd = history.count { it.result == "ODD" }
                val laplaceHist = (histOdd + 2f) / (n + 4f)

                val windowSize = min(30, n)
                val window = history.takeLast(windowSize)
                val rollingOdd = (window.count { it.result == "ODD" }.toFloat() / windowSize).coerceIn(0.15f, 0.85f)

                var oddToOdd = 0
                var oddToEven = 0
                var evenToOdd = 0
                var evenToEven = 0
                for (j in 0 until n - 1) {
                    val curr = history[j].result
                    val next = history[j + 1].result
                    if (curr == "ODD") {
                        if (next == "ODD") oddToOdd++ else oddToEven++
                    } else {
                        if (next == "ODD") evenToOdd++ else evenToEven++
                    }
                }
                val pMarkov = if (lastResult == "ODD") {
                    (oddToOdd + 1f) / (oddToOdd + oddToEven + 2f)
                } else {
                    (evenToOdd + 1f) / (evenToOdd + evenToEven + 2f)
                }

                ((laplaceHist * 0.20f) + (rollingOdd * 0.35f) + (pMarkov * 0.45f)).coerceIn(0.20f, 0.80f)
            }
        }
    }
}
