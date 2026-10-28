package com.rpay.v2.data.mapper

import com.google.gson.Gson
import com.rpay.v2.data.model.entity.TransactionExtendInfo
import com.rpay.v2.domain.AppInfoProvider
import com.rpay.v2.domain.model.QrDiscountType
import javax.inject.Inject

class TransactionInfoMapper @Inject constructor(
    private val gson: Gson,
    private val appInfoProvider: AppInfoProvider
) {
    fun map(flow: QrDiscountType): String? {
        return when (flow) {
            is QrDiscountType.None -> null
            is QrDiscountType.Applied -> gson.toJson(
                TransactionExtendInfo(flow.qrHistoryOperationId , appPackageName = appInfoProvider.packageName)
            )
        }
    }
}