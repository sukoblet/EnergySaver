// src/test/java/com/eti/energysaver/repository/DeviceTypeRepositoryTest.kt
package com.eti.energysaver.repository

import com.eti.energysaver.model.DeviceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceTypeRepositoryTest {

    private val fakeRepository = FakeDeviceTypeRepository()

    @Test
    fun testGetDeviceTypes() = runTest {
        val deviceTypes = fakeRepository.getDeviceTypes().first()
        // Очікуємо, що фейковий репозиторій поверне 3 типи пристроїв
        assertEquals(3, deviceTypes.size)
    }
}

class FakeDeviceTypeRepository : DeviceTypeRepository() {
    private val fakeDeviceTypes = listOf(
        DeviceType(id = "1", name = "TV", recommendedConsumption = 150.0),
        DeviceType(id = "2", name = "Fridge", recommendedConsumption = 200.0),
        DeviceType(id = "3", name = "Washing Machine", recommendedConsumption = 250.0)
    )
    override fun getDeviceTypes() = kotlinx.coroutines.flow.flowOf(fakeDeviceTypes)
}
