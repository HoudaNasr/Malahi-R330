package com.rpay.v2.domain.hardware

interface DeviceInfoProvider {
    fun getSerialNumber(): String?
}