package com.eti.energysaver.ui.device

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eti.energysaver.model.Device
import com.eti.energysaver.model.DeviceSettings
import com.eti.energysaver.viewmodel.DeviceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eti.energysaver.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceId: String,
    onNavigateBack: () -> Unit,
    deviceViewModel: DeviceViewModel = viewModel()
) {
    var device by remember { mutableStateOf<Device?>(null) }
    var deviceName by remember { mutableStateOf("") }
    var actualConsumption by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var isActive by remember { mutableStateOf(true) }
    var isMandatory by remember { mutableStateOf(false) }
    val devices by deviceViewModel.devicesFlow.collectAsState()
    var co2EmissionText by remember { mutableStateOf(device?.settings?.co2Emission?.toString() ?: "0.0") }
    // Завантаження даних пристрою за ID та асинхронне отримання рекомендованого порогу
    LaunchedEffect(deviceId, devices) {
        device = devices.find { it.id == deviceId }
        device?.let { d ->
            deviceName = d.name
            actualConsumption = d.energyConsumption.toString()
            isActive = d.isActive
            isMandatory = d.isMandatory
            // Якщо threshold не встановлено, використовуємо рекомендоване значення з DeviceTypeRepository через ViewModel
            threshold = d.settings?.threshold?.toString() ?: deviceViewModel.getRecommendedConsumption(d.type)
            notificationsEnabled = d.settings?.notificationsEnabled ?: true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Details & Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Eco background image
        Image(
            painter = painterResource(id = R.drawable.green_leaf),
            contentDescription = "Eco Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.05f
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Device Details
            Text("Device Details", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Device Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = actualConsumption,
                onValueChange = { actualConsumption = it },
                label = { Text("Actual Consumption (kWh)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Divider()
            // Section: Device Settings
            Text("Device Settings", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = threshold,
                onValueChange = { threshold = it },
                label = { Text("Threshold (kWh)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = co2EmissionText,
                onValueChange = { co2EmissionText = it },
                label = { Text("CO₂ Emission (kg/kWh)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Notifications")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFA5D6A7),
                        checkedTrackColor = Color(0xFFA5D6A7).copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isActive) "Active" else "Inactive")
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = isActive,
                        onCheckedChange = { newValue -> isActive = newValue },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFA5D6A7),
                            checkedTrackColor = Color(0xFFA5D6A7).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isMandatory) "Mandatory" else "Optional")
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = isMandatory,
                        onCheckedChange = { newValue -> isMandatory = newValue },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFA5D6A7),
                            checkedTrackColor = Color(0xFFA5D6A7).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }
            // Buttons: Save Changes (left) and Delete Device (right)
            if (device != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            val updatedDevice = device?.copy(
                                name = deviceName,
                                energyConsumption = actualConsumption.toDoubleOrNull() ?: 0.0,
                                settings = DeviceSettings(
                                    threshold = threshold.toDoubleOrNull() ?: 0.0,
                                    notificationsEnabled = notificationsEnabled,
                                    co2Emission = co2EmissionText.toDoubleOrNull() ?: 0.0
                                )
                            )
                            if (updatedDevice != null) {
                                deviceViewModel.updateDevice(updatedDevice)
                            }
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Changes")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            deviceViewModel.deleteDevice(device!!.id)
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete Device")
                    }
                }
            }
        }
    }
}
