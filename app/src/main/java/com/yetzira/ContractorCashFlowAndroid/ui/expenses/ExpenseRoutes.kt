package com.yetzira.ContractorCashFlowAndroid.ui.expenses

object ExpenseRoutes {
    const val GRAPH = "expenses_graph"
    const val LIST = "expenses/list"
    const val NEW = "expenses/new"
    const val DETAIL = "expenses/detail/{expenseId}"
    const val EDIT = "expenses/edit/{expenseId}"
    const val SCAN = "expenses/scan"
    const val SCAN_REVIEW = "expenses/scan_review/{imageUri}"

    fun detail(expenseId: String): String = "expenses/detail/$expenseId"
    fun edit(expenseId: String): String = "expenses/edit/$expenseId"
    fun scanReview(imageUri: String): String = "expenses/scan_review/$imageUri"
}

