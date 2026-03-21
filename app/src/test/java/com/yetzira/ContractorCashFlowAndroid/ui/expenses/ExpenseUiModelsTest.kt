package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseUiModelsTest {

    @Test
    fun `filter state inactive by default`() {
        val state = ExpenseFilterState()
        assertFalse(state.isActive)
    }

    @Test
    fun `filter state active when category is selected`() {
        val state = ExpenseFilterState(category = ExpenseCategory.LABOR)
        assertTrue(state.isActive)
    }

    @Test
    fun `filter state active when date bounds are enabled`() {
        val withStart = ExpenseFilterState(hasStartDate = true, startDate = System.currentTimeMillis())
        val withEnd = ExpenseFilterState(hasEndDate = true, endDate = System.currentTimeMillis())

        assertTrue(withStart.isActive)
        assertTrue(withEnd.isActive)
    }
}

