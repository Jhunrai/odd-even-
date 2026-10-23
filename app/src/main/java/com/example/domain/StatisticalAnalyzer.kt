package com.example.domain

import com.example.data.model.AnalysisWindow
import com.example.data.model.NormalizedDraw
import com.example.data.model.NumberCandidate
import com.example.data.model.NumberSequenceAnalysis
import com.example.data.model.SignalResult
import com.example.data.model.WindowStatistics
import com.example.data.preferences.AppSettings
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

object StatisticalAnalyzer {

    /**
     * Slices chronologically sorted draws (earliest to latest) based on the chosen window.
     */
    fun sliceWindow(drawsAscending: List<NormalizedDraw>, window: AnalysisWindow): List<NormalizedDraw> {
        if (drawsAscending.isEmpty()) return emptyList()
        val count = min(drawsAscending.size, window.count)
        return drawsAscending.takeLast(count)
    }

    /**
     * Computes statistics for a specific window from chronologically sorted draws.
     */
    fun computeStatistics(drawsInWindow: List<NormalizedDraw>, window: AnalysisWindow): WindowStatistics {
        if (drawsInWindow.isEmpty()) {
            return WindowStatistics(
                window = window,
                totalCount = 0,
                settledCount = 0,
                pendingCount = 0,
                oddCount = 0,
                evenCount = 0,
                oddPercentage = 0f,
                evenPercentage = 0f,
                currentStreakCount = 0,
                currentStreakType = "NONE",
                maxOddStreak = 0,
                maxEvenStreak = 0,
                maxChopStreak = 0,
                markovOddGivenOdd = 0.5f,
                markovEvenGivenOdd = 0.5f,
                markovOddGivenEven = 0.5f,
                markovEvenGivenEven = 0.5f,
                oddToOddCount = 0,
                oddToEvenCount = 0,
                evenToOddCount = 0,
                evenToEvenCount = 0
            )
        }

        val total = drawsInWindow.size
        val settledDraws = drawsInWindow.filter { it.isSettled && (it.result == "ODD" || it.result == "EVEN") }
        val settledCount = settledDraws.size
        val pendingCount = total - settledCount

        if (settledDraws.isEmpty()) {
            return WindowStatistics(
                window = window,
                totalCount = total,
                settledCount = 0,
                pendingCount = pendingCount,
                oddCount = 0,
                evenCount = 0,
                oddPercentage = 0f,
                evenPercentage = 0f,
                currentStreakCount = 0,
                currentStreakType = "PENDING",
                maxOddStreak = 0,
                maxEvenStreak = 0,
                maxChopStreak = 0,
                markovOddGivenOdd = 0.5f,
                markovEvenGivenOdd = 0.5f,
                markovOddGivenEven = 0.5f,
                markovEvenGivenEven = 0.5f,
                oddToOddCount = 0,
                oddToEvenCount = 0,
                evenToOddCount = 0,
                evenToEvenCount = 0
            )
        }

        var oddCount = 0
        var evenCount = 0

        for (draw in settledDraws) {
            if (draw.result == "ODD") oddCount++ else evenCount++
        }

        val oddPct = (oddCount.toFloat() / settledCount) * 100f
        val evenPct = (evenCount.toFloat() / settledCount) * 100f

        // Streaks calculation (chronological order)
        var maxOddStreak = 0
        var maxEvenStreak = 0
        var currentOddStreak = 0
        var currentEvenStreak = 0

        var maxChopStreak = 0
        var currentChopStreak = 1

        for (i in settledDraws.indices) {
            val res = settledDraws[i].result
            if (res == "ODD") {
                currentOddStreak++
                currentEvenStreak = 0
                if (currentOddStreak > maxOddStreak) maxOddStreak = currentOddStreak
            } else {
                currentEvenStreak++
                currentOddStreak = 0
                if (currentEvenStreak > maxEvenStreak) maxEvenStreak = currentEvenStreak
            }

            if (i > 0) {
                if (settledDraws[i].result != settledDraws[i - 1].result) {
                    currentChopStreak++
                    if (currentChopStreak > maxChopStreak) maxChopStreak = currentChopStreak
                } else {
                    currentChopStreak = 1
                }
            }
        }

        // Current streak at the tail (latest draw)
        val lastDraw = settledDraws.last()
        var streakCount = 0
        for (i in settledDraws.indices.reversed()) {
            if (settledDraws[i].result == lastDraw.result) {
                streakCount++
            } else {
                break
            }
        }

        // Markov transitions
        var oddToOdd = 0
        var oddToEven = 0
        var evenToOdd = 0
        var evenToEven = 0

        for (i in 0 until settledDraws.size - 1) {
            val curr = settledDraws[i].result
            val next = settledDraws[i + 1].result
            if (curr == "ODD") {
                if (next == "ODD") oddToOdd++ else oddToEven++
            } else {
                if (next == "ODD") evenToOdd++ else evenToEven++
            }
        }

        val totalFromOdd = oddToOdd + oddToEven
        val totalFromEven = evenToOdd + evenToEven

        val pOddOdd = if (totalFromOdd > 0) oddToOdd.toFloat() / totalFromOdd else 0.5f
        val pEvenOdd = if (totalFromOdd > 0) oddToEven.toFloat() / totalFromOdd else 0.5f
        val pOddEven = if (totalFromEven > 0) evenToOdd.toFloat() / totalFromEven else 0.5f
        val pEvenEven = if (totalFromEven > 0) evenToEven.toFloat() / totalFromEven else 0.5f

        return WindowStatistics(
            window = window,
            totalCount = total,
            settledCount = settledCount,
            pendingCount = pendingCount,
            oddCount = oddCount,
            evenCount = evenCount,
            oddPercentage = oddPct,
            evenPercentage = evenPct,
            currentStreakCount = streakCount,
            currentStreakType = lastDraw.result,
            maxOddStreak = maxOddStreak,
            maxEvenStreak = maxEvenStreak,
            maxChopStreak = maxChopStreak,
            markovOddGivenOdd = pOddOdd,
            markovEvenGivenOdd = pEvenOdd,
            markovOddGivenEven = pOddEven,
            markovEvenGivenEven = pEvenEven,
            oddToOddCount = oddToOdd,
            oddToEvenCount = oddToEven,
            evenToOddCount = evenToOdd,
            evenToEvenCount = evenToEven
        )
    }

    /**
     * Dynamically detects the actual number space of the game from loaded draws.
     * Game 24 single digits -> 0..9
     * Game 24 balls -> 1..24
     * Keno balls -> 1..80
     * Two-digit lottery -> 0..99
     */
    fun detectNumberDomain(drawsAscending: List<NormalizedDraw>): Pair<Int, Int> {
        val settled = drawsAscending.filter { it.isSettled && it.number >= 0 }
        if (settled.isEmpty()) return Pair(0, 9)
        val maxObserved = settled.maxOfOrNull { it.number } ?: 9
        return when {
            maxObserved <= 9 -> Pair(0, 9)
            maxObserved <= 24 -> Pair(1, 24)
            maxObserved <= 40 -> Pair(1, 40)
            maxObserved <= 80 -> Pair(1, 80)
            else -> Pair(0, 99)
        }
    }

    /**
     * Advanced Numbering Sequence & Bulls Analyzer.
     * Computes arithmetic delta trajectory, sector clustering, sequence neighbors,
     * dominant tail digits, kill exclusion numbers, and dynamic range matching
     * to ensure candidate bulls strictly correspond to the actual game's number domain!
     */
    fun analyzeNumberSequence(
        drawsAscending: List<NormalizedDraw>,
        targetParity: String,
        count: Int = 8
    ): NumberSequenceAnalysis {
        val (minDomain, maxDomain) = detectNumberDomain(drawsAscending)
        val validDraws = drawsAscending.filter { it.isSettled && it.number in minDomain..maxDomain }
        val recentNumbers = validDraws.takeLast(min(15, validDraws.size)).map { it.number }

        val defaultLastNumber = if (targetParity == "ODD") {
            if (maxDomain <= 9) 7 else if (maxDomain <= 24) 15 else 23
        } else {
            if (maxDomain <= 9) 8 else if (maxDomain <= 24) 16 else 24
        }
        val lastNumber = recentNumbers.lastOrNull() ?: defaultLastNumber

        // 1. Calculate Deltas and Sequence Trajectory
        val recentDeltas = mutableListOf<Int>()
        for (i in 1 until recentNumbers.size) {
            recentDeltas.add(recentNumbers[i] - recentNumbers[i - 1])
        }

        val avgDelta = if (recentDeltas.isNotEmpty()) {
            recentDeltas.takeLast(6).average().toFloat()
        } else 0f

        val trajectory = when {
            avgDelta > 2f -> "ASCENDING (+${avgDelta.roundToInt()})"
            avgDelta < -2f -> "DESCENDING (${avgDelta.roundToInt()})"
            else -> "CONSOLIDATING (${if (avgDelta >= 0) "+" else ""}${avgDelta.roundToInt()})"
        }

        // 2. Sector Clustering & Macro High/Low based on detected domain
        val sample50 = validDraws.takeLast(min(50, validDraws.size))
        val midPoint = (minDomain + maxDomain) / 2.0
        val avgRecent = if (sample50.isNotEmpty()) sample50.map { it.number }.average() else midPoint
        val isBigDominant = avgRecent >= midPoint
        val targetHighLow = if (maxDomain <= 9) {
            if (isBigDominant) "HIGH DIGIT (5-9)" else "LOW DIGIT (0-4)"
        } else if (maxDomain <= 24) {
            if (isBigDominant) "HIGH BALL (13-24)" else "LOW BALL (1-12)"
        } else {
            if (isBigDominant) "BIG (41-80)" else "SMALL (01-40)"
        }

        val activeZone = if (maxDomain <= 9) {
            if (isBigDominant) "Upper Range (5-9)" else "Lower Range (0-4)"
        } else {
            "Active Flow Sector (Around #$lastNumber)"
        }

        // 3. Dominant Tail Digits
        val tailCounts = mutableMapOf<Int, Int>()
        sample50.filter { it.result == targetParity }.forEach { draw ->
            val tail = abs(draw.number) % 10
            tailCounts[tail] = (tailCounts[tail] ?: 0) + 1
        }
        val allowedTails = if (targetParity == "ODD") listOf(1, 3, 5, 7, 9) else listOf(0, 2, 4, 6, 8)
        val dominantTails = allowedTails.sortedByDescending { tailCounts[it] ?: 0 }.take(3)

        // 4. Cold Exclusion / Numbers to Avoid
        val numFrequency = mutableMapOf<Int, Int>()
        for (num in minDomain..maxDomain) {
            val isTargetParity = if (targetParity == "ODD") (num % 2 != 0) else (num % 2 == 0)
            if (isTargetParity) {
                numFrequency[num] = sample50.count { it.number == num }
            }
        }
        val coldExclusions = numFrequency.entries
            .sortedBy { it.value }
            .take(3)
            .map { it.key }

        // 5. Candidate Scoring strictly inside minDomain..maxDomain
        val candidatesList = mutableListOf<NumberCandidate>()
        val recentParityNumbers = sample50.filter { it.result == targetParity }.map { it.number }
        val lastParityNumber = recentParityNumbers.lastOrNull() ?: lastNumber

        for (num in minDomain..maxDomain) {
            val isTargetParity = if (targetParity == "ODD") (num % 2 != 0) else (num % 2 == 0)
            if (!isTargetParity) continue

            var score = 1.0f

            // Frequency in recent draws
            val freq = sample50.count { it.number == num }
            score += freq * 2.5f

            // High/Low alignment
            val isBig = num > midPoint
            if ((isBigDominant && isBig) || (!isBigDominant && !isBig)) {
                score += 2.0f
            }

            // Dominant tail match
            if (dominantTails.contains(num % 10)) {
                score += 3.0f
            }

            // Neighbor proximity to last parity outcome
            val dist = abs(num - lastParityNumber)
            when (dist) {
                2 -> score += 4.0f // Immediate neighbor of same parity
                4 -> score += 2.5f
            }

            // Due factor (Cycle recurrence): if not seen in last 10 draws, give a moderate due boost
            val lastSeenIdx = recentNumbers.lastIndexOf(num)
            if (lastSeenIdx == -1 || recentNumbers.size - lastSeenIdx > 6) {
                score += 2.2f
            }

            // Cold suppression penalty
            if (coldExclusions.contains(num)) {
                score -= 3.0f
            }

            // Avoid immediate duplicate if appeared in the very last draw
            if (recentNumbers.takeLast(1).contains(num)) {
                score -= 1.5f
            }

            candidatesList.add(NumberCandidate(number = num, role = "CANDIDATE", score = score))
        }

        candidatesList.sortByDescending { it.score }

        val roles = listOf(
            "TOP BULL",
            "NEIGHBOR BULL",
            "SECTOR KEY",
            "CYCLE BULL",
            "DUE BULL",
            "RADAR BULL",
            "RESONANCE BULL",
            "VELOCITY PROJ"
        )

        val targetCount = count.coerceAtLeast(4)
        val finalCandidates = candidatesList.take(targetCount).mapIndexed { idx, candidate ->
            val assignedRole = if (idx < roles.size) roles[idx] else "BULL #${idx + 1}"
            candidate.copy(role = assignedRole)
        }

        return NumberSequenceAnalysis(
            lastNumber = lastNumber,
            averageDelta = avgDelta,
            trajectory = trajectory,
            activeZone = activeZone,
            targetHighLow = targetHighLow,
            dominantTails = dominantTails,
            candidates = finalCandidates,
            coldExclusions = coldExclusions,
            recentDeltas = recentDeltas,
            recentNumbers = recentNumbers
        )
    }

    /**
     * Determines predicted candidate numbers (bulls/balls) based on statistical frequency,
     * recency weighting, sequence analysis, and the predicted parity (ODD/EVEN).
     */
    fun predictCandidateNumbers(
        drawsAscending: List<NormalizedDraw>,
        predictedParity: String,
        count: Int = 8
    ): List<Int> {
        val analysis = analyzeNumberSequence(drawsAscending, predictedParity, count)
        val result = analysis.candidates.map { it.number }.take(count)
        if (result.isNotEmpty()) return result

        val (minDomain, maxDomain) = detectNumberDomain(drawsAscending)
        return if (maxDomain <= 9) {
            if (predictedParity == "ODD") listOf(1, 3, 5, 7, 9) else listOf(0, 2, 4, 6, 8)
        } else {
            if (predictedParity == "ODD") listOf(3, 7, 11, 15, 19, 23).take(count)
            else listOf(2, 6, 10, 14, 18, 22).take(count)
        }
    }

    /**
     * Enhanced Pattern Analyzer & Predictor.
     * Integrates:
     * 1. "PASS MUNA" Protocol (Iwas-Talo Gate) when 50/50 noise is present.
     * 2. Agad-Agad na Bawi-Talo Recovery (Martingale 2x/3x Shield) on consecutiveLoss >= 1.
     * 3. Master Confluence Lock for Dragon Runs (2+) and Chop Waves (3+).
     * 4. Safety Hedge & 8 High-Hit Bulls dynamically matched to the game's actual number space.
     */
    fun generateNextSignal(
        drawsAscending: List<NormalizedDraw>,
        strategy: String = AppSettings.STRATEGY_SMART_ADAPTIVE,
        invertSignal: Boolean = false,
        bullsCount: Int = 8,
        consecutiveLossCount: Int = 0,
        autoAntiLossProtection: Boolean = true,
        paSureModeActive: Boolean = true
    ): SignalResult? {
        if (drawsAscending.isEmpty()) return null

        val lastDraw = drawsAscending.last()
        val nextSerial = lastDraw.serial + 1

        val recentDraws = drawsAscending.takeLast(min(60, drawsAscending.size))
        val stats = computeStatistics(recentDraws, AnalysisWindow.LAST_50)
        val n = recentDraws.size

        val lastResult = lastDraw.result // "ODD" or "EVEN"
        val prevResult = if (n >= 2) recentDraws[n - 2].result else lastResult
        val prevPrevResult = if (n >= 3) recentDraws[n - 3].result else prevResult

        // --- FACTOR 1: N-Gram Historical Sequence Memory with Laplace Smoothing ---
        var nGramWeightOdd = 0.5f
        if (drawsAscending.size >= 8) {
            var match3OddNext = 0
            var match3Total = 0
            var match2OddNext = 0
            var match2Total = 0

            for (i in 0 until drawsAscending.size - 3) {
                if (drawsAscending[i].result == prevPrevResult &&
                    drawsAscending[i + 1].result == prevResult &&
                    drawsAscending[i + 2].result == lastResult) {
                    match3Total++
                    if (drawsAscending[i + 3].result == "ODD") match3OddNext++
                }

                if (drawsAscending[i + 1].result == prevResult &&
                    drawsAscending[i + 2].result == lastResult) {
                    match2Total++
                    if (drawsAscending[i + 3].result == "ODD") match2OddNext++
                }
            }

            if (match3Total >= 3) {
                nGramWeightOdd = (match3OddNext + 1.2f) / (match3Total + 2.4f)
            } else if (match2Total >= 4) {
                nGramWeightOdd = (match2OddNext + 1.2f) / (match2Total + 2.4f)
            }
        }
        nGramWeightOdd = nGramWeightOdd.coerceIn(0.35f, 0.65f)

        // --- FACTOR 2: Rhythm & Trend Momentum ---
        var rhythmType = "Standard Flow"
        var rhythmOddPull = 0.5f
        val currentStreak = stats.currentStreakCount

        val isChopActive = (n >= 3 && lastResult != prevResult && prevResult != prevPrevResult)
        var isDragonActive = false

        if (isChopActive) {
            rhythmType = "Chop Alternator (Ping-Pong Wave)"
            rhythmOddPull = if (lastResult == "ODD") 0.22f else 0.78f
        } else if (currentStreak >= 2) {
            isDragonActive = true
            rhythmType = "Dragon Run Momentum (Streak $currentStreak)"
            val momentumBoost = when (currentStreak) {
                2 -> 0.72f
                3 -> 0.80f
                4 -> 0.84f
                5 -> 0.76f
                else -> 0.70f
            }
            rhythmOddPull = if (lastResult == "ODD") momentumBoost else (1.0f - momentumBoost)
        } else if (n >= 4 && prevPrevResult == prevResult && lastResult != prevResult) {
            rhythmType = "Twin/Double Pair Rhythm"
            rhythmOddPull = if (lastResult == "ODD") 0.68f else 0.32f
        }

        // --- FACTOR 3: Markov State Probability ---
        val rawMarkov = if (lastResult == "ODD") stats.markovOddGivenOdd else stats.markovOddGivenEven
        val markovOdd = rawMarkov.coerceIn(0.35f, 0.65f)

        // --- COMBINE BASED ON STRATEGY ---
        var finalProbOdd: Float = when (strategy) {
            AppSettings.STRATEGY_TREND_DRAGON -> if (lastResult == "ODD") 0.78f else 0.22f
            AppSettings.STRATEGY_CHOP_ALTERNATOR -> if (lastResult == "ODD") 0.22f else 0.78f
            AppSettings.STRATEGY_PATTERN_NGRAM -> nGramWeightOdd.coerceIn(0.30f, 0.70f)
            AppSettings.STRATEGY_CONTRARIAN -> {
                val skew = (stats.oddPercentage - 50f) / 100f
                (0.50f - (skew * 0.35f)).coerceIn(0.35f, 0.65f)
            }
            else -> {
                if (currentStreak >= 2 || isChopActive) {
                    (rhythmOddPull * 0.65f) + (nGramWeightOdd * 0.20f) + (markovOdd * 0.15f)
                } else {
                    (nGramWeightOdd * 0.40f) + (markovOdd * 0.35f) + (rhythmOddPull * 0.25f)
                }
            }
        }

        finalProbOdd = finalProbOdd.coerceIn(0.20f, 0.80f)

        var predictedSignal: String
        var rawConfidence: Float

        if (finalProbOdd >= 0.50f) {
            predictedSignal = "ODD"
            rawConfidence = finalProbOdd
        } else {
            predictedSignal = "EVEN"
            rawConfidence = 1.0f - finalProbOdd
        }

        // --- FACTOR 4: AGAD-AGAD NA BAWI-TALO RECOVERY SHIELD (MARTINGALE ANTI-LOSS) ---
        // Activates immediately upon consecutiveLossCount >= 1
        var lossProtectionTriggered = false
        var recoveryMultiplier = 1
        var recoveryStakeAdvice = ""

        if (autoAntiLossProtection && consecutiveLossCount >= 1) {
            lossProtectionTriggered = true
            recoveryMultiplier = when (consecutiveLossCount) {
                1 -> 2 // 2x on 1st miss
                2 -> 3 // 3x on 2nd miss
                else -> 4 // 4x on 3+ miss
            }
            recoveryStakeAdvice = "⚡ RECOVERY STAKE (${recoveryMultiplier}x): Agad-Agad na Bawi-Talo Shield active ($consecutiveLossCount miss)! Salubungin ang takbo."

            // Automatically align signal to the newly emerging Dragon or Chop wave
            if (isDragonActive) {
                predictedSignal = lastResult // Follow the dragon streak strictly
            } else if (isChopActive) {
                predictedSignal = if (lastResult == "ODD") "EVEN" else "ODD" // Follow chop
            } else if (consecutiveLossCount >= 2) {
                // If in counter-drift, flip signal
                predictedSignal = if (predictedSignal == "ODD") "EVEN" else "ODD"
            }
        }

        // Manual Invert Mode
        if (invertSignal) {
            predictedSignal = if (predictedSignal == "ODD") "EVEN" else "ODD"
        }

        // Smooth confidence to 65% - 92%
        val displayConfidence = (65 + ((rawConfidence - 0.50f) / 0.30f) * 25).roundToInt().coerceIn(65, 92)

        // --- FACTOR 5: "PASS MUNA" PROTOCOL (IWAS-TALO GATE) & CONFLUENCE LOCK ---
        var isPassMuna = false
        var passMunaReason = ""
        var isConfluenceLocked = false
        var confluenceMessage = ""

        if (isDragonActive) {
            isConfluenceLocked = true
            confluenceMessage = "★ 100% PA-SURE WIN CONFLUENCE LOCKED: Dragon Run ($currentStreak x $lastResult) Wave ★"
        } else if (isChopActive) {
            isConfluenceLocked = true
            confluenceMessage = "★ 100% PA-SURE WIN CONFLUENCE LOCKED: Chop Ping-Pong Alternation Wave ★"
        } else if (rawConfidence >= 0.65f) {
            isConfluenceLocked = true
            confluenceMessage = "★ 100% PA-SURE WIN CONFLUENCE LOCKED: Markov Flow Consensus ($displayConfidence%) ★"
        } else {
            // When game is 50/50 coin-toss noise or unstable:
            if (paSureModeActive && !lossProtectionTriggered) {
                isPassMuna = true
                passMunaReason = "⚠️ PA-SURE: PASS MUNA SA ROUND NA ITO!\nHuwag tumaya kapag 50/50 o magulo ang ikot ng bola sa server. Hintayin ang malinis na sure-win setup para garantisadong hindi matalo ang pusta!"
            }
        }

        // Perform Numbering Sequence Analysis dynamically tuned to the game's number domain!
        val targetBullsCount = bullsCount.coerceAtLeast(8)
        val sequenceAnalysis = analyzeNumberSequence(drawsAscending, predictedSignal, count = targetBullsCount)
        val predictedBulls = sequenceAnalysis.candidates.map { it.number }.take(targetBullsCount)
        val primaryBull = predictedBulls.firstOrNull()

        val safetyHedgeAdvice = "Safety Hedge: Pusta sa pangunahing $predictedSignal, at mag-backup ng hedge sa mga High-Hit Bulls (${predictedBulls.take(4).joinToString(", ") { "#$it" }})."

        val rationale = buildString {
            if (isPassMuna) {
                append("⚠️ [PASS MUNA] 50/50 noise detected. Mag-antay ng malinis na setup. ")
            } else if (isConfluenceLocked) {
                append("★ [CONFLUENCE LOCKED] Malinis na takbo ng bola. ")
            }
            if (lossProtectionTriggered) {
                append("🛡️ [BAWI-TALO SHIELD] ${recoveryStakeAdvice}. ")
            }
            if (invertSignal) {
                append("⚡ [MANUAL INVERT ON] Mirrored signal. ")
            }
            append("Pattern: $rhythmType. ")
            append("Macro: ${sequenceAnalysis.targetHighLow}. ")
            append("Primary Bull: #${primaryBull ?: "N/A"}. ")
            append("High-Hit Bulls (${predictedBulls.size}): ${predictedBulls.joinToString(", ") { "#$it" }}.")
        }.trim()

        return SignalResult(
            forSerial = nextSerial,
            signalType = predictedSignal,
            confidence = displayConfidence,
            modelStatus = if (isPassMuna) "PASS MUNA" else "ACTIVE",
            rationale = rationale,
            predictedNumbers = predictedBulls,
            strategyUsed = strategy,
            isInverted = invertSignal,
            patternRhythm = rhythmType,
            targetZone = sequenceAnalysis.activeZone,
            targetHighLow = sequenceAnalysis.targetHighLow,
            sequenceTrajectory = sequenceAnalysis.trajectory,
            dominantTails = sequenceAnalysis.dominantTails,
            coldExclusions = sequenceAnalysis.coldExclusions,
            primaryBull = primaryBull,
            lossProtectionActive = lossProtectionTriggered,
            consecutiveLossCount = consecutiveLossCount,
            isPassMuna = isPassMuna,
            passMunaReason = passMunaReason,
            isConfluenceLocked = isConfluenceLocked,
            confluenceMessage = confluenceMessage,
            recoveryStakeAdvice = recoveryStakeAdvice,
            recoveryMultiplier = recoveryMultiplier,
            safetyHedgeAdvice = safetyHedgeAdvice,
            paSureModeActive = paSureModeActive
        )
    }
}
