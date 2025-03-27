package com.eti.energysaver.model

data class DeviceType(
    val id: String = "", // Унікальний ідентифікатор типу
    val name: String = "", // Назва типу (наприклад, "Телевізор")
    val recommendedConsumption: Double = 0.0 // Рекомендоване споживання (кВт·год)
)