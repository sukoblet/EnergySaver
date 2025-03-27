package com.eti.energysaver.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eti.energysaver.model.ConsumptionRecord
import com.eti.energysaver.model.Device
import com.eti.energysaver.model.DeviceType
import com.eti.energysaver.model.EcoStats
import com.eti.energysaver.model.TimePeriod
import com.eti.energysaver.repository.DatabaseRepository
import com.eti.energysaver.repository.DeviceRepository
import com.eti.energysaver.repository.DeviceTypeRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

open class DeviceViewModel : ViewModel() {

    private val dbRepository = DatabaseRepository()

    private val deviceRepository = DeviceRepository()
    open val deviceTypeRepository: DeviceTypeRepository = DeviceTypeRepository()

    private val _deviceTypes = MutableStateFlow<List<DeviceType>>(emptyList())
    val deviceTypes: StateFlow<List<DeviceType>> = _deviceTypes.asStateFlow()

    var costCoefficient: Double = 1.5

    init {
        loadDeviceTypes()
        //addInitialDeviceTypes()
    }

    private fun loadDeviceTypes() {
        viewModelScope.launch {
            deviceTypeRepository.getDeviceTypes().collect { types ->
                _deviceTypes.value = types
            }
        }
    }

    private fun addInitialDeviceTypes() {
        deviceTypeRepository.addInitialDeviceTypes()
    }

    fun addDevice(device: Device) {
        viewModelScope.launch {
            deviceRepository.addDevice(device)
        }
    }

    // Потік даних пристроїв
    val devicesFlow: StateFlow<List<Device>> = dbRepository.getDevicesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateDevice(device: Device) {
        dbRepository.updateDevice(device)
    }

    fun deleteDevice(deviceId: String) {
        dbRepository.deleteDevice(deviceId)
    }

    fun optimizeDevices() {
        dbRepository.optimizeDevices()
    }

    suspend fun getRecommendedConsumption(deviceType: String): String =
        suspendCancellableCoroutine { cont ->
            val ref = FirebaseDatabase.getInstance().reference.child("deviceTypes")
            // Виконуємо запит по назві пристрою
            ref.orderByChild("name").equalTo(deviceType)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val dt: DeviceType? = snapshot.children.firstOrNull()?.getValue(DeviceType::class.java)
                        cont.resume(dt?.recommendedConsumption?.toString() ?: "0.0")
                    }
                    override fun onCancelled(error: DatabaseError) {
                        cont.resume("0.0")
                    }
                })
        }

    // Обрахунок загального споживання електроенергії (рахуємо лише активні пристрої)
    val totalEnergyConsumption: StateFlow<Double> = devicesFlow.map { devices ->
        devices.filter { it.isActive }.sumOf { it.energyConsumption }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Топ-3 пристрої за споживанням (не обов'язково лише активні, але можна за бажанням додатково фільтрувати)
    val topDevices: StateFlow<List<Device>> = devicesFlow.map { devices ->
        devices.sortedByDescending { it.energyConsumption }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Пристрій з найбільшим споживанням (опційно)
    val highestConsumptionDevice: StateFlow<Device?> = devicesFlow.map { devices ->
        devices.maxByOrNull { it.energyConsumption }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun simulateConsumptionRecords(period: TimePeriod): List<ConsumptionRecord> {
        val now = System.currentTimeMillis()
        val activeDevices = devicesFlow.value.filter { it.isActive }
        val records = mutableListOf<ConsumptionRecord>()
        for (i in 0 until period.hours) {
            // Розраховуємо сумарне споживання для кожної години
            val consumptionForHour = activeDevices.sumOf { device ->
                // Випадкова варіація від 0.9 до 1.1
                val variation = 0.9 + Math.random() * 0.2
                device.energyConsumption * variation
            }
            // Обчислюємо часову мітку для кожного запису (припустимо, дані за годину)
            val timestamp = now - ((period.hours - 1 - i) * 3600 * 1000L)
            records.add(ConsumptionRecord(timestamp, consumptionForHour))
        }
        return records
    }

    fun getEcoStatsForPeriod(period: String, devices: List<Device>): EcoStats {
        // Реальний алгоритм повинен фільтрувати пристрої за часовими мітками відповідно до періоду
        // Тут використовується спрощена логіка: зекономлена енергія = сума всіх пристроїв - сума активних
        val totalConsumption = devices.sumOf { it.energyConsumption }
        val activeConsumption = devices.filter { it.isActive }.sumOf { it.energyConsumption }
        val energySavings = totalConsumption - activeConsumption
        val moneySavings = energySavings * costCoefficient
        val co2Savings = calculateCO2Savings(devices)
        return EcoStats(energySavings, moneySavings, co2Savings)
    }
    fun calculateCO2Savings(devices: List<Device>): Double {
        val totalKg = devices.filter { !it.isActive }
            .sumOf { it.energyConsumption * (it.settings?.co2Emission ?: 0.0) }
        return totalKg // перетворюємо кілограми в грами
    }
}