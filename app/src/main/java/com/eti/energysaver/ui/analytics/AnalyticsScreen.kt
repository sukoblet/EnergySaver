package com.eti.energysaver.ui.analytics

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eti.energysaver.model.ConsumptionRecord
import com.eti.energysaver.model.Device
import com.eti.energysaver.model.TimePeriod
import com.eti.energysaver.viewmodel.DeviceViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eti.energysaver.R

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    deviceViewModel: DeviceViewModel = viewModel()
) {
    // Дропдаун для загального графіка: вибір періоду
    var overallPeriodExpanded by remember { mutableStateOf(false) }
    var overallPeriod by remember { mutableStateOf(TimePeriod.LAST_24_HOURS) }
    val periodOptions = TimePeriod.values().toList()

    // Дропдаун для вибору пристрою (для пристрою, що аналізується)
    val devices by deviceViewModel.devicesFlow.collectAsState()
    var deviceDropdownExpanded by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<Device?>(if (devices.isNotEmpty()) devices[0] else null) }

    // Дропдаун для вибору періоду для пристрою
    var devicePeriodExpanded by remember { mutableStateOf(false) }
    var devicePeriod by remember { mutableStateOf(TimePeriod.LAST_24_HOURS) }

    // Для дебагу
    println("Period options: $periodOptions")
    println("Devices: $devices")

    // Генеруємо симульовані дані для загального графіка (усі активні пристрої)
    val overallRecords = remember(overallPeriod, deviceViewModel.devicesFlow.value) {
        deviceViewModel.simulateConsumptionRecords(overallPeriod).resamplePoints(12)
    }
    // Генеруємо симульовані дані для графіка вибраного пристрою
    val deviceRecords = remember(selectedDevice, devicePeriod) {
        selectedDevice?.let { simulateSingleDeviceRecords(it, devicePeriod).resamplePoints(12) } ?: emptyList()
    }

    // Змінено формат, щоб відображався день, місяць та година
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Analytics", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            )
        }
    ) { paddingValues ->
        Image(
            painter = painterResource(id = R.drawable.green_leaf),
            contentDescription = "Eco Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.05f
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Розділ 1: Overall Energy Consumption Graph
            item {
                Text(
                    text = "Overall Energy Consumption",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Dropdown для вибору періоду загального графіка
                ExposedDropdownMenuBox(
                    expanded = overallPeriodExpanded,
                    onExpandedChange = { overallPeriodExpanded = !overallPeriodExpanded },
                ) {
                    OutlinedTextField(
                        value = overallPeriod.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Period") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = overallPeriodExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = overallPeriodExpanded,
                        onDismissRequest = { overallPeriodExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        periodOptions.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.label) },
                                onClick = {
                                    overallPeriod = period
                                    overallPeriodExpanded = false
                                    println("Selected overall period: $period")
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Графік для загального споживання – Box з висотою 150.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    NonInteractiveConsumptionLineChart(
                        consumptionRecords = overallRecords,
                        dateFormat = dateFormat
                    )
                }
            }
            // Розділ 2: Device-Specific Energy Consumption Graph
            item {
                Text(
                    text = "Device-Specific Energy Consumption",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Dropdown для вибору пристрою
                ExposedDropdownMenuBox(
                    expanded = deviceDropdownExpanded,
                    onExpandedChange = { deviceDropdownExpanded = !deviceDropdownExpanded },
                ) {
                    OutlinedTextField(
                        value = selectedDevice?.name ?: "Select Device",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Device") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviceDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = deviceDropdownExpanded,
                        onDismissRequest = { deviceDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        devices.forEach { device ->
                            DropdownMenuItem(
                                text = { Text(device.name) },
                                onClick = {
                                    selectedDevice = device
                                    deviceDropdownExpanded = false
                                    println("Selected device: ${device.name}")
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Dropdown для вибору періоду для пристрою
                ExposedDropdownMenuBox(
                    expanded = devicePeriodExpanded,
                    onExpandedChange = { devicePeriodExpanded = !devicePeriodExpanded },
                ) {
                    OutlinedTextField(
                        value = devicePeriod.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Period") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = devicePeriodExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = devicePeriodExpanded,
                        onDismissRequest = { devicePeriodExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        periodOptions.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period.label) },
                                onClick = {
                                    devicePeriod = period
                                    devicePeriodExpanded = false
                                    println("Selected device period: $period")
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Графік для вибраного пристрою – Box з висотою 150.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    if (selectedDevice != null) {
                        NonInteractiveConsumptionLineChart(
                            consumptionRecords = deviceRecords,
                            dateFormat = dateFormat
                        )
                    } else {
                        Text(
                            text = "No device selected",
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Допоміжна функція, що перетворює список записів у потрібну кількість точок.
 * Зараз вона повертає desiredCount точок (за замовчуванням 12), вибираючи їх рівномірно.
 */
fun List<ConsumptionRecord>.resamplePoints(desiredCount: Int = 12): List<ConsumptionRecord> {
    if (this.size <= desiredCount) return this
    val step = this.size.toFloat() / desiredCount
    return List(desiredCount) { index ->
        this[(index * step).toInt()]
    }
}

/**
 * Функція для симуляції даних споживання для одного пристрою.
 * Використовується варіація від 0.75 до 1.25 (±25%), а вночі (до 6 та після 22) з ймовірністю 50% споживання падає до 0.
 */
fun simulateSingleDeviceRecords(device: Device, period: TimePeriod): List<ConsumptionRecord> {
    val now = System.currentTimeMillis()
    return List(period.hours.toInt()) { i ->
        val variation = 0.75 + Math.random() * 0.5  // [0.75, 1.25]
        var consumption = device.energyConsumption * variation
        val timestamp = now - ((period.hours - 1 - i) * 3600 * 1000L)
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        if (hourOfDay < 6 || hourOfDay > 22) {
            if (Math.random() < 0.5) {
                consumption = 0.0
            }
        }
        ConsumptionRecord(timestamp, consumption)
    }
}

/**
 * Компонент для малювання лінійного графіка (без інтерактивності) із відображенням осей.
 */
@Composable
fun NonInteractiveConsumptionLineChart(
    consumptionRecords: List<ConsumptionRecord>,
    dateFormat: SimpleDateFormat
) {
    if (consumptionRecords.isEmpty()) return

    val leftMargin = 50.dp
    val bottomMargin = 40.dp
    val padding = 8.dp

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(padding)
    ) {
        val leftMarginPx = leftMargin.toPx()
        val bottomMarginPx = bottomMargin.toPx()

        val canvasWidth = size.width - leftMarginPx
        val canvasHeight = size.height - bottomMarginPx

        val maxConsumption = consumptionRecords.maxOf { it.consumption }
        val pointSpacing = if (consumptionRecords.size > 1) canvasWidth / (consumptionRecords.size - 1) else canvasWidth

        val points = consumptionRecords.mapIndexed { index, record ->
            val x = leftMarginPx + index * pointSpacing
            val y = canvasHeight - (record.consumption / maxConsumption.toFloat()) * canvasHeight
            Offset(x, y.toFloat())
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFFA5D6A7),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f
            )
        }

        points.forEachIndexed { index, point ->
            drawCircle(
                color = Color(0xFFA5D6A7),
                radius = 6f,
                center = point
            )
        }

        // Малюємо осі
        drawLine(
            color = Color.Black,
            start = Offset(leftMarginPx, 0f),
            end = Offset(leftMarginPx, canvasHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Black,
            start = Offset(leftMarginPx, canvasHeight),
            end = Offset(size.width, canvasHeight),
            strokeWidth = 2f
        )

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                textSize = 32f
                color = android.graphics.Color.BLACK
            }
            drawText("${"%.1f".format(maxConsumption)} kWh", 0f, 32f, paint)
            drawText("0 kWh", 0f, canvasHeight, paint)
        }

        // Відображаємо часові мітки для першої, середньої та останньої точки
        if (points.isNotEmpty()) {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    textSize = 24f
                    color = android.graphics.Color.DKGRAY
                }
                val indices = listOf(0, points.size / 2, points.size - 1)
                indices.forEach { index ->
                    drawText(
                        dateFormat.format(Date(consumptionRecords[index].timestamp)),
                        points[index].x,
                        canvasHeight + 30f,
                        paint
                    )
                }
            }
        }
    }
}
