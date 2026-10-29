package com.rpay.v2.domain.terminal

import android.content.Context
import com.rpay.v2.core.payment.RefactoredNearPayManager
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.data.repository.AppPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SyncTerminalCredentialsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val nearPayManager: RefactoredNearPayManager
) {
    suspend operator fun invoke(
        newTid: String?,
        token: String?
    ) {
        val oldTid = appPreferencesRepository.getDeviceTid()
        val tidChanged = newTid?.isNotBlank() == true && oldTid != newTid
        FileLogger.log(context , "SyncTerminalCredentialsUseCase" , tidChanged.toString())
        if (tidChanged) {
            nearPayManager.logoutAndWait(token!!)
            if (token.isNotBlank()) {
                nearPayManager.updateJwtAuthentication(token)
                appPreferencesRepository.setNearPayToken(token)
            }
        } else {
            if (token!!.isNotBlank()) {
                if (nearPayManager.updateJwtAuthentication(token)) {
                    appPreferencesRepository.setNearPayToken(token)
                }
            }
        }
    }
}