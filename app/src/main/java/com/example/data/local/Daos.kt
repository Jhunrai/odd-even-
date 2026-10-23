package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraws(draws: List<DrawEntity>)

    @Query("SELECT * FROM draws ORDER BY serial DESC, timestamp DESC")
    fun getAllDrawsDesc(): Flow<List<DrawEntity>>

    @Query("SELECT * FROM draws ORDER BY serial ASC, timestamp ASC")
    fun getAllDrawsAsc(): Flow<List<DrawEntity>>

    @Query("SELECT * FROM draws ORDER BY serial ASC, timestamp ASC")
    suspend fun getAllDrawsAscDirect(): List<DrawEntity>

    @Query("SELECT * FROM draws ORDER BY serial DESC, timestamp DESC LIMIT 1")
    suspend fun getLatestDraw(): DrawEntity?

    @Query("SELECT COUNT(*) FROM draws")
    fun getDrawCountFlow(): Flow<Int>

    @Query("DELETE FROM draws")
    suspend fun deleteAllDraws()
}

@Dao
interface SignalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity): Long

    @Query("SELECT * FROM signals WHERE forSerial = :serial LIMIT 1")
    suspend fun getSignalForSerial(serial: Long): SignalEntity?

    @Query("SELECT * FROM signals WHERE actualResult IS NULL")
    suspend fun getPendingSignals(): List<SignalEntity>

    @Update
    suspend fun updateSignal(signal: SignalEntity)

    @Query("SELECT * FROM signals ORDER BY id DESC")
    fun getAllSignalsFlow(): Flow<List<SignalEntity>>

    @Query("SELECT * FROM signals WHERE actualResult IS NOT NULL ORDER BY id DESC")
    fun getResolvedSignalsFlow(): Flow<List<SignalEntity>>

    @Query("SELECT * FROM signals WHERE actualResult IS NOT NULL ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentResolvedSignals(limit: Int = 20): List<SignalEntity>

    @Query("SELECT COUNT(*) FROM signals WHERE actualResult IS NOT NULL")
    suspend fun getResolvedSignalsCount(): Int

    @Query("DELETE FROM signals")
    suspend fun deleteAllSignals()
}
