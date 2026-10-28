package com.rpay.v2.core.network

import com.rpay.v2.data.request.ExceptionRequest
import com.rpay.v2.data.request.QrRequest
import com.rpay.v2.data.request.TransactionRequest
import com.rpay.v2.data.response.GetNearPayTokenResponse
import com.rpay.v2.data.response.GetTerminalsResponse
import com.rpay.v2.data.response.MachineTerminalInfoResponse
import com.rpay.v2.data.response.TerminalsDataDto
import com.rpay.v2.data.response.ValidateQrCodeInfoResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID

interface RestfulApis {

    @POST("Transaction/Create")
    suspend fun createTransaction(@Body transactionRequest: TransactionRequest): Response<UUID>

    @POST("QrCode/validate")
    suspend fun validate(@Body req: QrRequest): Response<ResponseBody>

    @POST("QrCode/charge")
    suspend fun charge(@Body req: QrRequest): Response<ResponseBody>

    @POST("QrCode/ExcuteCharge")
    suspend fun excuteCharge(@Body req: QrRequest): Response<ValidateQrCodeInfoResponse>

    @POST("QrCode/reverse")
    suspend fun reverse(@Body req: QrRequest): Response<ResponseBody>

    @POST("QrCode/ExcuteReverse")
    suspend fun excuteReverse(@Body req: QrRequest): Response<ValidateQrCodeInfoResponse>

    @POST("PosAppLogger/Create")
    suspend fun logException(@Body req: ExceptionRequest): Response<ResponseBody>


    @POST("QrCode/ValidateInfo")
    suspend fun validateAndApplyDiscount(@Body req: QrRequest): Response<ValidateQrCodeInfoResponse>

    @GET("machine/infoByTerminalSerialNo")
    suspend fun getTerminalDataBySN(
        @Query("terminalSerialNo")
        terminalSerialNo: String
    ): Response<MachineTerminalInfoResponse>


    @GET("NearPay/Terminals")
    suspend fun getTerminals(
        @Query("TerminalSN") terminalSN: String? = null,
        @Query("Tid") tid: String? = null,
        @Query("IsConnected") isConnected: Boolean? = null,
        @Query("IsAssigned") isAssigned: Boolean? = null,
        @Query("From") from: Long? = null,
        @Query("To") to: Long? = null,
        @Query("Page") page: Int? = null,
        @Query("Limit") limit: Int? = null,
        @Header("Accept-Language") language: String = "en"
    ): Response<TerminalsDataDto>

    @GET("NearPay/Token")
    suspend fun getToken(
        @Query("TerminalSN") terminalSN: String? = null,
        @Query("Tid") tid: String? = null,
        @Header("Accept-Language") language: String = "en"
    ): Response<GetNearPayTokenResponse>
}