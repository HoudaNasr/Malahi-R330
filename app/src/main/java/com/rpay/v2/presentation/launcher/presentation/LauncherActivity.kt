package com.rpay.v2.presentation.launcher.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.rpay.v2.data.migration.MigrateLegacyPreferencesUseCase
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.domain.repository.LegacyPreferencesRepository
import com.rpay.v2.features.systemselector.presentation.SystemSelectorActivity
import com.rpay.v2.ui.theme.Wizar_posTheme
import com.rpay.v2.presentation.home.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * LauncherActivity is the single entry point of the application.
 *
 * Responsibilities:
 * - Display a lightweight launcher (logo only)
 * - Wait for a short timeout (handled by LauncherScreen)
 * - Decide where the app should navigate next based on stored system selection
 *
 * This activity contains no business UI and no navigation graph.
 * It acts as a bootstrap gate before the real application starts.
 */
@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                proceed()
            } else {
                requestLocationPermission()
                // Stay on LauncherActivity
            }
        }

    private val launcherViewModel: LauncherViewModel by viewModels()

    /**
     * Injected preferences repository used to read the persisted system type.
     * Direct injection is intentional here — no ViewModel is required since
     * this activity has no UI state and performs a one-shot decision.
     */
    @Inject
    lateinit var prefs: AppPreferencesRepository
    @Inject
    lateinit var migrateLegacyPreferencesUseCase: MigrateLegacyPreferencesUseCase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Compose content that shows only the launcher screen (logo).
        // The screen will trigger the onTimeout callback after a fixed delay.
        setContent {
            Wizar_posTheme {
                LauncherScreen(
                )
            }
        }
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (hasLocationPermission()) {
            proceed()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Decides the next screen after the launcher timeout.
     *
     * - If no system is stored in preferences:
     *   → Open SystemSelectorActivity
     *
     * - If a system is already selected:
     *   → Open MainActivity and pass the system type as an intent extra
     *
     * This logic runs once and finishes the LauncherActivity to keep
     * the back stack clean.
     */
    private fun proceed() {
        lifecycleScope.launch {

            migrateLegacyPreferencesUseCase()

            val systemType =
                prefs.getGeneralSystemType().first()

            val intent =
                if (systemType == null) {
                    Intent(
                        this@LauncherActivity,
                        SystemSelectorActivity::class.java
                    )
                } else {
                    Intent(
                        this@LauncherActivity,
                        MainActivity::class.java
                    ).apply {
                        putExtra(
                            "EXTRA_GENERAL_SYSTEM_TYPE",
                            systemType
                        )
                    }
                }

            startActivity(intent)
            finish()
        }
    }
}