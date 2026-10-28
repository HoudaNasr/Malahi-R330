package com.rpay.v2.features.systemselector.domain

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun saveGeneralSystemType(systemType: GeneralSystemType)
    fun getGeneralSystemType(): Flow<String?>
}