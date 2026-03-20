package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ClientDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.repository.ProjectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModel(
    private val repository: ProjectRepository,
    private val expenseDao: ExpenseDao,
    private val invoiceDao: InvoiceDao,
    private val clientDao: ClientDao
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedProjectId = MutableStateFlow<String?>(null)

    private var recentlyDeletedProject: ProjectEntity? = null
    private var recentlyDeletedExpense: ExpenseEntity? = null
    private var recentlyDeletedInvoice: InvoiceEntity? = null

    private val projectsFlow = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) repository.getAllProjects() else repository.searchProjects(query)
    }

    val projectsUiState: StateFlow<ProjectListUiState> = combine(
        searchQuery,
        projectsFlow,
        expenseDao.getAll(),
        invoiceDao.getAll()
    ) { query, projects, expenses, invoices ->
        val rows = projects.map { project ->
            val projectExpenses = expenses.filter { it.projectId == project.id }
            val projectInvoices = invoices.filter { it.projectId == project.id }
            val totalExpenses = projectExpenses.sumOf { it.amount }
            val totalIncome = projectInvoices.filter { it.isPaid }.sumOf { it.amount }
            val balance = totalIncome - totalExpenses
            val profitMargin = if (totalIncome > 0.0) (balance / totalIncome) * 100.0 else 0.0
            val budgetUtilization = if (project.budget > 0.0) (totalExpenses / project.budget) * 100.0 else 0.0

            ProjectListItemUi(
                project = project,
                totalExpenses = totalExpenses,
                totalIncome = totalIncome,
                balance = balance,
                profitMargin = profitMargin,
                budgetUtilization = budgetUtilization
            )
        }

        ProjectListUiState(
            query = query,
            projects = rows
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProjectListUiState()
    )

    val detailUiState: StateFlow<ProjectDetailUiState> = selectedProjectId
        .flatMapLatest { projectId ->
            if (projectId == null) {
                combine(
                    repository.getAllProjects().map { emptyList<ProjectEntity>() },
                    expenseDao.getAll().map { emptyList<ExpenseEntity>() },
                    invoiceDao.getAll().map { emptyList<InvoiceEntity>() }
                ) { _, _, _ -> ProjectDetailUiState() }
            } else {
                createProjectDetailFlow(projectId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProjectDetailUiState()
        )

    val existingClients: StateFlow<List<ClientEntity>> = clientDao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectProject(projectId: String) {
        selectedProjectId.value = projectId
    }

    fun createProject(
        name: String,
        budgetText: String,
        useExistingClient: Boolean,
        selectedClientName: String,
        newClientName: String,
        newClientEmail: String,
        newClientPhone: String,
        newClientAddress: String,
        newClientNotes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val budget = budgetText.toDoubleOrNull() ?: 0.0
            val clientName = if (useExistingClient) selectedClientName else newClientName
            if (name.isBlank() || clientName.isBlank() || budget <= 0.0) return@launch

            if (!useExistingClient) {
                clientDao.insert(
                    ClientEntity(
                        name = newClientName,
                        email = newClientEmail.ifBlank { null },
                        phone = newClientPhone.ifBlank { null },
                        address = newClientAddress.ifBlank { null },
                        notes = newClientNotes.ifBlank { null }
                    )
                )
            }

            repository.insertProject(
                ProjectEntity(
                    name = name,
                    clientName = clientName,
                    budget = budget
                )
            )
            onSuccess()
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            recentlyDeletedProject = project
            repository.deleteProject(project)
        }
    }

    fun undoDeleteProject() {
        val toRestore = recentlyDeletedProject ?: return
        viewModelScope.launch {
            repository.insertProject(toRestore)
            recentlyDeletedProject = null
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            recentlyDeletedExpense = expense
            expenseDao.delete(expense)
        }
    }

    fun undoDeleteExpense() {
        val toRestore = recentlyDeletedExpense ?: return
        viewModelScope.launch {
            expenseDao.insert(toRestore)
            recentlyDeletedExpense = null
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            recentlyDeletedInvoice = invoice
            invoiceDao.delete(invoice)
        }
    }

    fun undoDeleteInvoice() {
        val toRestore = recentlyDeletedInvoice ?: return
        viewModelScope.launch {
            invoiceDao.insert(toRestore)
            recentlyDeletedInvoice = null
        }
    }

    private fun createProjectDetailFlow(projectId: String): Flow<ProjectDetailUiState> = combine(
        projectsFlow,
        expenseDao.getForProject(projectId),
        invoiceDao.getForProject(projectId)
    ) { projects, expenses, invoices ->
        val project = projects.firstOrNull { it.id == projectId }
        val totalExpenses = expenses.sumOf { it.amount }
        val totalIncome = invoices.filter { it.isPaid }.sumOf { it.amount }
        val balance = totalIncome - totalExpenses
        val profitMargin = if (totalIncome > 0.0) (balance / totalIncome) * 100.0 else 0.0
        val budgetUtilization = if ((project?.budget ?: 0.0) > 0.0) {
            (totalExpenses / (project?.budget ?: 1.0)) * 100.0
        } else {
            0.0
        }

        val categories = expenses
            .groupBy { it.category }
            .map { (category, categoryExpenses) ->
                val amount = categoryExpenses.sumOf { it.amount }
                val percent = if (totalExpenses > 0.0) ((amount / totalExpenses) * 100f).toFloat() else 0f
                CategoryBreakdownUi(category, amount, percent)
            }
            .sortedByDescending { it.amount }

        ProjectDetailUiState(
            project = project,
            expenses = expenses.sortedByDescending { it.date },
            invoices = invoices.sortedByDescending { it.createdDate },
            totalExpenses = totalExpenses,
            totalIncome = totalIncome,
            balance = balance,
            profitMargin = profitMargin,
            budgetUtilization = budgetUtilization,
            categories = categories
        )
    }
}

