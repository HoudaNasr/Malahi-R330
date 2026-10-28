package com.rpay.v2.data.repository

import android.content.Context
import com.google.gson.GsonBuilder
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.core.utils.enums.PulseType
import com.rpay.v2.core.utils.enums.SystemType
import com.rpay.v2.data.datasource.local.AppPreferencesDataSource
import com.rpay.v2.data.migration.LegacySystemTypeMapper
import com.rpay.v2.data.response.MachineTerminalInfoResponse
import com.rpay.v2.domain.location.GeoLocation
import com.rpay.v2.domain.machine.VMCMode
import com.rpay.v2.domain.mapper.MachineSystemMapper
import com.rpay.v2.domain.model.LegacySettings
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject


/**
 * Repository responsible for managing application preferences.
 *
 * Acts as a single access point for reading and writing
 * device, system, and machine configuration values.
 *
 * This repository abstracts the underlying data source
 * and provides additional device-related defaults when needed.
 */
class AppPreferencesRepository @Inject constructor(
    private val dataSource: AppPreferencesDataSource,
    @ApplicationContext private val context: Context
) {

    suspend fun getVersionName() = dataSource.getVersionName()

    suspend fun getTerminalSn() = dataSource.getTerminalSn()

    suspend fun getLatLng() = dataSource.getLatLng()
    suspend fun getDeviceTid() = dataSource.getDeviceTid()
    suspend fun getNearPayToken() = dataSource.getNearPayToken()
    suspend fun getIsPosAttachedToMachine() = dataSource.isPosAttachedToMachine()
    suspend fun getServerTime() = dataSource.getServerTime()
    suspend fun getLastResponseElapsed() = dataSource.getLastResponseElapsed()
    suspend fun getServerDomain() = dataSource.getServerDomain()

    suspend fun isSystemOutService() = dataSource.isSystemOutService()

    suspend fun observableSystemOutService() = dataSource.observableSystemOutService()

    suspend fun isSubscriptionValid() = dataSource.isSubscriptionValid()

    suspend fun getMobileNumber() = dataSource.getMobileNumber()

    suspend fun getGameCost() = dataSource.getGameCost()
    fun observeGameCost() = dataSource.observeGameCost()
    fun observeSemnoxGameCost() = dataSource.observeSemnoxGameCost()
    suspend fun getPulseNumber() = dataSource.getPulseNumber()
    suspend fun getPulsePeriod() = dataSource.getPulsePeriod()
    suspend fun getPulseWidth() = dataSource.getPulseWidth()
    suspend fun getPulseType() = dataSource.getPulseType()

    suspend fun getPaymentWaitDuration() = dataSource.getWaitDuration()

    suspend fun getDevicePassword() = dataSource.getDevicePassword()

    suspend fun getGeneralSystemType() = dataSource.getGeneralSystemType()
    suspend fun getSystemType() = dataSource.getMachineType()
    fun getSystemTypeAsFlow() = dataSource.getMachineTypeAsFlow()
    suspend fun getVmcSession() = dataSource.getVmcSessionMode()
    suspend fun getTcpPort() = dataSource.getTcpPort()
    suspend fun getAutoRefund() = dataSource.getAutoRefund()
    suspend fun getEnablePageLock() = dataSource.getPageLock()

    ////////////////// Setters

    // -------- Terminal
    suspend fun setIsSystemOutService(isOnService: Boolean) =
        dataSource.setIsSystemOutService(isOnService)

    suspend fun setIsSubscriptionValid(isSubscripotionValid: Boolean) =
        dataSource.setIsSubscriptionValid(isSubscripotionValid)

    suspend fun setIsPosAttachedToMachine(isPosAttched: Boolean) =
        dataSource.setIsPosAttachedToMachine(isPosAttched)

    suspend fun setServerTime(serverTime: String) = dataSource.setServerTime(serverTime)
    suspend fun setLastResponseElapsed(lastResponseElapsed: String) = dataSource.setLastResponseElapsed(lastResponseElapsed)
    suspend fun setDeviceTid(tid: String) = dataSource.setDeviceTid(tid)
    suspend fun setNearPayToken(token: String) = dataSource.setNearPayToken(token)


    suspend fun setTerminalSn(terminalSn: String?) {
        val sn = terminalSn ?: "NoSerialNumber"
        dataSource.saveTerminalSn(terminalSN = sn.lowercase())
    }

    suspend fun saveVersionName(versioinName: String) {
        dataSource.saveVersionName(versionName = versioinName)
    }

    suspend fun setLatLng(gpoLocation: GeoLocation) {
        dataSource.saveLatLng(gpoLocation.latitude, gpoLocation.longitude)
    }

    suspend fun setDevicePass(pass: String) =
        dataSource.setDevicePass(pass)

    suspend fun setMobileNumber(mobile: String) =
        dataSource.setMobileNumber(mobile)

    // -------- System type
    suspend fun setGeneralSystemType(type: GeneralSystemType) {
        dataSource.setGeneraSystemType(type)
    }

    suspend fun setSystemType(type: SystemType) {
        dataSource.setMachineType(type.name)
    }

    suspend fun setVmcSessionMode(type: VMCMode) {
        dataSource.setVmcSessionMode(type)
    }

    // -------- Pulse
    suspend fun setPulseNumber(value: String) =
        dataSource.setPulseNumber(value)

    suspend fun setPulsePeriod(value: String) =
        dataSource.setPulsePeriod(value)

    suspend fun setPulseWidth(value: String) =
        dataSource.setPulseWidth(value)

    suspend fun setPulseType(value: PulseType) =
        dataSource.setPulseType(value)

    // -------- Network
    suspend fun setServerDomain(domain: String) =
        dataSource.setServerDomain(domain)

    suspend fun setTcpPort(port: String) =
        dataSource.setTcpPort(port)

    // -------- Game
    suspend fun setGameCost(cost: String) =
        dataSource.setGameCost(cost)

    suspend fun setWaitDuration(value: String) =
        dataSource.setWaitDuration(value)

    // -------- Flags
    suspend fun setAutoRefund(enabled: Boolean) =
        dataSource.setAutoRefund(enabled)

    suspend fun setEnablePageLock(enabled: Boolean) =
        dataSource.setPageLock(enabled)


    /* =========================
    * Semnox API configuration
    * ========================= */

    suspend fun isSemnoxEnabled() =
        dataSource.getSemnoxEnabled()

    suspend fun setIsSemnoxEnabled(isEnabled: Boolean) =
        dataSource.setSemnoxEnabled(isEnabled)

    suspend fun getSemnoxApiBaseUrl() =
        dataSource.getSemnoxApiBaseUrl()

    suspend fun saveSemnoxApiBaseUrl(url: String) =
        dataSource.saveSemnoxApiBaseUrl(url)


    suspend fun getSemnoxUsername() =
        dataSource.getSemnoxUsername()

    suspend fun saveSemnoxUsername(username: String) =
        dataSource.saveSemnoxUsername(username)


    suspend fun getSemnoxPassword() =
        dataSource.getSemnoxPassword()

    suspend fun saveSemnoxPassword(password: String) =
        dataSource.saveSemnoxPassword(password)


    suspend fun getSemnoxSelectedMachineName() =
        dataSource.getSemnoxSelectedMachineName()

    suspend fun saveSemnoxSelectedMachineName(name: String) =
        dataSource.saveSemnoxSelectedMachineName(name)


    suspend fun getSemnoxSelectedMachineReference() =
        dataSource.getSemnoxSelectedMachineReference()

    suspend fun saveSemnoxSelectedMachineReference(reference: String) =
        dataSource.saveSemnoxSelectedMachineReference(reference)


    suspend fun getSemnoxSelectedMachinePrice() =
        dataSource.getSemnoxSelectedMachinePrice()

    suspend fun saveSemnoxSelectedMachinePrice(price: Float) =
        dataSource.saveSemnoxSelectedMachinePrice(price)


    suspend fun getSemnoxSelectedMachineId() =
        dataSource.getSemnoxSelectedMachineId()

    suspend fun saveSemnoxSelectedMachineId(id: Int) =
        dataSource.saveSemnoxSelectedMachineId(id)


    suspend fun clearSemnoxSelectedMachine() =
        dataSource.clearSemnoxSelectedMachine()

    suspend fun migrateLegacySettings(
        settings: LegacySettings
    ) {
        FileLogger.log(
            context,
            "migrateLegacySettings",
            "settings are ${settings.toString()}"
        )
        setGeneralSystemType(
            LegacySystemTypeMapper.toGeneralSystemType(
                settings.generalSystemType
            )
        )

        val systemType =
            SystemType.valueOf(
                settings.systemType
            )
        setSystemType(systemType)
        setTerminalSn(settings.terminalSn)

        setDeviceTid(settings.deviceTid)
        setNearPayToken(settings.nearPayToken)

        setDevicePass(settings.devicePassword)

        setServerDomain(settings.serverDomain)
        setTcpPort(settings.tcpPort)

        setMobileNumber(settings.mobileNumber)

        setPulseNumber(settings.pulseNumber.toString())
        setPulsePeriod(settings.pulsePeriod.toString())
        setPulseWidth(settings.pulseWidth.toString())

        setGameCost(settings.gameCost.toString())

        setLatLng(
            GeoLocation(
                settings.latitude.toDouble(),
                settings.longitude.toDouble()
            )
        )

        setAutoRefund(settings.enableAutoRefund)

        setEnablePageLock(settings.enableAdminLock)

        setIsSystemOutService(settings.systemOutService)

        setWaitDuration(
            settings.paymentReceiptWaitingDuration.toString()
        )

        runCatching {
            setPulseType(
                PulseType.valueOf(
                    settings.pulseType.uppercase()
                )
            )
        }

        setVmcSessionMode(
            if (settings.isPosStartsSession)
                VMCMode.POS_SESSION
            else
                VMCMode.VMC_SESSION
        )

        setIsSemnoxEnabled(settings.enableSemnox)

        saveSemnoxApiBaseUrl(settings.semnoxApiBaseUrl)

        saveSemnoxUsername(settings.semnoxUsername)

        saveSemnoxPassword(settings.semnoxPassword)

        saveSemnoxSelectedMachineName(
            settings.semnoxSelectedMachineName
        )

        saveSemnoxSelectedMachineReference(
            settings.semnoxSelectedMachineReference
        )

        saveSemnoxSelectedMachinePrice(
            settings.semnoxSelectedMachinePrice
        )

        settings.semnoxSelectedMachineId
            .toIntOrNull()
            ?.let {
                saveSemnoxSelectedMachineId(it)
            }
    }

    suspend fun syncMachineTerminalInfo(dto: MachineTerminalInfoResponse) {

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create()

        FileLogger.log(
            context,
            "syncMachineTerminalInfo",
            """
--- API DTO RECEIVED ---
${gson.toJson(dto)}
------------------------
""".trimIndent()
        )

        dto.terminalSN?.let { setTerminalSn(it) }
        dto.devicePassword?.let { setDevicePass(it) }
        dto.mobileNumber?.let { setMobileNumber(it) }

        dto.isSubscriptionValid?.let {
            setIsSubscriptionValid(it)
        }

        dto.isActive?.let {
            setIsSystemOutService(!it)
        }

        dto.enableAutoRefund?.let {
            setAutoRefund(it)
        }

        dto.serverDomain?.takeIf { it.isNotBlank() }?.let {
            setServerDomain(it)
        }

        dto.tcpPort?.let {
            setTcpPort(it.toString())
        }

        dto.cost?.let {
            setGameCost(it.toString())
        }

        dto.pulseNumber?.let {
            setPulseNumber(it.toString())
        }

        dto.pulsePeriod?.let {
            setPulsePeriod(it.toString())
        }

        dto.pulseWidth?.let {
            setPulseWidth(it.toString())
        }

        dto.pulseType?.let { pulseType ->
            runCatching {
                setPulseType(
                    PulseType.valueOf(
                        pulseType.uppercase()
                    )
                )
            }.onFailure {
                FileLogger.log(
                    context,
                    "syncMachineTerminalInfo",
                    "PulseType parse failed: $pulseType"
                )
            }
        }

        dto.systemType?.let { systemType ->
            runCatching {
                setSystemType(
                    SystemType.valueOf(
                        systemType.uppercase()
                    )
                )
            }.onFailure {
                FileLogger.log(
                    context,
                    "syncMachineTerminalInfo",
                    "SystemType parse failed: $systemType"
                )
            }
        }

        val semnoxEnabled =
            !dto.semnoxBaseUrl.isNullOrBlank()

        setIsSemnoxEnabled(semnoxEnabled)

        dto.semnoxBaseUrl?.let {
            saveSemnoxApiBaseUrl(it)
        }

        dto.semnoxUsername?.let {
            saveSemnoxUsername(it)
        }

        dto.semnoxPassword?.let {
            saveSemnoxPassword(it)
        }

        dto.semnoxPrice?.let {
            saveSemnoxSelectedMachinePrice(it.toFloat())
        }
        val generalSystemType =
            MachineSystemMapper.toGeneralSystemType(
                machineTypeLookup = dto.machineTypeLookup,
                paymentMethodsLookup = dto.paymentMethodsLookup,
                isPosStartSession = dto.isPosStartSession
            )
        FileLogger.log(context, "syncMachineTerminalInfo", generalSystemType.value)
        setGeneralSystemType(generalSystemType)

        FileLogger.log(
            context = context,
            "syncMachineTerminalInfo",
            """
--- DATASTORE AFTER SAVE ---
terminalSN=${getTerminalSn()}
devicePassword=${getDevicePassword()}
mobileNumber=${getMobileNumber()}
subscriptionValid=${isSubscriptionValid().first()}
systemOutService=${isSystemOutService()}
autoRefund=${getAutoRefund()}
serverDomain=${getServerDomain()}
tcpPort=${getTcpPort()}
gameCost=${getGameCost()}
pulseNumber=${getPulseNumber()}
pulsePeriod=${getPulsePeriod()}
pulseWidth=${getPulseWidth()}
pulseType=${getPulseType()}
systemType=${getSystemType()}
vmcSession=${getVmcSession()}
semnoxEnabled=${isSemnoxEnabled()}
semnoxBaseUrl=${getSemnoxApiBaseUrl()}
semnoxUsername=${getSemnoxUsername()}
semnoxPassword=${getSemnoxPassword()}
semnoxPrice=${getSemnoxSelectedMachinePrice()}
----------------------------
""".trimIndent()
        )
    }
}
