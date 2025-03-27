// src/test/java/com/eti/energysaver/model/ModelsTest.kt
package com.eti.energysaver.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {

    @Test
    fun testConsumptionRecordCopy() {
        val record1 = ConsumptionRecord(timestamp = 1000L, consumption = 10.0)
        val record2 = record1.copy(consumption = 15.0)
        assertEquals(15.0, record2.consumption, 0.0)
    }

    @Test
    fun testDeviceCopy() {
        val device1 = Device(
            id = "1",
            name = "Test Device",
            energyConsumption = 100.0,
            type = "TV",
            isActive = true,
            isMandatory = false,
            optimized = false,
            timestamp = 1000L,
            settings = null
        )
        val device2 = device1.copy(name = "Updated Device")
        assertEquals("Updated Device", device2.name)
        assertEquals(device1.energyConsumption, device2.energyConsumption, 0.0)
    }

    @Test
    fun testDeviceTypeEquality() {
        val dt1 = DeviceType(id = "1", name = "TV", recommendedConsumption = 150.0)
        val dt2 = DeviceType(id = "1", name = "TV", recommendedConsumption = 150.0)
        assertEquals(dt1, dt2)
    }
}
