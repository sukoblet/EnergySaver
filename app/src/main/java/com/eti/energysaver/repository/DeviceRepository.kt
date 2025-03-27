package com.eti.energysaver.repository

import androidx.compose.ui.input.key.key
import com.eti.energysaver.model.Device
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.auth.FirebaseAuth

class DeviceRepository {

    private val database = FirebaseDatabase.getInstance().reference.child("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun addDevice(device: Device) {
        val userId = auth.currentUser?.uid ?: return // Отримуємо userId з Firebase Auth
        val key = database.child(userId).child("devices").push().key ?: return
        database.child(userId).child("devices").child(key).setValue(device)
    }

    // Додайте інші методи для роботи з пристроями, якщо потрібно
    // наприклад, getDevices(), updateDevice(), deleteDevice()
}