package com.yetzira.ContractorCashFlowAndroid.ui.projects

import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ClientDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.repository.ProjectRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `projectsUiState computes totals and balance`() = runTest {
        val repo = FakeProjectRepository(
            projects = listOf(ProjectEntity(id = "p1", name = "Build", clientName = "Acme", budget = 1000.0))
        )
        val expenseDao = FakeExpenseDao(
            initial = listOf(ExpenseEntity(id = "e1", category = "MATERIALS", amount = 200.0, descriptionText = "Mat", projectId = "p1"))
        )
        val invoiceDao = FakeInvoiceDao(
            initial = listOf(InvoiceEntity(id = "i1", amount = 500.0, dueDate = 1L, isPaid = true, clientName = "Acme", projectId = "p1"))
        )
        val clientDao = FakeClientDao()

        val viewModel = ProjectViewModel(repo, expenseDao, invoiceDao, clientDao)
        val collectJob = launch { viewModel.projectsUiState.collect { } }
        advanceUntilIdle()

        val row = viewModel.projectsUiState.value.projects.single()
        assertEquals(200.0, row.totalExpenses, 0.0)
        assertEquals(500.0, row.totalIncome, 0.0)
        assertEquals(300.0, row.balance, 0.0)

        collectJob.cancel()
    }

    @Test
    fun `createProject with new client inserts client and project`() = runTest {
        val repo = FakeProjectRepository()
        val expenseDao = FakeExpenseDao()
        val invoiceDao = FakeInvoiceDao()
        val clientDao = FakeClientDao()
        val viewModel = ProjectViewModel(repo, expenseDao, invoiceDao, clientDao)

        var successCalled = false
        viewModel.createProject(
            name = "Home Renovation",
            budgetText = "1.000",
            useExistingClient = false,
            selectedClientName = "",
            newClientName = "Nikita",
            newClientEmail = "test@mail.com",
            newClientPhone = "",
            newClientAddress = "",
            newClientNotes = "",
            onSuccess = { successCalled = true }
        )
        advanceUntilIdle()

        assertEquals(1, clientDao.insertedClients.size)
        assertEquals("Nikita", clientDao.insertedClients.first().name)
        assertEquals(1, repo.insertedProjects.size)
        assertEquals(1000.0, repo.insertedProjects.first().budget, 0.0)
        assertTrue(successCalled)
    }

    @Test
    fun `createProject rejects invalid input`() = runTest {
        val repo = FakeProjectRepository()
        val viewModel = ProjectViewModel(repo, FakeExpenseDao(), FakeInvoiceDao(), FakeClientDao())

        viewModel.createProject(
            name = "",
            budgetText = "0",
            useExistingClient = true,
            selectedClientName = "",
            newClientName = "",
            newClientEmail = "",
            newClientPhone = "",
            newClientAddress = "",
            newClientNotes = "",
            onSuccess = {}
        )
        advanceUntilIdle()

        assertTrue(repo.insertedProjects.isEmpty())
    }

     @Test
    fun `createProject with duplicate typed client does not insert duplicate client`() = runTest {
        val repo = FakeProjectRepository()
        val clientDao = FakeClientDao(
            initial = listOf(ClientEntity(id = "c1", name = "Gindi", email = "gindi@g.com"))
        )
        val viewModel = ProjectViewModel(repo, FakeExpenseDao(), FakeInvoiceDao(), clientDao)

        var successCalled = false
        viewModel.createProject(
            name = "Tel Aviv",
            budgetText = "1000",
            useExistingClient = false,
            selectedClientName = "",
            newClientName = "Gindi",
            newClientEmail = "another@g.com",
            newClientPhone = "",
            newClientAddress = "",
            newClientNotes = "",
            onSuccess = { successCalled = true }
        )
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals(0, clientDao.insertedClients.size)
        assertEquals(1, repo.insertedProjects.size)
        assertEquals("Gindi", repo.insertedProjects.first().clientName)
    }

    @Test
    fun `delete and undo project restores item`() = runTest {
        val project = ProjectEntity(id = "p1", name = "A", clientName = "B", budget = 100.0)
        val repo = FakeProjectRepository(projects = listOf(project))
        val viewModel = ProjectViewModel(repo, FakeExpenseDao(), FakeInvoiceDao(), FakeClientDao())

        val collectJob = launch { viewModel.projectsUiState.collect { } }

        viewModel.deleteProject(project)
        advanceUntilIdle()
        assertFalse(repo.projects.value.any { it.id == "p1" })

        viewModel.undoDeleteProject()
        advanceUntilIdle()
        assertTrue(repo.projects.value.any { it.id == "p1" })

        collectJob.cancel()
    }

    private class FakeProjectRepository(
        projects: List<ProjectEntity> = emptyList()
    ) : ProjectRepositoryContract {
        val projects = MutableStateFlow(projects)
        val insertedProjects = mutableListOf<ProjectEntity>()

        override fun getAllProjects(): Flow<List<ProjectEntity>> = projects

        override fun searchProjects(query: String): Flow<List<ProjectEntity>> {
            return projects.map { list ->
                list.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        it.clientName.contains(query, ignoreCase = true)
                }
            }
        }

        override suspend fun getProjectById(id: String): ProjectEntity? =
            projects.value.firstOrNull { it.id == id }

        override suspend fun insertProject(project: ProjectEntity) {
            insertedProjects += project
            projects.value = projects.value.filterNot { it.id == project.id } + project
        }

        override suspend fun updateProject(project: ProjectEntity) {
            projects.value = projects.value.map { if (it.id == project.id) project else it }
        }

        override suspend fun deleteProject(project: ProjectEntity) {
            projects.value = projects.value.filterNot { it.id == project.id }
        }
    }

    private class FakeExpenseDao(
        initial: List<ExpenseEntity> = emptyList()
    ) : ExpenseDao {
        private val expenses = MutableStateFlow(initial)

        override fun getAll(): Flow<List<ExpenseEntity>> = expenses

        override suspend fun getById(id: String): ExpenseEntity? =
            expenses.value.firstOrNull { it.id == id }

        override fun search(query: String): Flow<List<ExpenseEntity>> {
            return expenses.map { list ->
                list.filter {
                    it.descriptionText.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
                }
            }
        }

        override fun getForProject(projectId: String): Flow<List<ExpenseEntity>> {
            return expenses.map { list -> list.filter { it.projectId == projectId } }
        }

        override fun getForWorker(workerId: String): Flow<List<ExpenseEntity>> {
            return expenses.map { list -> list.filter { it.workerId == workerId } }
        }

        override fun getTotalForProject(projectId: String): Flow<Double?> {
            return expenses.map { list ->
                list.filter { it.projectId == projectId }.sumOf { it.amount }
            }
        }

        override suspend fun insert(expense: ExpenseEntity) {
            expenses.value = expenses.value + expense
        }

        override suspend fun update(expense: ExpenseEntity) {
            expenses.value = expenses.value.map { if (it.id == expense.id) expense else it }
        }

        override suspend fun delete(expense: ExpenseEntity) {
            expenses.value = expenses.value.filterNot { it.id == expense.id }
        }
    }

    private class FakeInvoiceDao(
        initial: List<InvoiceEntity> = emptyList()
    ) : InvoiceDao {
        private val invoices = MutableStateFlow(initial)

        override fun getAll(): Flow<List<InvoiceEntity>> = invoices

        override suspend fun getById(id: String): InvoiceEntity? =
            invoices.value.firstOrNull { it.id == id }

        override fun search(query: String): Flow<List<InvoiceEntity>> {
            return invoices.map { list -> list.filter { it.clientName.contains(query, ignoreCase = true) } }
        }

        override fun getForProject(projectId: String): Flow<List<InvoiceEntity>> {
            return invoices.map { list -> list.filter { it.projectId == projectId } }
        }

        override fun getUnpaid(): Flow<List<InvoiceEntity>> {
            return invoices.map { list -> list.filter { !it.isPaid } }
        }

        override suspend fun insert(invoice: InvoiceEntity) {
            invoices.value = invoices.value + invoice
        }

        override suspend fun update(invoice: InvoiceEntity) {
            invoices.value = invoices.value.map { if (it.id == invoice.id) invoice else it }
        }

        override suspend fun delete(invoice: InvoiceEntity) {
            invoices.value = invoices.value.filterNot { it.id == invoice.id }
        }
    }

    private class FakeClientDao(
        initial: List<ClientEntity> = emptyList()
    ) : ClientDao {
        private val clients = MutableStateFlow(initial)
        val insertedClients = mutableListOf<ClientEntity>()

        override fun getAll(): Flow<List<ClientEntity>> = clients

        override suspend fun getById(id: String): ClientEntity? =
            clients.value.firstOrNull { it.id == id }

        override suspend fun findByNameIgnoreCase(name: String): ClientEntity? =
            clients.value.firstOrNull { it.name.equals(name, ignoreCase = true) }

        override fun search(query: String): Flow<List<ClientEntity>> {
            return clients.map { list ->
                list.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        (it.email?.contains(query, ignoreCase = true) == true) ||
                        (it.phone?.contains(query, ignoreCase = true) == true)
                }
            }
        }

        override suspend fun insert(client: ClientEntity) {
            insertedClients += client
            clients.value = clients.value + client
        }

        override suspend fun update(client: ClientEntity) {
            clients.value = clients.value.map { if (it.id == client.id) client else it }
        }

        override suspend fun delete(client: ClientEntity) {
            clients.value = clients.value.filterNot { it.id == client.id }
        }
    }
}

