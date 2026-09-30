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
import com.infynity.leadcrm.core.auth.LeadPermissions
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.feature.home.HomeScreen
import com.infynity.leadcrm.feature.leads.LeadCreateScreen
import com.infynity.leadcrm.feature.leads.LeadDetailScreen
import com.infynity.leadcrm.feature.leads.LeadCreateViewModel
import com.infynity.leadcrm.feature.leads.LeadDetailViewModel
import com.infynity.leadcrm.feature.leads.LeadCreateViewModelFactory
import com.infynity.leadcrm.feature.leads.LeadDetailViewModelFactory
import com.infynity.leadcrm.feature.leads.EditLeadScreen
import com.infynity.leadcrm.feature.leads.LeadEditViewModel
import com.infynity.leadcrm.feature.leads.LeadEditViewModelFactory
import com.infynity.leadcrm.feature.leads.LeadsScreen
import com.infynity.leadcrm.feature.leads.LeadsViewModel
import com.infynity.leadcrm.feature.leads.LeadsViewModelFactory
import com.infynity.leadcrm.feature.tasks.TaskDetailScreen
import com.infynity.leadcrm.feature.tasks.TaskDetailViewModel
import com.infynity.leadcrm.feature.tasks.TaskDetailViewModelFactory
import com.infynity.leadcrm.feature.tasks.TaskEditScreen
import com.infynity.leadcrm.feature.tasks.TaskEditViewModel
import com.infynity.leadcrm.feature.tasks.TaskEditViewModelFactory
import com.infynity.leadcrm.feature.tasks.TasksScreen
import com.infynity.leadcrm.feature.tasks.TasksViewModel
import com.infynity.leadcrm.feature.tasks.TasksViewModelFactory
import com.infynity.leadcrm.feature.home.HomeViewModel
import com.infynity.leadcrm.feature.home.HomeViewModelFactory
import com.infynity.leadcrm.feature.calendar.CalendarEventCreateScreen
import com.infynity.leadcrm.feature.calendar.CalendarEventCreateViewModel
import com.infynity.leadcrm.feature.calendar.CalendarEventCreateViewModelFactory
import com.infynity.leadcrm.feature.calendar.CalendarEventDetailScreen
import com.infynity.leadcrm.feature.calendar.CalendarEventDetailViewModel
import com.infynity.leadcrm.feature.calendar.CalendarEventDetailViewModelFactory
import com.infynity.leadcrm.feature.calendar.CalendarEventEditScreen
import com.infynity.leadcrm.feature.calendar.CalendarEventEditViewModel
import com.infynity.leadcrm.feature.calendar.CalendarEventEditViewModelFactory
import com.infynity.leadcrm.feature.calendar.CalendarScreen
import com.infynity.leadcrm.feature.calendar.CalendarViewModel
import com.infynity.leadcrm.feature.calendar.CalendarViewModelFactory

@Composable
fun AppShell(
    user: UserResponse,
    onLogout: () -> Unit
) {
    var currentDestination by remember {
        mutableStateOf(AppDestination.HOME)
    }

    var selectedLeadId by remember {
        mutableStateOf<Int?>(null)
    }

    var editingLeadId by remember {
        mutableStateOf<Int?>(null)
    }

    var creatingLead by remember {
        mutableStateOf(false)
    }

    var leadCreateSession by remember {
        mutableStateOf(0)
    }

    var selectedTaskId by remember {
        mutableStateOf<Int?>(null)
    }

    var editingTaskId by remember {
        mutableStateOf<Int?>(null)
    }

    var selectedCalendarEventId by remember {
        mutableStateOf<Int?>(null)
    }

    var editingCalendarEventId by remember {
        mutableStateOf<Int?>(null)
    }

    var creatingCalendarEvent by remember {
        mutableStateOf(false)
    }

    var calendarEventCreateSession by remember {
        mutableStateOf(0)
    }

    val application = LocalContext.current.applicationContext as LeadCrmApplication

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            application.appContainer.dashboardRepository
        )
    )

    val leadsViewModel: LeadsViewModel = viewModel(
        factory = LeadsViewModelFactory(
            application.appContainer.leadRepository
        )
    )

    val tasksViewModel: TasksViewModel = viewModel(
        factory = TasksViewModelFactory(
            application.appContainer.taskRepository
        )
    )

    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(
            application.appContainer.calendarRepository
        )
    )

    val taskDetailViewModel: TaskDetailViewModel? = selectedTaskId?.let { taskId ->
        viewModel(
            key = "task-detail-$taskId",
            factory = TaskDetailViewModelFactory(
                application.appContainer.taskRepository,
                taskId
            )
        )
    }

    val taskEditViewModel: TaskEditViewModel? = editingTaskId?.let { taskId ->
        viewModel(
            key = "task-edit-$taskId",
            factory = TaskEditViewModelFactory(
                application.appContainer.taskRepository,
                taskId
            )
        )
    }

    val calendarEventDetailViewModel: CalendarEventDetailViewModel? =
        selectedCalendarEventId?.let { eventId ->
            viewModel(
                key = "calendar-event-detail-$eventId",
                factory = CalendarEventDetailViewModelFactory(
                    application.appContainer.calendarRepository,
                    eventId
                )
            )
        }

    val calendarEventCreateViewModel: CalendarEventCreateViewModel? =
        if (creatingCalendarEvent) {
            viewModel(
                key = "calendar-event-create-$calendarEventCreateSession",
                factory = CalendarEventCreateViewModelFactory(
                    application.appContainer.calendarRepository,
                    application.appContainer.leadRepository,
                    application.appContainer.taskRepository
                )
            )
        } else {
            null
        }

    val calendarEventEditViewModel: CalendarEventEditViewModel? =
        editingCalendarEventId?.let { eventId ->
            viewModel(
                key = "calendar-event-edit-$eventId",
                factory = CalendarEventEditViewModelFactory(
                    application.appContainer.calendarRepository,
                    eventId
                )
            )
        }

    val leadCreateViewModel: LeadCreateViewModel? =
        if (creatingLead) {
            viewModel(
                key = "lead-create-$leadCreateSession",
                factory = LeadCreateViewModelFactory(
                    application.appContainer.leadRepository
                )
            )
        } else {
            null
        }

    val leadDetailViewModel: LeadDetailViewModel? = selectedLeadId?.let { leadId ->
        viewModel(
            key = "lead-detail-$leadId",
            factory = LeadDetailViewModelFactory(
                application.appContainer.leadRepository,
                leadId,
                LocalContext.current.applicationContext
            )
        )
    }

    val leadEditViewModel: LeadEditViewModel? = editingLeadId?.let { leadId ->
        viewModel(
            key = "lead-edit-$leadId",
            factory = LeadEditViewModelFactory(
                application.appContainer.leadRepository,
                leadId
            )
        )
    }

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
                    if (creatingLead && leadCreateViewModel != null) {
                        LeadCreateScreen(
                            viewModel = leadCreateViewModel,
                            currentUser = user,
                            onBack = {
                                creatingLead = false
                                leadsViewModel.refresh()
                            }
                        )
                    } else if (editingLeadId != null && leadEditViewModel != null) {
                        EditLeadScreen(
                            viewModel = leadEditViewModel,
                            currentUser = user,
                            onBack = {
                                editingLeadId = null
                                leadDetailViewModel?.refresh()
                            }
                        )
                    } else if (selectedLeadId != null && leadDetailViewModel != null) {
                        LeadDetailScreen(
                            viewModel = leadDetailViewModel,
                            canDeleteLead = LeadPermissions.canDeleteLead(user.role),
                            onBack = {
                                selectedLeadId = null
                            },
                            onEdit = {
                                editingLeadId = selectedLeadId
                            }
                        )
                    } else {
                        LeadsScreen(
                            viewModel = leadsViewModel,
                            canCreateLead = LeadPermissions.canCreateLead(user.role),
                            onLeadSelected = { leadId ->
                                selectedLeadId = leadId
                            },
                            onNewLead = {
                                leadCreateSession += 1
                                creatingLead = true
                            }
                        )
                    }
                }

                AppDestination.TASKS -> {
                    if (editingTaskId != null && taskEditViewModel != null) {
                        TaskEditScreen(
                            viewModel = taskEditViewModel,
                            onBack = {
                                editingTaskId = null
                                taskDetailViewModel?.refresh()
                            }
                        )
                    } else if (selectedTaskId != null && taskDetailViewModel != null) {
                        TaskDetailScreen(
                            viewModel = taskDetailViewModel,
                            onBack = {
                                selectedTaskId = null
                            },
                            onEdit = {
                                editingTaskId = selectedTaskId
                            }
                        )
                    } else {
                        TasksScreen(
                            viewModel = tasksViewModel,
                            onTaskSelected = { taskId ->
                                selectedTaskId = taskId
                            }
                        )
                    }
                }

                AppDestination.CALENDAR -> {
                    if (creatingCalendarEvent &&
                        calendarEventCreateViewModel != null
                    ) {
                        CalendarEventCreateScreen(
                            viewModel = calendarEventCreateViewModel,
                            onBack = {
                                creatingCalendarEvent = false
                                calendarViewModel.refresh()
                            }
                        )
                    } else if (editingCalendarEventId != null &&
                        calendarEventEditViewModel != null
                    ) {
                        CalendarEventEditScreen(
                            viewModel = calendarEventEditViewModel,
                            onBack = {
                                editingCalendarEventId = null
                                calendarEventDetailViewModel?.refresh()
                            }
                        )
                    } else if (selectedCalendarEventId != null &&
                        calendarEventDetailViewModel != null
                    ) {
                        CalendarEventDetailScreen(
                            viewModel = calendarEventDetailViewModel,
                            onBack = {
                                selectedCalendarEventId = null
                            },
                            onEdit = {
                                editingCalendarEventId = selectedCalendarEventId
                            }
                        )
                    } else {
                        CalendarScreen(
                            viewModel = calendarViewModel,
                            onEventSelected = { eventId ->
                                selectedCalendarEventId = eventId
                            },
                            onNewEvent = {
                                calendarEventCreateSession += 1
                                creatingCalendarEvent = true
                            }
                        )
                    }
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
