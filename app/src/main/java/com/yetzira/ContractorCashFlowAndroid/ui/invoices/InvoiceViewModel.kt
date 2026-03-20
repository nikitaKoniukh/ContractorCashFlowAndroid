package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.repository.InvoiceRepository
import com.yetzira.ContractorCashFlowAndroid.notification.InvoiceNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val repository: InvoiceRepository,
    private val notificationScheduler: InvoiceNotificationScheduler
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val statusFilter = MutableStateFlow(InvoiceStatusFilter.ALL)
    private val editingInvoiceId = MutableStateFlow<String?>(null)

    private var recentlyDeleted: InvoiceEntity? = null

    val listUiState: StateFlow<InvoiceListUiState> = combine(
        query,
        statusFilter,
        repository.getAllInvoices(),
        repository.getAllProjects()
    ) { q, filter, invoices, projects ->
        val now = System.currentTimeMillis()

        val items = invoices
            .asSequence()
            .filter { q.isBlank() || it.clientName.contains(q, ignoreCase = true) }
            .filter { invoice ->
                when (filter) {
                    InvoiceStatusFilter.ALL -> true
                    InvoiceStatusFilter.PAID -> invoice.isPaid
                    InvoiceStatusFilter.UNPAID -> !invoice.isPaid
                    InvoiceStatusFilter.OVERDUE -> !invoice.isPaid && invoice.dueDate < now
                }
            }
            .sortedByDescending { it.createdDate }
            .map { invoice ->
                InvoiceListItemUi(
                    invoice = invoice,
                    projectName = projects.firstOrNull { it.id == invoice.projectId }?.name
                )
            }
            .toList()

        InvoiceListUiState(
            query = q,
            statusFilter = filter,
            invoices = items
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InvoiceListUiState()
    )

    val formUiState: StateFlow<InvoiceFormUiState> = combine(
        editingInvoiceId,
        repository.getAllInvoices(),
        repository.getAllClients(),
        repository.getAllProjects()
    ) { editId, invoices, clients, projects ->
        val activeProjects = projects.filter { it.isActive }
        val invoice = editId?.let { id -> invoices.firstOrNull { it.id == id } }

        if (invoice == null) {
            InvoiceFormUiState(
                existingClients = clients,
                projects = activeProjects
            )
        } else {
            InvoiceFormUiState(
                invoiceId = invoice.id,
                useExistingClient = clients.any { it.name == invoice.clientName },
                selectedClientName = invoice.clientName,
                enteredClientName = invoice.clientName,
                amount = invoice.amount.toString(),
                dueDate = invoice.dueDate,
                isPaid = invoice.isPaid,
                projectId = invoice.projectId,
                existingClients = clients,
                projects = activeProjects,
                canSave = true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InvoiceFormUiState()
    )

    fun startCreate() {
        editingInvoiceId.value = null
    }

    fun startEdit(invoiceId: String) {
        editingInvoiceId.value = invoiceId
    }

    fun setSearchQuery(value: String) {
        query.value = value
    }

    fun setStatusFilter(value: InvoiceStatusFilter) {
        statusFilter.value = value
    }

    fun updateForm(state: InvoiceFormUiState): InvoiceFormUiState {
        val clientName = if (state.useExistingClient) state.selectedClientName else state.enteredClientName
        val canSave = clientName.isNotBlank() && (state.amount.toDoubleOrNull() ?: 0.0) > 0.0
        return state.copy(canSave = canSave)
    }

    fun saveInvoice(state: InvoiceFormUiState, onDone: () -> Unit) {
        viewModelScope.launch {
            val amount = state.amount.toDoubleOrNull() ?: 0.0
            val clientName = if (state.useExistingClient) state.selectedClientName else state.enteredClientName
            if (clientName.isBlank() || amount <= 0.0) return@launch

            if (!state.useExistingClient) {
                val exists = state.existingClients.any { it.name.equals(clientName, ignoreCase = true) }
                if (!exists) {
                    repository.insertClient(ClientEntity(name = clientName))
                }
            }

            val invoice = InvoiceEntity(
                id = state.invoiceId ?: java.util.UUID.randomUUID().toString(),
                amount = amount,
                dueDate = state.dueDate,
                isPaid = state.isPaid,
                clientName = clientName,
                projectId = state.projectId,
                createdDate = if (state.invoiceId == null) System.currentTimeMillis() else {
                    repository.getInvoiceById(state.invoiceId)?.createdDate ?: System.currentTimeMillis()
                }
            )

            if (state.invoiceId == null) {
                repository.insertInvoice(invoice)
            } else {
                repository.updateInvoice(invoice)
            }

            if (invoice.isPaid) {
                notificationScheduler.cancel(invoice.id)
            } else {
                notificationScheduler.schedule(
                    invoiceId = invoice.id,
                    clientName = invoice.clientName,
                    dueDate = invoice.dueDate,
                    isPaid = invoice.isPaid
                )
            }

            onDone()
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            recentlyDeleted = invoice
            repository.deleteInvoice(invoice)
            notificationScheduler.cancel(invoice.id)
        }
    }

    fun deleteById(invoiceId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val invoice = repository.getInvoiceById(invoiceId) ?: return@launch
            repository.deleteInvoice(invoice)
            notificationScheduler.cancel(invoice.id)
            onDone()
        }
    }

    fun undoDelete() {
        val invoice = recentlyDeleted ?: return
        viewModelScope.launch {
            repository.insertInvoice(invoice)
            if (!invoice.isPaid) {
                notificationScheduler.schedule(
                    invoiceId = invoice.id,
                    clientName = invoice.clientName,
                    dueDate = invoice.dueDate,
                    isPaid = invoice.isPaid
                )
            }
            recentlyDeleted = null
        }
    }
}

