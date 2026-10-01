package com.infynity.leadcrm.feature.tasks.components

import androidx.compose.ui.graphics.Color

fun taskPriorityContainerColor(priority: String?): Color {
    return when (
        priority
            ?.lowercase()
            ?.trim()
    ) {
        "urgent" -> Color(0xFFFEE2E2)

        "high" -> Color(0xFFFFEDD5)

        "medium" -> Color(0xFFDBEAFE)

        "low" -> Color(0xFFE2E8F0)

        else -> Color(0xFFE2E8F0)
    }
}

fun taskPriorityContentColor(priority: String?): Color {
    return when (
        priority
            ?.lowercase()
            ?.trim()
    ) {
        "urgent" -> Color(0xFF991B1B)

        "high" -> Color(0xFF9A3412)

        "medium" -> Color(0xFF1E40AF)

        "low" -> Color(0xFF334155)

        else -> Color(0xFF334155)
    }
}
