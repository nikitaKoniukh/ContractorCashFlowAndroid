package com.yetzira.ContractorCashFlowAndroid.sync

/**
 * Strips device-local receipt URIs before writing to Firestore so other devices
 * never receive unusable file:/content: paths. Remote Storage URLs are kept.
 */
object ReceiptUriSanitizer {
    fun forCloudWrite(uri: String?): String? {
        if (uri.isNullOrBlank()) return null
        return when {
            ReceiptStorageHelper.isRemoteUri(uri) -> uri
            else -> null
        }
    }
}
