package com.rpay.v2.domain

interface AppInfoProvider {
    val version: String
    val buildNumber: Int
    val packageName: String
}