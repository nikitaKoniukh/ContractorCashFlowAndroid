package com.yetzira.ContractorCashFlowAndroid.ui.labor

object LaborRoutes {
    const val GRAPH = "labor_graph"
    const val LIST = "labor/list"
    const val ADD = "labor/add"
    const val EDIT = "labor/edit/{workerId}"
    const val WORKER_EXPENSES = "labor/expenses/{workerId}"

    fun edit(workerId: String): String = "labor/edit/$workerId"
    fun workerExpenses(workerId: String): String = "labor/expenses/$workerId"
}

