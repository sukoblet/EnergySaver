package com.eti.energysaver.ui.simulation

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eti.energysaver.model.ConsumptionRecord
import com.eti.energysaver.model.TimePeriod
import com.eti.energysaver.viewmodel.DeviceViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphsContent(
    onClose: () -> Unit,
    deviceViewModel: DeviceViewModel = viewModel()
) {
    // Стан для вибору періоду через dropdown
    var expanded by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(TimePeriod.LAST_12_HOURS) }
    val periodOptions = TimePeriod.values().toList()

    // Отримуємо симульовані дані за обраний період
    val consumptionRecords = remember(selectedPeriod, deviceViewModel.devicesFlow.value) {
        deviceViewModel.simulateConsumptionRecords(selectedPeriod)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
                onValueChange = {},
                label = { Text("Choose time period") },
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

        // Графік (лінійний) на базі Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            NonInteractiveConsumptionLineChart(consumptionRecords = consumptionRecords)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Close")
        }
    }
}

@Composable
fun NonInteractiveConsumptionLineChart(consumptionRecords: List<ConsumptionRecord>) {
    if (consumptionRecords.isEmpty()) return

    // Визначаємо максимальне споживання для масштабування
    val maxConsumption = consumptionRecords.maxOf { it.consumption }
    val padding = 16.dp
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Розрахунок відстані між точками по горизонталі
        val pointSpacing = if (consumptionRecords.size > 1) canvasWidth / (consumptionRecords.size - 1) else canvasWidth

        // Обчислюємо координати точок графіка
        val points = consumptionRecords.mapIndexed { index, record ->
            val x = index * pointSpacing
            val y = canvasHeight - (record.consumption / maxConsumption.toFloat()) * canvasHeight
            Offset(x, y.toFloat())
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

        // Малюємо точки та підписуємо їх
        points.forEachIndexed { index, point ->
            drawCircle(
                color = Color(0xFFA5D6A7),
                radius = 6f,
                center = point
            )
            // Використовуємо drawIntoCanvas для відображення тексту
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    textSize = 32f
                    color = android.graphics.Color.BLACK
                }
                canvas.nativeCanvas.drawText(
                    "${"%.1f".format(consumptionRecords[index].consumption)} kWh",
                    point.x,
                    point.y - 10,
                    paint
                )
                val paintSmall = android.graphics.Paint().apply {
                    textSize = 24f
                    color = android.graphics.Color.DKGRAY
                }
                canvas.nativeCanvas.drawText(
                    dateFormat.format(Date(consumptionRecords[index].timestamp)),
                    point.x,
                    point.y + 30,
                    paintSmall
                )
            }
        }
    }
}
