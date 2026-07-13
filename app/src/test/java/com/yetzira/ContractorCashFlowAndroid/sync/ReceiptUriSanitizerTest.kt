package com.yetzira.ContractorCashFlowAndroid.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReceiptUriSanitizerTest {
    @Test
    fun `keeps remote https url`() {
        val url = "https://firebasestorage.googleapis.com/v0/b/bucket/o/receipt.jpg"
        assertEquals(url, ReceiptUriSanitizer.forCloudWrite(url))
    }

    @Test
    fun `strips local file uri`() {
        assertNull(ReceiptUriSanitizer.forCloudWrite("file:///data/user/0/app/files/receipts/a.jpg"))
    }

    @Test
    fun `strips content uri`() {
        assertNull(ReceiptUriSanitizer.forCloudWrite("content://media/external/images/1"))
    }

    @Test
    fun `strips blank`() {
        assertNull(ReceiptUriSanitizer.forCloudWrite(null))
        assertNull(ReceiptUriSanitizer.forCloudWrite(""))
    }
}
