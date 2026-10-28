package com.rpay.v2.features.systemselector.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rpay.v2.core.base.UiState
import com.rpay.v2.core.utils.presentation.rememberPosScale
import com.rpay.v2.domain.model.MachineSystemsState
import com.rpay.v2.domain.model.MainSystemType
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import com.rpay.v2.R


@Composable
fun SystemsMainContent(
    state: MachineSystemsState,
    stateForUi: State,
    onIntent: (SystemsIntent) -> Unit,
    onSystemSelected: (GeneralSystemType) -> Unit,
    modifier: Modifier = Modifier
) {
    val myBlue = colorResource(id = R.color.primary)
    val lightBlue = myBlue.copy(alpha = 0.92f)
    val scale = rememberPosScale()

    var showKioskConfirmation by remember { mutableStateOf(false) }
    if (showKioskConfirmation) {
        AlertDialog(
            onDismissRequest = { showKioskConfirmation = false },
            title = {
                Text("Confirm selection")
            },
            text = {
                Text("Do you want to select Kiosk Integration and continue?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showKioskConfirmation = false
                        onSystemSelected(GeneralSystemType.KIOSK_MACHINE)
                    }
                ) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showKioskConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp * scale)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = lightBlue,
                        shape = RoundedCornerShape(
                            topStart = 32.dp,
                            topEnd = 32.dp
                        )
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {

                    Text(
                        text = "Select System Type",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🖥️ Kiosk
                    SystemCard(
                        title = "Kiosk Integration",
                        icon = painterResource(id = R.drawable.point_of_sale),
                        selected = state.selectedMain == MainSystemType.KIOSK_MACHINE,
                        onClick = {
                            showKioskConfirmation = true
                            onIntent(SystemsIntent.MainSelected(MainSystemType.KIOSK_MACHINE))
                        }
                    )
                }
            }
        }

        // ✅ Footer at bottom
        MachineFooter(
            state = stateForUi,
            scale = scale
        )
    }
}



@Composable
fun SystemCard(
    title: String,
    icon: Painter,
    selected: Boolean,
    onClick: () -> Unit
) {
    val myBlue = colorResource(id = R.color.primary)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = myBlue
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) myBlue else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 6.dp else 2.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔹 Icon
            Icon(
                painter = icon,
                contentDescription = title,
                tint = if (selected) Color.White else myBlue,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 🔹 Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) Color.White else myBlue,
                modifier = Modifier.weight(1f)
            )

            // 🔹 Selected indicator
            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun BoxScope.MachineFooter(
    state: State,
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

        Text(
            text = "version ${state.appVersion}",
            color = textColor,
            fontSize = MaterialTheme.typography.titleMedium.fontSize * scale
        )

        if (state.deviceSn.isNotBlank()) {
            Text(
                text = state.deviceSn,
                color = textColor,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize * scale
            )
        }
    }
}

data class State(
    val appVersion: String = "",
    val deviceSn: String = "",
) : UiState

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SystemsMainContentPreview() {
    MaterialTheme {

        val fakeState = MachineSystemsState(
            selectedMain = MainSystemType.KIOSK_MACHINE
        )

        SystemsMainContent(
            state = fakeState,
            onIntent = {},
            stateForUi = State(),
            onSystemSelected = {}, // ✅ added
            modifier = Modifier.fillMaxSize()
        )
    }
}