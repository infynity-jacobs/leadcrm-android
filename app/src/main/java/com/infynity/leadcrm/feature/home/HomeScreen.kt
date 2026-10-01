package com.infynity.leadcrm.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.core.network.models.CalendarEventResponse
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.feature.calendar.components.CalendarEventCard
import com.infynity.leadcrm.feature.leads.components.LeadCard
import com.infynity.leadcrm.feature.tasks.components.TaskCard
import com.infynity.leadcrm.core.network.models.TaskResponse
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun HomeScreen(
    user: UserResponse,
    viewModel: HomeViewModel,
    onLeadSelected: (Int) -> Unit,
    onTaskSelected: (Int) -> Unit,
    onCalendarEventSelected: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
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
                onLeadSelected = onLeadSelected,
                onTaskSelected = onTaskSelected,
                onCalendarEventSelected = onCalendarEventSelected
            )
        }
    }
}

@Composable
private fun LoadingDashboard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Loading dashboard...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Unable to load dashboard",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null
            )

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
    onLeadSelected: (Int) -> Unit,
    onTaskSelected: (Int) -> Unit,
    onCalendarEventSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 12.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DashboardHeader(
                user = user,
                onRefresh = onRefresh
            )
        }

        item {
            SummaryRow(
                taskCount = tasks.size,
                eventCount = calendarEvents.size,
                leadCount = leads.size
            )
        }

        item {
            SectionTitle(
                title = "Today's Tasks",
                icon = Icons.Default.TaskAlt
            )
        }

        if (tasks.isEmpty()) {
            item {
                EmptyCard("No open tasks due today.")
            }
        } else {
            items(
                tasks.take(5),
                key = { it.id }
            ) { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskSelected(task.id) }
                )
            }
        }

        item {
            SectionTitle(
                title = "Upcoming Calendar",
                icon = Icons.Default.CalendarMonth
            )
        }

        if (calendarEvents.isEmpty()) {
            item {
                EmptyCard("No upcoming calendar events.")
            }
        } else {
            items(
                calendarEvents.take(5),
                key = { it.id }
            ) { event ->
                CalendarEventCard(
                    event = event,
                    onClick = { onCalendarEventSelected(event.id) }
                )
            }
        }

        item {
            SectionTitle(
                title = "Recent Leads",
                icon = Icons.Default.People
            )
        }

        if (leads.isEmpty()) {
            item {
                EmptyCard("No recent leads.")
            }
        } else {
            items(
                leads,
                key = { it.id }
            ) { lead ->
                LeadCard(
                    lead = lead,
                    onClick = { onLeadSelected(lead.id) }
                )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 2.dp,
                end = 2.dp,
                bottom = 4.dp
            ),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Good day, ${user.fullName}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = user.role,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onRefresh
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh dashboard",
                tint = MaterialTheme.colorScheme.onSurface
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
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.TaskAlt,
            label = "Tasks",
            value = taskCount
        )

        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CalendarMonth,
            label = "Events",
            value = eventCount
        )

        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.People,
            label = "Leads",
            value = leadCount
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: Int
) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 13.dp
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.padding(
            top = 4.dp,
            bottom = 2.dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(9.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 18.dp
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
