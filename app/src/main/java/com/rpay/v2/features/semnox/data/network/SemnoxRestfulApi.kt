package com.rpay.v2.features.semnox.data.network

import com.rpay.v2.features.semnox.data.GamePlayData
import com.rpay.v2.features.semnox.data.GamePlayRequest
import com.rpay.v2.features.semnox.data.LoginRequest
import com.rpay.v2.features.semnox.data.LoginResponse
import com.rpay.v2.features.semnox.data.Machine
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface   SemnoxRestfulApi {
    @POST("api/External/Login/AuthenticateUsers")
    suspend fun authenticateUser(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @GET("api/External/Game/Machines")
    suspend fun getMachines(
        @Header("Authorization") authorization: String
    ): Response<List<Machine>>

    @POST("api/External/Game/GamePlays")
    suspend fun triggerGamePlay(
        @Header("Authorization") authorization: String,
        @Body gamePlayRequests: List<GamePlayRequest>
    ): Response<List<GamePlayData>>
}