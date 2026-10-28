package com.rpay.cm30.v2.domain.usecase

import com.rpay.v2.data.transactions.domain.model.Transaction
import com.rpay.v2.domain.transactions.TransactionRepository
import kotlinx.coroutines.flow.Flow


/**
 * Use case responsible for retrieving all stored transactions.
 *
 * Exposes a [Flow] that emits updates whenever the underlying
 * transaction data changes.
 */
class GetTransactionsUseCase(
    private val repository: TransactionRepository
) {
    /**
     * Returns a stream of all transactions.
     */
    operator fun invoke(): Flow<List<Transaction>> = repository.getAllTransactions()
}
