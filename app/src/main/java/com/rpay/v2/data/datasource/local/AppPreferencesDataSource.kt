package com.rpay.v2.data.datasource.local


import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.core.utils.enums.PulseType
import com.rpay.v2.core.utils.enums.SystemType
import com.rpay.v2.domain.location.GeoLocation
import com.rpay.v2.domain.machine.VMCMode
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Local data source responsible for managing application preferences.
 *
 * Uses Jetpack DataStore to persist and retrieve device, system,
 * and machine configuration values.
 */
private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_prefs"
)

class AppPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore: DataStore<Preferences> = context.appDataStore

    /**
     * Preference keys used to store application and machine configuration.
     */
    private object Keys {
        val VERSION_NAME = stringPreferencesKey("versionName")
        val TERMINAL_SN = stringPreferencesKey("terminalSN")
        val LAT = stringPreferencesKey("latitude")
        val LNG = stringPreferencesKey("longitude")
        val DEVICE_TID = stringPreferencesKey("deviceTid")
        val NEARPAY_TOKEN = stringPreferencesKey("nearPayToken")

        val SYSTEM_OUT_SERVICE = booleanPreferencesKey("systemOutService")
        val SYSTEM_SUBSCRIPTION = booleanPreferencesKey("systemOutSubscription")
        val MOBILE_NUMBER = stringPreferencesKey("mobileNumber")
        val GAME_COST = stringPreferencesKey("gameCost")
        val PULSE_NUMBER = stringPreferencesKey("pulseNumber")
        val PULSE_WIDTH = stringPreferencesKey("pulseWidth")
        val PULSE_PERIOD = stringPreferencesKey("pulsePeriod")
        val PULSE_TYPE = stringPreferencesKey("pulseType")
        val DEVICE_PASSWORD = stringPreferencesKey("devicePassword")
        val GENERAL_SYSTEM_TYPE = stringPreferencesKey("general_system_type")
        val VMC_SESSION_MODE = stringPreferencesKey("vmc_session")
        val SERVER_DOMAIN = stringPreferencesKey("server_domain")
        val TCP_PORT = stringPreferencesKey("tcp_port")
        val WAIT_DURATION = stringPreferencesKey("wait_duration")
        val ENABLE_PAGE_LOCK = booleanPreferencesKey("enable_page_lock")
        val AUTO_REFUND = booleanPreferencesKey("auto_refund")
        val MACHINE_TYPE = stringPreferencesKey("machine_type")

        val IS_POS_ATTACHED = booleanPreferencesKey("isPosAttachedToMachine")
        val SERVER_TIME = stringPreferencesKey("serverTime")
        val LAST_RES_TIME = stringPreferencesKey("lastResponseElapsed")

        //////////Semnox Keys
        val SEMNOX_IS_ENABLE = booleanPreferencesKey("isSemnoxEnabled")
        val SEMNOX_API_BASE_URL = stringPreferencesKey("semnoxApiBaseUrl")
        val SEMNOX_USERNAME = stringPreferencesKey("semnoxUsername")
        val SEMNOX_PASSWORD = stringPreferencesKey("semnoxPassword")
        val SEMNOX_SELECTED_MACHINE_NAME = stringPreferencesKey("semnoxSelectedMachineName")
        val SEMNOX_SELECTED_MACHINE_REFERENCE = stringPreferencesKey("semnoxSelectedMachineReference")
        val SEMNOX_SELECTED_MACHINE_PRICE = floatPreferencesKey("semnoxSelectedMachinePrice")
        val SEMNOX_SELECTED_MACHINE_ID = intPreferencesKey("semnoxSelectedMachineId")
    }

    // -------- Helpers عامة --------

    private suspend fun getString(key: Preferences.Key<String>, default: String = ""): String {
        return dataStore.data.first()[key] ?: default
    }

    private suspend fun getBoolean(
        key: Preferences.Key<Boolean>,
        default: Boolean = false
    ): Boolean {
        return dataStore.data.first()[key] ?: default
    }

    private suspend fun putString(key: Preferences.Key<String>, value: String) {
        Log.d("AppPrefData", "key: ${key} value is : $value" )

        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    private suspend fun putBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    private suspend fun getFloat(
        key: Preferences.Key<Float>,
        default: Float = 0.0f
    ): Float {
        return dataStore.data.first()[key] ?: default
    }

    private suspend fun putFloat(
        key: Preferences.Key<Float>,
        value: Float
    ) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    private suspend fun getInt(
        key: Preferences.Key<Int>,
        default: Int = 0
    ): Int {
        return dataStore.data.first()[key] ?: default
    }

    private suspend fun putInt(
        key: Preferences.Key<Int>,
        value: Int
    ) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    suspend fun remove(vararg keys: Preferences.Key<*>) {
        dataStore.edit { prefs ->
            keys.forEach { key ->
                prefs.remove(key)
            }
        }
    }

    // -------- API خاصة بالمجال (Machine Game) --------

    suspend fun getVersionName(): String = getString(Keys.VERSION_NAME, "")
    suspend fun getTerminalSn(): String = getString(Keys.TERMINAL_SN, "")
    suspend fun saveTerminalSn(terminalSN: String) = putString(Keys.TERMINAL_SN, terminalSN)
    suspend fun saveVersionName(versionName: String) = putString(Keys.VERSION_NAME, versionName)
    suspend fun saveLatLng(lat: Double, lng: Double) {
        dataStore.edit {
            it[Keys.LAT] = lat.toString()
            it[Keys.LNG] = lng.toString()
        }
    }
    suspend fun getLatLng(): GeoLocation? {
        val lat = getString(Keys.LAT).toDoubleOrNull()
        val lng = getString(Keys.LNG).toDoubleOrNull()

        return if (lat != null && lng != null) {
            GeoLocation(lat, lng)
        } else {
            null
        }
    }
    suspend fun setDeviceTid(tid: String) = putString(Keys.DEVICE_TID, tid)
    suspend fun getDeviceTid(): String = getString(Keys.DEVICE_TID, "")

    suspend fun setNearPayToken(tid: String) = putString(Keys.NEARPAY_TOKEN, tid)

    suspend fun getNearPayToken(): String = getString(Keys.NEARPAY_TOKEN, "")

    suspend fun setServerTime(serverTime: String) = putString(Keys.SERVER_TIME, serverTime)
    suspend fun getServerTime() = getString(Keys.SERVER_TIME ,"")
    suspend fun setLastResponseElapsed(lastResponseElapsed: String) = putString(Keys.LAST_RES_TIME, lastResponseElapsed)
    suspend fun getLastResponseElapsed() = getString(Keys.LAST_RES_TIME, "")

    suspend fun setGeneraSystemType(type: GeneralSystemType) = putString(Keys.GENERAL_SYSTEM_TYPE, type.value)
    suspend fun setVmcSessionMode(type: VMCMode) = putString(Keys.VMC_SESSION_MODE, type.name)

    suspend fun setIsPosAttachedToMachine(isPosAttachedToMachine: Boolean) = putBoolean(Keys.IS_POS_ATTACHED , isPosAttachedToMachine)
    suspend fun isPosAttachedToMachine() = getBoolean(Keys.IS_POS_ATTACHED , false)

    suspend fun isSystemOutService(): Boolean =
        getBoolean(Keys.SYSTEM_OUT_SERVICE, false)

    fun observableSystemOutService(): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[Keys.SYSTEM_OUT_SERVICE] ?: false
        }


    suspend fun setIsSystemOutService(isOnService: Boolean) = putBoolean(Keys.SYSTEM_OUT_SERVICE , isOnService)

    fun isSubscriptionValid(): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[Keys.SYSTEM_SUBSCRIPTION] ?: true
        }
    suspend fun setIsSubscriptionValid(isSubscriptionValid: Boolean) = putBoolean(Keys.SYSTEM_SUBSCRIPTION , isSubscriptionValid)

    suspend fun setMobileNumber(mobileNumber: String) {
        putString(Keys.MOBILE_NUMBER, mobileNumber)
    }

    suspend fun getMobileNumber(): String {
        val v = getString(Keys.MOBILE_NUMBER, "")
        return v
    }

    suspend fun setGameCost(gameCost: String) = putString(Keys.GAME_COST, gameCost)

    suspend fun getGameCost(): String = getString(Keys.GAME_COST, "1")

    fun observeGameCost(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[Keys.GAME_COST] ?: "1"
        }
    }

    fun observeSemnoxGameCost(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[Keys.SEMNOX_SELECTED_MACHINE_PRICE] ?: 1.0f
        }
    }

    suspend fun setPulsePeriod(pulsePeriod: String) = putString(Keys.PULSE_PERIOD, pulsePeriod)
    suspend fun getPulsePeriod(): String = getString(Keys.PULSE_PERIOD, "150")


    suspend fun setPulseWidth(pulseWidth: String) = putString(Keys.PULSE_WIDTH, pulseWidth)
    suspend fun getPulseWidth(): String = getString(Keys.PULSE_WIDTH, "100")


    suspend fun setPulseNumber(pulseNumber: String) = putString(Keys.PULSE_NUMBER, pulseNumber)
    suspend fun getPulseNumber(): String = getString(Keys.PULSE_NUMBER, "1")


    suspend fun setPulseType(pulseType: PulseType) = putString(Keys.PULSE_TYPE, pulseType.name)
    suspend fun getPulseType(): String = getString(Keys.PULSE_TYPE, PulseType.NEGATIVE.name)

    suspend fun setServerDomain(serverDomain: String) = putString(Keys.SERVER_DOMAIN, serverDomain)
    suspend fun getServerDomain(): String = getString(Keys.SERVER_DOMAIN, "")

    suspend fun setTcpPort(tcpPort: String) = putString(Keys.TCP_PORT, tcpPort)
    suspend fun getTcpPort(): String = getString(Keys.TCP_PORT, "")

    suspend fun setWaitDuration(waitDur: String) = putString(Keys.WAIT_DURATION, waitDur)
    suspend fun getWaitDuration(): String = getString(Keys.WAIT_DURATION, "1")

    suspend fun setPageLock(pageLock: Boolean) = putBoolean(Keys.ENABLE_PAGE_LOCK, pageLock)
    suspend fun getPageLock(): Boolean = getBoolean(Keys.ENABLE_PAGE_LOCK)

    val pageLockFlow: Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[Keys.ENABLE_PAGE_LOCK] ?: false
        }

    suspend fun setAutoRefund(autoRef: Boolean) = putBoolean(Keys.AUTO_REFUND, autoRef)
    suspend fun getAutoRefund(): Boolean = getBoolean(Keys.AUTO_REFUND)

    suspend fun getDevicePassword(): String = getString(Keys.DEVICE_PASSWORD, "135246$")

    suspend fun setDevicePass(pass: String) = putString(Keys.DEVICE_PASSWORD, pass)


    fun getGeneralSystemType(): Flow<String?> =
        dataStore.data.map { prefs ->
            prefs[Keys.GENERAL_SYSTEM_TYPE].takeIf { !it.isNullOrBlank() }
        }

    // لو حابب تستخدمها كـ Flow
    val systemOutServiceFlow: Flow<Boolean> =
        dataStore.data.map { it[Keys.SYSTEM_OUT_SERVICE] ?: false }

    suspend fun setMachineType(value: String) {
        putString(Keys.MACHINE_TYPE, value)
    }


    suspend fun getVmcSessionMode(): String {
        return getString(Keys.VMC_SESSION_MODE, VMCMode.POS_SESSION.name)
    }

    suspend fun getMachineType(): String {
        return getString(Keys.MACHINE_TYPE, SystemType.PULSE.name)
    }


    fun getMachineTypeAsFlow(): Flow<String> {
        return dataStore.data
            .map { preferences ->
                preferences[Keys.MACHINE_TYPE] ?: SystemType.PULSE.name
            }
            .distinctUntilChanged()
    }
    /////////// Semnox


    suspend fun setSemnoxEnabled(isSemnox: Boolean) = putBoolean(Keys.SEMNOX_IS_ENABLE, isSemnox)

    suspend fun getSemnoxEnabled(): Boolean = getBoolean(Keys.SEMNOX_IS_ENABLE)

    suspend fun getSemnoxApiBaseUrl(): String =
        getString(Keys.SEMNOX_API_BASE_URL, "")

    suspend fun saveSemnoxApiBaseUrl(url: String) =
        putString(Keys.SEMNOX_API_BASE_URL, url)


    suspend fun getSemnoxUsername(): String =
        getString(Keys.SEMNOX_USERNAME, "admin")

    suspend fun saveSemnoxUsername(username: String) =
        putString(Keys.SEMNOX_USERNAME, username)


    suspend fun getSemnoxPassword(): String =
        getString(Keys.SEMNOX_PASSWORD, "parafait")

    suspend fun saveSemnoxPassword(password: String) =
        putString(Keys.SEMNOX_PASSWORD, password)

    suspend fun getSemnoxSelectedMachineName(): String =
        getString(Keys.SEMNOX_SELECTED_MACHINE_NAME, "")

    suspend fun saveSemnoxSelectedMachineName(name: String) =
        putString(Keys.SEMNOX_SELECTED_MACHINE_NAME, name)


    suspend fun getSemnoxSelectedMachineReference(): String =
        getString(Keys.SEMNOX_SELECTED_MACHINE_REFERENCE, "")

    suspend fun saveSemnoxSelectedMachineReference(reference: String) =
        putString(Keys.SEMNOX_SELECTED_MACHINE_REFERENCE, reference)


    suspend fun getSemnoxSelectedMachinePrice(): Float =
        getFloat(Keys.SEMNOX_SELECTED_MACHINE_PRICE, 1.0f)

    suspend fun saveSemnoxSelectedMachinePrice(price: Float) =
        putFloat(Keys.SEMNOX_SELECTED_MACHINE_PRICE, price)


    suspend fun getSemnoxSelectedMachineId(): Int =
        getInt(Keys.SEMNOX_SELECTED_MACHINE_ID, -1)

    suspend fun saveSemnoxSelectedMachineId(id: Int) =
        putInt(Keys.SEMNOX_SELECTED_MACHINE_ID, id)

    suspend fun clearSemnoxSelectedMachine() {
        FileLogger.log(context , "AppPrefUtils " , "clearSemnoxSelectedMachine")
        remove(
            Keys.SEMNOX_SELECTED_MACHINE_NAME,
            Keys.SEMNOX_SELECTED_MACHINE_REFERENCE,
            Keys.SEMNOX_SELECTED_MACHINE_PRICE,
            Keys.SEMNOX_SELECTED_MACHINE_ID
        )
    }
}
