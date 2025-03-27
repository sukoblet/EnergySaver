package com.eti.energysaver.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.eti.energysaver.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.CountDownLatch

class ThresholdWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "threshold_notifications"
        const val NOTIFICATION_ID = 1001
    }

    override fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // Зчитування порогового значення з Firebase
        var threshold = 0.0
        val latch1 = CountDownLatch(1)
        userRef.child("settings/overallThreshold").get().addOnSuccessListener { snapshot ->
            threshold = snapshot.getValue(Double::class.java) ?: 0.0
            latch1.countDown()
        }.addOnFailureListener {
            latch1.countDown()
        }
        latch1.await()

        // Отримання поточного споживання з Firebase
        val currentConsumption = getCurrentConsumption(userId)

        // Перевірка: якщо споживання перевищує поріг, надсилаємо сповіщення
        if (currentConsumption > threshold && threshold > 0) {
            sendNotification(
                "Przekroczono limit zużycia",
                "Aktualne zużycie: $currentConsumption kWh, limit: $threshold kWh. Rozważ optymalizację."
            )
            if (isAutoOptimizationEnabled(userId)) {
                performOptimization(userId)
            }
        }
        return Result.success()
    }

    private fun getCurrentConsumption(userId: String): Double {
        var consumption = 0.0
        val latch = CountDownLatch(1)
        FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("currentConsumption")
            .get()
            .addOnSuccessListener { snapshot ->
                consumption = snapshot.getValue(Double::class.java) ?: 0.0
                latch.countDown()
            }
            .addOnFailureListener { latch.countDown() }
        latch.await()
        return consumption
    }

    private fun isAutoOptimizationEnabled(userId: String): Boolean {
        var enabled = false
        val latch = CountDownLatch(1)
        FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("settings/autoOptimizationEnabled")
            .get()
            .addOnSuccessListener { snapshot ->
                enabled = snapshot.getValue(Boolean::class.java) ?: false
                latch.countDown()
            }
            .addOnFailureListener { latch.countDown() }
        latch.await()
        return enabled
    }

    private fun performOptimization(userId: String) {
        // Приклад оптимізації: оновлюємо всі пристрої, які не є обов’язковими, встановлюючи isActive = false.
        val devicesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("devices")
        devicesRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { deviceSnapshot ->
                val device = deviceSnapshot.getValue(com.eti.energysaver.model.Device::class.java)
                device?.let {
                    if (!it.isMandatory) {
                        it.isActive = false
                        deviceSnapshot.ref.setValue(it)
                    }
                }
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Threshold Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when energy consumption exceeds threshold."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.green_leaf) // переконайтеся, що цей ресурс існує
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
