package com.yetzira.ContractorCashFlowAndroid.data.local.entity

enum class LaborType(
    val displayName: String,
    val usesQuantity: Boolean,
    val rateSuffix: String
) {
    HOURLY(
        displayName = "שכר לשעה",
        usesQuantity = true,
        rateSuffix = "/hr"
    ),
    DAILY(
        displayName = "שכר יומי",
        usesQuantity = true,
        rateSuffix = "/day"
    ),
    SUBCONTRACTOR(
        displayName = "קבלן משנה",
        usesQuantity = false,
        rateSuffix = ""
    );

    companion object {
        fun fromString(value: String?): LaborType? =
            when (value) {
                "CONTRACT" -> SUBCONTRACTOR
                else -> entries.find { it.name == value }
            }
    }
}

