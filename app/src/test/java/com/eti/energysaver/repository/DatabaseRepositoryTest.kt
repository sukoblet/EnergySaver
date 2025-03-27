package com.eti.energysaver.repository

import com.eti.energysaver.model.Device
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseRepositoryTest {

    private val repository = DatabaseRepository()

    @Test
    fun testGetDevicesFlow() = runTest {
        val devices = repository.getDevicesFlow().first()
        // Перевіряємо, що список пристроїв не є null (може бути порожнім)
        assertNotNull(devices)
    }
}