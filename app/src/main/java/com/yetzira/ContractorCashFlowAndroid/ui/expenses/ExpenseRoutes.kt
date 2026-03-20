package com.yetzira.ContractorCashFlowAndroid.ui.expenses

object ExpenseRoutes {
    const val GRAPH = "expenses_graph"
    const val LIST = "expenses/list"
    const val NEW = "expenses/new"
    const val EDIT = "expenses/edit/{expenseId}"

    fun edit(expenseId: String): String = "expenses/edit/$expenseId"
}

