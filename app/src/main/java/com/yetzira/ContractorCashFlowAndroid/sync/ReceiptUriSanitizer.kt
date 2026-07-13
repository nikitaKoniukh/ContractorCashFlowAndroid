package com.yetzira.ContractorCashFlowAndroid.sync

/**
 * Receipt images: local file/content URIs must never be synced to Firestore.
 * Remote Storage / HTTPS URLs are kept so other devices can download them.
 */
object ReceiptUriSanitizer {
    fun forCloudWrite(uri: String?): String? {
        if (uri.isNullOrBlank()) return null
        return when {
            ReceiptStorageHelper.isRemoteUri(uri) -> uri
            else -> null
        }
    }

    /** Alias used by sync paths that strip local URIs before cloud write. */
    fun forCloudSync(uri: String?): String? {
        if (uri.isNullOrBlank()) return null
        return if (isLocalDeviceUri(uri)) null else uri
    }

    fun forLocalMerge(uri: String?): String? {
        if (uri.isNullOrBlank()) return null
        return if (isLocalDeviceUri(uri)) null else uri
    }

    fun isLocalDeviceUri(uri: String): Boolean {
        val value = uri.trim()
        return value.startsWith("file:", ignoreCase = true) ||
            value.startsWith("content:", ignoreCase = true) ||
            value.startsWith("/") // absolute filesystem path
    }
}
