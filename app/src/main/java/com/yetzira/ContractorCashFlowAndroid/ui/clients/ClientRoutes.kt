package com.yetzira.ContractorCashFlowAndroid.ui.clients

object ClientRoutes {
    const val GRAPH = "clients_graph"
    const val LIST = "clients/list"
    const val NEW = "clients/new"
    const val DETAIL = "clients/detail/{clientId}"
    const val EDIT = "clients/edit/{clientId}"

    fun detail(clientId: String): String = "clients/detail/$clientId"
    fun edit(clientId: String): String = "clients/edit/$clientId"
}

