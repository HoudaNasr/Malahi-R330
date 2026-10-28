package com.rpay.v2.features.pay.data

import com.rpay.v2.core.utils.enums.TransactionTypeLookup
import com.rpay.v2.features.pay.domain.PaymentRepository
import com.rpay.v2.features.pay.domain.PaymentResult
import javax.inject.Inject

class PaymentRepositoryImp @Inject constructor(
//    private val nearPayManager: NearPayManager
): PaymentRepository {
    override suspend fun loginMobile(mobile: String): Boolean {
//        return nearPayManager.emailLogin(mobile)
        return true
    }

    override suspend fun startPayment(amount: Long, timeout: Long): PaymentResult {
//        return nearPayManager.doPayment(amount = amount , timeout)
        return PaymentResult.Failed("sd" , TransactionTypeLookup.Purchase)
    }

    override suspend fun reverse(transactionId: String, timeout: Long): PaymentResult {
//        nearPayManager.reverseTransaction(transactionId, timeout)
        return PaymentResult.Failed("sd" , TransactionTypeLookup.Purchase)
//        return PaymentResult.Success(null)
    }
}