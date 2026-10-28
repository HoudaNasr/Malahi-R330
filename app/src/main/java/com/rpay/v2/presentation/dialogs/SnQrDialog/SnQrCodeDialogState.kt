package com.rpay.v2.presentation.dialogs.SnQrDialog

data class SnQrCodeDialogState(
    val isVisible: Boolean = false,
    val serialNumber: String = "",
    val secondsRemaining: Int = 60
)
