package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.data.repository.InvoiceRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.notification.InvoiceNotificationSchedulerContract
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
class InvoiceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `updateForm enables save when client and amount are valid`() {
        val viewModel = createViewModel()

        val updated = viewModel.updateForm(
            InvoiceFormUiState(
                useExistingClient = false,
                enteredClientName = "Alice",
                amount = "1000"
            )
        )

        assertTrue(updated.canSave)
    }

    @Test
    fun `saveInvoice for unpaid invoice schedules notifications`() = runTest {
        val repository = FakeInvoiceRepository()
        val scheduler = FakeInvoiceNotificationScheduler()
        val preferences = FakePreferencesRepository(invoiceReminders = true, overdueAlerts = true)
        val viewModel = createViewModel(repository, scheduler, preferences)

        viewModel.saveInvoice(
            state = InvoiceFormUiState(
                useExistingClient = false,
                enteredClientName = "New Client",
                amount = "2.500",
                dueDate = System.currentTimeMillis() + 86_400_000L,
                isPaid = false
            ),
            onDone = {}
        )
        advanceUntilIdle()

        assertEquals(1, repository.insertedInvoices.size)
        assertEquals(1, scheduler.scheduled.size)
        assertEquals("New Client", scheduler.scheduled.first().clientName)
        assertEquals(1, repository.insertedClients.size)
    }

    @Test
    fun `saveInvoice for paid invoice cancels notifications`() = runTest {
        val scheduler = FakeInvoiceNotificationScheduler()
        val repository = FakeInvoiceRepository()
        val viewModel = createViewModel(repository = repository, scheduler = scheduler)

        viewModel.saveInvoice(
            state = InvoiceFormUiState(
                useExistingClient = false,
                enteredClientName = "Client",
                amount = "100",
                isPaid = true
            ),
            onDone = {}
        )
        advanceUntilIdle()

        assertEquals(1, repository.insertedInvoices.size)
        assertEquals(1, scheduler.canceledIds.size)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `delete and undo restore invoice and reschedule when unpaid`() = runTest {
        val invoice = InvoiceEntity(
            id = "inv1",
            amount = 300.0,
            dueDate = System.currentTimeMillis() + 86_400_000L,
            isPaid = false,
            clientName = "Client A"
        )
        val repository = FakeInvoiceRepository(initialInvoices = listOf(invoice))
        val scheduler = FakeInvoiceNotificationScheduler()
        val viewModel = createViewModel(repository = repository, scheduler = scheduler)

        val collectJob = launch { viewModel.listUiState.collect { } }

        viewModel.deleteInvoice(invoice)
        advanceUntilIdle()
        assertEquals(listOf("inv1"), scheduler.canceledIds)
        assertTrue(repository.invoices.value.none { it.id == "inv1" })

        viewModel.undoDelete()
        advanceUntilIdle()
        assertTrue(repository.invoices.value.any { it.id == "inv1" })
        assertEquals(1, scheduler.scheduled.size)

        collectJob.cancel()
    }

    @Test
    fun `status filter overdue returns only overdue invoices`() = runTest {
        val now = System.currentTimeMillis()
        val repository = FakeInvoiceRepository(
            initialInvoices = listOf(
                InvoiceEntity(id = "a", amount = 100.0, dueDate = now - 1_000, isPaid = false, clientName = "A", createdDate = now),
                InvoiceEntity(id = "b", amount = 100.0, dueDate = now + 1_000, isPaid = false, clientName = "B", createdDate = now),
                InvoiceEntity(id = "c", amount = 100.0, dueDate = now - 2_000, isPaid = true, clientName = "C", createdDate = now)
            )
        )
        val viewModel = createViewModel(repository = repository)

        val collectJob = launch { viewModel.listUiState.collect { } }
        viewModel.setStatusFilter(InvoiceStatusFilter.OVERDUE)
        advanceUntilIdle()

        val ids = viewModel.listUiState.value.invoices.map { it.invoice.id }
        assertEquals(listOf("a"), ids)

        collectJob.cancel()
    }

    private fun createViewModel(
        repository: FakeInvoiceRepository = FakeInvoiceRepository(),
        scheduler: FakeInvoiceNotificationScheduler = FakeInvoiceNotificationScheduler(),
        preferences: FakePreferencesRepository = FakePreferencesRepository()
    ): InvoiceViewModel {
        return InvoiceViewModel(repository, scheduler, preferences)
    }

    private class FakeInvoiceRepository(
        initialInvoices: List<InvoiceEntity> = emptyList(),
        initialClients: List<ClientEntity> = emptyList(),
        initialProjects: List<ProjectEntity> = emptyList()
    ) : InvoiceRepositoryContract {
        val invoices = MutableStateFlow(initialInvoices)
        val clients = MutableStateFlow(initialClients)
        val projects = MutableStateFlow(initialProjects)

        val insertedInvoices = mutableListOf<InvoiceEntity>()
        val updatedInvoices = mutableListOf<InvoiceEntity>()
        val insertedClients = mutableListOf<ClientEntity>()

        override fun getAllInvoices(): Flow<List<InvoiceEntity>> = invoices

        override suspend fun getInvoiceById(id: String): InvoiceEntity? =
            invoices.value.firstOrNull { it.id == id }

        override suspend fun insertInvoice(invoice: InvoiceEntity) {
            insertedInvoices += invoice
            invoices.value = invoices.value.filterNot { it.id == invoice.id } + invoice
        }

        override suspend fun updateInvoice(invoice: InvoiceEntity) {
            updatedInvoices += invoice
            invoices.value = invoices.value.map { if (it.id == invoice.id) invoice else it }
        }

        override suspend fun deleteInvoice(invoice: InvoiceEntity) {
            invoices.value = invoices.value.filterNot { it.id == invoice.id }
        }

        override fun getAllClients(): Flow<List<ClientEntity>> = clients

        override suspend fun insertClient(client: ClientEntity) {
            insertedClients += client
            clients.value = clients.value + client
        }

        override fun getAllProjects(): Flow<List<ProjectEntity>> = projects
    }

    private class FakeInvoiceNotificationScheduler : InvoiceNotificationSchedulerContract {
        data class Scheduled(
            val invoiceId: String,
            val clientName: String,
            val dueDate: Long,
            val isPaid: Boolean,
            val invoiceRemindersEnabled: Boolean,
            val overdueAlertsEnabled: Boolean
        )

        val scheduled = mutableListOf<Scheduled>()
        val canceledIds = mutableListOf<String>()

        override fun schedule(
            invoiceId: String,
            clientName: String,
            dueDate: Long,
            isPaid: Boolean,
            invoiceRemindersEnabled: Boolean,
            overdueAlertsEnabled: Boolean
        ) {
            scheduled += Scheduled(
                invoiceId,
                clientName,
                dueDate,
                isPaid,
                invoiceRemindersEnabled,
                overdueAlertsEnabled
            )
        }

        override fun rescheduleAll(
            invoices: List<InvoiceEntity>,
            invoiceRemindersEnabled: Boolean,
            overdueAlertsEnabled: Boolean
        ) = Unit

        override fun cancel(invoiceId: String) {
            canceledIds += invoiceId
        }
    }

    private class FakePreferencesRepository(
        invoiceReminders: Boolean = true,
        overdueAlerts: Boolean = true
    ) : UserPreferencesRepositoryContract {
        override val invoiceRemindersEnabled: Flow<Boolean> = MutableStateFlow(invoiceReminders)
        override val overdueAlertsEnabled: Flow<Boolean> = MutableStateFlow(overdueAlerts)
    }
}

