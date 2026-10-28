package com.rpay.v2.data.datasource.local

import com.rpay.v2.core.network.RestfulApis
import com.rpay.v2.data.request.TransactionRequest
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

/**
 * Remote data source responsible for transaction-related API calls.
 *
 * Acts as a thin wrapper around [RestfulApis] to execute
 * network requests related to transactions.
 */
class TransactionRepoDataBase @Inject constructor(
    private val api: RestfulApis
) {

    /**
     * Sends a transaction creation request to the backend.
     *
     * @param transactionRequest Payload used to create the transaction
     * @return Retrofit [Response] containing the created transaction UUID
     */
    suspend fun createTransaction(
        transactionRequest: TransactionRequest
    ): Response<UUID> {
        return api.createTransaction(transactionRequest)
    }
}