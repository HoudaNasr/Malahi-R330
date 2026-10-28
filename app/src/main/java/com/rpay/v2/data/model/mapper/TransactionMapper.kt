package com.rpay.v2.data.model.mapper

import com.rpay.v2.data.model.entity.TransactionEntity
import com.rpay.v2.data.transactions.domain.model.Transaction

/**
 * Maps a [TransactionEntity] (data layer) to a [Transaction] (domain layer).
 */
fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    slotCode = slotCode,
    bankCard = bankCard,
    quantity = quantity,
    tid = tid,
    transactionId = transactionId,
    latitude = latitude,
    longitude = longitude,
    date = date,
    terminalSN = terminalSN ?: null,
    giftCount = giftCount,
    transactionType = transactionType,
    info = info
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    slotCode = slotCode,
    bankCard = bankCard,
    quantity = quantity,
    tid = tid,
    transactionId = transactionId,
    latitude = latitude,
    longitude = longitude,
    date = date,
    terminalSN = terminalSN,
    giftCount = giftCount,
    transactionType = transactionType,
    info = info
)
