package com.eti.energysaver.ui.simulation

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.eti.energysaver.model.ConsumptionRecord
import com.eti.energysaver.model.TimePeriod
import com.eti.energysaver.viewmodel.DeviceViewModel
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionChartScreen(deviceViewModel: DeviceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    // Стан для вибору періоду через dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(TimePeriod.LAST_12_HOURS) }
    val periodOptions = TimePeriod.values().toList()

    // Отримуємо симульовані дані за обраний період
    // Функція simulateConsumptionRecords повинна бути реалізована у вашому DeviceViewModel
    val consumptionRecords = remember(selectedPeriod, deviceViewModel.devicesFlow.value) {
        deviceViewModel.simulateConsumptionRecords(selectedPeriod)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dropdown для вибору періоду
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedPeriod.label,
                onValueChange = { },
                label = { Text("Виберіть період") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                periodOptions.forEach { period ->
                    DropdownMenuItem(
                        text = { Text(period.label) },
                        onClick = {
                            selectedPeriod = period
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Лінійний графік, який малюється за допомогою Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            ConsumptionLineChart(consumptionRecords = consumptionRecords)
        }
    }
}

/**
 * Компонент ConsumptionLineChart малює простий лінійний графік на основі списку ConsumptionRecord.
 * Він розраховує координати точок, малює лінію, кола та відображає числові значення.
 */
@Composable
fun ConsumptionLineChart(consumptionRecords: List<ConsumptionRecord>) {
    if (consumptionRecords.isEmpty()) return

    // Знаходимо максимальне та мінімальне споживання для масштабування по вертикалі
    val maxConsumption = consumptionRecords.maxOf { it.consumption }
    val minConsumption = consumptionRecords.minOf { it.consumption }
    val padding = 16.dp
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Обчислюємо відстань між точками по горизонталі
        val pointSpacing = if (consumptionRecords.size > 1) canvasWidth / (consumptionRecords.size - 1) else canvasWidth

        // Масштабування по вертикалі: якщо range = 0, використовуємо scale 1, інакше розраховуємо нормалізоване значення
        val consumptionRange = maxConsumption - minConsumption
        val scaleY = if (consumptionRange == 0.0) 1f else canvasHeight / consumptionRange.toFloat()

        // Обчислюємо координати точок: x = індекс * pointSpacing, y = canvasHeight - (consumption - minConsumption) * scaleY
        val points = consumptionRecords.mapIndexed { index, record ->
            val x = index * pointSpacing
            val y = canvasHeight - ((record.consumption - minConsumption).toFloat() * scaleY)
            Offset(x, y)
        }

        // Малюємо лінію, що з'єднує точки
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFFA5D6A7),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f
            )
        }

        // Малюємо точки та текст над кожною точкою
        points.forEachIndexed { index, point ->
            drawCircle(
                color = Color(0xFFA5D6A7),
                radius = 6f,
                center = point
            )
            // Відображаємо числове значення споживання над точкою
            drawContext.canvas.nativeCanvas.drawText(
                "${"%.1f".format(consumptionRecords[index].consumption)} kWh",
                point.x,
                point.y - 10,
                android.graphics.Paint().apply {
                    textSize = 32f
                    color = android.graphics.Color.BLACK
                }
            )
        }
    }
}
