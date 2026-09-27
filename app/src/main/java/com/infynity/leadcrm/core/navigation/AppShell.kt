package com.infynity.leadcrm.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infynity.leadcrm.LeadCrmApplication
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.feature.home.HomeScreen
import com.infynity.leadcrm.feature.home.HomeViewModel
import com.infynity.leadcrm.feature.home.HomeViewModelFactory

@Composable
fun AppShell(
    user: UserResponse,
    onLogout: () -> Unit
) {
    var currentDestination by remember {
        mutableStateOf(AppDestination.HOME)
    }

    val application = LocalContext.current.applicationContext as LeadCrmApplication

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            application.appContainer.dashboardRepository
        )
    )

    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    currentDestination = destination
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (currentDestination) {
                AppDestination.HOME -> {
                    HomeScreen(
                        user = user,
                        viewModel = homeViewModel,
                        onLogout = onLogout
                    )
                }

                AppDestination.LEADS -> {
                    PlaceholderScreen(title = "Leads")
                }

                AppDestination.TASKS -> {
                    PlaceholderScreen(title = "Tasks")
                }

                AppDestination.CALENDAR -> {
                    PlaceholderScreen(title = "Calendar")
                }

                AppDestination.MORE -> {
                    PlaceholderScreen(title = "More")
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String
) {
    Text(text = title)
}
