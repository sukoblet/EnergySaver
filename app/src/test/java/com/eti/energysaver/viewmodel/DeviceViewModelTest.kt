// src/test/java/com/eti/energysaver/viewmodel/DeviceViewModelTest.kt
package com.eti.energysaver.viewmodel

import com.eti.energysaver.model.DeviceType
import com.eti.energysaver.repository.DeviceTypeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceViewModelTest {

    private lateinit var viewModel: TestDeviceViewModel
    private lateinit var fakeRepository: FakeDeviceTypeRepository

    @Before
    fun setup() {
        fakeRepository = FakeDeviceTypeRepository()
        viewModel = TestDeviceViewModel(fakeRepository)
    }

    @Test
    fun testGetRecommendedConsumption_existingType() = runTest {
        val result = viewModel.getRecommendedConsumption("TV")
        assertEquals("150.0", result)
    }

    @Test
    fun testGetRecommendedConsumption_nonExistingType() = runTest {
        val result = viewModel.getRecommendedConsumption("NonExistingType")
        assertEquals("0.0", result)
    }
}

class FakeDeviceTypeRepository : DeviceTypeRepository() {
    private val fakeDeviceTypes = listOf(
        DeviceType(id = "1", name = "TV", recommendedConsumption = 150.0),
        DeviceType(id = "2", name = "Fridge", recommendedConsumption = 200.0),
        DeviceType(id = "3", name = "Washing Machine", recommendedConsumption = 250.0)
    )
    override fun getDeviceTypes() = flowOf(fakeDeviceTypes)
}

class TestDeviceViewModel(private val fakeRepository: DeviceTypeRepository) : DeviceViewModel() {
    override val deviceTypeRepository: DeviceTypeRepository
        get() = fakeRepository
}
