package com.eti.energysaver.model

enum class TimePeriod(val label: String, val hours: Long) {
    LAST_12_HOURS("Last 12 hours", 12),
    LAST_24_HOURS("Last 24 hours", 24),
    LAST_WEEK("Last week", 7 * 24),
    LAST_MONTH("Last month", 30 * 24)
}
