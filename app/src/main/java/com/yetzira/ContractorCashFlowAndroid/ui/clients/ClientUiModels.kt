package com.yetzira.ContractorCashFlowAndroid.ui.clients

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity

data class ClientsListUiState(
    val query: String = "",
    val clients: List<ClientEntity> = emptyList()
)

data class ClientFormUiState(
    val id: String? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val canSave: Boolean = false,
    val hasChanges: Boolean = false
)

data class ClientDetailUiState(
    val client: ClientEntity? = null
)

