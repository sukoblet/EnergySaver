package com.eti.energysaver.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eti.energysaver.R
import com.eti.energysaver.model.ConsumptionRecord
import com.eti.energysaver.model.Device
import com.eti.energysaver.model.TimePeriod
import com.eti.energysaver.model.EcoStats
import com.eti.energysaver.ui.analytics.AnalyticsScreen
import com.eti.energysaver.ui.settings.SettingsScreen
import com.eti.energysaver.viewmodel.DeviceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

sealed class HomeTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : HomeTab("Home", Icons.Default.Home)
    object Analytics : HomeTab("Analytics", Icons.Default.Info)
    object Devices : HomeTab("Devices", Icons.AutoMirrored.Filled.List)
    object Settings : HomeTab("Settings", Icons.Default.Settings)
}

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddDevice: () -> Unit,
    onNavigateToDeviceDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    deviceViewModel: DeviceViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Home) }

    // Get data from the ViewModel
    val devices by deviceViewModel.devicesFlow.collectAsState()
    val totalEnergy by deviceViewModel.totalEnergyConsumption.collectAsState()
    val topDevices by deviceViewModel.topDevices.collectAsState()

    // Змінна для вибору періоду типу TimePeriod (для симуляції)
    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.LAST_24_HOURS) }
    // Змінна для UI-дропдауна (рядок)
    var selectedPeriodString by remember { mutableStateOf("Now") }
    val periodOptions = listOf("Now", "Day", "Week", "Month")
    var dropdownExpanded by remember { mutableStateOf(false) }

    // overallRecords і highestRecords використовують selectedTimePeriod (можна налаштувати перетворення, якщо потрібно)
    val overallRecords = remember(selectedTimePeriod, deviceViewModel.devicesFlow.value) {
        deviceViewModel.simulateConsumptionRecords(selectedTimePeriod).reducePoints()
    }
    val highestDevice = devices.maxByOrNull { it.energyConsumption }
    val highestRecords = remember(selectedTimePeriod, highestDevice) {
        highestDevice?.let { simulateSingleDeviceRecords(it, selectedTimePeriod).reducePoints() } ?: emptyList()
    }

    var snackMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    fun performOptimization() {
        deviceViewModel.optimizeDevices()
        snackMessage = "Optimization complete: non-mandatory devices deactivated"
    }

    var showEnergyGraph by remember { mutableStateOf(false) }
    var showMoneyGraph by remember { mutableStateOf(false) }
    var showCO2Graph by remember { mutableStateOf(false) }

    // Для демонстрації – приклад значень
    val dailyUsage = 120.0   // Replace with computed value
    val weeklyUsage = 840.0  // Replace with computed value
    val monthlyUsage = 3600.0 // Replace with computed value

    // Оновлення ecoStats на основі вибраного UI-періоду (selectedPeriodString)
    var ecoStats by remember { mutableStateOf<EcoStats>(EcoStats()) }
    LaunchedEffect(selectedPeriodString, devices) {
        ecoStats = deviceViewModel.getEcoStatsForPeriod(selectedPeriodString, devices)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Energy Saver",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Left
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddDevice) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Device")
            }
        },
        bottomBar = {
            NavigationBar {
                listOf(HomeTab.Home, HomeTab.Analytics, HomeTab.Devices, HomeTab.Settings).forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.green_leaf),
                contentDescription = "Eco Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.05f
            )
            when (selectedTab) {
                HomeTab.Home -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Section 1: Top 5 Energy Consuming Devices
                        item {
                            Text(
                                text = "Top 5 Energy Consuming Devices",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val sortedDevices = devices.sortedByDescending { it.energyConsumption }.take(5)
                                sortedDevices.forEach { device ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val textColor = if (device.isActive) Color.Black else Color.Gray
                                        Text(
                                            text = device.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${device.energyConsumption} kWh",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (!device.isActive) {
                                            Text(
                                                text = "deactivated",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        if (device.isMandatory) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Top Device",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.weight(0.3f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.LightGray)
                            )
                        }
                        // Section 2: Real-Time Consumption & Optimization
                        item {
                            Text(
                                text = "Real-Time Consumption",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Current Consumption: $totalEnergy kWh",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { performOptimization() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5D6A7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Optimize")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Optimize Devices",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.LightGray)
                            )
                        }
                        // Section 3: Overall Usage Summary
                        item {
                            Text(
                                text = "Overall Usage",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Card(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .defaultMinSize(minWidth = 80.dp, minHeight = 40.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFA5D6A7))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("Daily", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "$dailyUsage kWh",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .defaultMinSize(minWidth = 80.dp, minHeight = 40.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFA5D6A7))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("Weekly", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "$weeklyUsage kWh",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .defaultMinSize(minWidth = 80.dp, minHeight = 40.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFA5D6A7))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("Monthly", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "$monthlyUsage kWh",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                        // Section 4: Overall Savings with Period Selection
                        item {
                            Text(
                                text = "Overall Savings",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Drop-down for period selection
                            ExposedDropdownMenuBox(
                                expanded = dropdownExpanded,
                                onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedPeriodString,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Period") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    periodOptions.forEach { period ->
                                        DropdownMenuItem(
                                            text = { Text(period) },
                                            onClick = {
                                                selectedPeriodString = period
                                                dropdownExpanded = false
                                                // Перетворюємо вибраний рядок на TimePeriod, якщо потрібно,
                                                // або використовуємо рядок як параметр для getEcoStatsForPeriod
                                                ecoStats = deviceViewModel.getEcoStatsForPeriod(selectedPeriodString, devices)
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            // Clickable cards for each eco stat
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Card(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .defaultMinSize(minWidth = 80.dp, minHeight = 40.dp)
                                        .clickable { showEnergyGraph = true },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (ecoStats.energySavings == 0.0) Color(0xFFD6A5A5) else Color(0xFFA5D6A7)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Energy",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = String.format("%.2f kWh", ecoStats.energySavings),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                // Картка "Money"
                                Card(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .defaultMinSize(minWidth = 80.dp, minHeight = 40.dp)
                                        .clickable { showMoneyGraph = true },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (ecoStats.moneySavings == 0.0) Color(0xFFD6A5A5) else Color(
                                            0xFFA5D6A7
                                        )
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Money",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = String.format("%.2f PLN", ecoStats.moneySavings),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

// Картка "CO₂"
                                Card(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .defaultMinSize(minWidth = 80.dp, minHeight = 40.dp)
                                        .clickable { showCO2Graph = true },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (ecoStats.co2Savings == 0.0) Color(0xFFD6A5A5) else Color(0xFFA5D6A7)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "CO₂",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = String.format("%.2f kg", ecoStats.co2Savings),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
                HomeTab.Analytics -> {
                    AnalyticsScreen()
                }
                HomeTab.Devices -> {
                    DevicesTab(
                        devices = devices,
                        onDeviceClick = onNavigateToDeviceDetail,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
                HomeTab.Settings -> {
                    SettingsScreen(onNavigateBack = { /* For example, set Home as active tab */ })
                }
            }
        }
        if (showEnergyGraph) {
            AlertDialog(
                onDismissRequest = { showEnergyGraph = false },
                title = { Text("Energy Savings Graph") },
                text = {
                    // Відображення стовпчикової діаграми для енергозбереження
                    ColumnChart(
                        baseValue = ecoStats.energySavings,
                        period = selectedPeriodString, // використовується рядок з дропдауна
                        unit = "kWh"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showEnergyGraph = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showMoneyGraph) {
            AlertDialog(
                onDismissRequest = { showMoneyGraph = false },
                title = { Text("Money Savings Graph") },
                text = {
                    ColumnChart(
                        baseValue = ecoStats.moneySavings,
                        period = selectedPeriodString,
                        unit = "PLN"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showMoneyGraph = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showCO2Graph) {
            AlertDialog(
                onDismissRequest = { showCO2Graph = false },
                title = { Text("CO₂ Savings Graph") },
                text = {
                    ColumnChart(
                        baseValue = ecoStats.co2Savings,
                        period = selectedPeriodString,
                        unit = "kg"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showCO2Graph = false }) {
                        Text("Close")
                    }
                }
            )
        }
        if (snackMessage.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { snackMessage = "" },
                title = { Text("Info", textAlign = TextAlign.Center) },
                text = { Text(snackMessage, textAlign = TextAlign.Center) },
                confirmButton = {
                    TextButton(onClick = { snackMessage = "" }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

/**
 * Helper function to reduce the number of data points by selecting every other point.
 */
fun List<ConsumptionRecord>.reducePoints(): List<ConsumptionRecord> {
    return this.filterIndexed { index, _ -> index % 2 == 0 }
}

/**
 * Function to simulate energy consumption data for a device.
 */
fun simulateSingleDeviceRecords(device: Device, period: TimePeriod): List<ConsumptionRecord> {
    val now = System.currentTimeMillis()
    return List(period.hours.toInt()) { i ->
        val variation = 0.9 + Math.random() * 0.2
        val consumption = device.energyConsumption * variation
        val timestamp = now - ((period.hours - 1 - i) * 3600 * 1000L)
        ConsumptionRecord(timestamp, consumption)
    }
}

@Composable
fun ColumnChart(baseValue: Double, period: String, unit: String) {
    // Генеруємо 8 точок із варіацією ±25%
    val dataPoints = remember(baseValue) {
        // Якщо baseValue == 0, всі точки будуть 0
        if (baseValue == 0.0) List(8) { 0.0 }
        else List(8) {
            baseValue * (0.75 + Math.random() * 0.5) // від 0.75 * baseValue до 1.25 * baseValue
        }
    }
    // Знаходимо максимальне значення для масштабування, якщо всі 0, effectiveMax = 1.0
    val maxValue = dataPoints.maxOrNull() ?: 0.0
    val effectiveMax = if (maxValue == 0.0) 1.0 else maxValue

    // Розрахунок Y-міток: наприклад, 5 міток (0, 25%, 50%, 75%, 100%)
    val numberOfYLabels = 5
    val yLabels = List(numberOfYLabels) { index ->
        (effectiveMax * index / (numberOfYLabels - 1))
    }

    // Розрахунок X-міток – припустимо, що для періоду "Now" та "Day" діапазон = 24 години, кожна колонка ~ 3 години
    val xLabels = List(8) { index -> "${index * 3}h" }

    // Малюємо діаграму за допомогою Canvas
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Відступ для Y-осі
        val yAxisOffset = 40.dp.toPx()
        // Обчислюємо ширину кожного стовпчика (залишається 8 колонок)
        val barAreaWidth = canvasWidth - yAxisOffset
        val barWidth = barAreaWidth / (dataPoints.size * 2f)

        // Намалювати Y-вісь
        drawLine(
            color = Color.Gray,
            start = Offset(x = yAxisOffset, y = 0f),
            end = Offset(x = yAxisOffset, y = canvasHeight),
            strokeWidth = 2f
        )
        // Намалювати мітки для Y-осі
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                textSize = 30f
                color = android.graphics.Color.BLACK
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            yLabels.forEachIndexed { index, value ->
                val y = canvasHeight - (canvasHeight * index / (numberOfYLabels - 1))
                drawText(String.format("%.1f %s", value, unit), yAxisOffset - 8f, y, paint)
            }
        }

        // Малюємо стовпчики
        dataPoints.forEachIndexed { index, value ->
            val x = yAxisOffset + index * 2 * barWidth + barWidth / 2
            val barHeight = (value / effectiveMax * canvasHeight)
            drawRect(
                color = Color(0xFFA5D6A7),
                topLeft = Offset(x, (canvasHeight - barHeight).toFloat()),
                size = Size(width = barWidth, height = barHeight.toFloat())
            )
            // Намалювати X-мітку
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    textSize = 30f
                    color = android.graphics.Color.BLACK
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(xLabels[index], x + barWidth / 2, canvasHeight + 30f, paint)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesTab(
    devices: List<Device>,
    onDeviceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(devices) { device ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        if (!device.isActive) Color.LightGray
                        else MaterialTheme.colorScheme.surface
                    ),
                onClick = { onDeviceClick(device.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = device.name, style = MaterialTheme.typography.headlineSmall)
                        Text(text = "Type: ${device.type}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Consumption: ${device.energyConsumption} kWh",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (device.isActive) "Active" else "Inactive",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (device.isMandatory) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Mandatory",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}