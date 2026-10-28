package com.rpay.v2.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rpay.v2.core.network.RestfulApis
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.response.MachineTerminalInfoResponse
import com.rpay.v2.data.response.RPayApiErrorResponse
import com.rpay.v2.domain.repository.MachineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MachineRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: RestfulApis
) : MachineRepository {

    override suspend fun getMachineInfoByTerminalSerialNo(
        terminalSerialNo: String
    ): Result<MachineTerminalInfoResponse> =
        runCatching {
            val gson = GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create()

            // 🔥 LOG REQUEST
            FileLogger.log(
                context,
                "getMachineInfoByTerminalSerialNo",
                """
--- REQUEST ---
ENDPOINT: /api/machine/infoByTerminalSerialNo
QUERY:
terminalSerialNo=$terminalSerialNo
----------------------
""".trimIndent()
            )

            val response =
                api.getTerminalDataBySN(
                    terminalSerialNo = terminalSerialNo
                )

            FileLogger.log(
                context,
                "getMachineInfoByTerminalSerialNo",
                "Response Code: ${response.code()}"
            )

            if (!response.isSuccessful) {

                val errorBody = response.errorBody()?.string()

                Log.e(
                    "MachineInfoError",
                    errorBody ?: "Unknown error"
                )

                // 🔥 LOG ERROR BODY
                FileLogger.log(
                    context,
                    "getMachineInfoByTerminalSerialNo",
                    """
--- ERROR RESPONSE ---
$errorBody
----------------------
""".trimIndent()
                )

                val parsedMessage = try {

                    val apiError = Gson().fromJson(
                        errorBody,
                        RPayApiErrorResponse::class.java
                    )

                    apiError.errors["validation"]?.joinToString(", ")
                        ?: apiError.title
                        ?: "Unknown error"

                } catch (e: Exception) {

                    "HTTP ${response.code()}"
                }

                throw Exception(parsedMessage)
            }

            val body = response.body()
                ?: throw Exception("Empty response body")
            Log.e(
                "SUCCESS RESPONSE",
                """
--- SUCCESS RESPONSE ---
${gson.toJson(body)}
----------------------
""".trimIndent()
            )
            // 🔥 LOG SUCCESS RESPONSE
            FileLogger.log(
                context,
                "getMachineInfoByTerminalSerialNo",
                """
--- SUCCESS RESPONSE ---
${gson.toJson(body)}
----------------------
""".trimIndent()
            )

            body
        }
}