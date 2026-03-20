package com.yetzira.ContractorCashFlowAndroid.ui.projects

import android.net.Uri

object ProjectRoutes {
    const val GRAPH = "projects_graph"
    const val LIST = "projects/list"
    const val NEW = "projects/new"
    const val DETAIL = "projects/detail/{projectId}"
    const val EDIT = "projects/edit/{projectId}"
    const val CLIENT_DETAIL = "projects/client/{clientName}"

    fun detail(projectId: String): String = "projects/detail/$projectId"
    fun edit(projectId: String): String = "projects/edit/$projectId"
    fun clientDetail(clientName: String): String = "projects/client/${Uri.encode(clientName)}"
}

