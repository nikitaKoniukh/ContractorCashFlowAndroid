package com.yetzira.ContractorCashFlowAndroid.export

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectExportService {

    fun generateProjectReport(
        project: ProjectEntity,
        expenses: List<ExpenseEntity>,
        invoices: List<InvoiceEntity>,
        includeExpenses: Boolean,
        includeInvoices: Boolean,
        currencyCode: String
    ): String {
        val paidIncome = invoices.filter { it.isPaid }.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val balance = paidIncome - totalExpenses
        val now = System.currentTimeMillis()

        val builder = StringBuilder()
        builder.appendLine("KablanPro Project Report")
        builder.appendLine("========================")
        builder.appendLine("Generated: ${formatDateTime(now)}")
        builder.appendLine("Project: ${project.name}")
        builder.appendLine("Client: ${project.clientName}")
        builder.appendLine("Status: ${if (project.isActive) "Active" else "Inactive"}")
        builder.appendLine("Created: ${formatDate(project.createdDate)}")
        builder.appendLine()

        builder.appendLine("Financial Summary")
        builder.appendLine("-----------------")
        builder.appendLine("Budget: ${formatCurrency(project.budget, currencyCode)}")
        builder.appendLine("Total Expenses: ${formatCurrency(totalExpenses, currencyCode)}")
        builder.appendLine("Total Income (Paid): ${formatCurrency(paidIncome, currencyCode)}")
        builder.appendLine("Balance: ${formatCurrency(balance, currencyCode)}")

        if (includeExpenses) {
            builder.appendLine()
            builder.appendLine("Expenses")
            builder.appendLine("--------")
            if (expenses.isEmpty()) {
                builder.appendLine("No expenses")
            } else {
                builder.appendLine("Date | Category | Description | Amount")
                builder.appendLine("---- | -------- | ----------- | ------")
                expenses.sortedByDescending { it.date }.forEach { expense ->
                    builder.appendLine(
                        "${formatDate(expense.date)} | ${expense.category} | ${expense.descriptionText} | ${formatCurrency(expense.amount, currencyCode)}"
                    )
                }
            }
        }

        if (includeInvoices) {
            builder.appendLine()
            builder.appendLine("Invoices")
            builder.appendLine("--------")
            if (invoices.isEmpty()) {
                builder.appendLine("No invoices")
            } else {
                builder.appendLine("Due Date | Client | Status | Amount")
                builder.appendLine("-------- | ------ | ------ | ------")
                invoices.sortedByDescending { it.createdDate }.forEach { invoice ->
                    builder.appendLine(
                        "${formatDate(invoice.dueDate)} | ${invoice.clientName} | ${invoice.statusLabel()} | ${formatCurrency(invoice.amount, currencyCode)}"
                    )
                }
            }
        }

        return builder.toString().trimEnd()
    }

    private fun formatDate(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))

    private fun formatDateTime(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))

    private fun formatCurrency(amount: Double, currencyCode: String): String =
        "${String.format(Locale.US, "%,.2f", amount)} $currencyCode"

    private fun InvoiceEntity.statusLabel(): String {
        val now = System.currentTimeMillis()
        return when {
            isPaid -> "Paid"
            dueDate < now -> "Overdue"
            else -> "Pending"
        }
    }
}

