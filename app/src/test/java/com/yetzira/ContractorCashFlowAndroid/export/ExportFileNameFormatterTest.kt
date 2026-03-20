package com.yetzira.ContractorCashFlowAndroid.export

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class ExportFileNameFormatterTest {

    @Test
    fun `suggested file name uses kablanpro prefix and ISO date`() {
        val defaultLocale = Locale.getDefault()
        val defaultTimeZone = TimeZone.getDefault()
        try {
            Locale.setDefault(Locale.US)
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val fileName = ExportFileNameFormatter.suggestedFileName(1_735_689_600_000L)
            assertEquals("KablanPro_Export_2025-01-01.json", fileName)
        } finally {
            Locale.setDefault(defaultLocale)
            TimeZone.setDefault(defaultTimeZone)
        }
    }
}

