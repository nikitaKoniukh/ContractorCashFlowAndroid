package com.yetzira.ContractorCashFlowAndroid.data.preferences

enum class CurrencyOption(
    val code: String,
    val symbol: String,
    val displayName: String
) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    ILS("ILS", "₪", "Israeli Shekel"),
    RUB("RUB", "₽", "Russian Ruble"),
    JPY("JPY", "¥", "Japanese Yen"),
    CAD("CAD", "C$", "Canadian Dollar"),
    AUD("AUD", "A$", "Australian Dollar");

    companion object {
        fun fromCode(code: String?): CurrencyOption = values().find { it.code == code } ?: ILS

        fun fromSymbol(symbol: String?): CurrencyOption? =
            values().find { it.symbol == symbol }
    }
}

