package com.rpay.v2.presentation.dialogs.SnQrDialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rpay.v2.R
import com.rpay.v2.core.utils.QrCodeGenerator
import kotlinx.coroutines.delay


@Composable
fun SnQrCodeDialogContainer(
    isVisible: Boolean,
    serialNumber: String,
    onDismiss: () -> Unit
) {
    var seconds by remember {
        mutableStateOf(60)
    }

    // Timer
    LaunchedEffect(isVisible, serialNumber) {
        if (isVisible) {
            seconds = 60

            while (seconds > 0) {
                delay(1000)
                seconds--
            }

            onDismiss()
        }
    }

    SnQrCodeDialog(
        state = SnQrCodeDialogState(
            isVisible = isVisible,
            serialNumber = serialNumber,
            secondsRemaining = seconds
        ),
        onDismiss = {
            onDismiss()
        }
    )
}


@Composable
fun SnQrCodeDialog(
    state: SnQrCodeDialogState,
    onDismiss: () -> Unit
) {
    if (!state.isVisible) return

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        SnQrCodeDialogContent(
            serialNumber = state.serialNumber,
            seconds = state.secondsRemaining,
            onDismiss = onDismiss
        )
    }
}


@Composable
fun SnQrCodeDialogContent(
    serialNumber: String,
    seconds: Int,
    onDismiss: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    val qrBitmap = remember(serialNumber) {
        QrCodeGenerator.generate(serialNumber)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.scan_to_unlock_pos),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.scan_qr_using_portal),
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Image(
            bitmap = qrBitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.scan_qr_settings),
            modifier = Modifier
                .padding(top = 4.dp)
                .size(180.dp),
            contentScale = ContentScale.Fit
        )

        Text(
            text = stringResource(R.string.serial_number),
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        SelectionContainer {
            Text(
                text = serialNumber,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = stringResource(
                R.string.valid_for,
                String.format(
                    "%02d:%02d",
                    seconds / 60,
                    seconds % 60
                )
            ),
            modifier = Modifier.padding(top = 8.dp),
            color = Color.Red,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.primary),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.close),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun Preview_QrCodeDialog() {
    MaterialTheme {
        SnQrCodeDialogContent(
            serialNumber = "CM300012345678",
            seconds = 45,
            onDismiss = {}
        )
    }
}