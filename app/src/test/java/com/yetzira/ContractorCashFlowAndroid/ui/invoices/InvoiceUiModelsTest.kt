package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceUiModelsTest {

    @Test
    fun `invoice list item is overdue when unpaid and due date is in the past`() {
        val invoice = InvoiceEntity(
            id = "inv-overdue",
            amount = 100.0,
            dueDate = System.currentTimeMillis() - 1_000,
            isPaid = false,
            clientName = "Client"
        )

        val item = InvoiceListItemUi(invoice = invoice, projectName = null)

        assertTrue(item.isOverdue)
    }

    @Test
    fun `invoice list item is not overdue when paid`() {
        val invoice = InvoiceEntity(
            id = "inv-paid",
            amount = 100.0,
            dueDate = System.currentTimeMillis() - 1_000,
            isPaid = true,
            clientName = "Client"
        )

        val item = InvoiceListItemUi(invoice = invoice, projectName = null)

        assertFalse(item.isOverdue)
    }
}

