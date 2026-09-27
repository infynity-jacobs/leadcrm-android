package com.infynity.leadcrm.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.core.network.models.TaskResponse
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    user: UserResponse,
    viewModel: HomeViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            HomeUiState.Loading -> LoadingDashboard()

            is HomeUiState.Error -> DashboardError(
                message = state.message,
                onRetry = viewModel::loadDashboard
            )

            is HomeUiState.Success -> DashboardContent(
                user = user,
                tasks = state.tasks,
                calendarEvents = state.calendarEvents,
                leads = state.leads,
                onRefresh = viewModel::loadDashboard,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun LoadingDashboard() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Loading dashboard...")
    }
}

@Composable
private fun DashboardError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unable to load dashboard",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun DashboardContent(
    user: UserResponse,
    tasks: List<TaskResponse>,
    calendarEvents: List<CalendarEventResponse>,
    leads: List<LeadResponse>,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DashboardHeader(user, onRefresh)
        }

        item {
            SummaryRow(
                taskCount = tasks.size,
                eventCount = calendarEvents.size,
                leadCount = leads.size
            )
        }

        item {
            SectionTitle("Today's Tasks", Icons.Default.TaskAlt)
        }

        if (tasks.isEmpty()) {
            item { EmptyCard("No open tasks due today.") }
        } else {
            items(tasks.take(5), key = { it.id }) { task ->
                TaskCard(task)
            }
        }

        item {
            SectionTitle("Upcoming Calendar", Icons.Default.CalendarMonth)
        }

        if (calendarEvents.isEmpty()) {
            item { EmptyCard("No upcoming calendar events.") }
        } else {
            items(calendarEvents.take(5), key = { it.id }) { event ->
                CalendarEventCard(event)
            }
        }

        item {
            SectionTitle("Recent Leads", Icons.Default.People)
        }

        if (leads.isEmpty()) {
            item { EmptyCard("No recent leads.") }
        } else {
            items(leads, key = { it.id }) { lead ->
                LeadCard(lead)
            }
        }

        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogout
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    user: UserResponse,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Good day, ${user.fullName}",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = user.role,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        IconButton(onClick = onRefresh) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh dashboard"
            )
        }
    }
}

@Composable
private fun SummaryRow(
    taskCount: Int,
    eventCount: Int,
    leadCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(Modifier.weight(1f), "Tasks", taskCount)
        SummaryCard(Modifier.weight(1f), "Events", eventCount)
        SummaryCard(Modifier.weight(1f), "Leads", leadCount)
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    label: String,
    value: Int
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null)

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun TaskCard(task: TaskResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            task.leadName?.takeIf { it.isNotBlank() }?.let {
                Text("Lead: $it")
            }

            Text(
                text = "Priority: ${task.priority}",
                style = MaterialTheme.typography.bodySmall
            )

            task.dueDate?.let {
                Text(
                    text = "Due: ${formatDateTime(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CalendarEventCard(event: CalendarEventResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${formatDateTime(event.startAt)} - ${formatTime(event.endAt)}",
                style = MaterialTheme.typography.bodyMedium
            )

            event.leadName?.takeIf { it.isNotBlank() }?.let {
                Text("Lead: $it", style = MaterialTheme.typography.bodySmall)
            }

            event.location?.takeIf { it.isNotBlank() }?.let {
                Text("Location: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LeadCard(lead: LeadResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = listOfNotNull(
                    lead.firstName,
                    lead.lastName?.takeIf { it.isNotBlank() }
                ).joinToString(" "),
                style = MaterialTheme.typography.titleMedium
            )

            lead.company?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }

            Text(
                text = lead.status,
                style = MaterialTheme.typography.bodySmall
            )

            lead.phone?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatDateTime(value: String): String {
    return try {
        OffsetDateTime.parse(value)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .format(
                DateTimeFormatter.ofPattern(
                    "dd MMM, HH:mm",
                    Locale.getDefault()
                )
            )
    } catch (_: Exception) {
        value
    }
}

private fun formatTime(value: String): String {
    return try {
        OffsetDateTime.parse(value)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .format(
                DateTimeFormatter.ofPattern(
                    "HH:mm",
                    Locale.getDefault()
                )
            )
    } catch (_: Exception) {
        value
    }
}
