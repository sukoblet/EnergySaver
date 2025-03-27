package com.eti.energysaver.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eti.energysaver.R
import com.eti.energysaver.viewmodel.DeviceViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    deviceViewModel: DeviceViewModel = viewModel()
) {
    var dailyThreshold by remember { mutableStateOf("") }
    var weeklyThreshold by remember { mutableStateOf("") }
    var monthlyThreshold by remember { mutableStateOf("") }
    var costCoefficient by remember { mutableStateOf("1.5") }
    //var updateInterval by remember { mutableStateOf("1") }
    var localNotificationsEnabled by remember { mutableStateOf(true) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var autoOptimizationEnabled by remember { mutableStateOf(false) }
    var updateInterval by remember { mutableStateOf("5") }// в хвилинах

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.green_leaf),
                contentDescription = "Eco Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.05f
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("EcoStats Settings", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                OutlinedTextField(
                    value = costCoefficient,
                    onValueChange = { costCoefficient = it },
                    label = { Text("Cost coefficient") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Energy Savings Settings", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = dailyThreshold,
                    onValueChange = { dailyThreshold = it },
                    label = { Text("Daily Threshold (kWh)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weeklyThreshold,
                    onValueChange = { weeklyThreshold = it },
                    label = { Text("Weekly Threshold (kWh)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = monthlyThreshold,
                    onValueChange = { monthlyThreshold = it },
                    label = { Text("Monthly Threshold (kWh)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notifications & Update Interval", style = MaterialTheme.typography.headlineSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Local Notifications")
                    Switch(
                        checked = localNotificationsEnabled,
                        onCheckedChange = { localNotificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFA5D6A7),
                            checkedTrackColor = Color(0xFFA5D6A7).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Push Notifications")
                    Switch(
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { pushNotificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFA5D6A7),
                            checkedTrackColor = Color(0xFFA5D6A7).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )

                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto Optimization")
                    Switch(
                        checked = autoOptimizationEnabled,
                        onCheckedChange = { autoOptimizationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFA5D6A7),
                            checkedTrackColor = Color(0xFFA5D6A7).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
                OutlinedTextField(
                    value = updateInterval,
                    onValueChange = { updateInterval = it },
                    label = { Text("Update Interval (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        saveSettings(
                            dailyThreshold,
                            weeklyThreshold,
                            monthlyThreshold,
                            localNotificationsEnabled,
                            pushNotificationsEnabled,
                            autoOptimizationEnabled,
                            updateInterval
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Settings")
                }
            }
        }
    }
}

private fun saveSettings(
    dailyThreshold: String,
    weeklyThreshold: String,
    monthlyThreshold: String,
    localNotificationsEnabled: Boolean,
    pushNotificationsEnabled: Boolean,
    autoOptimizationEnabled: Boolean,
    updateInterval: String
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val settings = mapOf(
        "dailyThreshold" to dailyThreshold.toDoubleOrNull(),
        "weeklyThreshold" to weeklyThreshold.toDoubleOrNull(),
        "monthlyThreshold" to monthlyThreshold.toDoubleOrNull(),
        "localNotificationsEnabled" to localNotificationsEnabled,
        "pushNotificationsEnabled" to pushNotificationsEnabled,
        "autoOptimizationEnabled" to autoOptimizationEnabled,
        "updateInterval" to updateInterval.toIntOrNull()
    )
    FirebaseDatabase.getInstance().getReference("users")
        .child(userId)
        .child("settings")
        .setValue(settings)
}
