package com.rpay.v2.core.utils.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


@Preview(showBackground = true, backgroundColor = 0xFFEFEFEF)
@Composable
fun ResultDialogOverlaySuccessPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        ResultDialogOverlay(
            visible = true,
            icon = Icons.Default.Check,
            iconColor = Color(0xFF2AA952), // success green
            title = "Success",
            message = "Your item has been dispensed successfully.\nPlease collect your product."
        )
    }
}