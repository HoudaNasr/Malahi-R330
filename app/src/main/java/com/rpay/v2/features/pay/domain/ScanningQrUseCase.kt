package com.rpay.v2.features.pay.domain

import javax.inject.Inject

class ScanningQrUseCase @Inject constructor(
    private val repo: QrScanningRepository
) {
    suspend operator fun invoke(amount: Long, timeout: Long): PaymentResult {
        return repo.scanQr(amount, timeout)
    }
}