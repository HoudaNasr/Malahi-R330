package com.rpay.v2.domain.repository

import com.rpay.v2.domain.model.LegacySettings

interface LegacyPreferencesRepository {
    fun getLegacySettings(): LegacySettings
    fun hasLegacyConfiguration(): Boolean
}