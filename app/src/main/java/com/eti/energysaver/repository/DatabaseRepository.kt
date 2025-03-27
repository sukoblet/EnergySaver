package com.eti.energysaver.repository

import androidx.compose.ui.input.key.key
import com.eti.energysaver.model.Device
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DatabaseRepository {

    private val database = FirebaseDatabase.getInstance().reference.child("users")
    private val auth = FirebaseAuth.getInstance()

    fun getDevicesFlow(): Flow<List<Device>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow // Отримуємо userId з Firebase Auth
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val devices = mutableListOf<Device>()
                val devicesSnapshot = snapshot.child(userId).child("devices")
                for (deviceSnapshot in devicesSnapshot.children) {
                    val device = deviceSnapshot.getValue(Device::class.java)
                    device?.let {
                        devices.add(it.copy(id = deviceSnapshot.key ?: ""))
                    }
                }
                trySend(devices)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.addValueEventListener(listener)

        awaitClose { database.removeEventListener(listener) }
    }

    fun updateDevice(device: Device) {
        val userId = auth.currentUser?.uid ?: return // Отримуємо userId з Firebase Auth
        database.child(userId).child("devices").child(device.id).setValue(device)
    }

    fun deleteDevice(deviceId: String) {
        val userId = auth.currentUser?.uid ?: return // Отримуємо userId з Firebase Auth
        database.child(userId).child("devices").child(deviceId).removeValue()
    }

    fun optimizeDevices() {
        val ref = getUserDevicesRef() ?: return
        ref.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { deviceSnapshot ->
                val device = deviceSnapshot.getValue(Device::class.java)
                device?.let {
                    // Якщо пристрій не є обов'язковим, його вимикаємо
                    if (!it.isMandatory) {
                        it.isActive = false
                        deviceSnapshot.ref.setValue(it)
                    }
                }
            }
        }
    }

    private fun getUserDevicesRef(): DatabaseReference? {
        val userId = auth.currentUser?.uid ?: return null
        return database.child(userId).child("devices")
    }
}