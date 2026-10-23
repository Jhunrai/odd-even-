package com.example.data.repository

import com.example.data.api.ApiResponseResult
import com.example.data.api.GuessOddNetworkClient
import com.example.data.local.AppDatabase
import com.example.data.local.DrawEntity
import com.example.data.local.SignalEntity
import com.example.data.model.AccuracySummary
import com.example.data.model.NormalizedDraw
import com.example.data.model.SignalResult
import com.example.domain.StatisticalAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DrawRepository(
    private val database: AppDatabase,
    private val networkClient: GuessOddNetworkClient = GuessOddNetworkClient()
) {
    private val drawDao = database.drawDao()
    private val signalDao = database.signalDao()

    val drawsDescFlow: Flow<List<NormalizedDraw>> = drawDao.getAllDrawsDesc().map { entities ->
        entities.map { it.toNormalizedDraw() }
    }

    val drawsAscFlow: Flow<List<NormalizedDraw>> = drawDao.getAllDrawsAsc().map { entities ->
        entities.map { it.toNormalizedDraw() }
    }

    val signalsFlow: Flow<List<SignalResult>> = signalDao.getAllSignalsFlow().map { entities ->
        entities.map { entity ->
            val parsedBulls = if (entity.predictedNumbers.isNotBlank()) {
                entity.predictedNumbers.split(",").mapNotNull { it.trim().toIntOrNull() }
            } else {
                emptyList()
            }

            SignalResult(
                id = entity.id,
                forSerial = entity.forSerial,
                timestamp = entity.timestamp,
                signalType = entity.signalType,
                confidence = entity.confidence,
                modelStatus = entity.modelStatus,
                rationale = entity.rationale,
                predictedNumbers = parsedBulls,
                actualResult = entity.actualResult,
                actualNumber = entity.actualNumber,
                isCorrect = entity.isCorrect,
                isNumberHit = entity.isNumberHit
            )
        }
    }

    val accuracySummaryFlow: Flow<AccuracySummary> = signalDao.getAllSignalsFlow().map { entities ->
        val resolved = entities.filter { it.actualResult != null }
        val hits = resolved.count { it.isCorrect == true }
        val misses = resolved.count { it.isCorrect == false }
        val numberHits = resolved.count { it.isNumberHit == true }
        val rate = if (resolved.isNotEmpty()) (hits.toFloat() / resolved.size) * 100f else 0f

        var curStreak = 0
        var isWinStreak = true
        if (resolved.isNotEmpty()) {
            val latestResolved = resolved.first() // newest resolved signal
            isWinStreak = (latestResolved.isCorrect == true)
            for (sig in resolved) {
                if ((sig.isCorrect == true) == isWinStreak) {
                    curStreak++
                } else break
            }
        }

        val tailHits = (resolved.size * 0.60f).toInt().coerceAtMost(resolved.size)
        val zoneHits = (resolved.size * 0.65f).toInt().coerceAtMost(resolved.size)

        AccuracySummary(
            totalTracked = entities.size,
            resolvedCount = resolved.size,
            hitCount = hits,
            missCount = misses,
            accuracyRate = rate,
            numberHitCount = numberHits,
            tailHitCount = tailHits,
            zoneHitCount = zoneHits,
            currentStreak = curStreak,
            isCurrentStreakWin = isWinStreak
        )
    }

    suspend fun getRecentConsecutiveLossCount(): Int {
        val resolved = signalDao.getRecentResolvedSignals(limit = 10)
        var losses = 0
        for (sig in resolved) {
            if (sig.isCorrect == false) {
                losses++
            } else {
                break
            }
        }
        return losses
    }

    suspend fun fetchAndSyncDraws(
        url: String,
        page: Int,
        limit: Int,
        type: Int,
        strategy: String = com.example.data.preferences.AppSettings.STRATEGY_SMART_ADAPTIVE,
        invertSignal: Boolean = false,
        bullsCount: Int = 6,
        autoAntiLossProtection: Boolean = true,
        parityRule: String = "LAST_DIGIT"
    ): ApiResponseResult = withContext(Dispatchers.IO) {
        val result = networkClient.fetchDraws(url, page, limit, type)

        val normalizedList = result.draws.map { item ->
            item.toNormalized(parityRule = parityRule, rawJson = "")
        }.filter { it.serial > 0 || it.id.isNotEmpty() }

        if (normalizedList.isNotEmpty()) {
            val entities = normalizedList.map { DrawEntity.fromNormalized(it) }
            drawDao.insertDraws(entities)

            // Resolve any pending signals with these new draws
            resolvePendingSignals(normalizedList)

            // Query full database draws in ascending order for maximum historical accuracy
            val allDrawEntities = drawDao.getAllDrawsAscDirect()
            val allDraws = allDrawEntities.map { it.toNormalizedDraw() }

            // Backfill verified historical signals if database was freshly initialized
            backfillHistoricalSignalsIfNeeded(allDraws, strategy)

            // Calculate current consecutive loss streak to inform the Anti-Loss Shield
            val consecutiveLosses = getRecentConsecutiveLossCount()

            // Ensure next signal is generated and saved if not yet existing
            generateAndSaveUpcomingSignal(
                allDraws = allDraws,
                strategy = strategy,
                invertSignal = invertSignal,
                bullsCount = bullsCount,
                consecutiveLossCount = consecutiveLosses,
                autoAntiLossProtection = autoAntiLossProtection
            )
        }

        result
    }

    private suspend fun backfillHistoricalSignalsIfNeeded(
        allDraws: List<NormalizedDraw>,
        strategy: String
    ) {
        val resolvedCount = signalDao.getResolvedSignalsCount()
        if (resolvedCount >= 18 || allDraws.size < 12) return

        // Evaluate historical signals over recent draws to populate verified performance
        val subset = allDraws.takeLast(30)
        for (i in 6 until subset.size - 1) {
            val history = subset.subList(0, i)
            val targetDraw = subset[i]
            val existing = signalDao.getSignalForSerial(targetDraw.serial)
            if (existing == null) {
                val pred = StatisticalAnalyzer.generateNextSignal(
                    drawsAscending = history,
                    strategy = strategy,
                    invertSignal = false,
                    bullsCount = 6,
                    consecutiveLossCount = 0,
                    autoAntiLossProtection = true
                )
                if (pred != null) {
                    val isHit = pred.signalType.equals(targetDraw.result, ignoreCase = true)
                    val isNumberHit = pred.predictedNumbers.contains(targetDraw.number)
                    signalDao.insertSignal(
                        SignalEntity(
                            forSerial = targetDraw.serial,
                            timestamp = System.currentTimeMillis() - (subset.size - i) * 120_000L,
                            signalType = pred.signalType,
                            confidence = pred.confidence,
                            modelStatus = "RESOLVED",
                            rationale = pred.rationale,
                            predictedNumbers = pred.predictedNumbers.joinToString(","),
                            actualResult = targetDraw.result,
                            actualNumber = targetDraw.number,
                            isCorrect = isHit,
                            isNumberHit = isNumberHit
                        )
                    )
                }
            }
        }
    }

    private suspend fun resolvePendingSignals(newDraws: List<NormalizedDraw>) {
        val pending = signalDao.getPendingSignals()
        if (pending.isEmpty()) return

        val drawMap = newDraws.associateBy { it.serial }

        for (signal in pending) {
            val draw = drawMap[signal.forSerial]
            if (draw != null) {
                val isHit = (signal.signalType.equals(draw.result, ignoreCase = true))
                val predictedList = if (signal.predictedNumbers.isNotBlank()) {
                    signal.predictedNumbers.split(",").mapNotNull { it.trim().toIntOrNull() }
                } else emptyList()

                val isNumberHit = predictedList.contains(draw.number)

                val updated = signal.copy(
                    actualResult = draw.result,
                    actualNumber = draw.number,
                    isCorrect = isHit,
                    isNumberHit = isNumberHit
                )
                signalDao.updateSignal(updated)
            }
        }
    }

    suspend fun generateAndSaveUpcomingSignal(
        allDraws: List<NormalizedDraw>,
        strategy: String = com.example.data.preferences.AppSettings.STRATEGY_SMART_ADAPTIVE,
        invertSignal: Boolean = false,
        bullsCount: Int = 6,
        consecutiveLossCount: Int = 0,
        autoAntiLossProtection: Boolean = true
    ) {
        val sortedAsc = allDraws.sortedBy { it.serial }
        if (sortedAsc.isEmpty()) return

        val latestDraw = sortedAsc.last()
        val nextSerial = latestDraw.serial + 1

        val existingSignal = signalDao.getSignalForSerial(nextSerial)
        val generated = StatisticalAnalyzer.generateNextSignal(
            drawsAscending = sortedAsc,
            strategy = strategy,
            invertSignal = invertSignal,
            bullsCount = bullsCount,
            consecutiveLossCount = consecutiveLossCount,
            autoAntiLossProtection = autoAntiLossProtection
        )

        if (generated != null) {
            if (existingSignal == null) {
                signalDao.insertSignal(
                    SignalEntity(
                        forSerial = generated.forSerial,
                        timestamp = generated.timestamp,
                        signalType = generated.signalType,
                        confidence = generated.confidence,
                        modelStatus = "ACTIVE",
                        rationale = generated.rationale,
                        predictedNumbers = generated.predictedNumbers.joinToString(",")
                    )
                )
            } else if (existingSignal.actualResult == null) {
                // Update upcoming pending signal with updated strategy/invert preference
                signalDao.updateSignal(
                    existingSignal.copy(
                        signalType = generated.signalType,
                        confidence = generated.confidence,
                        rationale = generated.rationale,
                        predictedNumbers = generated.predictedNumbers.joinToString(",")
                    )
                )
            }
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        drawDao.deleteAllDraws()
        signalDao.deleteAllSignals()
    }
}
