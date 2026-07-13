package com.yetzira.ContractorCashFlowAndroid.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptUriSanitizerTest {
    @Test
    fun `keeps remote https url for cloud write`() {
        val url = "https://firebasestorage.googleapis.com/v0/b/bucket/o/receipt.jpg"
        assertEquals(url, ReceiptUriSanitizer.forCloudWrite(url))
    }

    @Test
    fun `strips local file uri for cloud write`() {
        assertNull(ReceiptUriSanitizer.forCloudWrite("file:///data/user/0/app/files/receipts/a.jpg"))
    }

    @Test
    fun `strips content uri for cloud write`() {
        assertNull(ReceiptUriSanitizer.forCloudWrite("content://media/external/images/1"))
    }

    @Test
    fun `strips blank for cloud write`() {
        assertNull(ReceiptUriSanitizer.forCloudWrite(null))
        assertNull(ReceiptUriSanitizer.forCloudWrite(""))
    }

    @Test
    fun `strips local device uris for cloud sync`() {
        assertNull(ReceiptUriSanitizer.forCloudSync("file:///data/user/0/app/files/receipts/a.jpg"))
        assertNull(ReceiptUriSanitizer.forCloudSync("content://media/external/images/1"))
        assertNull(ReceiptUriSanitizer.forCloudSync("/data/user/0/app/files/receipts/a.jpg"))
    }

    @Test
    fun `keeps remote https uris for sync and merge`() {
        val remote = "https://example.com/receipts/a.jpg"
        assertEquals(remote, ReceiptUriSanitizer.forCloudSync(remote))
        assertEquals(remote, ReceiptUriSanitizer.forLocalMerge(remote))
    }

    @Test
    fun `detects local device uris`() {
        assertTrue(ReceiptUriSanitizer.isLocalDeviceUri("file:///tmp/x"))
        assertTrue(ReceiptUriSanitizer.isLocalDeviceUri("content://x"))
        assertFalse(ReceiptUriSanitizer.isLocalDeviceUri("https://cdn.example/x"))
    }
}
