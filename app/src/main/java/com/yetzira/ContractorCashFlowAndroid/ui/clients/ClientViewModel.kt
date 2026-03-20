package com.yetzira.ContractorCashFlowAndroid.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.repository.ClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ClientViewModel(
    private val repository: ClientRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedClientId = MutableStateFlow<String?>(null)

    private var originalClient: ClientEntity? = null
    private var recentlyDeleted: ClientEntity? = null

    private val clientsFlow = query.flatMapLatest { q ->
        if (q.isBlank()) repository.getAllClients() else repository.searchClients(q)
    }

    val listUiState: StateFlow<ClientsListUiState> = combine(
        query,
        clientsFlow
    ) { q, clients ->
        ClientsListUiState(
            query = q,
            clients = clients.sortedBy { it.name.lowercase() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientsListUiState()
    )

    val detailUiState: StateFlow<ClientDetailUiState> = combine(
        selectedClientId,
        clientsFlow
    ) { id, clients ->
        ClientDetailUiState(client = clients.firstOrNull { it.id == id })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClientDetailUiState()
    )

    fun setSearchQuery(value: String) {
        query.value = value
    }

    fun selectClient(clientId: String) {
        selectedClientId.value = clientId
    }

    fun setOriginalClient(client: ClientEntity?) {
        originalClient = client
    }

    fun buildFormState(client: ClientEntity?): ClientFormUiState {
        return if (client == null) {
            ClientFormUiState()
        } else {
            ClientFormUiState(
                id = client.id,
                name = client.name,
                email = client.email.orEmpty(),
                phone = client.phone.orEmpty(),
                address = client.address.orEmpty(),
                notes = client.notes.orEmpty(),
                canSave = client.name.isNotBlank(),
                hasChanges = false
            )
        }
    }

    fun updateForm(state: ClientFormUiState): ClientFormUiState {
        val trimmedName = state.name.trim()
        val normalized = state.copy(name = trimmedName)

        val original = originalClient
        val hasChanges = if (original == null) {
            trimmedName.isNotBlank() ||
                normalized.email.isNotBlank() ||
                normalized.phone.isNotBlank() ||
                normalized.address.isNotBlank() ||
                normalized.notes.isNotBlank()
        } else {
            trimmedName != original.name ||
                normalized.email.trim().ifBlank { null } != original.email ||
                normalized.phone.trim().ifBlank { null } != original.phone ||
                normalized.address.trim().ifBlank { null } != original.address ||
                normalized.notes.trim().ifBlank { null } != original.notes
        }

        return normalized.copy(
            canSave = trimmedName.isNotBlank() && (original == null || hasChanges),
            hasChanges = hasChanges
        )
    }

    fun saveClient(state: ClientFormUiState, onDone: () -> Unit) {
        viewModelScope.launch {
            if (state.name.isBlank()) return@launch
            val entity = ClientEntity(
                id = state.id ?: java.util.UUID.randomUUID().toString(),
                name = state.name.trim(),
                email = state.email.trim().ifBlank { null },
                phone = state.phone.trim().ifBlank { null },
                address = state.address.trim().ifBlank { null },
                notes = state.notes.trim().ifBlank { null }
            )

            if (state.id == null) {
                repository.insertClient(entity)
            } else {
                repository.updateClient(entity)
            }
            onDone()
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            recentlyDeleted = client
            repository.deleteClient(client)
        }
    }

    fun deleteById(clientId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val client = repository.getClientById(clientId) ?: return@launch
            repository.deleteClient(client)
            onDone()
        }
    }

    fun undoDelete() {
        val client = recentlyDeleted ?: return
        viewModelScope.launch {
            repository.insertClient(client)
            recentlyDeleted = null
        }
    }
}

