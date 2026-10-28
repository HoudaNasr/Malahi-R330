package com.rpay.v2.data.datasource.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rpay.v2.data.model.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
/**
 * Data Access Object (DAO) for managing transaction records.
 *
 * Provides database operations for creating, reading,
 * and deleting stored transactions.
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp ASC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}
