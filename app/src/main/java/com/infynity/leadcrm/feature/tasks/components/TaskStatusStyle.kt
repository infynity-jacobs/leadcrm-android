package com.infynity.leadcrm.feature.tasks.components

import androidx.compose.ui.graphics.Color

fun taskStatusContainerColor(status: String?): Color {
    return when (
        status
            ?.lowercase()
            ?.replace("_", " ")
    ) {
        "completed",
        "done" -> Color(0xFFD1FAE5)

        "cancelled",
        "canceled" -> Color(0xFFFEE2E2)

        "in progress",
        "ongoing" -> Color(0xFFDBEAFE)

        "pending",
        "scheduled" -> Color(0xFFFEF3C7)

        else -> Color(0xFFE2E8F0)
    }
}

fun taskStatusContentColor(status: String?): Color {
    return when (
        status
            ?.lowercase()
            ?.replace("_", " ")
    ) {
        "completed",
        "done" -> Color(0xFF065F46)

        "cancelled",
        "canceled" -> Color(0xFF991B1B)

        "in progress",
        "ongoing" -> Color(0xFF1E40AF)

        "pending",
        "scheduled" -> Color(0xFF92400E)

        else -> Color(0xFF334155)
    }
}
