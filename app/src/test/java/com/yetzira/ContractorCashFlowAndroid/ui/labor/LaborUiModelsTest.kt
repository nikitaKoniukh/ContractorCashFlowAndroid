package com.yetzira.ContractorCashFlowAndroid.ui.labor

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LaborUiModelsTest {

    @Test
    fun `labor filters inactive by default`() {
        assertFalse(LaborFiltersState().isActive)
    }

    @Test
    fun `labor filters active when month filter is enabled with month`() {
        val now = System.currentTimeMillis()
        val month = LaborMonthOption(
            label = "Jan 2026",
            startMillis = now - 1_000,
            endMillis = now
        )
        val state = LaborFiltersState(monthEnabled = true, month = month)

        assertTrue(state.isActive)
    }

    @Test
    fun `worker metrics effective rate maps correctly by labor type`() {
        val worker = LaborDetailsEntity(
            id = "w1",
            workerName = "Alex",
            laborType = LaborType.HOURLY.name,
            hourlyRate = 75.0,
            dailyRate = 500.0,
            contractPrice = 4000.0,
            notes = null,
            createdDate = 1L
        )

        val hourly = WorkerMetricsUi(worker, LaborType.HOURLY, 0.0, 0.0, 0, emptyList(), 0)
        val daily = WorkerMetricsUi(worker, LaborType.DAILY, 0.0, 0.0, 0, emptyList(), 0)
        val subcontractor = WorkerMetricsUi(worker, LaborType.SUBCONTRACTOR, 0.0, 0.0, 0, emptyList(), 0)

        assertEquals(75.0, hourly.effectiveRateAmount)
        assertEquals("/hr", hourly.rateSuffix)
        assertEquals(500.0, daily.effectiveRateAmount)
        assertEquals("/day", daily.rateSuffix)
        assertEquals(4000.0, subcontractor.effectiveRateAmount)
        assertEquals("", subcontractor.rateSuffix)
    }

    @Test
    fun `worker metrics effective rate is null when rate missing`() {
        val worker = LaborDetailsEntity(
            id = "w2",
            workerName = "Sam",
            laborType = LaborType.HOURLY.name,
            hourlyRate = null,
            dailyRate = null,
            contractPrice = null,
            notes = null,
            createdDate = 1L
        )
        val metrics = WorkerMetricsUi(worker, LaborType.HOURLY, 0.0, 0.0, 0, emptyList(), 0)
        assertNull(metrics.effectiveRateAmount)
    }
}
