package com.yetzira.ContractorCashFlowAndroid.export

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ExportFileNameFormatter {
    fun suggestedFileName(now: Long = System.currentTimeMillis()): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
        return "KablanPro_Export_${date}.json"
    }
}

class DataExportService(
    private val context: Context,
    private val database: AppDatabase,
    private val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(context.applicationContext)
) {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun generateExportJson(@Suppress("UNUSED_PARAMETER") context: Context = this.context): String {
        val now = System.currentTimeMillis()
        val projects = database.projectDao().getAll().first()
        val expenses = database.expenseDao().getAll().first()
        val invoices = database.invoiceDao().getAll().first()
        val clients = database.clientDao().getAll().first()

        val payload = DataExportPayload(
            exportedAt = now.toIso8601(),
            preferences = ExportPreferences(
                appLanguage = preferencesRepository.appLanguage.first().code,
                selectedCurrencyCode = preferencesRepository.selectedCurrencyCode.first().code,
                invoiceRemindersEnabled = preferencesRepository.invoiceRemindersEnabled.first(),
                overdueAlertsEnabled = preferencesRepository.overdueAlertsEnabled.first(),
                budgetWarningsEnabled = preferencesRepository.budgetWarningsEnabled.first()
            ),
            projects = projects.map { project ->
                val projectExpenses = expenses.filter { it.projectId == project.id }
                val projectIncome = invoices.filter { it.projectId == project.id && it.isPaid }.sumOf { it.amount }
                val projectExpensesTotal = projectExpenses.sumOf { it.amount }
                ExportProject(
                    id = project.id,
                    name = project.name,
                    clientName = project.clientName,
                    budget = project.budget,
                    isActive = project.isActive,
                    createdDate = project.createdDate.toIso8601(),
                    lastModified = project.lastModified.toIso8601(),
                    totalExpenses = projectExpensesTotal,
                    totalIncome = projectIncome,
                    balance = projectIncome - projectExpensesTotal
                )
            },
            expenses = expenses.map { it.toExportExpense() },
            invoices = invoices.map { it.toExportInvoice(now) },
            clients = clients.map { it.toExportClient() }
        )

        return gson.toJson(payload)
    }

    suspend fun exportToUri(uri: Uri): Result<Unit> {
        return runCatching {
            val json = generateExportJson()
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: error("Unable to open export destination")
            outputStream.bufferedWriter().use { writer ->
                writer.write(json)
            }
        }
    }

    fun suggestedFileName(now: Long = System.currentTimeMillis()): String =
        ExportFileNameFormatter.suggestedFileName(now)
}

private data class DataExportPayload(
    val exportedAt: String,
    val preferences: ExportPreferences,
    val projects: List<ExportProject>,
    val expenses: List<ExportExpense>,
    val invoices: List<ExportInvoice>,
    val clients: List<ExportClient>
)

private data class ExportPreferences(
    val appLanguage: String,
    val selectedCurrencyCode: String,
    val invoiceRemindersEnabled: Boolean,
    val overdueAlertsEnabled: Boolean,
    val budgetWarningsEnabled: Boolean
)

private data class ExportProject(
    val id: String,
    val name: String,
    val clientName: String,
    val budget: Double,
    val isActive: Boolean,
    val createdDate: String,
    val lastModified: String,
    val totalExpenses: Double,
    val totalIncome: Double,
    val balance: Double
)

private data class ExportExpense(
    val id: String,
    val category: String,
    val amount: Double,
    val descriptionText: String,
    val date: String,
    val projectId: String?,
    val workerId: String?,
    val unitsWorked: Double?,
    val laborTypeSnapshot: String?,
    val lastModified: String
)

private data class ExportInvoice(
    val id: String,
    val amount: Double,
    val dueDate: String,
    val isPaid: Boolean,
    val isOverdue: Boolean,
    val clientName: String,
    val createdDate: String,
    val projectId: String?,
    val lastModified: String
)

private data class ExportClient(
    val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val notes: String?,
    val lastModified: String
)

private fun ExpenseEntity.toExportExpense(): ExportExpense = ExportExpense(
    id = id,
    category = category,
    amount = amount,
    descriptionText = descriptionText,
    date = date.toIso8601(),
    projectId = projectId,
    workerId = workerId,
    unitsWorked = unitsWorked,
    laborTypeSnapshot = laborTypeSnapshot,
    lastModified = lastModified.toIso8601()
)

private fun InvoiceEntity.toExportInvoice(now: Long): ExportInvoice = ExportInvoice(
    id = id,
    amount = amount,
    dueDate = dueDate.toIso8601(),
    isPaid = isPaid,
    isOverdue = !isPaid && dueDate < now,
    clientName = clientName,
    createdDate = createdDate.toIso8601(),
    projectId = projectId,
    lastModified = lastModified.toIso8601()
)

private fun ClientEntity.toExportClient(): ExportClient = ExportClient(
    id = id,
    name = name,
    email = email,
    phone = phone,
    address = address,
    notes = notes,
    lastModified = lastModified.toIso8601()
)

private fun Long.toIso8601(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(this))
}

