package com.yetzira.ContractorCashFlowAndroid.sync

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Uploads / downloads receipt images via Firebase Storage
 * (Android equivalent of iOS CloudKit external storage for receipt binaries).
 *
 * Cloud path: users/{uid}/receipts/{expenseId}
 * Local cache: filesDir/receipts/synced_{expenseId}.*
 */
object ReceiptStorageHelper {

    fun isRemoteUri(uri: String?): Boolean {
        if (uri.isNullOrBlank()) return false
        return uri.startsWith("https://") ||
            uri.startsWith("gs://") ||
            uri.startsWith("http://")
    }

    fun isLocalUri(uri: String?): Boolean {
        if (uri.isNullOrBlank()) return false
        return uri.startsWith("file:") ||
            uri.startsWith("content:") ||
            uri.startsWith("/")
    }

    /**
     * If [localOrRemoteUri] points at a local file, upload it and return the download URL.
     * If it is already remote, return it unchanged. Returns null when there is nothing usable.
     */
    suspend fun ensureRemoteUrl(
        context: Context,
        expenseId: String,
        localOrRemoteUri: String?
    ): String? {
        if (localOrRemoteUri.isNullOrBlank()) return null
        if (isRemoteUri(localOrRemoteUri)) return localOrRemoteUri
        if (!isLocalUri(localOrRemoteUri)) return null

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val localUri = Uri.parse(localOrRemoteUri)
        val file = when (localUri.scheme) {
            "file" -> File(localUri.path ?: return null)
            null -> File(localOrRemoteUri)
            else -> {
                // content: — copy to temp then upload
                val temp = File(context.cacheDir, "upload_$expenseId.tmp")
                context.contentResolver.openInputStream(localUri)?.use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                } ?: return null
                temp
            }
        }
        if (!file.exists()) return null

        val extension = file.extension.ifBlank { "jpg" }
        val ref = Firebase.storage.reference
            .child("users")
            .child(uid)
            .child("receipts")
            .child("$expenseId.$extension")

        ref.putFile(Uri.fromFile(file)).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * If [remoteOrLocalUri] is a remote URL, download into local cache and return file URI.
     * Local URIs are returned as-is when the file still exists.
     */
    suspend fun ensureLocalUri(
        context: Context,
        expenseId: String,
        remoteOrLocalUri: String?
    ): String? = withContext(Dispatchers.IO) {
        if (remoteOrLocalUri.isNullOrBlank()) return@withContext null
        if (isLocalUri(remoteOrLocalUri)) {
            val local = Uri.parse(remoteOrLocalUri)
            val path = local.path
            if (path != null && File(path).exists()) return@withContext remoteOrLocalUri
            // Stale local path from another device — ignore
            if (!isRemoteUri(remoteOrLocalUri)) return@withContext null
        }
        if (!isRemoteUri(remoteOrLocalUri)) return@withContext null

        val receiptsDir = File(context.filesDir, "receipts").apply { mkdirs() }
        val extension = remoteOrLocalUri.substringAfterLast('.', "jpg")
            .substringBefore('?')
            .ifBlank { "jpg" }
        val dest = File(receiptsDir, "synced_$expenseId.$extension")
        if (dest.exists() && dest.length() > 0L) {
            return@withContext Uri.fromFile(dest).toString()
        }

        runCatching {
            val ref = if (remoteOrLocalUri.startsWith("gs://")) {
                Firebase.storage.getReferenceFromUrl(remoteOrLocalUri)
            } else {
                Firebase.storage.getReferenceFromUrl(remoteOrLocalUri)
            }
            ref.getFile(dest).await()
            Uri.fromFile(dest).toString()
        }.getOrNull()
    }
}
