package com.infynity.leadcrm.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(
        route = "home",
        label = "Home",
        icon = Icons.Filled.Home
    ),
    LEADS(
        route = "leads",
        label = "Leads",
        icon = Icons.Filled.People
    ),
    TASKS(
        route = "tasks",
        label = "Tasks",
        icon = Icons.Filled.ListAlt
    ),
    CALENDAR(
        route = "calendar",
        label = "Calendar",
        icon = Icons.Filled.CalendarMonth
    ),
    ME(
        route = "me",
        label = "Me",
        icon = Icons.Filled.Person
    )
}
