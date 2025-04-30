package com.aj.flourish

object UserSettings {
    var currency: String = "ZAR"

    val currencySymbolMap = mapOf(
        "ZAR" to "R",
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£"
    )
    val currencySymbol: String
        get() = currencySymbolMap[currency] ?: currency
}