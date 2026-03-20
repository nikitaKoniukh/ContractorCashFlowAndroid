package com.yetzira.ContractorCashFlowAndroid.ui.labor

object LaborRoutes {
    const val GRAPH = "labor_graph"
    const val LIST = "labor/list"
    const val ADD = "labor/add"
    const val EDIT = "labor/edit/{workerId}"

    fun edit(workerId: String): String = "labor/edit/$workerId"
}

