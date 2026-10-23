package com.example.domain

import com.example.data.model.ForecastModelType
import com.example.data.model.NormalizedDraw
import com.example.data.model.ProbabilityForecast
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.roundToInt

object ForecastEngine {

    /**
     * Minimum settled draws required before a statistically meaningful forecast can be generated.
     */
    const val MINIMUM_SETTLED_DRAWS = 10

    /**
     * Generates transparent probability estimation for the next upcoming draw
     * using the specified forecast model over verified settled draws.
     */
    fun generateForecast(
        drawsAscending: List<NormalizedDraw>,
        modelType: ForecastModelType = ForecastModelType.COMBINED_SMOOTHED,
        rollingWindowSize: Int = 50
    ): ProbabilityForecast {
        // Filter strictly verified settled draws (no pending records in training)
        val settledDraws = drawsAscending.filter { it.isSettled && (it.result == "ODD" || it.result == "EVEN") }
        val lastTimestamp = settledDraws.lastOrNull()?.timestamp ?: "N/A"

        if (settledDraws.size < MINIMUM_SETTLED_DRAWS) {
            return ProbabilityForecast(
                modelType = modelType,
                estimatedOddProb = 0.50f,
                estimatedEvenProb = 0.50f,
                mostLikelyCategory = "INSUFFICIENT DATA",
                confidence = 0f,
                dataWindow = settledDraws.size,
                lastDataTimestamp = lastTimestamp,
                isSufficientEvidence = false,
                insufficientReason = "INSUFFICIENT EVIDENCE FOR A RELIABLE PREDICTION. At least $MINIMUM_SETTLED_DRAWS verified settled records required (current: ${settledDraws.size})."
            )
        }

        val totalSettled = settledDraws.size
        val lastDraw = settledDraws.last()
        val lastResult = lastDraw.result

        // 1. Uniform Baseline Model (50% / 50%)
        val baselineOdd = 0.50f

        // 2. Historical Frequency Model: P(Odd) = n_odd / N
        val historicalOddCount = settledDraws.count { it.result == "ODD" }
        val historicalOddProb = (historicalOddCount.toFloat() / totalSettled).coerceIn(0.05f, 0.95f)

        // 3. Rolling-Window Frequency Model
        val rollingWindow = settledDraws.takeLast(min(rollingWindowSize, totalSettled))
        val rollingOddCount = rollingWindow.count { it.result == "ODD" }
        val rollingOddProb = (rollingOddCount.toFloat() / rollingWindow.size).coerceIn(0.10f, 0.90f)

        // 4. First-Order Markov Transition Model with Laplace Smoothing
        var oddToOdd = 0
        var oddToEven = 0
        var evenToOdd = 0
        var evenToEven = 0

        for (i in 0 until totalSettled - 1) {
            val curr = settledDraws[i].result
            val next = settledDraws[i + 1].result
            if (curr == "ODD") {
                if (next == "ODD") oddToOdd++ else oddToEven++
            } else {
                if (next == "ODD") evenToOdd++ else evenToEven++
            }
        }

        // Laplace smoothed transitions: (k + 1) / (n + 2)
        val pOddGivenOdd = (oddToOdd + 1f) / (oddToOdd + oddToEven + 2f)
        val pOddGivenEven = (evenToOdd + 1f) / (evenToOdd + evenToEven + 2f)

        val markovOddProb = if (lastResult == "ODD") pOddGivenOdd else pOddGivenEven

        // 5. Combined Model with Bayesian / Laplace Smoothing
        // Blends global historical prior (smoothed with alpha=2, beta=2), rolling window, and Markov transition
        val laplaceHistorical = (historicalOddCount + 2f) / (totalSettled + 4f)
        val combinedOddProb = (
            (laplaceHistorical * 0.20f) +
            (rollingOddProb * 0.35f) +
            (markovOddProb * 0.45f)
        ).coerceIn(0.20f, 0.80f)

        // Select probability based on requested model
        val finalOddProb = when (modelType) {
            ForecastModelType.BASELINE -> baselineOdd
            ForecastModelType.HISTORICAL_FREQ -> historicalOddProb
            ForecastModelType.ROLLING_FREQ -> rollingOddProb
            ForecastModelType.MARKOV_TRANSITION -> markovOddProb
            ForecastModelType.COMBINED_SMOOTHED -> combinedOddProb
        }

        val finalEvenProb = 1.0f - finalOddProb
        val mostLikely = when {
            finalOddProb > 0.505f -> "ODD"
            finalOddProb < 0.495f -> "EVEN"
            else -> "50/50 NEUTRAL"
        }

        // Calibrated confidence reflects evidence deviation from baseline 0.50
        val diffFromNeutral = abs(finalOddProb - 0.50f)
        val confidenceScore = (50f + (diffFromNeutral * 100f)).coerceIn(50f, 85f)

        // Quick walk-forward measure on recent 30 draws
        val quickEval = BacktestingEngine.runWalkForwardBacktest(
            drawsAscending = settledDraws,
            modelType = modelType,
            trainingWindow = min(30, totalSettled / 2).coerceAtLeast(10)
        )

        return ProbabilityForecast(
            modelType = modelType,
            estimatedOddProb = finalOddProb,
            estimatedEvenProb = finalEvenProb,
            mostLikelyCategory = mostLikely,
            confidence = (confidenceScore * 10).roundToInt() / 10f,
            dataWindow = totalSettled,
            lastDataTimestamp = lastTimestamp,
            isSufficientEvidence = true,
            backtestedAccuracy = quickEval.actualAccuracy,
            backtestedBrierScore = quickEval.brierScore,
            backtestedLogLoss = quickEval.logLoss,
            limitations = "Walk-forward verified over ${quickEval.evaluatedCount} recent draws. Does not claim a guaranteed win."
        )
    }

    /**
     * Compares all 5 forecasting models simultaneously on current settled data.
     */
    fun compareAllModels(
        drawsAscending: List<NormalizedDraw>,
        rollingWindowSize: Int = 50
    ): List<ProbabilityForecast> {
        return ForecastModelType.values().map { type ->
            generateForecast(drawsAscending, type, rollingWindowSize)
        }
    }
}
