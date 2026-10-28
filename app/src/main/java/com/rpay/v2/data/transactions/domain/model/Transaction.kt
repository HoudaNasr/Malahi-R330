package com.rpay.v2.data.transactions.domain.model

data class Transaction(
    val id: Long,          // optional (local id)
    val terminalSN: String?,
    val transactionType: String,
    val amount: Double?,
    val transactionId: String?,
    val date: String?,
    val latitude: Double?,
    val longitude: Double?,
    val giftCount: Int?,
    val slotCode: String?,
    val bankCard: String?,
    val quantity: Int?,
    val tid: String?,
    val info: String?
)