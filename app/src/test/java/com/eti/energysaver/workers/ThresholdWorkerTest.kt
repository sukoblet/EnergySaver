package com.eti.energysaver.workers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.impl.utils.taskexecutor.TaskExecutor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID
import com.eti.energysaver.workers.ThresholdWorker
import androidx.work.testing.TestWorkerBuilder

class ThresholdWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = androidx.work.Configuration.Builder().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun testThresholdWorkerSuccess() {
        // Create WorkerParameters
        val workerParameters = WorkerParameters(
            id = UUID.randomUUID(),
            inputData = Data.EMPTY,
            tags = setOf(),
            runAttemptCount = 0,
            // For testing, use a test Scheduler
            //scheduler = TaskExecutor { }
        )

        // Build the worker using TestWorkerBuilder
        val worker = TestWorkerBuilder<ThresholdWorker>(
            context = context,
            workerParameters = workerParameters
        ).build()

        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }

    private fun <T : Worker?> TestWorkerBuilder(
        context: Context,
        workerParameters: WorkerParameters
    ): TestWorkerBuilder<T> {
        TODO("Not yet implemented")
    }

    private fun WorkerParameters(
        id: UUID?,
        inputData: Data,
        tags: Set<String>,
        runAttemptCount: Int
    ): WorkerParameters {
            TODO("Not yet implemented")
    }
}