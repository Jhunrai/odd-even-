package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.NormalizedDraw

@Entity(tableName = "draws")
data class DrawEntity(
    @PrimaryKey
    val compositeKey: String, // serial + "_" + timestamp or id
    val serial: Long,
    val timestamp: String,
    val openTime: String = "",
    val lotteryNum: String = "",
    val userGuess: String = "",
    val gameState: String = "Settled",
    val isSettled: Boolean = true,
    val number: Int,
    val result: String, // "ODD", "EVEN", or "PENDING"
    val resultValue: Int, // 1 for ODD, 0 for EVEN, -1 for PENDING
    val rawData: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toNormalizedDraw(): NormalizedDraw {
        return NormalizedDraw(
            id = compositeKey,
            timestamp = timestamp,
            openTime = openTime,
            serial = serial,
            number = number,
            lotteryNum = lotteryNum,
            userGuess = userGuess,
            gameState = gameState,
            isSettled = isSettled,
            result = result,
            resultValue = resultValue,
            rawData = rawData
        )
    }

    companion object {
        fun fromNormalized(draw: NormalizedDraw): DrawEntity {
            return DrawEntity(
                compositeKey = draw.id,
                serial = draw.serial,
                timestamp = draw.timestamp,
                openTime = draw.openTime,
                lotteryNum = draw.lotteryNum,
                userGuess = draw.userGuess,
                gameState = draw.gameState,
                isSettled = draw.isSettled,
                number = draw.number,
                result = draw.result,
                resultValue = draw.resultValue,
                rawData = draw.rawData
            )
        }
    }
}

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val forSerial: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val signalType: String, // "ODD" or "EVEN"
    val confidence: Int,
    val modelStatus: String = "ACTIVE",
    val rationale: String,
    val predictedNumbers: String = "",
    val actualResult: String? = null,
    val actualNumber: Int? = null,
    val isCorrect: Boolean? = null,
    val isNumberHit: Boolean? = null
)
