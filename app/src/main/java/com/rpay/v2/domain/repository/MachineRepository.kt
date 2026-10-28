package com.rpay.v2.domain.repository

import com.rpay.v2.data.response.MachineTerminalInfoResponse

interface MachineRepository {

    suspend fun getMachineInfoByTerminalSerialNo(
        terminalSerialNo: String
    ): Result<MachineTerminalInfoResponse>
}