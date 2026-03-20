package com.yetzira.ContractorCashFlowAndroid.ui.projects

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity

data class ProjectListItemUi(
    val project: ProjectEntity,
    val totalExpenses: Double,
    val totalIncome: Double,
    val balance: Double,
    val profitMargin: Double,
    val budgetUtilization: Double
)

data class CategoryBreakdownUi(
    val category: String,
    val amount: Double,
    val percent: Float
)

data class ProjectListUiState(
    val query: String = "",
    val projects: List<ProjectListItemUi> = emptyList()
)

data class ProjectDetailUiState(
    val project: ProjectEntity? = null,
    val expenses: List<ExpenseEntity> = emptyList(),
    val invoices: List<InvoiceEntity> = emptyList(),
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    val profitMargin: Double = 0.0,
    val budgetUtilization: Double = 0.0,
    val categories: List<CategoryBreakdownUi> = emptyList()
)

data class ProjectFormUiState(
    val name: String = "",
    val budget: String = "",
    val useExistingClient: Boolean = true,
    val selectedClientName: String = "",
    val newClientName: String = "",
    val newClientEmail: String = "",
    val newClientPhone: String = "",
    val newClientAddress: String = "",
    val newClientNotes: String = "",
    val existingClients: List<ClientEntity> = emptyList(),
    val duplicateClientName: Boolean = false,
    val canSave: Boolean = false
)

