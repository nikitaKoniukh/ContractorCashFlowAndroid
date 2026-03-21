package com.yetzira.ContractorCashFlowAndroid.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeOptionTest {

    @Test
    fun `fromCode resolves known theme options`() {
        assertEquals(ThemeModeOption.SYSTEM, ThemeModeOption.fromCode("system"))
        assertEquals(ThemeModeOption.LIGHT, ThemeModeOption.fromCode("light"))
        assertEquals(ThemeModeOption.DARK, ThemeModeOption.fromCode("dark"))
    }

    @Test
    fun `fromCode falls back to system when unknown or null`() {
        assertEquals(ThemeModeOption.SYSTEM, ThemeModeOption.fromCode("unknown"))
        assertEquals(ThemeModeOption.SYSTEM, ThemeModeOption.fromCode(null))
    }
}

