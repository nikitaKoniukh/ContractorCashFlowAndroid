package com.yetzira.ContractorCashFlowAndroid.ui.components

import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrencyFormattingTest {

    @Test
    fun `formatAmountInput adds group separators by thousands`() {
        assertEquals("1", formatAmountInput("1"))
        assertEquals("1.000", formatAmountInput("1000"))
        assertEquals("1.000.000", formatAmountInput("1000000"))
    }

    @Test
    fun `formatAmountInput strips non digits before grouping`() {
        assertEquals("12.345", formatAmountInput("12a3,45"))
        assertEquals("", formatAmountInput("abc"))
    }

    @Test
    fun `parseAmountInput parses grouped values`() {
        assertEquals(1234567.0, parseAmountInput("1.234.567"))
        assertEquals(123.0, parseAmountInput("123"))
        assertNull(parseAmountInput(""))
    }

    @Test
    fun `formatAmountWithGrouping keeps sign`() {
        assertEquals("1.000", formatAmountWithGrouping(1000.0))
        assertEquals("-1.000", formatAmountWithGrouping(-1000.0))
    }

    @Test
    fun `formatCurrencyAmount includes symbol and grouped amount`() {
        assertEquals("1.000 $", formatCurrencyAmount(1000.0, CurrencyOption.USD))
        assertEquals("1.234 ₪", formatCurrencyAmount(1234.0, CurrencyOption.ILS))
    }
}

