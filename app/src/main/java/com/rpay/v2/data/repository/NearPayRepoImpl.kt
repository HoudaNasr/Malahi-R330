package com.rpay.v2.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rpay.v2.core.network.RestfulApis
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.request.GetNearPayTokenRequest
import com.rpay.v2.data.request.GetTerminalsRequest
import com.rpay.v2.data.response.GetNearPayTokenResponse
import com.rpay.v2.data.response.GetTerminalsResponse
import com.rpay.v2.data.response.RPayApiErrorResponse
import com.rpay.v2.data.response.TerminalsDataDto
import com.rpay.v2.domain.repository.NearPayRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NearPayRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: RestfulApis
) : NearPayRepo {

    override suspend fun getTerminals(
        request: GetTerminalsRequest
    ): Result<TerminalsDataDto> =
        runCatching {

            val gson = GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create()

            FileLogger.log(
                context,
                "getTerminals",
                """
--- REQUEST ---
ENDPOINT: /api/NearPay/Terminals

QUERY:
TerminalSN=${request.terminalSN}
Tid=${request.tid}
IsConnected=${request.isConnected}
IsAssigned=${request.isAssigned}
From=${request.from}
To=${request.to}
Page=${request.page}
Limit=${request.limit}
----------------------
""".trimIndent()
            )

            val response = api.getTerminals(
                terminalSN = request.terminalSN,
                tid = request.tid,
                isConnected = request.isConnected,
                isAssigned = request.isAssigned,
                from = request.from,
                to = request.to,
                page = request.page,
                limit = request.limit
            )

            FileLogger.log(
                context,
                "getTerminals",
                "Response Code: ${response.code()}"
            )

            if (!response.isSuccessful) {

                val errorBody = response.errorBody()?.string()

                Log.e(
                    "NearPayTerminalsError",
                    errorBody ?: "Unknown error"
                )

                FileLogger.log(
                    context,
                    "getTerminals",
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
                gson.toJson(body)
            )

            FileLogger.log(
                context,
                "getTerminals",
                """
--- SUCCESS RESPONSE ---
${gson.toJson(body)}
----------------------
""".trimIndent()
            )

            body
        }

    override suspend fun getToken(
        request: GetNearPayTokenRequest
    ): Result<GetNearPayTokenResponse> =
        runCatching {

            val gson = GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create()

            FileLogger.log(
                context,
                "getToken",
                """
--- REQUEST ---
ENDPOINT: /api/NearPay/Token

QUERY:
TerminalSN=${request.terminalSN}
Tid=${request.tid}
----------------------
""".trimIndent()
            )

            val response = api.getToken(
                terminalSN = request.terminalSN,
                tid = request.tid
            )

            FileLogger.log(
                context,
                "getToken",
                "Response Code: ${response.code()}"
            )

            if (!response.isSuccessful) {

                val errorBody = response.errorBody()?.string()

                Log.e(
                    "NearPayTokenError",
                    errorBody ?: "Unknown error"
                )

                FileLogger.log(
                    context,
                    "getToken",
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
                gson.toJson(body)
            )

            FileLogger.log(
                context,
                "getToken",
                """
--- SUCCESS RESPONSE ---
${gson.toJson(body)}
----------------------
""".trimIndent()
            )

            body
        }
}