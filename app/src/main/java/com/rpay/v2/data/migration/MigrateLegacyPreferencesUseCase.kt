package com.rpay.v2.data.migration

import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.domain.repository.LegacyPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MigrateLegacyPreferencesUseCase @Inject constructor(
    private val legacyPreferencesRepository: LegacyPreferencesRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) {

    suspend operator fun invoke() {

        // Already migrated
        if (appPreferencesRepository.getGeneralSystemType().first() != null) {
            return
        }

        // No legacy configuration
        if (!legacyPreferencesRepository.hasLegacyConfiguration()) {
            return
        }

        val legacySettings = legacyPreferencesRepository.getLegacySettings()

        appPreferencesRepository.migrateLegacySettings(
            legacySettings
        )
    }
}