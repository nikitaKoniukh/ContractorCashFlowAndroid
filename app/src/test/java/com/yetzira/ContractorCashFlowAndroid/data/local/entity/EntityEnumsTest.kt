package com.yetzira.ContractorCashFlowAndroid.data.local.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityEnumsTest {

    @Test
    fun `expense category fromString resolves known enum names`() {
        assertEquals(ExpenseCategory.MATERIALS, ExpenseCategory.fromString("MATERIALS"))
        assertEquals(ExpenseCategory.LABOR, ExpenseCategory.fromString("LABOR"))
        assertEquals(ExpenseCategory.EQUIPMENT, ExpenseCategory.fromString("EQUIPMENT"))
        assertEquals(ExpenseCategory.MISC, ExpenseCategory.fromString("MISC"))
    }

    @Test
    fun `expense category fromString returns null for unknown`() {
        assertNull(ExpenseCategory.fromString("UNKNOWN"))
        assertNull(ExpenseCategory.fromString(null))
    }

    @Test
    fun `expense category visual metadata is present`() {
        ExpenseCategory.entries.forEach { category ->
            assertTrue(category.displayName.isNotBlank())
            assertNotNull(category.iconResId)
        }
    }

    @Test
    fun `labor type fromString resolves known enum names`() {
        assertEquals(LaborType.HOURLY, LaborType.fromString("HOURLY"))
        assertEquals(LaborType.DAILY, LaborType.fromString("DAILY"))
        assertEquals(LaborType.CONTRACT, LaborType.fromString("CONTRACT"))
        assertEquals(LaborType.SUBCONTRACTOR, LaborType.fromString("SUBCONTRACTOR"))
    }

    @Test
    fun `labor type rules are mapped correctly`() {
        assertTrue(LaborType.HOURLY.usesQuantity)
        assertTrue(LaborType.DAILY.usesQuantity)
        assertFalse(LaborType.CONTRACT.usesQuantity)
        assertFalse(LaborType.SUBCONTRACTOR.usesQuantity)

        assertEquals("/hr", LaborType.HOURLY.rateSuffix)
        assertEquals("/day", LaborType.DAILY.rateSuffix)
        assertEquals("", LaborType.CONTRACT.rateSuffix)
        assertEquals("", LaborType.SUBCONTRACTOR.rateSuffix)
    }
}

