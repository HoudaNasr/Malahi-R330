package com.rpay.v2.core.payment

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.core.utils.enums.TransactionTypeLookup
import com.rpay.v2.features.pay.domain.PaymentData
import com.rpay.v2.features.pay.domain.PaymentResult
import com.rpay.cm30.v2.core.utils.Helper


import io.nearpay.sdk.Environments
import io.nearpay.sdk.NearPay
import io.nearpay.sdk.data.models.SessionInfo
import io.nearpay.sdk.utils.PaymentText
import io.nearpay.sdk.utils.enums.AuthenticationData
import io.nearpay.sdk.utils.enums.NetworkConfiguration
import io.nearpay.sdk.utils.enums.PurchaseFailure
import io.nearpay.sdk.utils.enums.ReversalFailure
import io.nearpay.sdk.utils.enums.SessionFailure
import io.nearpay.sdk.utils.enums.SetupFailure
import io.nearpay.sdk.utils.enums.TransactionData
import io.nearpay.sdk.utils.enums.UIPosition
import io.nearpay.sdk.utils.listeners.CheckSessionListener
import io.nearpay.sdk.utils.listeners.PurchaseListener
import io.nearpay.sdk.utils.listeners.ReversalListener
import io.nearpay.sdk.utils.listeners.SetupListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale
import java.util.UUID

/**
 * Wrapper حول NearPay SDK
 * - يهيّئ NearPay
 * - يعمل purchase / reverse
 * - يبعت broadcast بالنتيجة (type + status + data)
 */
class NearPayManager(
    private val context: Context,
    isDevelopment: Boolean,
    initialToken: String? = null
) {
    private val _paymentEvents =
        MutableSharedFlow<PaymentResult>(extraBufferCapacity = 1)
    val paymentEvents = _paymentEvents.asSharedFlow()
    private var nearPay: NearPay? = null

    init {
        if (!initialToken.isNullOrBlank()) {
            nearPay = buildNearPayJwtAuth(isDevelopment, initialToken)
        }
    }

    companion object {
        @Volatile
        private var instance: NearPayManager? = null

        fun getInstance(
            context: Context,
            isDevelopment: Boolean,
            token: String? = null
        ): NearPayManager {
            return instance ?: synchronized(this) {
                instance ?: NearPayManager(
                    context = context.applicationContext,
                    isDevelopment = isDevelopment,
                    initialToken = token
                ).also {
                    instance = it
                }
            }
        }
    }

    private fun requireNearPay(): NearPay {
        return nearPay ?: throw IllegalStateException(
            "NearPay not initialized yet. Waiting for JWT token."
        )
    }

    fun isInitialized(): Boolean {
        return nearPay != null
    }
    private fun buildNearPay(isDevelopment: Boolean): NearPay {
        val env = if (isDevelopment) Environments.SANDBOX else Environments.PRODUCTION

        return NearPay.Builder()
            .context(context)
            .authenticationData(AuthenticationData.UserEnter) // المستخدم يدخل البيانات بنفسه
            .environment(env)
            .locale(Locale("ar"))
            .networkConfiguration(NetworkConfiguration.SIM_PREFERRED)
            .uiPosition(UIPosition.CENTER_BOTTOM)
            .paymentText(PaymentText("يرجى تمرير البطاقة", "Please tap your card"))
            .loadingUi(true)
            .build()
    }

    private fun buildNearPayJwtAuth(isDevelopment: Boolean, token: String): NearPay {

        val env = if (isDevelopment) Environments.SANDBOX else Environments.PRODUCTION
        FileLogger.log(context, "NearPaySdk", "buildNearPayJwtAuth :${token}")

        return NearPay.Builder()
            .context(context)
            .environment(env)
            .authenticationData(AuthenticationData.Jwt(token))
            .locale(Locale("ar"))
            .networkConfiguration(NetworkConfiguration.SIM_PREFERRED)
            .uiPosition(UIPosition.CENTER_BOTTOM)
            .paymentText(PaymentText("يرجى تمرير البطاقة", "Please tap your card"))
            .loadingUi(true)
            .build()
    }

    // -------------------------------------------------------------------------
    // PURCHASE
    // -------------------------------------------------------------------------

    fun makePayment(amount: Long, finishTimeOut: Long) {
        try {
            val customerReferenceNumber = UUID.randomUUID().toString()
            val enableReceiptUi = true
            val enableReversal = true
            val requestId = UUID.randomUUID()
            val enableUiDismiss = true

            nearPay?.purchase(
                amount,
                customerReferenceNumber,
                enableReceiptUi,
                enableReversal,
                finishTimeOut,
                requestId,
                enableUiDismiss,
                object : PurchaseListener {

                    override fun onPurchaseApproved(transactionData: TransactionData) {
                        Toast.makeText(context, "Purchase Approved", Toast.LENGTH_SHORT).show()

                        _paymentEvents.tryEmit(
                            PaymentResult.Success(
                                PaymentData(
                                    transactionData = transactionData,
                                    transactionType = TransactionTypeLookup.Purchase
                                )
                            )
                        )
                    }

                    override fun onPurchaseFailed(purchaseFailure: PurchaseFailure) {
                        val failureReason = when (purchaseFailure) {
                            is PurchaseFailure.PurchaseDeclined -> "declined"
                            is PurchaseFailure.PurchaseRejected -> "rejected"
                            is PurchaseFailure.InvalidStatus -> "invalid_status"
                            is PurchaseFailure.GeneralFailure -> "GeneralFailure Reason Is -> ${purchaseFailure.message}"
                            else -> "unknown_failure"
                        }
                        FileLogger.log(context, "NearPaySdk", failureReason)

                        println("Purchase Failed: $failureReason")
                        Toast.makeText(context, failureReason, Toast.LENGTH_SHORT).show()
                        _paymentEvents.tryEmit(
                            PaymentResult.Failed(
                                reason = failureReason,
                                transactionType = TransactionTypeLookup.Purchase
                            )
                        )
                        // Debug logging
                        when (purchaseFailure) {
                            is PurchaseFailure.PurchaseDeclined ->
                                println("Purchase Declined")

                            is PurchaseFailure.PurchaseRejected ->
                                println("Purchase Rejected")

                            is PurchaseFailure.InvalidStatus ->
                                println("Invalid Status: ${purchaseFailure.status}")

                            is PurchaseFailure.GeneralFailure ->
                                println("General Failure: ${purchaseFailure.message}")

                            else ->
                                println("Unknown Failure")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            val msg = "Payment Error: ${e.message}"
            println(msg)
            _paymentEvents.tryEmit(
                PaymentResult.Failed(
                    reason = msg, transactionType = TransactionTypeLookup.Purchase
                )
            )
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // -------------------------------------------------------------------------
    // REVERSAL
    // -------------------------------------------------------------------------

    fun reverseTransaction(transactionUuid: String, finishTimeOut: Long) {
        val enableReceiptUi = true
        val enableUiDismiss = true

        try {
            nearPay?.reverse(
                transactionUuid,
                enableReceiptUi,
                finishTimeOut,
                enableUiDismiss,
                object : ReversalListener {

                    override fun onReversalFinished(transactionData: TransactionData) {
                        _paymentEvents.tryEmit(
                            PaymentResult.Success(
                                PaymentData(
                                    transactionData = transactionData,
                                    transactionType = TransactionTypeLookup.Reversal
                                )
                            )
                        )
                        Toast.makeText(context, "Reversal Successful", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReversalFailed(reversalFailure: ReversalFailure) {
                        val failureReason = when (reversalFailure) {
                            is ReversalFailure.FailureMessage ->
                                "Failure Message: ${reversalFailure.message}"

                            is ReversalFailure.AuthenticationFailed ->
                                "Authentication Failed"

                            is ReversalFailure.InvalidStatus ->
                                "Invalid Status: ${reversalFailure.status}"

                            is ReversalFailure.GeneralFailure ->
                                "General Failure"
                        }

                        println("Reversal Failed: $failureReason")

                        sendBroadcast(
                            type = TransactionTypeLookup.Reversal.name,
                            status = "failed",
                            data = failureReason
                        )
                        _paymentEvents.tryEmit(
                            PaymentResult.Failed(
                                reason = failureReason,
                                transactionType = TransactionTypeLookup.Reversal
                            )
                        )
                        if (reversalFailure is ReversalFailure.AuthenticationFailed) {
                            // مثال: ممكن تحدّث التوكن هنا لو عندك
                            // nearPay.updateAuthentication(AuthenticationData.Jwt("NEW_JWT_HERE"))
                        }
                    }
                }
            )
        } catch (e: Exception) {
            val errorMessage = "Reversal Error: ${e.message}"
            _paymentEvents.tryEmit(
                PaymentResult.Failed(
                    reason = errorMessage, transactionType = TransactionTypeLookup.Reversal
                )
            )
            println(errorMessage)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // -------------------------------------------------------------------------
    // BROADCAST
    // -------------------------------------------------------------------------

    private fun sendBroadcast(type: String, status: String, data: String?) {
        val intent = Intent(Helper.PaymentBroadcaster).apply {
            putExtra("type", type)
            putExtra("status", status)
            putExtra("data", data)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    // -------------------------------------------------------------------------
    // AUTH HELPERS
    // -------------------------------------------------------------------------

//    fun emailLogin(email: String): Boolean {
//        return if (email.isNotEmpty()) {
//            nearPay.updateAuthentication(AuthenticationData.Email(email))
//            Toast.makeText(context, "Email login successful", Toast.LENGTH_SHORT).show()
//            true
//        } else {
//            Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
//            false
//        }
//    }
//
//    fun mobileLogin(mobile: String): Boolean {
//        return true
//        return if (mobile.isNotEmpty()) {
//            nearPay.updateAuthentication(AuthenticationData.Mobile(mobile))
//            true
//        } else {
//            Toast.makeText(context, "Mobile cannot be empty", Toast.LENGTH_SHORT).show()
//            false
//        }
//    }

    fun updateJwtAuthentication(token: String): Boolean {
        if (token.isBlank()) return false

        return try {
            if (nearPay == null) {
                nearPay = buildNearPayJwtAuth(false,token)
            } else {
                nearPay?.updateAuthentication(
                    AuthenticationData.Jwt(token)
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private var authRetryDone = false

    fun setup(mobileLogin: String) {
        nearPay?.setup(object : SetupListener {
            override fun onSetupCompleted() {
                Toast.makeText(context, "onSetupCompleted", Toast.LENGTH_SHORT).show()

            }

            override fun onSetupFailed(setupFailure: SetupFailure) {
                when (setupFailure) {
                    is SetupFailure.AuthenticationFailed -> {
                        if (!authRetryDone) {
                            authRetryDone = true
//                            mobileLogin(mobile = mobileLogin)
                            setup(mobileLogin)
                        }
                    }

                    SetupFailure.NotInstalled -> {
                        // show UI / block flow
                    }

                    is SetupFailure.InvalidStatus -> {
                        // show device status error
                    }

                    else -> {
                        // general error handling
                    }
                }
                Toast.makeText(context, "onSetupFailed", Toast.LENGTH_SHORT).show()
                FileLogger.log(
                    context = context,
                    tag = "NearPay",
                    message = "onSetupFailed → ${setupFailure.toLogString()}"
                )

            }
        })
    }

    private fun SetupFailure.toLogString(): String {
        return when (this) {
            SetupFailure.AlreadyInstalled ->
                "AlreadyInstalled: NearPay app already installed on device"

            SetupFailure.NotInstalled ->
                "NotInstalled: NearPay app is not installed on device"

            is SetupFailure.AuthenticationFailed ->
                "AuthenticationFailed: message=${message.ifBlank { "empty" }}"

            is SetupFailure.InvalidStatus ->
                "InvalidStatus: ${
                    status.joinToString(
                        prefix = "[",
                        postfix = "]"
                    ) { it.toString() }
                }"

            SetupFailure.GeneralFailure ->
                "GeneralFailure: Unknown setup error"
        }
    }
    // -------------------------------------------------------------------------
    // SESSION / TID
    // -------------------------------------------------------------------------

    fun getTid(onResult: (String?) -> Unit) {
        nearPay?.getUserSession(object : CheckSessionListener {

            override fun onSessionFree() {
                onResult(null)
            }

            override fun onSessionFailed(sessionFailure: SessionFailure) {
                onResult(null)
            }

            override fun onSessionBusy(message: String) {
                onResult(null)
            }

            override fun getSessionInfo(info: SessionInfo) {
                val tid = info.tid
                onResult(tid)
            }
        })
    }
}
