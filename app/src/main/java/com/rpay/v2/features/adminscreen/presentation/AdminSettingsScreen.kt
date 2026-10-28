package com.rpay.v2.features.adminscreen.presentation


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminSettingsScreen(
    viewModel: AdminSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    AdminSettingsEffects(
        viewModel = viewModel,
        onBack = onBack
    )

    LaunchedEffect(Unit) {
        viewModel.onIntent(
            AdminSettingsContract.Intent.Init
        )
    }

    AdminSettingsContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun AdminSettingsEffects(
    viewModel: AdminSettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val connectivitySettingsLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            // When user returns from Settings, refresh the states
            viewModel.onIntent(
                AdminSettingsContract.Intent.RefreshStates
            )
        }
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            viewModel.onIntent(
                AdminSettingsContract.Intent.RefreshStates
            )
            val activity = context as Activity
            if (!granted && !activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                )
            }
        }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            viewModel.onIntent(
                AdminSettingsContract.Intent.RefreshStates
            )
        }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->

            when (effect) {

                AdminSettingsContract.UiEffect.NavigateBack -> {
                    onBack()
                }

                AdminSettingsContract.UiEffect.OpenConnectivitySettings -> {

                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }

                    try {
                        connectivitySettingsLauncher.launch(intent)

                        Log.d(
                            "AdminSettings",
                            "Connectivity settings launcher.launch() called"
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "AdminSettings",
                            "Failed to launch connectivity settings",
                            e
                        )
                    }
                }

                AdminSettingsContract.UiEffect.OpenAppPermissionSettings -> {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }

                    Log.d(
                        "AdminSettings",
                        "Intent = $intent, canResolve = ${
                            intent.resolveActivity(context.packageManager) != null
                        }"
                    )

                    try {
                        connectivitySettingsLauncher.launch(intent)

                        Log.d(
                            "AdminSettings",
                            "Application details settings launcher.launch() called"
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "AdminSettings",
                            "Failed to open application details settings",
                            e
                        )
                    }
                }

                is AdminSettingsContract.UiEffect.ShowToast -> {
                    Toast.makeText(
                        context,
                        effect.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                AdminSettingsContract.UiEffect.RequestCameraPermission -> {
                    cameraPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }

                AdminSettingsContract.UiEffect.RequestLocationPermission -> {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsContent(
    state: AdminSettingsContract.State,
    onIntent: (AdminSettingsContract.Intent) -> Unit
) {
    Scaffold(
        topBar = {
            AdminSettingsHeader(
                onBack = {
                    onIntent(
                        AdminSettingsContract.Intent.BackClicked
                    )
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                SessionSection(
                    state = state,
                    onIntent = onIntent
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                SectionTitle("Device Controls")
            }

            item {
                SwitchRow(
                    title = "Mobile Data",
                    checked = state.isMobileDataEnabled,
                    onClick = {
                        onIntent(
                            AdminSettingsContract.Intent.MobileDataClicked
                        )
                    }
                )
            }

            item {
                SwitchRow(
                    title = "Wi-Fi",
                    checked = state.isWifiEnabled,
                    onClick = {
                        onIntent(
                            AdminSettingsContract.Intent.WifiClicked
                        )
                    }
                )
            }

            item {
                SwitchRow(
                    title = "Bluetooth",
                    checked = state.isBluetoothEnabled,
                    onClick = {
                        onIntent(
                            AdminSettingsContract.Intent.BluetoothClicked
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                SectionTitle("App Permissions")
            }

            item {
                StatusRow(
                    title = "Camera",
                    enabled = state.isCameraEnabled,
                    onClick = {
                        onIntent(
                            AdminSettingsContract.Intent.CameraClicked
                        )
                    }
                )
            }

            item {
                StatusRow(
                    title = "Location",
                    enabled = state.isLocationEnabled,
                    onClick = {
                        onIntent(
                            AdminSettingsContract.Intent.LocationClicked
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                SectionTitle("Connectivity")
            }

            item {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onIntent(
                            AdminSettingsContract.Intent
                                .ConnectivitySettingsClicked
                        )
                    }
                ) {
                    Text("Connectivity Settings")
                }
            }

            item {
                PingSection(
                    pingState = state.pingState,
                    onPing = {
                        onIntent(
                            AdminSettingsContract.Intent
                                .PingInternetClicked
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }



            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

}



@Composable
private fun AdminSettingsHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Admin Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )

        Button(
            onClick = onBack,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Back")
        }
    }

    HorizontalDivider()
}

@Composable
private fun SessionSection(
    state: AdminSettingsContract.State,
    onIntent: (AdminSettingsContract.Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Session",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Session expires in ${state.sessionRemainingSeconds}s"
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedButton(
            onClick = {
                onIntent(
                    AdminSettingsContract.Intent.RefreshSessionClicked
                )
            }
        ) {
            Text("Refresh Session")
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(
            vertical = 8.dp
        )
    )
}



@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = {
                onClick()
            }
        )
    }
}

@Composable
private fun StatusRow(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (enabled) "Enabled" else "Disabled",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PingSection(
    pingState: AdminSettingsContract.PingState,
    onPing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {

        Text(
            text = "Internet Ping",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        when (pingState) {

            AdminSettingsContract.PingState.Idle -> {
                Text("Not tested")
            }

            AdminSettingsContract.PingState.Testing -> {
                Text("Testing internet connection...")
            }

            is AdminSettingsContract.PingState.Success -> {
                Text(
                    text = "Internet available - " +
                            "${pingState.latencyMs} ms"
                )
            }

            is AdminSettingsContract.PingState.Failed -> {
                Text(
                    text = pingState.message
                        ?: "Internet connection failed"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = onPing,
            enabled = pingState !=
                    AdminSettingsContract.PingState.Testing
        ) {
            Text("Test Internet")
        }
    }
}

@Composable
private fun UninstallPasswordDialog(
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var password by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Enable Uninstallation")
        },

        text = {
            Column {

                Text(
                    text = "Enter admin password to enable app uninstallation."
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Password")
                    },
                    singleLine = true,
                    visualTransformation =
                        PasswordVisualTransformation()
                )

                if (error != null) {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(password)
                }
            ) {
                Text("Confirm")
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun PreviewAdminSettingsScreen() {
    MaterialTheme {
        AdminSettingsContent(
            state = AdminSettingsContract.State(
                sessionRemainingSeconds = 72,
                sessionTimeoutSeconds = 90,
                isWifiEnabled = true,
                isMobileDataEnabled = true,
                isBluetoothEnabled = false,
                isCameraEnabled = false,
                isLocationEnabled = true,
                pingState = AdminSettingsContract.PingState.Success(
                    latencyMs = 42
                ),
                isUninstallEnabled = false
            ),
            onIntent = {}
        )
    }
}

