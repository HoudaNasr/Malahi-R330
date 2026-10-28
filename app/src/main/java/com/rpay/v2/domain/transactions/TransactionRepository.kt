package com.rpay.v2.domain.transactions

import com.rpay.v2.data.model.entity.TransactionEntity
import com.rpay.v2.data.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.Flow


/**
 * Repository contract for managing transactions.
 *
 * Defines operations for retrieving, persisting,
 * and removing transaction records.
 */
interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun clearAll()
    suspend fun delete(transaction: TransactionEntity)
}
