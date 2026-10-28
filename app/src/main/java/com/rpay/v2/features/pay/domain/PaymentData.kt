package com.rpay.v2.features.pay.domain

import com.rpay.v2.core.utils.enums.TransactionTypeLookup
import io.nearpay.sdk.utils.enums.TransactionData

data class PaymentData(
    val transactionData: TransactionData?,
    val transactionType: TransactionTypeLookup,
)