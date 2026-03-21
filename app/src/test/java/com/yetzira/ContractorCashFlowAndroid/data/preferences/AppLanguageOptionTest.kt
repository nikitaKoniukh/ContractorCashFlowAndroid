package com.yetzira.ContractorCashFlowAndroid.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppLanguageOptionTest {

    @Test
    fun `fromCode returns expected languages`() {
        assertEquals(AppLanguageOption.ENGLISH, AppLanguageOption.fromCode("en"))
        assertEquals(AppLanguageOption.HEBREW, AppLanguageOption.fromCode("he"))
        assertEquals(AppLanguageOption.RUSSIAN, AppLanguageOption.fromCode("ru"))
    }

    @Test
    fun `fromCode falls back to hebrew for unknown or null`() {
        assertEquals(AppLanguageOption.HEBREW, AppLanguageOption.fromCode("xx"))
        assertEquals(AppLanguageOption.HEBREW, AppLanguageOption.fromCode(null))
    }

    @Test
    fun `fromDisplayName resolves existing entry`() {
        val result = AppLanguageOption.fromDisplayName(AppLanguageOption.HEBREW.displayName)
        assertNotNull(result)
        assertEquals(AppLanguageOption.HEBREW, result)
    }
}

