package com.rpay.v2.data.migration

import android.content.Context
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.domain.model.LegacySettings
import com.rpay.v2.domain.repository.LegacyPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
    class LegacyPreferencesRepositoryImpl @Inject constructor(
        @ApplicationContext private val context: Context
    ) : LegacyPreferencesRepository {

        companion object {
        private const val PREFS_NAME = "AppSettings"
    }

    private val prefs =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    override fun getLegacySettings(): LegacySettings {
        val settings = LegacySettings(
            generalSystemType =
                prefs.getString(LegacyPreferenceKeys.GENERAL_SYSTEM_TYPE, "") ?: "",

            systemType =
                prefs.getString(LegacyPreferenceKeys.SYSTEM_TYPE, "") ?: "",

            terminalSn =
                prefs.getString(LegacyPreferenceKeys.TERMINAL_SN, "") ?: "",

            deviceTid =
                prefs.getString(LegacyPreferenceKeys.DEVICE_TID, "") ?: "",

            nearPayToken =
                prefs.getString(LegacyPreferenceKeys.NEARPAY_TOKEN, "") ?: "",

            devicePassword =
                prefs.getString(LegacyPreferenceKeys.DEVICE_PASSWORD, "") ?: "",

            serverDomain =
                prefs.getString(LegacyPreferenceKeys.SERVER_DOMAIN, "") ?: "",

            tcpPort =
                prefs.getString(LegacyPreferenceKeys.TCP_PORT, "") ?: "",

            mobileNumber =
                prefs.getString(LegacyPreferenceKeys.MOBILE_NUMBER, "") ?: "",

            pulseNumber =
                prefs.getString(LegacyPreferenceKeys.PULSE_NUMBER, "1")!!,

            gameCost =
                prefs.getString(LegacyPreferenceKeys.GAME_COST, "1")!!,

            pulsePeriod =
                prefs.getString(LegacyPreferenceKeys.PULSE_PERIOD, "150")!!,

            pulseWidth =
                prefs.getString(LegacyPreferenceKeys.PULSE_WIDTH, "100")!!,

            latitude =
                prefs.getString(LegacyPreferenceKeys.LATITUDE, "") ?: "",

            longitude =
                prefs.getString(LegacyPreferenceKeys.LONGITUDE, "") ?: "",

            enableDebug =
                prefs.getBoolean(LegacyPreferenceKeys.ENABLE_DEBUG, false),

            enableAdminLock =
                prefs.getBoolean(LegacyPreferenceKeys.ENABLE_ADMIN_LOCK, false),

            systemOutService =
                prefs.getBoolean(LegacyPreferenceKeys.SYSTEM_OUT_SERVICE, false),

            enableAutoRefund =
                prefs.getBoolean(LegacyPreferenceKeys.ENABLE_AUTO_REFUND, false),

            paymentReceiptWaitingDuration =
                prefs.getString(
                    LegacyPreferenceKeys.PAYMENT_RECEIPT_WAITING_DURATION,
                    "1"
                )!!,

            pulseType =
                prefs.getString(LegacyPreferenceKeys.PULSE_TYPE, "") ?: "",

            isPosStartsSession =
                prefs.getBoolean(
                    LegacyPreferenceKeys.POS_STARTS_SESSION,
                    false
                ),

            enableSemnox =
                prefs.getBoolean(LegacyPreferenceKeys.ENABLE_SEMNOX, false),

            semnoxApiBaseUrl =
                prefs.getString(LegacyPreferenceKeys.SEMNOX_API_BASE_URL, "") ?: "",

            semnoxUsername =
                prefs.getString(LegacyPreferenceKeys.SEMNOX_USERNAME, "") ?: "",

            semnoxPassword =
                prefs.getString(LegacyPreferenceKeys.SEMNOX_PASSWORD, "") ?: "",

            semnoxSelectedMachineName =
                prefs.getString(
                    LegacyPreferenceKeys.SEMNOX_SELECTED_MACHINE_NAME,
                    ""
                ) ?: "",

            semnoxSelectedMachineReference =
                prefs.getString(
                    LegacyPreferenceKeys.SEMNOX_SELECTED_MACHINE_REFERENCE,
                    ""
                ) ?: "",

            semnoxSelectedMachinePrice =
                prefs.getFloat(
                    LegacyPreferenceKeys.SEMNOX_SELECTED_MACHINE_PRICE,
                    0f
                ),

            semnoxSelectedMachineId =
                prefs.getString(
                    LegacyPreferenceKeys.SEMNOX_SELECTED_MACHINE_ID,
                    ""
                ) ?: ""
        )

        FileLogger.log(
            context = context,
            "LegacyPreferences",
            settings.toString()
        )
        return settings
    }

    override fun hasLegacyConfiguration(): Boolean {
        return !prefs.getString(
            LegacyPreferenceKeys.GENERAL_SYSTEM_TYPE,
            ""
        ).isNullOrBlank()
    }
}