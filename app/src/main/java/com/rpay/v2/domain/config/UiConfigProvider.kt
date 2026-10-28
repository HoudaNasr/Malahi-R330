package com.rpay.v2.domain.config


interface UiConfigProvider {
    val uiMode: UiMode
}
enum class UiMode {
    COMPACT,
    LARGE
}