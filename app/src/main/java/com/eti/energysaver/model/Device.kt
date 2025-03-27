package com.eti.energysaver.model

data class DeviceSettings(
    val threshold: Double = 0.0,
    val notificationsEnabled: Boolean = true,
    val co2Emission: Double = 0.0
)

data class Device(
    var id: String = "",
    var name: String = "",
    var energyConsumption: Double = 0.0,
    var type: String = "",
    var isActive: Boolean = true,
    var isMandatory: Boolean = false,
    var optimized: Boolean = false,
    var timestamp: Long = System.currentTimeMillis(),
    var settings: DeviceSettings? = null
)
