package com.rpay.v2.features.pay.data

import com.rpay.v2.features.pay.domain.PaymentResult
import com.rpay.v2.features.pay.domain.QrScanningRepository
import javax.inject.Inject

class QrScanningRepositoryImp @Inject constructor(
//    private val nearPayManager: NearPayManager
): QrScanningRepository {
    override suspend fun scanQr(
        amount: Long,
        timeout: Long
    ): PaymentResult {
        TODO("Not yet implemented")
    }

    override suspend fun reverseQr(
        amount: Long,
        timeout: Long
    ): PaymentResult {
        TODO("Not yet implemented")
    }

}