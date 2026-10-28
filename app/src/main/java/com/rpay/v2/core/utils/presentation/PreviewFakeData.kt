package com.rpay.v2.core.utils.presentation

import com.rpay.v2.core.utils.enums.PulseType
import com.rpay.v2.features.semnox.data.Machine
import com.rpay.v2.features.settings.presentation.DeviceSettingsState
import com.rpay.v2.features.settings.presentation.SemnoxSettingsState

object PreviewFakeData {
    // ---------- Device Settings Preview ----------
    fun deviceSettingsState() = DeviceSettingsState(
        deviceSn = "SN123456789",
        systemType = "mdb",
        pulseNumber = "1",
        pulsePeriod = "150",
        pulseWidth = "100",
        pulseType = PulseType.POSITIVE,
        gameCost = "2",
        serverDomain = "osool.server.com",
        tcpPort = "5000",
        devicePassword = "135246$",
        mobileNumber = "966558235356",
        waitDuration = "3",
        enablePageLock = false,
        systemOutOfService = false,
        enableAutoRefund = true
    )

    fun semnoxStateFake() = SemnoxSettingsState(
        semnoxPassword = "sad"
    )

    fun fakeSemnoxMachines(): List<Machine> {
        return listOf(
            Machine(
                machineId = 1,
                machineName = "CM3001",
                gameName = "Claw Machine",
                price = 30.0,
                vipPrice = 25.0
            ),
            Machine(
                machineId = 2,
                machineName = "CM3002",
                gameName = "Racing Game",
                price = 20.0,
                vipPrice = 18.0
            ),
            Machine(
                machineId = 3,
                machineName = "CM3003",
                gameName = "Shooting Game",
                price = 15.0,
                vipPrice = 12.0
            ),
            Machine(
                machineId = 4,
                machineName = "CM3004",
                gameName = "Puzzle Game",
                price = 10.0,
                vipPrice = 8.0
            )
        )
    }
}