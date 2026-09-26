package com.infynity.leadcrm.core.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppBottomBar(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit
) {
    NavigationBar {
        AppDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination == destination,
                onClick = {
                    onDestinationSelected(destination)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(destination.label)
                }
            )
        }
    }
}
