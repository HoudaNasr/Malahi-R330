package com.rpay.v2.data

import android.content.Context
import com.rpay.v2.domain.AppInfoProvider
import com.rpay.v2.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


/**
 * Provides application build information.
 *
 * This implementation retrieves version and build number
 * from the generated [BuildConfig].
 */
class AndroidAppInfoProvider @Inject constructor(@ApplicationContext private val context: Context) : AppInfoProvider {
    override val version: String = BuildConfig.VERSION_NAME
    override val buildNumber: Int = BuildConfig.VERSION_CODE
    override val packageName: String = context.packageName

}