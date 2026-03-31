package com.yetzira.ContractorCashFlowAndroid.sync

import androidx.room.withTransaction
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.network.NetworkConnectivityChecker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.net.UnknownHostException

interface CloudSyncServiceContract {
    suspend fun pushAllData(): Result<Unit>
    suspend fun fullSync(): Result<Unit>
}

class FirestoreSyncService(
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val networkConnectivityChecker: NetworkConnectivityChecker = NetworkConnectivityChecker(
        FirebaseApp.getInstance().applicationContext
    )
) : CloudSyncServiceContract {
    suspend fun syncProject(project: ProjectEntity): Result<Unit> = writeDocument(
        collection = COLLECTION_PROJECTS,
        id = project.id,
        data = mapOf(
            "name" to project.name,
            "clientName" to project.clientName,
            "budget" to project.budget,
            "createdDate" to project.createdDate,
            "isActive" to project.isActive,
            "notes" to project.notes,
            "lastModified" to project.lastModified
        )
    )

    suspend fun syncExpense(expense: ExpenseEntity): Result<Unit> = writeDocument(
        collection = COLLECTION_EXPENSES,
        id = expense.id,
        data = mapOf(
            "category" to expense.category,
            "amount" to expense.amount,
            "descriptionText" to expense.descriptionText,
            "date" to expense.date,
            "projectId" to expense.projectId,
            "workerId" to expense.workerId,
            "unitsWorked" to expense.unitsWorked,
            "laborTypeSnapshot" to expense.laborTypeSnapshot,
            "notes" to expense.notes,
            "receiptImageUri" to expense.receiptImageUri,
            "lastModified" to expense.lastModified
        )
    )

    suspend fun syncInvoice(invoice: InvoiceEntity): Result<Unit> = writeDocument(
        collection = COLLECTION_INVOICES,
        id = invoice.id,
        data = mapOf(
            "amount" to invoice.amount,
            "dueDate" to invoice.dueDate,
            "isPaid" to invoice.isPaid,
            "clientName" to invoice.clientName,
            "createdDate" to invoice.createdDate,
            "projectId" to invoice.projectId,
            "lastModified" to invoice.lastModified
        )
    )

    suspend fun syncClient(client: ClientEntity): Result<Unit> = writeDocument(
        collection = COLLECTION_CLIENTS,
        id = client.id,
        data = mapOf(
            "name" to client.name,
            "email" to client.email,
            "phone" to client.phone,
            "address" to client.address,
            "notes" to client.notes,
            "lastModified" to client.lastModified
        )
    )

    suspend fun syncLaborDetails(labor: LaborDetailsEntity): Result<Unit> = writeDocument(
        collection = COLLECTION_LABOR_DETAILS,
        id = labor.id,
        data = mapOf(
            "workerName" to labor.workerName,
            "laborType" to labor.laborType,
            "hourlyRate" to labor.hourlyRate,
            "dailyRate" to labor.dailyRate,
            "contractPrice" to labor.contractPrice,
            "notes" to labor.notes,
            "createdDate" to labor.createdDate,
            "lastModified" to labor.lastModified
        )
    )

    suspend fun deleteProject(id: String): Result<Unit> = deleteDocument(COLLECTION_PROJECTS, id)

    suspend fun deleteExpense(id: String): Result<Unit> = deleteDocument(COLLECTION_EXPENSES, id)

    suspend fun deleteInvoice(id: String): Result<Unit> = deleteDocument(COLLECTION_INVOICES, id)

    suspend fun deleteClient(id: String): Result<Unit> = deleteDocument(COLLECTION_CLIENTS, id)

    suspend fun deleteLaborDetails(id: String): Result<Unit> = deleteDocument(COLLECTION_LABOR_DETAILS, id)

    /** Push all local Room data up to Firestore (local → cloud). */
    override suspend fun pushAllData(): Result<Unit> {
        return runCatching {
            requireNetwork()
            requireUserId() // fail fast if not signed in

            val projects = database.projectDao().getAll().first()
            val expenses = database.expenseDao().getAll().first()
            val invoices = database.invoiceDao().getAll().first()
            val clients = database.clientDao().getAll().first()
            val laborDetails = database.laborDetailsDao().getAll().first()

            projects.forEach { item ->
                runSyncStage("push projects/${item.id}") { syncProject(item).getOrThrow() }
            }
            expenses.forEach { item ->
                runSyncStage("push expenses/${item.id}") { syncExpense(item).getOrThrow() }
            }
            invoices.forEach { item ->
                runSyncStage("push invoices/${item.id}") { syncInvoice(item).getOrThrow() }
            }
            clients.forEach { item ->
                runSyncStage("push clients/${item.id}") { syncClient(item).getOrThrow() }
            }
            laborDetails.forEach { item ->
                runSyncStage("push laborDetails/${item.id}") { syncLaborDetails(item).getOrThrow() }
            }
        }
    }

    /** Full bidirectional sync: push local data up, then pull remote data down. */
    override suspend fun fullSync(): Result<Unit> {
        return runCatching {
            runSyncStage("fullSync.push") { pushAllData().getOrThrow() }
            runSyncStage("fullSync.pull") { pullAllData().getOrThrow() }
        }
    }

    suspend fun pullAllData(): Result<Unit> {
        return runCatching {
            requireNetwork()
            val uid = requireUserId()
            val userDoc = firestore.collection(COLLECTION_USERS).document(uid)

            val projectDocs = userDoc.collection(COLLECTION_PROJECTS).get().await().documents
            val expenseDocs = userDoc.collection(COLLECTION_EXPENSES).get().await().documents
            val invoiceDocs = userDoc.collection(COLLECTION_INVOICES).get().await().documents
            val clientDocs = userDoc.collection(COLLECTION_CLIENTS).get().await().documents
            val laborDocs = userDoc.collection(COLLECTION_LABOR_DETAILS).get().await().documents

            val projects = projectDocs.mapNotNull(::toProjectEntity)
            val expenses = expenseDocs.mapNotNull(::toExpenseEntity)
            val invoices = invoiceDocs.mapNotNull(::toInvoiceEntity)
            val clients = clientDocs.mapNotNull(::toClientEntity)
            val laborDetails = laborDocs.mapNotNull(::toLaborEntity)

            database.withTransaction {
                // Merge parent tables first so dependent rows can safely reference them.
                mergeProjects(projects)
                mergeClients(clients)
                mergeLaborDetails(laborDetails)
                mergeExpenses(expenses)
                mergeInvoices(invoices)
            }
        }
    }

    private suspend fun writeDocument(
        collection: String,
        id: String,
        data: Map<String, Any?>
    ): Result<Unit> {
        return runCatching {
            requireNetwork()
            val uid = requireUserId()
            firestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(collection)
                .document(id)
                .set(data, SetOptions.merge())
                .await()
        }
    }

    private suspend fun deleteDocument(collection: String, id: String): Result<Unit> {
        return runCatching {
            requireNetwork()
            val uid = requireUserId()
            firestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(collection)
                .document(id)
                .delete()
                .await()
        }
    }

    private fun requireUserId(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("User must sign in before cloud sync")

    private fun requireNetwork() {
        check(networkConnectivityChecker.canAttemptNetworkCall()) {
            "No validated internet connection. Check Wi‑Fi/mobile data, Private DNS, VPN, or ad-blocking settings."
        }
    }

    private suspend fun runSyncStage(stage: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (throwable: Throwable) {
            throw IllegalStateException(
                "Sync failed at $stage: ${formatSyncError(throwable)}",
                throwable
            )
        }
    }

    private fun formatSyncError(throwable: Throwable): String {
        val rootCause = throwable.rootCause()
        return when (throwable) {
            is FirebaseFirestoreException -> {
                val hint = when (throwable.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        "Check Firestore rules for users/{uid}/... write/read permissions."
                    FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                        "Auth session is missing or expired. Sign out and sign in again."
                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        if (rootCause is UnknownHostException) {
                            "Firestore DNS lookup failed. Check internet connection, Private DNS, VPN, or ad-blocking settings."
                        } else {
                            "Firestore service unavailable or network issue."
                        }
                    FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                        "Missing Firestore index or unmet precondition."
                    else -> null
                }
                if (hint == null) {
                    "Firestore ${throwable.code.name}: ${throwable.message.orEmpty()}"
                } else {
                    "Firestore ${throwable.code.name}: ${throwable.message.orEmpty()} $hint"
                }
            }
            is FirebaseNetworkException -> "Network error: ${throwable.message.orEmpty()}"
            is UnknownHostException -> "Unable to resolve Firestore host. Check internet connection, Private DNS, VPN, or ad-blocking settings."
            else -> {
                val rawMessage = throwable.message.orEmpty()
                when {
                    rootCause is UnknownHostException ->
                        "Unable to resolve Firestore host. Check internet connection, Private DNS, VPN, or ad-blocking settings."
                    rawMessage.contains("787") ->
                        "$rawMessage SQLite FK 787 while merging cloud data. Check project/worker references and resync."
                    else -> rawMessage.ifBlank { throwable::class.java.simpleName }
                }
            }
        }
    }

    private fun Throwable.rootCause(): Throwable {
        var current: Throwable = this
        while (current.cause != null && current.cause !== current) {
            current = current.cause!!
        }
        return current
    }

    private suspend fun mergeProjects(remote: List<ProjectEntity>) {
        remote.forEach { remoteItem ->
            val local = database.projectDao().getById(remoteItem.id)
            if (local == null) database.projectDao().insert(remoteItem)
            else if (remoteItem.lastModified >= local.lastModified) database.projectDao().update(remoteItem)
        }
    }

    private suspend fun mergeExpenses(remote: List<ExpenseEntity>) {
        remote.forEach { remoteItem ->
            val sanitized = sanitizeExpenseForeignKeys(remoteItem)
            val local = database.expenseDao().getById(remoteItem.id)
            if (local == null) database.expenseDao().insert(sanitized)
            else if (sanitized.lastModified >= local.lastModified) database.expenseDao().update(sanitized)
        }
    }

    private suspend fun mergeInvoices(remote: List<InvoiceEntity>) {
        remote.forEach { remoteItem ->
            val sanitized = sanitizeInvoiceForeignKeys(remoteItem)
            val local = database.invoiceDao().getById(remoteItem.id)
            if (local == null) database.invoiceDao().insert(sanitized)
            else if (sanitized.lastModified >= local.lastModified) database.invoiceDao().update(sanitized)
        }
    }

    private suspend fun sanitizeExpenseForeignKeys(expense: ExpenseEntity): ExpenseEntity {
        val validProjectId = expense.projectId?.let { projectId ->
            database.projectDao().getById(projectId)?.id
        }
        val validWorkerId = expense.workerId?.let { workerId ->
            database.laborDetailsDao().getById(workerId)?.id
        }
        return expense.copy(
            projectId = validProjectId,
            workerId = validWorkerId
        )
    }

    private suspend fun sanitizeInvoiceForeignKeys(invoice: InvoiceEntity): InvoiceEntity {
        val validProjectId = invoice.projectId?.let { projectId ->
            database.projectDao().getById(projectId)?.id
        }
        return invoice.copy(projectId = validProjectId)
    }

    private suspend fun mergeClients(remote: List<ClientEntity>) {
        remote.forEach { remoteItem ->
            val local = database.clientDao().getById(remoteItem.id)
            if (local == null) database.clientDao().insert(remoteItem)
            else if (remoteItem.lastModified >= local.lastModified) database.clientDao().update(remoteItem)
        }
    }

    private suspend fun mergeLaborDetails(remote: List<LaborDetailsEntity>) {
        remote.forEach { remoteItem ->
            val local = database.laborDetailsDao().getById(remoteItem.id)
            if (local == null) database.laborDetailsDao().insert(remoteItem)
            else if (remoteItem.lastModified >= local.lastModified) database.laborDetailsDao().update(remoteItem)
        }
    }

    private fun toProjectEntity(document: DocumentSnapshot): ProjectEntity? {
        val name = document.getString("name") ?: return null
        val clientName = document.getString("clientName") ?: ""
        return ProjectEntity(
            id = document.id,
            name = name,
            clientName = clientName,
            budget = document.getDouble("budget") ?: 0.0,
            createdDate = document.getLong("createdDate") ?: System.currentTimeMillis(),
            isActive = document.getBoolean("isActive") ?: true,
            notes = document.getString("notes") ?: "",
            lastModified = document.getLong("lastModified") ?: 0L
        )
    }

    private fun toExpenseEntity(document: DocumentSnapshot): ExpenseEntity? {
        val category = document.getString("category") ?: return null
        val description = document.getString("descriptionText") ?: return null
        return ExpenseEntity(
            id = document.id,
            category = category,
            amount = document.getDouble("amount") ?: 0.0,
            descriptionText = description,
            date = document.getLong("date") ?: System.currentTimeMillis(),
            projectId = document.getString("projectId"),
            workerId = document.getString("workerId"),
            unitsWorked = document.getDouble("unitsWorked"),
            laborTypeSnapshot = document.getString("laborTypeSnapshot"),
            notes = document.getString("notes"),
            receiptImageUri = document.getString("receiptImageUri"),
            lastModified = document.getLong("lastModified") ?: 0L
        )
    }

    private fun toInvoiceEntity(document: DocumentSnapshot): InvoiceEntity? {
        val clientName = document.getString("clientName") ?: return null
        return InvoiceEntity(
            id = document.id,
            amount = document.getDouble("amount") ?: 0.0,
            dueDate = document.getLong("dueDate") ?: System.currentTimeMillis(),
            isPaid = document.getBoolean("isPaid") ?: false,
            clientName = clientName,
            createdDate = document.getLong("createdDate") ?: System.currentTimeMillis(),
            projectId = document.getString("projectId"),
            lastModified = document.getLong("lastModified") ?: 0L
        )
    }

    private fun toClientEntity(document: DocumentSnapshot): ClientEntity? {
        val name = document.getString("name") ?: return null
        return ClientEntity(
            id = document.id,
            name = name,
            email = document.getString("email"),
            phone = document.getString("phone"),
            address = document.getString("address"),
            notes = document.getString("notes"),
            lastModified = document.getLong("lastModified") ?: 0L
        )
    }

    private fun toLaborEntity(document: DocumentSnapshot): LaborDetailsEntity? {
        val workerName = document.getString("workerName") ?: return null
        val laborType = document.getString("laborType") ?: return null
        return LaborDetailsEntity(
            id = document.id,
            workerName = workerName,
            laborType = laborType,
            hourlyRate = document.getDouble("hourlyRate"),
            dailyRate = document.getDouble("dailyRate"),
            contractPrice = document.getDouble("contractPrice"),
            notes = document.getString("notes"),
            createdDate = document.getLong("createdDate") ?: System.currentTimeMillis(),
            lastModified = document.getLong("lastModified") ?: 0L
        )
    }

    private companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_PROJECTS = "projects"
        const val COLLECTION_EXPENSES = "expenses"
        const val COLLECTION_INVOICES = "invoices"
        const val COLLECTION_CLIENTS = "clients"
        const val COLLECTION_LABOR_DETAILS = "laborDetails"
    }
}

