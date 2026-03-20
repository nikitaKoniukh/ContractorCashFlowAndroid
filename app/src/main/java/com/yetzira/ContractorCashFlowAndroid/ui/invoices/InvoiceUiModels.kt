package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity

enum class InvoiceStatusFilter {
    ALL,
    PAID,
    UNPAID,
    OVERDUE
}

data class InvoiceListItemUi(
    val invoice: InvoiceEntity,
    val projectName: String?
) {
    val isOverdue: Boolean
        get() = !invoice.isPaid && invoice.dueDate < System.currentTimeMillis()
}

data class InvoiceListUiState(
    val query: String = "",
    val statusFilter: InvoiceStatusFilter = InvoiceStatusFilter.ALL,
    val invoices: List<InvoiceListItemUi> = emptyList()
)

data class InvoiceFormUiState(
    val invoiceId: String? = null,
    val useExistingClient: Boolean = false,
    val selectedClientName: String = "",
    val enteredClientName: String = "",
    val amount: String = "",
    val dueDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val isPaid: Boolean = false,
    val projectId: String? = null,
    val existingClients: List<ClientEntity> = emptyList(),
    val projects: List<ProjectEntity> = emptyList(),
    val canSave: Boolean = false
)

