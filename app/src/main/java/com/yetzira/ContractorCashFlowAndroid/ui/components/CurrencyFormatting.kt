package com.yetzira.ContractorCashFlowAndroid.ui.components

import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

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

fun formatAmountFromDouble(amount: Double): String =
    formatAmountInput(abs(amount).roundToLong().toString())

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
    val sign = if (amount < 0) "-" else ""
    val absFormatted = formatAmountWithGrouping(abs(amount))
    return "${sign}${absFormatted} ${currency.symbol}"
}

fun Double.toFormattedCurrency(currency: CurrencyOption): String {
    return formatCurrencyAmount(this, currency)
}

