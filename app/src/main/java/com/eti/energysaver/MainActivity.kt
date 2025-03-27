package com.eti.energysaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.eti.energysaver.navigation.NavGraph
import com.eti.energysaver.ui.theme.EnergySaverTheme
import com.eti.energysaver.workers.ThresholdWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workRequest = PeriodicWorkRequestBuilder<ThresholdWorker>(60, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "ThresholdWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        setContent {
            EnergySaverTheme {
                NavGraph()
            }
        }
    }
}

