package com.yetzira.ContractorCashFlowAndroid.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyOptionTest {

    @Test
    fun `fromCode resolves known currencies`() {
        assertEquals(CurrencyOption.USD, CurrencyOption.fromCode("USD"))
        assertEquals(CurrencyOption.ILS, CurrencyOption.fromCode("ILS"))
        assertEquals(CurrencyOption.AUD, CurrencyOption.fromCode("AUD"))
    }

    @Test
    fun `fromCode falls back to ILS when unknown or null`() {
        assertEquals(CurrencyOption.ILS, CurrencyOption.fromCode("ZZZ"))
        assertEquals(CurrencyOption.ILS, CurrencyOption.fromCode(null))
    }

    @Test
    fun `fromSymbol resolves known symbols`() {
        assertEquals(CurrencyOption.GBP, CurrencyOption.fromSymbol("£"))
        assertEquals(CurrencyOption.CAD, CurrencyOption.fromSymbol("C$"))
    }

    @Test
    fun `fromSymbol returns null for unknown`() {
        assertNull(CurrencyOption.fromSymbol("?"))
    }
}

