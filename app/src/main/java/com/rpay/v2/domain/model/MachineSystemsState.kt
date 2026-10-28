package com.rpay.v2.domain.model

data class MachineSystemsState(
    val selectedMain: MainSystemType? = null,
    val showSubDialog: Boolean = false
)