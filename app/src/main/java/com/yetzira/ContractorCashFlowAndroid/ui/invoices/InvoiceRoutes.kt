package com.yetzira.ContractorCashFlowAndroid.ui.invoices

object InvoiceRoutes {
    const val GRAPH = "invoices_graph"
    const val LIST = "invoices/list"
    const val NEW = "invoices/new"
    const val DETAIL = "invoices/detail/{invoiceId}"
    const val EDIT = "invoices/edit/{invoiceId}"

    fun detail(invoiceId: String): String = "invoices/detail/$invoiceId"
    fun edit(invoiceId: String): String = "invoices/edit/$invoiceId"
}

