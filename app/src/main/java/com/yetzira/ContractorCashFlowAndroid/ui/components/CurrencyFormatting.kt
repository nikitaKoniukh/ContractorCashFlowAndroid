package com.yetzira.ContractorCashFlowAndroid.ui.components

import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

private val groupingSymbols = DecimalFormatSymbols(Locale.US).apply {
    groupingSeparator = '.'
    decimalSeparator = ','
}

private val integerFormatter = DecimalFormat("#,###", groupingSymbols)

fun formatAmountInput(raw: String): String {
    val digitsOnly = raw.filter { it.isDigit() }
    if (digitsOnly.isEmpty()) return ""

    return digitsOnly
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}

fun parseAmountInput(raw: String): Double? {
    val normalized = raw
        .replace(".", "")
        .trim()

    if (normalized.isBlank()) return null
    return normalized.toDoubleOrNull()
}

fun formatAmountWithGrouping(amount: Double): String {
    val sign = if (amount < 0) "-" else ""
    val value = abs(amount)
    return sign + integerFormatter.format(value)
}

fun formatCurrencyAmount(amount: Double, currency: CurrencyOption): String {
    return "${formatAmountWithGrouping(amount)} ${currency.symbol}"
}

