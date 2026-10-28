package com.rpay.v2.domain.transactions

import com.rpay.v2.core.utils.enums.TransactionTypeLookup
import com.rpay.v2.data.mapper.TransactionInfoMapper
import com.rpay.v2.data.model.NormalizedTransactionData
import com.rpay.v2.data.model.entity.TransactionEntity
import com.rpay.v2.data.model.mapper.toDomain
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.data.request.TransactionRequest
import com.rpay.v2.data.transactions.domain.CreateTransactionUseCase
import com.rpay.v2.domain.model.QrDiscountType
import com.rpay.v2.features.pay.domain.PaymentData
import com.rpay.cm30.v2.core.utils.Helper
import java.util.UUID
import javax.inject.Inject

/**
 * Use case responsible for handling approved payments or machine-triggered actions.
 *
 * It normalizes incoming payment or machine data, builds both
 * domain and network transaction models, and delegates persistence
 * to [CreateTransactionUseCase].
 */
class HandlePaymentApprovalUseCase @Inject constructor(
    private val prefsRepo: AppPreferencesRepository,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val transcationMapper: TransactionInfoMapper,
    ) {


    /**
     * Handles a successful payment or machine-triggered transaction.
     *
     * Depending on whether [paymentData] is provided, this method:
     * - Extracts and normalizes payment receipt data (card payments)
     * - Or builds a machine-only transaction (pulse / digital IO)
     */
    suspend operator fun invoke(
        paymentData: PaymentData? = null,
        slotCode: String? = null,
        giftCount: Int? = null,
        quantity: Int? = null,
        discountType: QrDiscountType = QrDiscountType.None,
        transactionType: TransactionTypeLookup,
        onTidUpdated: (String) -> Unit
    ): Result<UUID> {

        val info = transcationMapper.map(discountType)

        val terminalSN = prefsRepo.getTerminalSn()
        val latLng = prefsRepo.getLatLng()


        val normalized = if (paymentData != null) {

            val receipt = paymentData.transactionData
                ?.receipts
                ?.firstOrNull()
                ?: return Result.failure(IllegalStateException("No receipt found"))

            val tid = receipt.tid
                ?: return Result.failure(IllegalStateException("TID is null"))

            onTidUpdated(tid)

            NormalizedTransactionData(
                amount = receipt.amount_authorized?.value?.toDoubleOrNull() ?: 0.0,
                transactionId = receipt.transaction_uuid.orEmpty(),
                date = "${receipt.start_date} ${receipt.start_time}",
                cardNumber = receipt.card_scheme?.id,
                tid = tid
            )

        } else {
            // ✔ Machine / non-payment flow
            NormalizedTransactionData(
                amount = null,
                transactionId = null,
                date = Helper.getCurrentDateTimeString(),
                cardNumber = null,
                tid = null
            )
        }


        val transaction = TransactionEntity(
            terminalSN = terminalSN,
            transactionType = transactionType.name,
            amount = normalized.amount,
            transactionId = normalized.transactionId,
            date = normalized.date,
            latitude = latLng?.latitude ?: 0.0,
            longitude = latLng?.longitude ?: 0.0,
            giftCount = giftCount,
            slotCode = slotCode,
            bankCard = normalized.cardNumber,
            tid = normalized.tid,
            info = info
        ).toDomain()

        val request = TransactionRequest(
            amount = normalized.amount,
            transactionTypeLookup = transactionType.name,
            terminalSN = terminalSN,
            transactionId = normalized.transactionId,
            date = normalized.date,
            giftCount = giftCount,
            lat = latLng?.latitude ?: 0.0,
            lng = latLng?.longitude ?: 0.0,
            slotCode = slotCode,
            bankCard = normalized.cardNumber,
            quantity = quantity,
            tid = normalized.tid,
            info = info
        )

        return createTransactionUseCase(
            transaction = transaction,
            request = request
        )
    }
}