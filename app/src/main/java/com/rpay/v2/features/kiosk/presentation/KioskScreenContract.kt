package com.rpay.v2.features.kiosk.presentation

import com.rpay.v2.core.base.UiIntent
import com.rpay.v2.core.base.UiState
import com.rpay.v2.core.utils.enums.SystemType
import com.rpay.v2.core.utils.enums.TransactionTypeLookup
import com.rpay.v2.domain.config.UiMode
import com.rpay.v2.domain.machine.OutOfServiceReason
import com.rpay.v2.features.kiosk.presentation.KioskScreenViewModel.IncomingPaymentRequest
import com.rpay.v2.features.pay.domain.PaymentData
import com.rpay.v2.features.pay.domain.PaymentResult

object KioskScreenContract {

    data class State(
        val uiStep: UiStep = UiStep.Idle,
        val outOfServiceReason: OutOfServiceReason? = null,
        val dialogState: DialogState = DialogState.None,
        val uiScale: UiMode = UiMode.COMPACT,
        val lastPaymentRequest: IncomingPaymentRequest? = null,
        val isProcessing: Boolean = false,
        val appVersion: String = "",
        val deviceSn: String = "",
        val deviceTid: String = "",
        val transactionId: String = "",
        val showPasswordDialog: Boolean = false,
        val passwordError: String? = null,
        val errorMessage: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        object Init : Intent
        object SessionTimeOut : Intent
        object LogoLongPressed : Intent
        object PasswordDialogDismissed : Intent
        object ClearDialog : Intent
        data class PasswordSubmitted(val value: String) : Intent
        data class PaymentApproved (val data: PaymentData ,val transactionTypeLookup: TransactionTypeLookup) : Intent
        data class PaymentFailed (val value: PaymentResult.Failed) : Intent
        data class TidReceived(val tid: String) : Intent
    }
    sealed interface UiEffect {
        data class ShowToast(val message: String) : UiEffect
        object NavigateToSettings : UiEffect
        object getTid : UiEffect

    }
    sealed interface UiStep {
        data object Idle : UiStep
    }

    sealed class DialogState {
        object None : DialogState()
        object VendSuccess : DialogState()
        object VendFailure : DialogState()
    }
}
