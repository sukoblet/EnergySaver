package com.eti.energysaver.repository

import androidx.compose.ui.input.key.key
import com.eti.energysaver.model.DeviceType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

open class DeviceTypeRepository {

    private val database = FirebaseDatabase.getInstance().reference.child("deviceTypes")

    open fun getDeviceTypes(): Flow<List<DeviceType>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deviceTypes = mutableListOf<DeviceType>()
                for (deviceTypeSnapshot in snapshot.children) {
                    val deviceType = deviceTypeSnapshot.getValue(DeviceType::class.java)
                    deviceType?.let {
                        deviceTypes.add(it.copy(id = deviceTypeSnapshot.key ?: ""))
                    }
                }
                trySend(deviceTypes)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.addValueEventListener(listener)

        awaitClose { database.removeEventListener(listener) }
    }

    // Додавання нового типу (за потреби)
    fun addDeviceType(deviceType: DeviceType) {
        val key = database.push().key ?: return
        database.child(key).setValue(deviceType)
    }

    // Додавання початкових типів пристроїв
    fun addInitialDeviceTypes() {
        val initialDeviceTypes = listOf(
            DeviceType(name = "TV", recommendedConsumption = 0.1), // 0.1 кВт·год
            DeviceType(name = "Lamp", recommendedConsumption = 0.01), // 0.01 кВт·год
            DeviceType(name = "Refrigerator", recommendedConsumption = 0.05), // 0.05 кВт·год
            DeviceType(name = "Washing machine", recommendedConsumption = 0.2), // 0.2 кВт·год
            DeviceType(name = "AC", recommendedConsumption = 0.3), // 0.3 кВт·год
            DeviceType(name = "PC", recommendedConsumption = 0.15), // 0.15 кВт·год
            DeviceType(name = "Microwave", recommendedConsumption = 0.12), // 0.12 кВт·год
            DeviceType(name = "Electric kettle", recommendedConsumption = 0.18), // 0.18 кВт·год
            DeviceType(name = "Hair dryer", recommendedConsumption = 0.08), // 0.08 кВт·год
            DeviceType(name = "Iron", recommendedConsumption = 0.1), // 0.1 кВт·год
        )

        for (deviceType in initialDeviceTypes) {
            addDeviceType(deviceType)
        }
    }
}