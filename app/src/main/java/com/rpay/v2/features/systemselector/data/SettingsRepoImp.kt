package com.rpay.v2.features.systemselector.data

import com.rpay.v2.data.datasource.local.AppPreferencesDataSource
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import com.rpay.v2.features.systemselector.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepoImp @Inject constructor(
    private val prefs: AppPreferencesDataSource
): SettingsRepository {
    override suspend fun saveGeneralSystemType(systemType: GeneralSystemType) {
        return prefs.setGeneraSystemType(systemType)
    }

    override fun getGeneralSystemType(): Flow<String?> {
        return prefs.getGeneralSystemType()
    }
}