//package com.rpay.wizar.pos.features.systemselector.presentation
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.selection.selectableGroup
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import com.rpay.wizar.pos.domain.machine.VMCMode
//import com.rpay.wizar.pos.features.systemselector.domain.GeneralSystemType
//import com.rpay.wizar.pos.presentation.dialogs.vmc_type_selection.VmcSystemTypeDialogRoute
//import com.rpay.wizar.pos.ui.components.RadioItemM3
//
//@Composable
//fun SystemsScreen(
//    onNextClicked: (GeneralSystemType) -> Unit,
//    onBack: () -> Unit,
//    viewModel: SystemsViewModel = hiltViewModel()
//) {
//    val state = viewModel.state.collectAsStateWithLifecycle()
//    var showSessionDialog by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Title - Material 3 typography
//        Text(
//            text = "Select System Type",
//            style = MaterialTheme.typography.headlineSmall,
//            color = MaterialTheme.colorScheme.onBackground
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Radio group - using manual listing to match your original design
//        Column(
//            modifier = Modifier.selectableGroup()
//        ) {
//            RadioItemM3(
//                label = "Machine Game",
//                selected = state.value.selectedType == GeneralSystemType.MACHINE_GAME,
//                onClick = { viewModel.onEvent(SystemsEvent.TypeSelected(GeneralSystemType.MACHINE_GAME)) }
//            )
//
//            RadioItemM3(
//                label = "Vending Machine",
//                selected = state.value.selectedType == GeneralSystemType.VENDING_MACHINE,
//                onClick = { viewModel.onEvent(SystemsEvent.TypeSelected(GeneralSystemType.VENDING_MACHINE)) }
//            )
//
//            RadioItemM3(
//                label = "Vending Machine QR Code",
//                visible = false,
//                selected = state.value.selectedType == GeneralSystemType.VENDING_QR,
//                onClick = { viewModel.onEvent(SystemsEvent.TypeSelected(GeneralSystemType.VENDING_QR)) }
//            )
//
//            RadioItemM3(
//                label = "Vending Machine (QR and Payment)",
//                selected = state.value.selectedType == GeneralSystemType.VENDING_QR_PAYMENT,
//                onClick = { viewModel.onEvent(SystemsEvent.TypeSelected(GeneralSystemType.VENDING_QR_PAYMENT)) }
//            )
//
//            RadioItemM3(
//                label = "Machine (QR and Payment)",
//                selected = state.value.selectedType == GeneralSystemType.MACHINE_QR_PAYMENT,
//                onClick = { viewModel.onEvent(SystemsEvent.TypeSelected(GeneralSystemType.MACHINE_QR_PAYMENT)) }
//            )
//
//            RadioItemM3(
//                label = "Machine (Semnox and Payment)",
//                selected = state.value.selectedType == GeneralSystemType.SEMNOX_MACHINE,
//                onClick = { viewModel.onEvent(SystemsEvent.TypeSelected(GeneralSystemType.SEMNOX_MACHINE)) }
//            )
//
//            RadioItemM3(
//                label = "Photo Machine",
//                selected = state.value.selectedType == GeneralSystemType.PHOTO_MACHINE,
//                onClick = { viewModel.onEvent(SystemsEvent.TypeSelected(GeneralSystemType.PHOTO_MACHINE)) },
//                visible = false
//            )
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // M3 Button
//        Button(
//            onClick = {
//                val selected = state.value.selectedType
//
//                when (selected) {
//                    GeneralSystemType.VENDING_MACHINE -> {
//                        showSessionDialog = true
//                    }
//
//                    else -> {
//                        selected?.let { onNextClicked(it) }
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            shape = MaterialTheme.shapes.medium,
//            enabled = state.value.selectedType != null
//        ) {
//            Text(
//                text = "Next",
//                style = MaterialTheme.typography.titleMedium
//            )
//        }
//    }
//
//    if (showSessionDialog) {
//        VmcSystemTypeDialogRoute(
//            onDismiss = { showSessionDialog = false },
//            onNavigateNext = { mode ->
//                showSessionDialog = false
//                when(mode) {
//                    GeneralSystemType.VMC_POS_STARTS_SESSION -> {
//                        onNextClicked(GeneralSystemType.VMC_POS_STARTS_SESSION)
//                    }
//                    GeneralSystemType.VMC_MACHINE_STARTS_SESSION -> {
//                        onNextClicked(GeneralSystemType.VMC_MACHINE_STARTS_SESSION)
//                    }
//
//                    GeneralSystemType.VENDING_QR_PAYMENT -> {
//                        onNextClicked(GeneralSystemType.VENDING_QR_PAYMENT)
//                    }
//                    else -> {}
//                }
//
//            }
//        )
//    }
//}
//
//
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewM3() {
//    MaterialTheme {
//        SystemsScreen(
//            onNextClicked = {},
//            onBack = {}
//        )
//    }
//}