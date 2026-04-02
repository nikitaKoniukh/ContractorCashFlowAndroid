package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.repository.ExpenseRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `updateForm for subcontractor worker auto-fills amount and read-only`() {
        val viewModel = ExpenseViewModel(FakeExpenseRepository())
        val worker = LaborDetailsEntity(
            id = "w1",
            workerName = "Contractor",
            laborType = LaborType.SUBCONTRACTOR.name,
            contractPrice = 4500.0,
            createdDate = 1L
        )

        val state = ExpenseFormUiState(
            category = ExpenseCategory.LABOR,
            workerId = "w1",
            workers = listOf(
                WorkerOptionUi(
                    worker = worker,
                    laborType = LaborType.SUBCONTRACTOR,
                    hourlyRate = null,
                    dailyRate = null,
                    contractPrice = 4500.0
                )
            )
        )

        val updated = viewModel.updateForm(state)

        assertEquals("4.500", updated.amount)
        assertTrue(updated.isAmountReadOnly)
        assertEquals(LaborType.SUBCONTRACTOR, updated.laborTypeSnapshot)
        assertTrue(updated.description.startsWith("Worker:"))
    }

    @Test
    fun `updateForm requires hourly-daily choice when both rates exist`() {
        val viewModel = ExpenseViewModel(FakeExpenseRepository())
        val worker = LaborDetailsEntity(
            id = "w2",
            workerName = "Dual",
            laborType = LaborType.HOURLY.name,
            hourlyRate = 100.0,
            dailyRate = 700.0,
            createdDate = 1L
        )

        val state = ExpenseFormUiState(
            category = ExpenseCategory.LABOR,
            workerId = "w2",
            unitsWorked = "2",
            workers = listOf(
                WorkerOptionUi(
                    worker = worker,
                    laborType = LaborType.HOURLY,
                    hourlyRate = 100.0,
                    dailyRate = 700.0,
                    contractPrice = null
                )
            )
        )

        val updated = viewModel.updateForm(state)

        assertFalse(updated.canSave)
        assertEquals(null, updated.laborTypeSnapshot)
    }

    @Test
    fun `saveExpense rejects invalid input`() = runTest {
        val repository = FakeExpenseRepository()
        val viewModel = ExpenseViewModel(repository)

        viewModel.saveExpense(
            ExpenseFormUiState(
                description = "",
                amount = "0"
            )
        )
        advanceUntilIdle()

        assertTrue(repository.inserted.isEmpty())
        assertEquals(ExpenseSaveResult.None, viewModel.lastSaveResult.value)
    }

    @Test
    fun `saveExpense emits budget warning when utilization reaches threshold`() = runTest {
        val repository = FakeExpenseRepository(
            projects = listOf(ProjectEntity(id = "p1", name = "P", clientName = "C", budget = 1000.0))
        )
        repository.projectTotals["p1"] = 850.0
        val viewModel = ExpenseViewModel(repository)

        viewModel.saveExpense(
            ExpenseFormUiState(
                category = ExpenseCategory.MATERIALS,
                description = "Cement",
                amount = "200",
                projectId = "p1"
            )
        )
        advanceUntilIdle()

        assertEquals(1, repository.inserted.size)
        val result = viewModel.lastSaveResult.value
        assertTrue(result is ExpenseSaveResult.BudgetWarning)
        assertEquals(80, (result as ExpenseSaveResult.BudgetWarning).utilizationPercent)
    }

    @Test
    fun `delete and undo restore expense`() = runTest {
        val expense = ExpenseEntity(id = "e1", category = "MISC", amount = 100.0, descriptionText = "Item")
        val repository = FakeExpenseRepository(expenses = listOf(expense))
        val viewModel = ExpenseViewModel(repository)

        val collectJob = launch { viewModel.listUiState.collect { } }

        viewModel.deleteExpense(expense)
        advanceUntilIdle()
        assertTrue(repository.expenses.value.none { it.id == "e1" })

        viewModel.undoDeleteExpense()
        advanceUntilIdle()
        assertTrue(repository.expenses.value.any { it.id == "e1" })

        collectJob.cancel()
    }

    private class FakeExpenseRepository(
        expenses: List<ExpenseEntity> = emptyList(),
        projects: List<ProjectEntity> = emptyList(),
        workers: List<LaborDetailsEntity> = emptyList()
    ) : ExpenseRepositoryContract {
        val expenses = MutableStateFlow(expenses)
        val projects = MutableStateFlow(projects)
        val workers = MutableStateFlow(workers)
        val projectTotals: MutableMap<String, Double?> = mutableMapOf()

        val inserted = mutableListOf<ExpenseEntity>()
        val updated = mutableListOf<ExpenseEntity>()

        override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenses

        override suspend fun getExpenseById(id: String): ExpenseEntity? =
            expenses.value.firstOrNull { it.id == id }

        override suspend fun insertExpense(expense: ExpenseEntity) {
            inserted += expense
            expenses.value = expenses.value.filterNot { it.id == expense.id } + expense
        }

        override suspend fun updateExpense(expense: ExpenseEntity) {
            updated += expense
            expenses.value = expenses.value.map { if (it.id == expense.id) expense else it }
        }

        override suspend fun deleteExpense(expense: ExpenseEntity) {
            expenses.value = expenses.value.filterNot { it.id == expense.id }
        }

        override fun getAllProjects(): Flow<List<ProjectEntity>> = projects

        override suspend fun getProjectById(id: String): ProjectEntity? =
            projects.value.firstOrNull { it.id == id }

        override fun getAllWorkers(): Flow<List<LaborDetailsEntity>> = workers

        override fun getProjectTotalExpenses(projectId: String): Flow<Double?> {
            return MutableStateFlow(projectTotals[projectId])
        }
    }
}

