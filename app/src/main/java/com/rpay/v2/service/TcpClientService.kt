package com.rpay.v2.service

import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.rpay.v2.core.utils.enums.PulseType
import com.rpay.v2.core.utils.enums.SystemType
import com.rpay.v2.core.utils.enums.TcpCommunicationKeyEnum
import com.rpay.v2.data.model.CommunicationModel
import com.rpay.v2.data.model.SendPreferencesData
import com.rpay.v2.data.model.SubscriptionCheckData
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.cm30.v2.core.utils.Helper
import com.rpay.v2.BuildConfig
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.model.MachineStatus
import com.rpay.v2.data.model.NearPayAuthUpdateData
import com.rpay.v2.data.model.TerminalSetting
import com.rpay.v2.domain.hardware.KioskManager
import com.rpay.v2.domain.mapper.MachineSystemMapper
import com.rpay.v2.domain.terminal.SyncTerminalCredentialsUseCase
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.*
import java.net.Socket
import java.net.SocketTimeoutException
import javax.inject.Inject
import kotlin.jvm.java

/**
 * Service that maintains a persistent TCP connection to the server and reconnects if lost.
 */
@AndroidEntryPoint
class TcpClientService : Service() {
    var TAG = "TcpClientServiceTAG "
    private val clientScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Inject
    lateinit var appPrefsRepo: AppPreferencesRepository

    @Inject
    lateinit var syncTerminalCredentialsUseCase: SyncTerminalCredentialsUseCase
    @Inject
    lateinit var kioskManager: KioskManager
    @Volatile
    private var socket: Socket? = null

    @Inject
    @ApplicationContext
    lateinit var context: Context
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTcpClient()
        return START_STICKY
    }

    private fun startTcpClient() {
        clientScope.launch {
            while (isActive) {
                try {
                    Log.d(
                        "TcpClientService",
                        "Attempting to connect: ${Helper.TcpBaseURL}:${BuildConfig.TCP_PORT}"
                    )


                    // Create the socket (blocking call). If unreachable, throws an exception.
                    socket = Socket(Helper.TcpBaseURL, BuildConfig.TCP_PORT).apply {
                        // Optional: read timeout in milliseconds
                        soTimeout = 10_000 // 10 seconds
                    }

                    Log.d("TcpClientService", "Connected to TCP server.")

                    val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                    val writer = PrintWriter(
                        BufferedWriter(OutputStreamWriter(socket!!.getOutputStream())),
                        true
                    )

                    // Continuously send messages (heartbeat, updates, etc.)
                    val sendingJob = launch {
                        while (isActive && socket?.isConnected == true) {
                            sendPeriodicMessage(writer)
                            delay(30_000)
                        }
                    }

                    // Continuously read messages
                    while (isActive && socket?.isConnected == true) {
                        try {
                            val line = reader.readLine()
                            // If 'line' is null, it means the server closed the connection
                            if (line == null) {
                                Log.w("TcpClientService", "Server closed connection.")
                                break
                            }
                            handleIncomingLine(line)
                        } catch (timeoutEx: SocketTimeoutException) {
//                            FileLogger.log(context = context , tag = TAG , "timeoutEx ${timeoutEx.message}")
                            // If no data arrived before soTimeout, we get here
                            // Instead of closing the connection, just log and continue
                            Log.d("TcpClientService", "Read timed out. No data yet, continuing...")
                        }
                    }

                    // If we exit the while-loop, either the server closed the connection
                    // or the socket is no longer valid. Cancel the sending job too.
                    sendingJob.cancelAndJoin()

                } catch (e: Exception) {
                    Log.e("TcpClientService", "Connection error: ${e.localizedMessage}", e)
                } finally {
                    // Ensure the socket is closed
                    try {
                        socket?.close()
                    } catch (ex: IOException) {
                        Log.e(
                            "TcpClientService",
                            "Error closing socket: ${ex.localizedMessage}",
                            ex
                        )
                    } finally {
                        socket = null
                    }
                    // Wait before retrying
                    Log.d("TcpClientService", "Disconnected. Retrying in 5 seconds...")
                    delay(5000)
                }
            }
        }
    }

    private suspend fun sendPeriodicMessage(writer: PrintWriter) {
        try {
            val latLng = appPrefsRepo.getLatLng()
            val generalSystemType =
                GeneralSystemType.from(appPrefsRepo.getGeneralSystemType().first().toString())


            val backendConfig =
                MachineSystemMapper.toBackendConfig(
                    generalSystemType ?: GeneralSystemType.MACHINE_GAME
                )
            val prefsMap = mutableMapOf<String, Any?>(
                "latitude" to latLng?.latitude,
                "longitude" to latLng?.longitude,
                "appVersion" to appPrefsRepo.getVersionName(),
                "serverDomain" to appPrefsRepo.getServerDomain(),
                "tcpPort" to appPrefsRepo.getTcpPort(),
                "gameCost" to appPrefsRepo.getGameCost(),
                "pulseNumber" to appPrefsRepo.getPulseNumber(),
                "pulseWidth" to appPrefsRepo.getPulseWidth(),
                "pulsePeriod" to appPrefsRepo.getPulsePeriod(),
                "pulseType" to appPrefsRepo.getPulseType(),
                "systemType" to appPrefsRepo.getSystemType(),
                "enableAutoRefund" to appPrefsRepo.getAutoRefund(),
                "enableDebug" to false,
                "enableAdminLock" to appPrefsRepo.getEnablePageLock(),
                "general_system_type" to appPrefsRepo.getGeneralSystemType().first(),
                "paymentReceiptWaitingDuration" to appPrefsRepo.getPaymentWaitDuration(),
                "mobileNumber" to appPrefsRepo.getMobileNumber(),
                "terminalSN" to appPrefsRepo.getTerminalSn(),
                "versionName" to appPrefsRepo.getVersionName(),
                "deviceTid" to appPrefsRepo.getDeviceTid(),
                "isSubscriptionValid" to appPrefsRepo.isSubscriptionValid().first(),
                "systemOutService" to appPrefsRepo.isSystemOutService(),
                "devicePassword" to appPrefsRepo.getDevicePassword(),
                "isPosAttachedToMachine" to appPrefsRepo.getIsPosAttachedToMachine(),
                "serverTime" to appPrefsRepo.getServerTime(),
                "lastResponseElapsed" to appPrefsRepo.getLastResponseElapsed(),
                "semnoxApiBaseUrl" to appPrefsRepo.getSemnoxApiBaseUrl(),
                "semnoxUsername" to appPrefsRepo.getSemnoxUsername(),
                "semnoxPassword" to appPrefsRepo.getSemnoxPassword(),
                "semnoxSelectedMachinePrice" to appPrefsRepo.getSemnoxSelectedMachinePrice().toInt(),
                "semnoxSelectedMachineId" to appPrefsRepo.getSemnoxSelectedMachineId(),

                "machineTypeLookup" to backendConfig.machineTypeLookup,
                "paymentMethodsLookup" to backendConfig.paymentMethodsLookup,
                "isPosStartSession" to backendConfig.isPosStartSession
            )

            val message = CommunicationModel(
                msgKey = TcpCommunicationKeyEnum.SendPreferences.name,
                data = prefsMap,
                apiKey = BuildConfig.API_KEY,
                terminalSN = appPrefsRepo.getTerminalSn() ?: ""
            )

            val json = gson.toJson(message)
            writer.println(json)
            writer.flush()

            Log.d("TcpClientService", "Sent: $json")
        } catch (e: Exception) {
            Log.e("TcpClientService", "Error sending message", e)
            FileLogger.log(context = context, tag = TAG, "Error sending message ${e.message}")
        }
    }


    private fun handleIncomingLine(line: String) {
        try {
            val communicationModelObj = gson.fromJson(line, CommunicationModel::class.java)
            val keyValue = TcpCommunicationKeyEnum.valueOf(communicationModelObj.msgKey)
            Log.d(TAG, "Received preferences: $keyValue")
            when (keyValue) {
                TcpCommunicationKeyEnum.SendPreferences -> {
                    val data = gson.fromJson(
                        gson.toJsonTree(communicationModelObj.data),
                        SendPreferencesData::class.java
                    )

                    clientScope.launch {
                        data.serverDomain?.let { appPrefsRepo.setServerDomain(it) }
                        data.tcpPort?.let { appPrefsRepo.setTcpPort(it) }
                        data.gameCost?.let { appPrefsRepo.setGameCost(it) }
                        data.systemType?.let { appPrefsRepo.setSystemType(SystemType.valueOf(it)) }
                        data.mobileNumber?.let { appPrefsRepo.setMobileNumber(it) }
                        data.devicePassword?.let { appPrefsRepo.setDevicePass(it) }
                        data.enableAutoRefund?.let { appPrefsRepo.setAutoRefund(it) }

                        data.pulseNumber?.let { appPrefsRepo.setPulseNumber(it) }
                        data.pulsePeriod?.let { appPrefsRepo.setPulsePeriod(it) }
                        data.pulseWidth?.let { appPrefsRepo.setPulseWidth(it) }
                        data.pulseType?.let { appPrefsRepo.setPulseType(PulseType.valueOf(it)) }
                    }
                }

                TcpCommunicationKeyEnum.EnableSystemOutOfService -> {
                    Log.d(TAG, "System marked Out of Service.")
                    clientScope.launch {
                        appPrefsRepo.setIsSystemOutService(true)
                    }
                }

                TcpCommunicationKeyEnum.SubscriptionCheck -> {
                    try {
                        val jsonElement = gson.toJsonTree(communicationModelObj.data)
                        val preferences =
                            gson.fromJson(jsonElement, SubscriptionCheckData::class.java)
                        Log.d(TAG, "SubscriptionCheck. ${preferences}")
                        clientScope.launch {
                            preferences.IsSubscriptionValid?.let {
                                appPrefsRepo.setIsSubscriptionValid(
                                    it
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse SubscriptionCheck data", e)
                    }
                }

                TcpCommunicationKeyEnum.DisableSystemOutOfService -> {
                    Log.d(TAG, "System marked in Service.")
                    clientScope.launch {
                        appPrefsRepo.setIsSystemOutService(false)
                    }
                }

                TcpCommunicationKeyEnum.NearpayTidAssginment -> {
                    try {
                        val jsonElement = gson.toJsonTree(communicationModelObj.data)
                        val authData = gson.fromJson(
                            jsonElement,
                            NearPayAuthUpdateData::class.java
                        )
                        FileLogger.log(context , TAG , authData.toString())
                        clientScope.launch {
                            syncTerminalCredentialsUseCase(
                                authData.Tid,
                                authData.Token
                            )
                        }
                    } catch (e: Exception) {
                        FileLogger.log(context , TAG , e.message!!)
                        Log.e(TAG, "Failed to parse NearPay auth update", e)
                    }
                }

                TcpCommunicationKeyEnum.RebootPos -> {
                    try {
                        FileLogger.log(context = context, tag = TAG, "RebootPos")
                        kioskManager.reboot()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse restartPos", e)
                    }
                }
                TcpCommunicationKeyEnum.EnterTerminalSetting -> {
                    navigateToSettingsNotification(communicationModelObj)
                }
                TcpCommunicationKeyEnum.MachineStatus -> {
                    handleMachineStatus(communicationModelObj)
                }

                else -> Unit
            }

        } catch (e: Exception) {
            Log.e("TcpClientService", "Failed to parse incoming", e)
        }
    }

    private fun handleMachineStatus(communicationModel: CommunicationModel){
        val jsonElement = gson.toJsonTree(communicationModel.data)
        val machineStatusObj = gson.fromJson(jsonElement, MachineStatus::class.java)
        Log.d(ContentValues.TAG, "Received MachineStatus: ")
        try {
            clientScope.launch {
               appPrefsRepo.setIsPosAttachedToMachine(machineStatusObj.isAttached)
               appPrefsRepo.setServerTime(machineStatusObj.serverTime)
               appPrefsRepo.setLastResponseElapsed(machineStatusObj.lastResponseElapsed)
            }
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "MachineStatus Exception", e)
        }
    }

    private fun navigateToSettingsNotification(communicationModel: CommunicationModel){
        Log.d(ContentValues.TAG, "Received EnterTerminalSetting: ")
        val jsonElement = gson.toJsonTree(communicationModel.data)
        val preferences = gson.fromJson(jsonElement, TerminalSetting::class.java)
        try {
            val timeoutSec = preferences.TimeoutSec!!
            LocalBroadcastManager
                .getInstance(applicationContext)
                .sendBroadcast(
                    Intent(
                        Helper.ACTION_RECIEVED_TIMEOUT
                    ).apply {
                        putExtra(
                            Helper.TIMEOUT_SEC,
                            timeoutSec
                        )
                    }
                )
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "EnterTerminalSetting Exception", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clientScope.cancel()
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e("TcpClientService", "Error closing socket: ${e.localizedMessage}", e)
        }
        socket = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This is a started service; not bound.
        return null
    }
}
