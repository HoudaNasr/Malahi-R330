package com.rpay.v2.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val terminalSN: String?,
    val transactionType: String,
    val amount: Double?,
    val transactionId: String?,
    val date: String?,
    val latitude: Double?,
    val longitude: Double?,
    val giftCount: Int?,
    val timestamp: Long = System.currentTimeMillis(),

    val slotCode: String? = null,
    val bankCard: String? = null,
    val quantity: Int? = null,
    val tid: String? = null,
    val info: String? = null,
)
