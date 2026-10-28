package com.rpay.v2.domain.repository

interface NetworkRepository {
    suspend fun pingInternet(): Result<Long>
}