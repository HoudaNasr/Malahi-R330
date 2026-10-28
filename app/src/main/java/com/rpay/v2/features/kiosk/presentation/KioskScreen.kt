package com.rpay.v2.features.kiosk.presentation


import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rpay.v2.core.utils.presentation.ResultDialogOverlay
import com.rpay.v2.core.utils.presentation.rememberPosScale
import com.rpay.v2.domain.machine.OutOfServiceReason
import com.rpay.v2.domain.machine.OutOfServiceUi
import kotlinx.coroutines.delay
import com.rpay.v2.R
import com.rpay.v2.domain.config.UiMode
import com.rpay.v2.features.kiosk.presentation.KioskScreenViewModel.IncomingPaymentRequest


@Composable
fun KioskScreen(
    viewModel: KioskScreenViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()


    state.outOfServiceReason?.let { reason ->
        OutOfServiceScreen(
            reason = reason,
            state = state,
            onIntent = viewModel::onIntent
        )
        return
    }

    KioskSideEffects(
        state = state,
        viewModel = viewModel,
        onOpenSettings = onOpenSettings
    )

    Box(modifier = Modifier.fillMaxSize()) {

        when (state.dialogState) {
            KioskScreenContract.DialogState.VendSuccess -> {
                ResultDialogOverlay(
                    visible = true,
                    icon = Icons.Default.Check,
                    iconColor = colorResource(R.color.success),
                    message = "Your item has been dispensed successfully.\nPlease collect your product."
                )
            }

            KioskScreenContract.DialogState.VendFailure -> {
                ResultDialogOverlay(
                    visible = true,
                    icon = Icons.Default.Close,
                    iconColor = colorResource(R.color.error),
                    message = "The item could not be dispensed.\nYour payment will be reversed."
                )
            }

            KioskScreenContract.DialogState.None -> Unit
        }

        PaymentRequestWaitingContent(
            state = state,
            onIntent = viewModel::onIntent
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PaymentRequestWaitingContent(
    state: KioskScreenContract.State,
    onIntent: (KioskScreenContract.Intent) -> Unit
) {
    val scale = rememberPosScale()
    val logoSize =
        if (state.uiScale == UiMode.LARGE) 220.dp * scale
        else 180.dp * scale

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(logoSize)
                .align(Alignment.CenterHorizontally)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onIntent(KioskScreenContract.Intent.LogoLongPressed)
                        }
                    )
                }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = colorResource(R.color.primary),
                    shape = RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.waiting_for_payment_request),
                    color = Color.White,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize * scale,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp * scale))

                Text(
                    text = stringResource(R.string.payment_request_received_message),
                    color = Color.White,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * scale,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val footerFontSize =
                    if (state.uiScale == UiMode.LARGE) {
                        MaterialTheme.typography.titleMedium.fontSize * scale
                    } else {
                        MaterialTheme.typography.titleSmall.fontSize
                    }

                Text(
                    text = "Version ${state.appVersion}",
                    color = Color.White,
                    fontSize = footerFontSize
                )

                if (state.deviceSn.isNotBlank()) {
                    Text(
                        text = state.deviceSn,
                        color = Color.White,
                        fontSize = footerFontSize
                    )
                }

                if (state.deviceTid.isNotBlank()) {
                    Text(
                        text = "TID: ${state.deviceTid}",
                        color = Color.White,
                        fontSize = footerFontSize
                    )
                }
            }
        }

        if (state.showPasswordDialog) {
            PasswordDialog(
                error = state.passwordError,
                onDismiss = {
                    onIntent(KioskScreenContract.Intent.PasswordDialogDismissed)
                },
                onSubmit = { password ->
                    onIntent(KioskScreenContract.Intent.PasswordSubmitted(password))
                }
            )
        }
    }
}

@Composable
private fun KioskSideEffects(
    viewModel: KioskScreenViewModel,
    state: KioskScreenContract.State,
    onOpenSettings: () -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(state.dialogState) {
        if (state.dialogState != KioskScreenContract.DialogState.None) {
            delay(3_000)
            viewModel.onIntent(KioskScreenContract.Intent.ClearDialog)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collect { effect ->
            when (effect) {
                KioskScreenContract.UiEffect.NavigateToSettings -> {
                    onOpenSettings()
                }

                is KioskScreenContract.UiEffect.ShowToast -> {
                    Toast.makeText(
                        context,
                        effect.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is KioskScreenContract.UiEffect.getTid -> {
                    viewModel.getNearPayTid()
                }
            }
        }
    }
}

@Composable
fun OutOfServiceScreen(
    reason: OutOfServiceReason,
    state: KioskScreenContract.State,
    onIntent: (KioskScreenContract.Intent) -> Unit,
    scale: Float = 1f
) {

    val ui = when (reason) {
        OutOfServiceReason.MAINTENANCE ->
            OutOfServiceUi(
                "Machine Under Maintenance",
                stringResource(R.string.maintenance),
                R.drawable.maintenance
            )

        OutOfServiceReason.SUBSCRIPTION_EXPIRED ->
            OutOfServiceUi(
                "Subscription Expired",
                stringResource(R.string.subscription_expired),
                R.drawable.stoppedworking
            )

        OutOfServiceReason.NOT_CONNECTED -> {
            OutOfServiceUi(
                "USB cable Isn't Connected",
                stringResource(R.string.pos_not_linked_to_kiosk),
                R.drawable.terminal_not_linked_to_machine
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔹 Logo at top
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(140.dp * scale)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                onIntent(KioskScreenContract.Intent.LogoLongPressed)
                            }
                        )
                    }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 🔹 Maintenance / Subscription icon
            Image(
                painter = painterResource(id = ui.icon),
                contentDescription = null,
                modifier = Modifier.size(80.dp * scale)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = ui.title,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(
                    fontWeight = FontWeight.Bold
                ),
                fontSize = MaterialTheme.typography.headlineMedium.fontSize * scale
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ui.message,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(
                    fontWeight = FontWeight.Bold
                ),
                fontSize = MaterialTheme.typography.bodyMedium.fontSize * scale
            )
        }

        MachineFooter(
            state = state,
            scale = scale,
            textColor = colorResource(id = R.color.primary)
        )
    }
}

@Composable
fun BoxScope.MachineFooter(
    state: KioskScreenContract.State,
    scale: Float = 1f,
    textColor: Color = Color.White
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = 10.dp * scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val fontSize =
            if (state.uiScale == UiMode.LARGE) MaterialTheme.typography.titleLarge.fontSize * scale
            else MaterialTheme.typography.titleMedium.fontSize

        Text(
            text = "version ${state.appVersion}",
            color = textColor,
            fontSize = MaterialTheme.typography.titleMedium.fontSize * scale
        )

        if (state.deviceSn.isNotBlank()) {
            Text(
                text = state.deviceSn,
                color = textColor,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize * scale
            )
        }

        if (state.deviceTid.isNotBlank()) {
            Text(
                text = "TID: ${state.deviceTid}",
                color = textColor,
                fontSize = MaterialTheme.typography.bodySmall.fontSize * scale
            )
        }
    }
}


@Composable
private fun PasswordDialog(
    error: String?,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Password Required") },
        text = {
            Column {
                Text("Please enter the password to access settings.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter Password") },
                    singleLine = true
                )
                if (!error.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(password) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewKioskScreen() {
    MaterialTheme {
        PaymentRequestWaitingContent (
            state = fakeKioskMachineState(),
            onIntent = {},
        )
    }
}

private fun fakeKioskMachineState() = KioskScreenContract.State(
    isProcessing = false,
    showPasswordDialog = false,
    passwordError = null,
    appVersion = "1.0.0",
    deviceSn = "SN-123456",
    deviceTid = "TID-987654"
)
