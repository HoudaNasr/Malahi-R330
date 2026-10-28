package com.rpay.v2.data

import com.rpay.v2.domain.config.UiConfigProvider
import com.rpay.v2.domain.config.UiMode
import javax.inject.Inject

class FlavorConfigImpl @Inject constructor() : UiConfigProvider {
    override val uiMode = UiMode.COMPACT
}