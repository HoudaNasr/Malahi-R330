package com.rpay.v2.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PasswordDialog(
    isVisible: Boolean,
    purpose: PasswordPurpose?,
    errorMessage: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var password by rememberSaveable {
        mutableStateOf("")
    }

    val title = when (purpose) {
        PasswordPurpose.ADMIN -> "Admin Screen"
        PasswordPurpose.UNLOCK -> "Unlock Screen"
        null -> return
    }

    val message = when (purpose) {
        PasswordPurpose.ADMIN ->
            "Enter the password to enter admin screen."

        PasswordPurpose.UNLOCK ->
            "Enter the password to unlock screen."

        null -> return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Column {
                Text(text = message)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Password")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(password)
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
@Preview(showBackground = true)
@Composable
fun AdminPasswordDialogPreview() {
    PasswordDialog(
        isVisible = true,
        purpose = PasswordPurpose.ADMIN,
        errorMessage = null,
        onConfirm = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun UnlockPasswordDialogPreview() {
    PasswordDialog(
        isVisible = true,
        purpose = PasswordPurpose.UNLOCK,
        errorMessage = null,
        onConfirm = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true)
@Composable
fun AdminPasswordDialogErrorPreview() {
    PasswordDialog(
        isVisible = true,
        purpose = PasswordPurpose.ADMIN,
        errorMessage = "Incorrect password",
        onConfirm = {},
        onDismiss = {}
    )
}