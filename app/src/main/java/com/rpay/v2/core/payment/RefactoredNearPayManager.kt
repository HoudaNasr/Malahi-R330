package com.rpay.v2.core.payment

import android.content.Context
import android.widget.Toast
import com.rpay.v2.BuildConfig
import com.rpay.v2.core.utils.enums.TransactionTypeLookup
import com.rpay.v2.features.pay.domain.PaymentData
import com.rpay.v2.features.pay.domain.PaymentResult
import com.rpay.v2.data.repository.AppPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext


import io.nearpay.sdk.Environments
import io.nearpay.sdk.NearPay
import io.nearpay.sdk.data.models.SessionInfo
import io.nearpay.sdk.utils.PaymentText
import io.nearpay.sdk.utils.enums.AuthenticationData
import io.nearpay.sdk.utils.enums.LogoutFailure
import io.nearpay.sdk.utils.enums.NetworkConfiguration
import io.nearpay.sdk.utils.enums.PurchaseFailure
import io.nearpay.sdk.utils.enums.ReversalFailure
import io.nearpay.sdk.utils.enums.SessionFailure
import io.nearpay.sdk.utils.enums.SetupFailure
import io.nearpay.sdk.utils.enums.TransactionData
import io.nearpay.sdk.utils.enums.UIPosition
import io.nearpay.sdk.utils.listeners.CheckSessionListener
import io.nearpay.sdk.utils.listeners.LogoutListener
import io.nearpay.sdk.utils.listeners.PurchaseListener
import io.nearpay.sdk.utils.listeners.ReversalListener
import io.nearpay.sdk.utils.listeners.SetupListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefactoredNearPayManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPrefsRepo: AppPreferencesRepository,
) {
    private val _paymentEvents =
        MutableSharedFlow<PaymentResult>(extraBufferCapacity = 1)
    val paymentEvents = _paymentEvents.asSharedFlow()
    private var nearPay: NearPay? = null
    private val clientScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun initializeIfNeeded(
        token: String,
        isDevelopment: Boolean = BuildConfig.IS_DEV_ENV
    ): Boolean {
        if (token.isBlank()) return false
        return try {
            if (nearPay == null) {
                nearPay = buildNearPayJwtAuth(
                    isDevelopment = isDevelopment,
                    token = token
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }


    fun isInitialized(): Boolean {
        return nearPay != null
    }

    private fun buildNearPayJwtAuth(isDevelopment: Boolean, token: String): NearPay {

        val env = if (isDevelopment) Environments.SANDBOX else Environments.PRODUCTION
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

    fun validateAuthentication(
        onResult: (Boolean) -> Unit
    ) {

        nearPay?.getUserSession(
            object : CheckSessionListener {

                override fun getSessionInfo(info: SessionInfo) {
                    clientScope.launch {
                        appPrefsRepo.setDeviceTid(info.tid!!)
                    }
                    onResult(true)
                }

                override fun onSessionFailed(
                    sessionFailure: SessionFailure
                ) {
                    onResult(false)
                }

                override fun onSessionFree() {
                    onResult(false)
                }

                override fun onSessionBusy(message: String) {
                    onResult(false)
                }
            }
        )
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

                        _paymentEvents.tryEmit(
                            PaymentResult.Failed(
                                reason = failureReason,
                                transactionType = TransactionTypeLookup.Reversal
                            )
                        )
                        if (reversalFailure is ReversalFailure.AuthenticationFailed) {
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


    fun updateJwtAuthentication(token: String): Boolean {
        if (token.isBlank()) return false
        return try {
            if (nearPay == null) {
                nearPay = buildNearPayJwtAuth(BuildConfig.IS_DEV_ENV, token)
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

    suspend fun logoutAndWait(token: String): Boolean =
        suspendCancellableCoroutine { cont ->
            if (!isInitialized()) {
                nearPay = buildNearPayJwtAuth(BuildConfig.IS_DEV_ENV,token)
            }
            nearPay?.logout(object : LogoutListener {
                override fun onLogoutCompleted() {

                    nearPay?.close()
                    nearPay = null

                    cont.resume(true) {}
                }

                override fun onLogoutFailed(logoutError: LogoutFailure) {


                    cont.resume(false) {}
                }
            })
        }
    fun setup(token: String) {
        nearPay?.setup(object : SetupListener {
            override fun onSetupCompleted() {

                Toast.makeText(context, "onSetupCompleted", Toast.LENGTH_SHORT).show()
                validateAuthentication { result ->

                }

            }

            override fun onSetupFailed(setupFailure: SetupFailure) {
                when (setupFailure) {
                    is SetupFailure.AuthenticationFailed -> {
                        Toast.makeText(context, "SetupFailure", Toast.LENGTH_SHORT).show()
                        if (!authRetryDone) {
                            authRetryDone = true
                            nearPay?.updateAuthentication(
                                AuthenticationData.Jwt(token)
                            )
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
