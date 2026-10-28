package com.rpay.v2.data.repository

import com.rpay.v2.data.datasource.local.TransactionDao
import com.rpay.v2.data.model.entity.TransactionEntity
import com.rpay.v2.data.model.mapper.toDomain
import com.rpay.v2.data.model.mapper.toEntity
import com.rpay.v2.data.transactions.domain.model.Transaction
import com.rpay.v2.domain.transactions.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return dao.getAll()
            .map { list ->
                list
                    .sortedByDescending { it.date }
                    .map { it.toDomain() }
            }
    }

    override suspend fun saveTransaction(transaction: Transaction) {
        dao.insert(transaction.toEntity())
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }

    override suspend fun delete(transaction: TransactionEntity) {
        dao.delete(transaction = transaction)
    }
}
