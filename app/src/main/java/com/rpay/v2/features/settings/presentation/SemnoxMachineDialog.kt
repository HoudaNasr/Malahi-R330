package com.rpay.v2.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rpay.v2.core.utils.presentation.PreviewFakeData
import com.rpay.v2.features.semnox.data.Machine

@Composable
fun SemnoxMachineDialog(
    machines: List<Machine>,
    onSelect: (Machine) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Machine") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                machines.forEach { machine ->
                    val price = machine.price?.let { " - $it SAR" } ?: ""
                    Text(
                        text = "${machine.machineName}$price",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(machine) }
                            .padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Row {
                TextButton(onClick = onClear) {
                    Text("Clear Selection")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun SemnoxMachineDialogPreview() {
    MaterialTheme {
        SemnoxMachineDialog(
            machines = PreviewFakeData.fakeSemnoxMachines(),
            onSelect = { },
            onClear = { },
            onDismiss = { }
        )
    }
}