package com.rpay.v2.domain.model

sealed class QrDiscountType {
    object None : QrDiscountType()
    data class Applied(val qrHistoryOperationId: String) : QrDiscountType()
}