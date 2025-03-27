package com.eti.energysaver.ui.device

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eti.energysaver.model.Device
import com.eti.energysaver.model.DeviceType
import com.eti.energysaver.viewmodel.DeviceViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceScreen(
    onNavigateToHome: () -> Unit,
    deviceViewModel: DeviceViewModel = viewModel()
) {
    val deviceTypes by deviceViewModel.deviceTypes.collectAsState()
    var selectedDeviceType by remember { mutableStateOf<DeviceType?>(null) }
    var deviceName by remember { mutableStateOf("") }
    var actualConsumption by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Device") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDeviceType?.name ?: "Select Device Type",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    deviceTypes.forEach { deviceType ->
                        DropdownMenuItem(
                            text = { Text(deviceType.name) },
                            onClick = {
                                selectedDeviceType = deviceType
                                isExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = selectedDeviceType?.recommendedConsumption?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Recommended Consumption (kWh)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = actualConsumption,
                onValueChange = { actualConsumption = it },
                label = { Text("Actual Consumption (kWh)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Device Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (selectedDeviceType != null && deviceName.isNotEmpty() && actualConsumption.isNotEmpty()) {
                        val device = Device(
                            name = deviceName,
                            type = selectedDeviceType!!.name,
                            energyConsumption = actualConsumption.toDouble(),
                            isActive = true
                        )
                        deviceViewModel.addDevice(device)
                        onNavigateToHome()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Device")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceTypeDropdown(
    deviceTypes: List<DeviceType>,
    selectedDeviceType: DeviceType?,
    onDeviceTypeSelected: (DeviceType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedDeviceType?.name ?: "Select Device Type",
            onValueChange = { },
            label = { Text("Device Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            deviceTypes.forEach { deviceType ->
                DropdownMenuItem(
                    text = { Text(deviceType.name) },
                    onClick = {
                        onDeviceTypeSelected(deviceType)
                        expanded = false
                    }
                )
            }
        }
    }
}