package com.yetzira.ContractorCashFlowAndroid.ui.invoices

object InvoiceRoutes {
    const val GRAPH = "invoices_graph"
    const val LIST = "invoices/list"
    const val NEW = "invoices/new"
    const val EDIT = "invoices/edit/{invoiceId}"

    fun edit(invoiceId: String): String = "invoices/edit/$invoiceId"
}

