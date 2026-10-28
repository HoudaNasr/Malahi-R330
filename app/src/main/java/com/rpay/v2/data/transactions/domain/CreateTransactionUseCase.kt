package com.rpay.v2.data.transactions.domain

import android.content.Context
import com.google.gson.Gson
import com.rpay.v2.core.network.RestfulApis
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.request.TransactionRequest
import com.rpay.v2.data.transactions.domain.model.Transaction
import com.rpay.v2.domain.transactions.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject


/**
 * Use case responsible for creating a transaction on the backend.
 *
 * In case of network failure or API error, the transaction is
 * saved locally as a fallback for later synchronization.
 */
class CreateTransactionUseCase @Inject constructor(
    private val transactionApi: RestfulApis,
    private val transactionRepository: TransactionRepository,
) {

    /**
     * Creates a transaction remotely using the provided request.
     *
     * On success:
     * - Returns the created transaction UUID.
     *
     * On failure:
     * - Persists the transaction locally for retry
     * - Returns a failure result with the corresponding exception
     *
     * @param transaction Domain transaction to be saved locally if needed
     * @param request Network request payload
     * @return [Result] containing transaction UUID on success or an error on failure
     */
    suspend operator fun invoke(
        transaction: Transaction,
        request: TransactionRequest
    ): Result<UUID> {
        return try {
            val response = transactionApi.createTransaction(request)
            if (response.isSuccessful == true) {
                Result.success(response.body()!!)
            } else {
                transactionRepository.saveTransaction(transaction)
                Result.failure(Exception(response?.message()))
            }
        } catch (ex: Exception) {
            transactionRepository.saveTransaction(transaction)
            Result.failure(ex)
        }
    }
}