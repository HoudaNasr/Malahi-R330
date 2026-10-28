package com.rpay.v2.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rpay.v2.core.utils.presentation.PreviewFakeData
import com.rpay.v2.data.response.BankDto
import com.rpay.v2.data.response.LocalizedTextDto
import com.rpay.v2.data.response.MerchantDto
import com.rpay.v2.data.response.TerminalDto
import com.rpay.v2.data.response.TerminalsDataDto
import com.rpay.v2.data.response.UserDto
import com.rpay.v2.features.semnox.data.Machine

@Composable
fun TerminalsDialog(
    terminal: List<TerminalDto>,
    onSelect: (TerminalDto) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Terminal") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                terminal.forEach { terminal ->

                    val connection =
                        if (terminal.is_connected!!) "Connected" else "Offline"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onSelect(terminal) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            TerminalInfoRow("Name", terminal.name?.english ?:"null")
                            TerminalInfoRow("TID", terminal.tid?:"null")
                            TerminalInfoRow("Bank", terminal.bank?.name?:"null")
                            TerminalInfoRow("Merchant", terminal?.merchant?.name?:"null")
                            TerminalInfoRow("Email", terminal.user?.email?:"null")
                            TerminalInfoRow("Mobile", terminal.user?.mobile?:"null")
                            TerminalInfoRow("Status", connection)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
@Composable
private fun TerminalInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = value,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TerminalListPreview() {
    val fakeTerminals = listOf(
        TerminalDto(
            uuid = "uuid_1",
            tid = "123456789",
            name = LocalizedTextDto(
                english = "Main POS Terminal",
                arabic = "الجهاز الرئيسي"
            ),
            status = LocalizedTextDto(
                english = "Active",
                arabic = "نشط"
            ),
            is_connected = true,
            is_assigned_to_user = true,
            bank = BankDto("Al Rajhi Bank"),
            merchant = MerchantDto(
                id = "1",
                name = "Fun Zone Entertainment Center With Very Long Merchant Name"
            ),
            user = UserDto(
                id = "1",
                email = "very.long.email.address@example.com",
                mobile = "0501234567"
            ),
            created_at = "",
            updated_at = ""
        )
    )

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            fakeTerminals.forEach { terminal ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        TerminalInfoRow("Name", terminal.name?.english ?:"null")
                        TerminalInfoRow("TID", terminal.tid?:"null")
                        TerminalInfoRow("Bank", terminal.bank?.name?:"null")
                        TerminalInfoRow("Merchant", terminal?.merchant?.name?:"null")
                        TerminalInfoRow("Email", terminal.user?.email?:"null")
                        TerminalInfoRow("Mobile", terminal.user?.mobile?:"null")
                    }
                }
            }
        }
    }
}