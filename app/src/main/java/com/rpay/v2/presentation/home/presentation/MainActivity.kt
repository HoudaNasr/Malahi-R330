package com.rpay.v2.presentation.home

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.rpay.cm30.v2.core.utils.Helper
import com.rpay.v2.core.utils.FileLogger
import com.rpay.v2.core.utils.presentation.hasLocationPermission
import com.rpay.cm30.v2.core.utils.toRoute
import com.rpay.v2.BuildConfig
import com.rpay.v2.R
import com.rpay.v2.core.payment.RefactoredNearPayManager
import com.rpay.v2.core.utils.presentation.showToast
import com.rpay.v2.data.datasource.local.AppPreferencesDataSource
import com.rpay.v2.domain.hardware.KioskManager
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import com.rpay.v2.features.systemselector.presentation.SystemSelectorActivity
import com.rpay.v2.navigation.NavGraph
import com.rpay.v2.navigation.NavigationHandler
import com.rpay.v2.navigation.NavigationManager
import com.rpay.v2.presentation.home.presentation.HomeViewModel
import com.rpay.v2.service.TcpClientService
import com.rpay.v2.service.TransactionSyncService
import com.rpay.v2.ui.theme.Wizar_posTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    private var relockJob: Job? = null

    private val viewModel: HomeViewModel by viewModels()
    lateinit var navController: NavHostController
    private var systemType: GeneralSystemType? = null

    @Inject
    lateinit var prefs: AppPreferencesDataSource

    @Inject
    lateinit var nearPayManager: RefactoredNearPayManager

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var kioskManager: KioskManager
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                viewModel.startLocationUpdates()
            }
        }

    private val terminalSettingsReceiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            val timeoutSec = intent?.getIntExtra(
                Helper.TIMEOUT_SEC, 0
            )
            onEnterTerminalSettings(
                timeoutSec!!
            )
        }
    }
    private val manageAllFilesAccessLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Log.d("StoragePermission", "All files access GRANTED")
                } else {
                    Log.e("StoragePermission", "All files access DENIED")
                }
            }
        }


    companion object {
        const val EXTRA_FROM_WATCHDOG = "from_watchdog"
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(EXTRA_FROM_WATCHDOG, false) && ::navController.isInitialized) {
            runCatching {
                navController.popBackStack(navController.graph.findStartDestination().id, inclusive = false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val initialized = initializeSystemType()
            if (!initialized) return@launch
            TransactionSyncService.startService(this@MainActivity)
            val intent = Intent(this@MainActivity, TcpClientService::class.java)
            startService(intent)
            registerReceiver()
                setContent {
                    Wizar_posTheme {
                        navController = rememberNavController()
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppContent(
                                navController = navController,
                                navigationManager = navigationManager,
                                startDestination = systemType!!.toRoute()
                            )
                            if (BuildConfig.IS_DEV_ENV) {
                                Text(
                                    text = "TEST BUILD",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFFFD1),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .rotate(-25f)
                                        .background(Color(0xAAFFB647))
                                        .padding(16.dp)
                                        .zIndex(100f)
                                )
                            }
                        }
                    }
                }
                viewModel.onAppStarted()
                if (hasLocationPermission()) {
                    viewModel.startLocationUpdates()
                } else {
                    requestLocationPermission()
                }
                onBackPressedDispatcher.addCallback(
                    this@MainActivity,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (!navController.popBackStack()) {
                                finish()
                            }
                        }
                    }
                )
                val token = prefs.getNearPayToken()

                if (!token.isNullOrBlank()) {
                    try {
                        nearPayManager.initializeIfNeeded(token)
                    }catch (e: Exception){
                        showToast("Payment Error: ${e.message ?: "Null"}")
                    }

                }
//            logPermissionState(this@MainActivity)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
        unRegisterReceiver()
    }


    override fun onResume() {
        super.onResume()
        relockJob?.cancel()
        relockJob = lifecycleScope.launch {
            delay(5_000)
            kioskManager.lock()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            !Environment.isExternalStorageManager()
        ) {
            requestAllFilesAccess()
        }
    }

    override fun onPause() {
        relockJob?.cancel()
        super.onPause()
    }

    private suspend fun initializeSystemType(): Boolean {
        val systemKey = resolveSystemKey() ?: return false
        systemType = GeneralSystemType.from(systemKey)
        return true
    }

    private suspend fun resolveSystemKey(): String? {
        val systemKey = intent.getStringExtra("EXTRA_GENERAL_SYSTEM_TYPE")
            ?: prefs.getGeneralSystemType().first()

        if (systemKey == null) {
            startActivity(
                Intent(this, SystemSelectorActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
            return null
        }
        return systemKey
    }

    fun requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                ).apply {
                    data = Uri.fromParts(
                        "package",
                        `package`,
                        null
                    )
                }
                manageAllFilesAccessLauncher.launch(intent)

            } catch (e: Exception) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                )
                manageAllFilesAccessLauncher.launch(intent)
            }
        }
    }

    fun logPermissionState(context: Context) {
        val tag = "PermCheck"
        Log.i(tag, "device=${Build.MODEL} firmware=${Build.DISPLAY} sdk=${Build.VERSION.SDK_INT}")

        // Every permission declared in the manifest, and whether it is granted
        val info = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_PERMISSIONS
        )
        val names = info.requestedPermissions ?: emptyArray()
        val flags = info.requestedPermissionsFlags ?: IntArray(0)
        names.forEachIndexed { i, name ->
            val granted = flags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0
            Log.i(tag, "$name granted=$granted")
        }

        // Overlay: ignore its line above, this is the real answer
        Log.i(tag, "overlay canDrawOverlays=${Settings.canDrawOverlays(context)}")
        val mode = context.getSystemService(AppOpsManager::class.java).unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, android.os.Process.myUid(), context.packageName
        )
        Log.i(tag, "overlay appOp mode=$mode")  // 0 allowed, 1 ignored, 2 errored, 3 default
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun onEnterTerminalSettings(timeout: Int) {
        navController.navigate("settings?timeout=$timeout")

    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this@MainActivity)
            .registerReceiver(
                terminalSettingsReceiver,
                IntentFilter(
                    Helper.ACTION_RECIEVED_TIMEOUT
                )
            )
    }

    private fun unRegisterReceiver() {
        LocalBroadcastManager.getInstance(this@MainActivity)
            .unregisterReceiver(terminalSettingsReceiver)
    }

}

@Composable
fun AppContent(
    navController: NavHostController,
    navigationManager: NavigationManager, startDestination: String
) {

    // Setup navigation handler
    NavigationHandler(
        navController = navController,
        navigationManager = navigationManager
    )

    NavGraph(
        navController = navController,
        startDestination = startDestination
    )
}



