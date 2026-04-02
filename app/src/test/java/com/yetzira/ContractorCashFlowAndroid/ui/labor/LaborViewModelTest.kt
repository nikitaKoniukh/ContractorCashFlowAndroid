package com.yetzira.ContractorCashFlowAndroid.ui.labor

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.repository.LaborRepositoryContract
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
class LaborViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `updateForm detects duplicate worker name`() {
        val repository = FakeLaborRepository(
            workers = listOf(
                LaborDetailsEntity(id = "w1", workerName = "Alex", laborType = LaborType.HOURLY.name),
                LaborDetailsEntity(id = "w2", workerName = "Ben", laborType = LaborType.DAILY.name)
            )
        )
        val viewModel = LaborViewModel(repository)

        val updated = viewModel.updateForm(LaborFormUiState(workerName = "alex"))

        assertTrue(updated.duplicateWarning)
        assertFalse(updated.canSave)
    }

    @Test
    fun `list summary computes totals days and hours`() = runTest {
        val jan2 = 1_704_153_600_000L
        val jan3 = jan2 + 86_400_000L
        val workers = listOf(
            LaborDetailsEntity(id = "w1", workerName = "Alex", laborType = LaborType.HOURLY.name),
            LaborDetailsEntity(id = "w2", workerName = "Ben", laborType = LaborType.SUBCONTRACTOR.name)
        )
        val expenses = listOf(
            ExpenseEntity(
                id = "e1",
                category = "LABOR",
                amount = 200.0,
                descriptionText = "Labor 1",
                date = jan2,
                workerId = "w1",
                unitsWorked = 2.0,
                laborTypeSnapshot = LaborType.HOURLY.name,
                projectId = "p1"
            ),
            ExpenseEntity(
                id = "e2",
                category = "LABOR",
                amount = 300.0,
                descriptionText = "Labor 2",
                date = jan3,
                workerId = "w2",
                laborTypeSnapshot = LaborType.SUBCONTRACTOR.name,
                projectId = "p2"
            )
        )
        val projects = listOf(
            ProjectEntity(id = "p1", name = "Project A", clientName = "C1", budget = 1000.0),
            ProjectEntity(id = "p2", name = "Project B", clientName = "C2", budget = 1000.0)
        )

        val repository = FakeLaborRepository(workers = workers, expenses = expenses, projects = projects)
        val viewModel = LaborViewModel(repository)
        val collectJob = launch { viewModel.listUiState.collect { } }
        advanceUntilIdle()

        val summary = viewModel.listUiState.value.summary
        assertEquals(500.0, summary.totalLaborCost, 0.0)
        assertEquals(2, summary.workerCount)
        assertEquals(2, summary.daysWorked)
        assertEquals(2.0, summary.totalHours, 0.0)

        collectJob.cancel()
    }

    @Test
    fun `delete worker triggers repository call`() = runTest {
        val worker = LaborDetailsEntity(id = "w1", workerName = "Alex", laborType = LaborType.HOURLY.name)
        val repository = FakeLaborRepository(workers = listOf(worker))
        val viewModel = LaborViewModel(repository)

        viewModel.deleteWorker(worker)
        advanceUntilIdle()

        assertEquals(listOf("w1"), repository.deletedWorkerIds)
    }

    private class FakeLaborRepository(
        workers: List<LaborDetailsEntity> = emptyList(),
        expenses: List<ExpenseEntity> = emptyList(),
        projects: List<ProjectEntity> = emptyList()
    ) : LaborRepositoryContract {
        val workersFlow = MutableStateFlow(workers)
        val expensesFlow = MutableStateFlow(expenses)
        val projectsFlow = MutableStateFlow(projects)

        val deletedWorkerIds = mutableListOf<String>()

        override fun getAllWorkers(): Flow<List<LaborDetailsEntity>> = workersFlow

        override suspend fun getWorkerById(id: String): LaborDetailsEntity? =
            workersFlow.value.firstOrNull { it.id == id }

        override suspend fun insertWorker(worker: LaborDetailsEntity) {
            workersFlow.value = workersFlow.value + worker
        }

        override suspend fun updateWorker(worker: LaborDetailsEntity) {
            workersFlow.value = workersFlow.value.map { if (it.id == worker.id) worker else it }
        }

        override suspend fun deleteWorker(worker: LaborDetailsEntity) {
            deletedWorkerIds += worker.id
            workersFlow.value = workersFlow.value.filterNot { it.id == worker.id }
        }

        override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expensesFlow

        override fun getAllProjects(): Flow<List<ProjectEntity>> = projectsFlow
    }
}

