package com.rpay.v2.features.systemselector.presentation

import com.rpay.v2.domain.model.MainSystemType

sealed class SystemsIntent {
    object LogoLongPressed : SystemsIntent()
    data class MainSelected(val type: MainSystemType) : SystemsIntent()
}