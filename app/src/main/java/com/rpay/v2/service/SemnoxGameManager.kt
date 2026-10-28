package com.rpay.v2.service

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.gson.GsonBuilder
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.features.semnox.data.GamePlayRequest
import com.rpay.v2.features.semnox.data.LoginRequest
import com.rpay.v2.features.semnox.data.Machine
import com.rpay.v2.features.semnox.data.network.SemnoxRestfulApi
import com.rpay.v2.features.semnox.domain.SemnoxError
import com.rpay.v2.features.semnox.domain.SemnoxResult
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


/**
 * SemnoxGameManager
 *
 * Responsibilities:
 * - Handle Semnox authentication
 * - Manage API token lifecycle
 * - Execute gameplay-related API calls
 *
 * Notes:
 * - No UI logic
 * - No coroutine scope ownership
 * - Depends on AppPreferencesRepository for configuration/state
 */
@Singleton
class SemnoxGameManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferencesRepository
) {

    private var authToken: String? = null

    private val TAG = "SemnoxGameManager"

    private suspend fun createSemnoxApi(): SemnoxRestfulApi {
        val baseUrl = prefs.getSemnoxApiBaseUrl().ensureTrailingSlash()
        FileLogger.log(
            context = context,
            TAG,
            "baseUrl : $baseUrl"
        )
        if (baseUrl.isBlank()) {
            throw IllegalStateException("Semnox base URL is not configured")
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(SemnoxRestfulApi::class.java)
    }

    fun String.ensureTrailingSlash(): String {
        return if (this.isNotEmpty() && !this.endsWith("/")) {
            "$this/"
        } else {
            this
        }
    }

    /**
     * Authenticates with the Semnox backend.
     * - Checks that Semnox is configured (base URL and credentials).
     * - Calls the authentication API.
     * - Saves the bearer token in memory on success.
     */
    suspend fun authenticateUser(): SemnoxResult<Unit> {
        return try {
            log("authenticateUser")
            // 1️⃣ Validate configuration first
            val baseUrl = prefs.getSemnoxApiBaseUrl()
            if (baseUrl.isBlank()) {
                return SemnoxResult.Error(SemnoxError.NotConfigured)
            }

            val loginId = prefs.getSemnoxUsername()
            val password = prefs.getSemnoxPassword()
            log("authenticateUser: $loginId $password")

            if (loginId.isBlank() || password.isBlank()) {
                return SemnoxResult.Error(SemnoxError.NotConfigured)
            }
            val loginRequest = LoginRequest(
                loginId = loginId,
                password = password
            )
            // 2️⃣ Call API
            val response = createSemnoxApi().authenticateUser(
                loginRequest
            )


            val gson = GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create()

            FileLogger.log(
                context,
                "authenticateUser",
                """
--- REQUEST ---
ENDPOINT: authenticateUser
BODY:
${gson.toJson(loginRequest)}
----------------------
""".trimIndent()
            )
            FileLogger.log(
                context = context,
                TAG,
                "response : Request Login: $loginId password: ${password}"
            )
            // 3️⃣ Handle response
            if (response.isSuccessful && response.body() != null) {
                log("success: ${response.body()?.toString()}")
                authToken = "Bearer ${response.body()!!.token}"
                SemnoxResult.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()

                FileLogger.log(
                    context,
                    "authenticateUser",
                    """
--- ERROR RESPONSE ---
$errorBody
----------------------
""".trimIndent()
                )

                log("Error: ${response.code()} ${response.body()?.toString()} ")
                val fullUrl = response.raw().request.url.toString()
                FileLogger.log(
                    context = context,
                    TAG,
                    "response : url $fullUrl ${response.code()} ${response.message()}"
                )
                when (response.code()) {

                    401, 403 -> SemnoxResult.Error(SemnoxError.Unauthorized)
                    else -> SemnoxResult.Error(
                        SemnoxError.Unknown(
                            "${fullUrl} Authentication failed (${response.code()})"
                        )
                    )
                }
            }

        } catch (e: IllegalStateException) {
            log("BaseUrlInterceptor Error token ${e.message}")
            // Thrown from BaseUrlInterceptor
            SemnoxResult.Error(SemnoxError.NotConfigured)

        } catch (e: IOException) {
            // No internet / timeout / DNS
            log("IOException Error token ${e.message}")
            SemnoxResult.Error(SemnoxError.Network)

        } catch (e: Exception) {
            log("Exception Error ${e.message ?: "Unexpected error"}")
            SemnoxResult.Error(
                SemnoxError.Unknown(e.message ?: "Unexpected error")
            )
        }
    }


    /**
     * Ensures that a valid Semnox authentication token is available.
     * - If no token exists, it authenticates.
     * - If the token is expired or about to expire, it refreshes it.
     * - If the token is still valid, nothing is done.
     */
    private suspend fun checkAndRefreshTokenIfNeeded(): SemnoxResult<Unit> {
        val currentToken = authToken

        if (currentToken == null) {
            Log.d(TAG, "No token exists, authenticating...")
            log("No token exists, authenticating...")
            return authenticateUser()
        }

        if (isTokenExpiredOrExpiringSoon(currentToken)) {
            Log.d(TAG, "Token expired or expiring soon, refreshing...")
            log("Token expired or expiring soon, refreshing...")
            return authenticateUser()
        }
        log("Token is still valid")

        Log.d(TAG, "Token is still valid")
        return SemnoxResult.Success(Unit)
    }

    /**
     * Triggers a game play for the given card number.
     * - Ensures a valid authentication token is available.
     * - Uses the currently selected machine.
     * - Calls the Semnox game play API.
     */
    suspend fun triggerGamePlay(cardNumber: String): SemnoxResult<Unit> {
        // 1️⃣ Ensure token
        val tokenResult = checkAndRefreshTokenIfNeeded()
        if (tokenResult is SemnoxResult.Error) {
            return SemnoxResult.Error(tokenResult.error)
        }

        // 2️⃣ Validate selected machine
        val machineId = prefs.getSemnoxSelectedMachineId()
        val machineReference = prefs.getSemnoxSelectedMachineReference()

        if (machineId == -1 || machineReference.isBlank()) {
            return SemnoxResult.Error(SemnoxError.MachineNotSelected)
        }

        return try {
            val dateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())

            val request = GamePlayRequest(
                machineId = machineId,
                machineReference = machineReference,
                cardNumber = cardNumber,
                playDate = dateFormat.format(Date()),
                commitGamePlay = true
            )

            val response = createSemnoxApi().triggerGamePlay(
                authToken!!,
                listOf(request)
            )

            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                SemnoxResult.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val friendlyMessage =
                    getFriendlyErrorMessage(errorBody ?: "Unknown error")

                SemnoxResult.Error(
                    SemnoxError.Api(friendlyMessage)
                )
            }

        } catch (e: IOException) {
            SemnoxResult.Error(SemnoxError.Network)
        } catch (e: Exception) {
            SemnoxResult.Error(
                SemnoxError.Unknown(e.message ?: "Unexpected error")
            )
        }
    }


    /**
     * Fetches the list of available machines from Semnox.
     * This method:
     * - Ensures a valid authentication token is available.
     * - Calls the Semnox machines API.
     */
    suspend fun fetchMachines(): SemnoxResult<List<Machine>> {
        return when (val tokenResult = checkAndRefreshTokenIfNeeded()) {

            is SemnoxResult.Success -> {
                try {
                    log("success checkAndRefreshTokenIfNeeded")
                    val response = createSemnoxApi().getMachines(authToken!!)
                    if (response.isSuccessful && response.body() != null) {
                        log("success machines")
                        SemnoxResult.Success(response.body()!!)
                    } else {
                        log("error machines")
                        SemnoxResult.Error(
                            SemnoxError.Unknown("Failed to fetch machines")
                        )
                    }

                } catch (e: IOException) {
                    SemnoxResult.Error(SemnoxError.Network)
                } catch (e: Exception) {
                    SemnoxResult.Error(
                        SemnoxError.Unknown(e.message ?: "Unexpected error")
                    )
                }
            }

            is SemnoxResult.Error -> {
                log("error token ${tokenResult.error}")
                SemnoxResult.Error(tokenResult.error)
            }
        }
    }

    // Check if token needs refresh (expired or expiring soon)
    private fun isTokenExpiredOrExpiringSoon(token: String): Boolean {
        val expiryTime = getTokenExpiry(token) ?: return true
        val currentTime = System.currentTimeMillis() / 1000
        val fiveMinutesInSeconds = 5 * 60

        return (expiryTime - currentTime) <= fiveMinutesInSeconds
    }

    // Decode JWT token and extract expiry time
    private fun getTokenExpiry(token: String): Long? {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = parts[1]
                val paddedPayload = when (payload.length % 4) {
                    0 -> payload
                    2 -> "$payload=="
                    3 -> "$payload="
                    else -> payload
                }

                val decodedPayload =
                    String(Base64.decode(paddedPayload, Base64.URL_SAFE))
                val jsonObject = JSONObject(decodedPayload)
                jsonObject.optLong("exp", 0)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding JWT token: ${e.message}")
            null
        }
    }


    // Convert API error messages to user-friendly messages
    private fun getFriendlyErrorMessage(errorBody: String): String {
        return when {
            errorBody.contains("Cards not found") -> "Cards not Available"
            errorBody.contains("Card does not have balance") -> "Insufficient balance. Please recharge the card."
            errorBody.contains("balance") -> "Insufficient balance. Please recharge the card."
            errorBody.contains("recharge") -> "Please recharge the card."
            errorBody.contains("not found") -> "Card not found in system."
            errorBody.contains("invalid") -> "Invalid card or card not registered."
            errorBody.contains("unauthorized") -> "Authentication failed. Please check settings."
            errorBody.contains("network") -> "Network error. Please check connection."
            else -> "Game not available for this card."
        }
    }

    private fun log(message: String) {
        FileLogger.log(context = context, TAG, message = message)

    }
}