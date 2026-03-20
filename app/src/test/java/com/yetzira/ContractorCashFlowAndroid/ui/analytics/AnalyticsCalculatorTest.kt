package com.yetzira.ContractorCashFlowAndroid.ui.analytics

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsCalculatorTest {

    private val now = 1_710_000_000_000L

    @Test
    fun `filters income expenses and overdue by selected period`() {
        val projects = listOf(
            ProjectEntity(id = "p1", name = "Project A", clientName = "Client A", budget = 2_000.0),
            ProjectEntity(id = "p2", name = "Project B", clientName = "Client B", budget = 1_500.0)
        )
        val expenses = listOf(
            ExpenseEntity(id = "e1", category = "LABOR", amount = 200.0, descriptionText = "Labor", date = now - 5 * 86_400_000L, projectId = "p1"),
            ExpenseEntity(id = "e2", category = "MATERIALS", amount = 100.0, descriptionText = "Old", date = now - 50 * 86_400_000L, projectId = "p2")
        )
        val invoices = listOf(
            InvoiceEntity(id = "i1", amount = 800.0, dueDate = now - 2 * 86_400_000L, isPaid = true, clientName = "Client A", createdDate = now - 4 * 86_400_000L, projectId = "p1"),
            InvoiceEntity(id = "i2", amount = 400.0, dueDate = now - 1 * 86_400_000L, isPaid = false, clientName = "Client B", createdDate = now - 3 * 86_400_000L, projectId = "p2"),
            InvoiceEntity(id = "i3", amount = 1_000.0, dueDate = now - 40 * 86_400_000L, isPaid = true, clientName = "Client B", createdDate = now - 40 * 86_400_000L, projectId = "p2")
        )

        val weekState = AnalyticsCalculator.buildUiState(
            selectedPeriod = AnalyticsPeriod.WEEK,
            currency = CurrencyOption.ILS,
            expenses = expenses,
            invoices = invoices,
            projects = projects,
            now = now
        )

        assertEquals(800.0, weekState.totalIncome, 0.0)
        assertEquals(200.0, weekState.totalExpenses, 0.0)
        assertEquals(600.0, weekState.netBalance, 0.0)
        assertEquals(400.0, weekState.overdueAmount, 0.0)
        assertTrue(weekState.monthlyTrend.isEmpty())
    }

    @Test
    fun `builds category budget and top project breakdowns`() {
        val projects = listOf(
            ProjectEntity(id = "p1", name = "Project A", clientName = "Client A", budget = 1_000.0),
            ProjectEntity(id = "p2", name = "Project B", clientName = "Client B", budget = 2_000.0)
        )
        val expenses = listOf(
            ExpenseEntity(id = "e1", category = "LABOR", amount = 300.0, descriptionText = "Labor", date = now - 10 * 86_400_000L, projectId = "p1"),
            ExpenseEntity(id = "e2", category = "MATERIALS", amount = 100.0, descriptionText = "Materials", date = now - 12 * 86_400_000L, projectId = "p1"),
            ExpenseEntity(id = "e3", category = "EQUIPMENT", amount = 250.0, descriptionText = "Equipment", date = now - 15 * 86_400_000L, projectId = "p2")
        )
        val invoices = listOf(
            InvoiceEntity(id = "i1", amount = 900.0, dueDate = now + 5 * 86_400_000L, isPaid = true, clientName = "Client A", createdDate = now - 6 * 86_400_000L, projectId = "p1"),
            InvoiceEntity(id = "i2", amount = 1_200.0, dueDate = now + 3 * 86_400_000L, isPaid = true, clientName = "Client B", createdDate = now - 7 * 86_400_000L, projectId = "p2")
        )

        val state = AnalyticsCalculator.buildUiState(
            selectedPeriod = AnalyticsPeriod.MONTH,
            currency = CurrencyOption.ILS,
            expenses = expenses,
            invoices = invoices,
            projects = projects,
            now = now
        )

        assertEquals(3, state.expensesByCategory.size)
        assertEquals("Project B", state.topProjects.first().projectName)
        assertEquals(45.0, state.budgetUtilization.first { it.projectId == "p1" }.utilization, 0.001)
        assertEquals(12.5, state.budgetUtilization.first { it.projectId == "p2" }.utilization, 0.001)
        assertTrue(state.averageBudgetUtilization > 0.0)
        assertTrue(state.monthlyTrend.isNotEmpty())
    }
}

