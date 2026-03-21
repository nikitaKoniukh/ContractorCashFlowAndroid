package com.yetzira.ContractorCashFlowAndroid.export

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectExportServiceTest {

    private val service = ProjectExportService()

    @Test
    fun `generateProjectReport includes summary and selected sections`() {
        val now = System.currentTimeMillis()
        val project = ProjectEntity(
            id = "p1",
            name = "Office Build",
            clientName = "Acme",
            budget = 10_000.0,
            createdDate = now - 86_400_000L,
            isActive = true,
            lastModified = now
        )
        val expenses = listOf(
            ExpenseEntity(
                id = "e1",
                category = "LABOR",
                amount = 1_250.0,
                descriptionText = "Painter",
                date = now - 10_000L,
                projectId = "p1",
                lastModified = now
            )
        )
        val invoices = listOf(
            InvoiceEntity(
                id = "i1",
                amount = 3_500.0,
                dueDate = now + 86_400_000L,
                isPaid = true,
                clientName = "Acme",
                createdDate = now,
                projectId = "p1",
                lastModified = now
            )
        )

        val report = service.generateProjectReport(
            project = project,
            expenses = expenses,
            invoices = invoices,
            includeExpenses = true,
            includeInvoices = true,
            currencyCode = "ILS"
        )

        assertTrue(report.contains("KablanPro Project Report"))
        assertTrue(report.contains("Project: Office Build"))
        assertTrue(report.contains("Financial Summary"))
        assertTrue(report.contains("Total Expenses: ILS 1,250.00"))
        assertTrue(report.contains("Total Income (Paid): ILS 3,500.00"))
        assertTrue(report.contains("Balance: ILS 2,250.00"))
        assertTrue(report.contains("Expenses"))
        assertTrue(report.contains("Invoices"))
    }

    @Test
    fun `generateProjectReport omits optional sections when toggles are off`() {
        val project = ProjectEntity(
            id = "p2",
            name = "Tiny Renovation",
            clientName = "Bob",
            budget = 500.0,
            createdDate = 1_700_000_000_000L,
            isActive = false,
            lastModified = 1_700_000_000_000L
        )

        val report = service.generateProjectReport(
            project = project,
            expenses = emptyList(),
            invoices = emptyList(),
            includeExpenses = false,
            includeInvoices = false,
            currencyCode = "USD"
        )

        assertTrue(report.contains("Status: Inactive"))
        assertFalse(report.contains("Date | Category | Description | Amount"))
        assertFalse(report.contains("Due Date | Client | Status | Amount"))
    }

    @Test
    fun `invoice status labels include overdue and pending`() {
        val now = System.currentTimeMillis()
        val project = ProjectEntity(
            id = "p3",
            name = "Warehouse",
            clientName = "Delta",
            budget = 7_000.0,
            createdDate = now,
            isActive = true,
            lastModified = now
        )
        val invoices = listOf(
            InvoiceEntity(
                id = "i-overdue",
                amount = 100.0,
                dueDate = now - 86_400_000L,
                isPaid = false,
                clientName = "Delta",
                createdDate = now,
                projectId = "p3",
                lastModified = now
            ),
            InvoiceEntity(
                id = "i-pending",
                amount = 100.0,
                dueDate = now + 86_400_000L,
                isPaid = false,
                clientName = "Delta",
                createdDate = now,
                projectId = "p3",
                lastModified = now
            )
        )

        val report = service.generateProjectReport(
            project = project,
            expenses = emptyList(),
            invoices = invoices,
            includeExpenses = false,
            includeInvoices = true,
            currencyCode = "USD"
        )

        assertTrue(report.contains("Overdue"))
        assertTrue(report.contains("Pending"))
    }
}

