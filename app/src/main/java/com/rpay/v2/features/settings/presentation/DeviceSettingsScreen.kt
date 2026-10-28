package com.rpay.v2.features.settings.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rpay.v2.core.utils.enums.PulseType
import com.rpay.v2.core.utils.presentation.AppProgressDialog
import com.rpay.v2.core.utils.presentation.PreviewFakeData


@Composable
fun DeviceSettingsRoute(
    timeout: Int? = null ,
    viewModel: DeviceSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onOpenAdminSettings: () -> Unit,
    onTestMdb: () -> Unit,
    onTestPulse: () -> Unit,
    onReboot: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val semnoxState by viewModel.semnoxState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(timeout) {
        viewModel.onEvent(
            DeviceSettingsEvent.SetAutoCloseTimeout(timeout)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                is DeviceSettingsUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }

                is DeviceSettingsUiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }

                DeviceSettingsUiEffect.NavigateBack -> {
                    onBack()
                }

                DeviceSettingsUiEffect.NavigateToAdminSettings -> {
                    onOpenAdminSettings()
                }
            }
        }
    }
    DeviceSettingsScreen(
        state = state,
        semnoxState = semnoxState,
        onEvent = viewModel::onEvent,
        onSave = {
            viewModel.onEvent(DeviceSettingsEvent.SaveClicked)
        },
        onBack = onBack,
        onAdminSettings = {
            viewModel.onEvent(DeviceSettingsEvent.AdminSettingsClicked)
        },
        onTestMdb = onTestMdb,
        onTestPulse = onTestPulse,
        onReboot = {
            viewModel.onEvent(DeviceSettingsEvent.RebootClicked)
        },

        )
}

@Composable
fun DeviceSettingsScreen(
    state: DeviceSettingsState,
    semnoxState: SemnoxSettingsState,
    onEvent: (DeviceSettingsEvent) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onAdminSettings: () -> Unit,
    onTestMdb: () -> Unit,
    onTestPulse: () -> Unit,
    onReboot: () -> Unit,
) {


    AppProgressDialog(
        visible = state.isLoading,
        message = "Fetching Machines....",
        cancellable = false
    )
    // 🔹 Show dialog as overlay
    if (semnoxState.showMachineDialog) {
        SemnoxMachineDialog(
            machines = semnoxState.semnoxMachines,
            onSelect = { onEvent(DeviceSettingsEvent.SemnoxMachineSelected(it)) },
            onClear = { onEvent(DeviceSettingsEvent.ClearSemnoxMachine) },
            onDismiss = { onEvent(DeviceSettingsEvent.HideMachineDialog) }
        )
    }

    PasswordDialog(
        isVisible = state.showPasswordDialog,
        purpose = state.passwordPurpose,
        errorMessage = state.passwordError,
        onConfirm = { password ->
            onEvent(
                DeviceSettingsEvent.PasswordSubmitted(password)
            )
        },
        onDismiss = {
            onEvent(
                DeviceSettingsEvent.PasswordDialogDismissed
            )
        }
    )



    // 🔹 Show dialog as overlay
    if (state.showTerminalsDialog) {
        TerminalsDialog(
            terminal = state.terminals,
            onSelect = { onEvent(DeviceSettingsEvent.TerminalSelected(it)) },
            onDismiss = { onEvent(DeviceSettingsEvent.HideTerminalDialog) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Title("Device Serial Number")
        ValueBox(value = state.deviceSn)
        DividerLine()

        // ---------------- Radio Group (Mdb / Pulse)

        TwoOptionRadioGroup(
            title1 = "MDB",
            title2 = "PULSE",
            selected = state.systemType,
            onSelect = { onEvent(DeviceSettingsEvent.SystemTypeChanged(it)) }
        )
        DividerLine()


        // ---------------- Pulse Number

        LabeledNumberField(
            label = "Plus Number",
            value = state.pulseNumber,
            onValueChange = { onEvent(DeviceSettingsEvent.PlusNumberChanged(it)) }
        )


        // ---------------- Pulse Period

        LabeledNumberField(
            label = "Pulse Period",
            value = state.pulsePeriod,
            onValueChange = { onEvent(DeviceSettingsEvent.PulsePeriodChanged(it)) }
        )


        // ---------------- Pulse Width

        LabeledNumberField(
            label = "Pulse Width",
            value = state.pulseWidth,
            onValueChange = { onEvent(DeviceSettingsEvent.PulseWidthChanged(it)) }
        )
        DividerLine()


        // ---------------- Pulse Type Group


        PulseTypeRadioGroup(
            selected = state.pulseType,
            onSelect = {
                onEvent(DeviceSettingsEvent.PulseTypeChanged(it))
            }
        )
        DividerLine()


        // ---------------- Game Cost

        LabeledNumberField(
            label = "Game Cost",
            value = state.gameCost,
            onValueChange = { onEvent(DeviceSettingsEvent.GameCostChanged(it)) }
        )
        DividerLine()


        // ---------------- Server Domain

        LabeledTextField(
            label = "Server Domain / IP",
            value = state.serverDomain,
            placeholder = "osool.server.com / 0.0.0.0",
            onValueChange = { onEvent(DeviceSettingsEvent.ServerDomainChanged(it)) }
        )


        // ---------------- TCP Port

        LabeledNumberField(
            label = "Tcp Port No",
            value = state.tcpPort,
            onValueChange = { onEvent(DeviceSettingsEvent.TcpPortChanged(it)) }
        )


        // ---------------- Device Password

        LabeledTextField(
            label = "Device Password",
            value = state.devicePassword,
            placeholder = "135246$",
            onValueChange = { onEvent(DeviceSettingsEvent.DevicePasswordChanged(it)) }
        )


        // ---------------- Mobile Number

        LabeledTextField(
            label = "Auth Mobile Number",
            value = state.mobileNumber,
            placeholder = "966558235356",
            onValueChange = { onEvent(DeviceSettingsEvent.MobileNumberChanged(it)) }
        )


        // ---------------- Receipt Wait Time

        LabeledNumberField(
            label = "Payment Receipt Waiting Seconds",
            value = state.waitDuration,
            onValueChange = { onEvent(DeviceSettingsEvent.WaitDurationChanged(it)) }
        )


        // ---------------- Checkboxes

        SettingCheckbox(
            title = "System out of service",
            checked = state.systemOutOfService,
            onCheckedChange = { onEvent(DeviceSettingsEvent.SystemOutChanged(it)) }
        )
        SettingCheckbox(
            title = "Enable auto Refund",
            checked = state.enableAutoRefund,
            onCheckedChange = { onEvent(DeviceSettingsEvent.AutoRefundChanged(it)) }
        )
        SettingCheckbox(
            title = "Enable Semnox Integration",
            checked = semnoxState.enableSemnox,
            onCheckedChange = {
                onEvent(DeviceSettingsEvent.EnableSemnoxChanged(it))
            }
        )
        SettingCheckbox(
            title = "Is Connected Terminals",
            checked = state.enableTerminalConnected,
            onCheckedChange = {
                onEvent(DeviceSettingsEvent.EnableIsConnected(it))
            }
        )
        SettingCheckbox(
            title = "Is Assigned Terminals",
            checked = state.enableTerminalAssigned,
            onCheckedChange = {
                onEvent(DeviceSettingsEvent.EnableIsAssigned(it))
            }
        )
        Button(
            onClick = {
                onEvent(DeviceSettingsEvent.UnlockDeviceClicked)
            }
        ) {
            Text("Unlock Terminal")
        }

        Spacer(Modifier.width(12.dp))


        if (semnoxState.enableSemnox) {
            SemnoxSettingsSection(
                state = state,
                semnoxState = semnoxState,
                onEvent = onEvent
            )
        }

        // ---------------- Save + Back

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                modifier = Modifier.width(100.dp),
                onClick = onSave,
            ) {
                Text("Save")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                modifier = Modifier.width(100.dp),
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF95021B)
                )
            ) {
                Text("Back")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                modifier = Modifier.wrapContentWidth(),
                onClick = onAdminSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF95021B)
                )
            ) {
                Text("Admin Settings")
            }
        }

        // ---------------- Test buttons
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            ActionButton("Test MDB", Color(0xFF4CAF50), onTestMdb)
            Spacer(Modifier.width(8.dp))
            ActionButton("Test Pulse", Color(0xFFA6A605), onTestPulse)
            Spacer(Modifier.width(8.dp))
            ActionButton("Reboot", Color(0xFFA60505), onReboot)
        }
    }
}

@Composable
fun DividerLine() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black)
        )
    }
}

@Composable
fun Title(text: String) {
    Text(
        text = text,
        fontSize = 25.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun ValueBox(value: String) {
    Text(
        text = value,
        fontSize = 25.sp,
        color = Color.Black,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .background(Color(0xFFF0F0F0))
            .padding(16.dp)
    )
}

@Composable
fun LabeledNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Text(label, fontSize = 16.sp, color = Color.Black)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Text(label, fontSize = 16.sp, color = Color.Black)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        singleLine = true
    )
}

@Composable
fun TwoOptionRadioGroup(
    title1: String,
    title2: String,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RadioButton(
                selected = selected == title1,
                onClick = { onSelect(title1) }
            )
            Text(
                text = title1,
                textAlign = TextAlign.Center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RadioButton(
                selected = selected == title2,
                onClick = { onSelect(title2) }
            )
            Text(
                text = title2,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PulseTypeRadioGroup(
    selected: PulseType,
    onSelect: (PulseType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PulseType.values().forEach { type ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RadioButton(
                    selected = selected == type,
                    onClick = { onSelect(type) }
                )
                Text(
                    text = "Pulse " + type.name.lowercase().replaceFirstChar(Char::uppercase),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@Composable
fun SettingCheckbox(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(title, color = Color.Black)
    }
}


@Composable
fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.height(50.dp)
    ) {
        Text(text)
    }
}

@Composable
fun SemnoxSettingsSection(
    state: DeviceSettingsState,
    semnoxState: SemnoxSettingsState,
    onEvent: (DeviceSettingsEvent) -> Unit
) {
    Spacer(Modifier.height(12.dp))

    Text(
        text = "Semnox Settings",
        fontSize = 18.sp,
        color = Color.Black
    )

    Spacer(Modifier.height(8.dp))

    // Base URL
    LabeledTextField(
        label = "Semnox Base API URL",
        value = semnoxState.semnoxBaseUrl,
        placeholder = "https://api.semnox.com",
        onValueChange = {
            onEvent(DeviceSettingsEvent.SemnoxBaseUrlChanged(it))
        }
    )

    // Username
    LabeledTextField(
        label = "Semnox Username",
        value = semnoxState.semnoxUsername,
        placeholder = "username",
        onValueChange = {
            onEvent(DeviceSettingsEvent.SemnoxUsernameChanged(it))
        }
    )

    // Password
    Text("Semnox Password", fontSize = 16.sp, color = Color.Black)
    OutlinedTextField(
        value = semnoxState.semnoxPassword,
        onValueChange = {
            onEvent(DeviceSettingsEvent.SemnoxPasswordChanged(it))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation()
    )

    // Selected machine
    Text("Selected Machine", fontSize = 16.sp, color = Color.Black)

    Text(
        text = semnoxState.selectedSemnoxMachineName,
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0))
            .padding(12.dp)
    )

    Spacer(Modifier.height(12.dp))

    // Buttons row
    Row {
        Button(
            onClick = {
                onEvent(DeviceSettingsEvent.FetchSemnoxMachinesClicked)
            },
            enabled = !state.isLoading
        ) {
            Text("Fetch Machines")
        }

        Spacer(Modifier.width(8.dp))

        Button(
            onClick = {
                onEvent(DeviceSettingsEvent.DisconnectSemnoxMachineClicked)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F)
            )
        ) {
            Text("Disconnect Machine")
        }
    }

    DividerLine()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DeviceSettingsScreenPreview() {
    MaterialTheme {
        DeviceSettingsScreen(
            state = PreviewFakeData.deviceSettingsState(),
            semnoxState = PreviewFakeData.semnoxStateFake(),
            onEvent = {},
            onSave = {},
            onBack = {},
            onTestMdb = {},
            onTestPulse = {},
            onReboot = {},
            onAdminSettings = {}
        )
    }
}