package com.yetzira.ContractorCashFlowAndroid.ui.clients

import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.repository.ClientRepositoryContract
import com.yetzira.ContractorCashFlowAndroid.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClientViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `updateForm marks new client as saveable when name is provided`() {
        val viewModel = ClientViewModel(FakeClientRepository())

        val updated = viewModel.updateForm(ClientFormUiState(name = "  Alice  "))

        assertEquals("Alice", updated.name)
        assertTrue(updated.canSave)
        assertTrue(updated.hasChanges)
    }

    @Test
    fun `updateForm detects no changes when editing unchanged existing client`() {
        val repository = FakeClientRepository()
        val viewModel = ClientViewModel(repository)
        val original = ClientEntity(id = "c1", name = "Alice", email = "a@mail.com")
        viewModel.setOriginalClient(original)

        val updated = viewModel.updateForm(
            ClientFormUiState(
                id = "c1",
                name = "Alice",
                email = "a@mail.com"
            )
        )

        assertFalse(updated.hasChanges)
        assertFalse(updated.canSave)
    }

    @Test
    fun `saveClient inserts and normalizes optional blanks to null`() = runTest {
        val repository = FakeClientRepository()
        val viewModel = ClientViewModel(repository)
        var doneCalled = false

        viewModel.saveClient(
            state = ClientFormUiState(
                name = " Bob ",
                email = " ",
                phone = " ",
                address = "",
                notes = "  note  "
            ),
            onDone = { doneCalled = true }
        )

        advanceUntilIdle()

        val saved = repository.clients.value.single()
        assertEquals("Bob", saved.name)
        assertNull(saved.email)
        assertNull(saved.phone)
        assertNull(saved.address)
        assertEquals("note", saved.notes)
        assertTrue(doneCalled)
    }

    @Test
    fun `delete and undo restore client`() = runTest {
        val repository = FakeClientRepository(
            initial = listOf(ClientEntity(id = "c1", name = "Client A"))
        )
        val viewModel = ClientViewModel(repository)

        val collectJob = launch { viewModel.listUiState.collect { } }

        val client = repository.clients.value.first()
        viewModel.deleteClient(client)
        advanceUntilIdle()
        assertTrue(repository.clients.value.isEmpty())

        viewModel.undoDelete()
        advanceUntilIdle()
        assertNotNull(repository.clients.value.firstOrNull { it.id == "c1" })

        collectJob.cancel()
    }

    private class FakeClientRepository(
        initial: List<ClientEntity> = emptyList()
    ) : ClientRepositoryContract {
        val clients = MutableStateFlow(initial)

        override fun getAllClients(): Flow<List<ClientEntity>> = clients

        override fun searchClients(query: String): Flow<List<ClientEntity>> {
            return clients.map { items ->
                items.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        (it.email?.contains(query, ignoreCase = true) == true) ||
                        (it.phone?.contains(query, ignoreCase = true) == true)
                }
            }
        }

        override suspend fun getClientById(id: String): ClientEntity? =
            clients.value.firstOrNull { it.id == id }

        override suspend fun insertClient(client: ClientEntity) {
            clients.value = clients.value.filterNot { it.id == client.id } + client
        }

        override suspend fun updateClient(client: ClientEntity) {
            clients.value = clients.value.map { if (it.id == client.id) client else it }
        }

        override suspend fun deleteClient(client: ClientEntity) {
            clients.value = clients.value.filterNot { it.id == client.id }
        }
    }
}

