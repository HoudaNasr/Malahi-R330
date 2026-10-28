package com.rpay.v2.features.pay.domain

import com.rpay.v2.core.utils.enums.TransactionTypeLookup

sealed class PaymentResult {
    data class Success(val data: PaymentData?) : PaymentResult()
    data class Failed(val reason: String , val transactionType: TransactionTypeLookup) : PaymentResult()
}