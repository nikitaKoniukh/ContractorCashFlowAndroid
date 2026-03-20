package com.yetzira.ContractorCashFlowAndroid.export

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportFileNameFormatter {
    fun suggestedFileName(now: Long = System.currentTimeMillis()): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
        return "KablanPro_Export_${date}.json"
    }
}

class AppDataSnapshotExporter(
    private val context: Context,
    private val database: AppDatabase
) {
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    suspend fun createSnapshotJson(): String {
        val snapshot = AppDataSnapshot(
            exportedAt = System.currentTimeMillis(),
            projects = database.projectDao().getAll().first(),
            expenses = database.expenseDao().getAll().first(),
            invoices = database.invoiceDao().getAll().first(),
            clients = database.clientDao().getAll().first(),
            labor = database.laborDetailsDao().getAll().first()
        )
        return gson.toJson(snapshot)
    }

    suspend fun exportToUri(uri: Uri): Result<Unit> {
        return runCatching {
            val json = createSnapshotJson()
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: error("Unable to open export destination")
            outputStream.bufferedWriter().use { writer ->
                writer.write(json)
            }
        }
    }

    fun suggestedFileName(now: Long = System.currentTimeMillis()): String =
        ExportFileNameFormatter.suggestedFileName(now)
}

