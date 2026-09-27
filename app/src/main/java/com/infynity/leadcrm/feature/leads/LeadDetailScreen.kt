package com.infynity.leadcrm.feature.leads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.infynity.leadcrm.core.network.models.FollowUpResponse
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.LeadHistoryResponse

@Composable
fun LeadDetailScreen(
    viewModel: LeadDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Lead Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = viewModel::refresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh lead"
                )
            }
        }

        when {
            uiState.isLoading && uiState.lead == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.lead == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Unable to load lead",
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = viewModel::refresh) {
                        Text("Retry")
                    }
                }
            }

            uiState.lead != null -> {
                LeadDetailContent(lead = uiState.lead!!)
            }
        }
    }
}

@Composable
private fun LeadDetailContent(
    lead: LeadDetailResponse
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LeadSectionCard(title = "Contact") {
                DetailText(
                    label = "Name",
                    value = listOfNotNull(
                        lead.firstName,
                        lead.lastName
                    ).joinToString(" ")
                )
                DetailText("Phone", lead.phone)
                DetailText("Phone 2", lead.phone2)
                DetailText("Email", lead.email)
            }
        }

        item {
            LeadSectionCard(title = "Lead") {
                DetailText("Status", lead.status.replace('_', ' '))
                DetailText("Company", lead.company)
                DetailText("Source", lead.source)
                DetailText("Place / Area", lead.placeArea)
                DetailText("Referred By", lead.referredBy)
                DetailText("Assigned To", lead.assignedToName)
                DetailText("Team", lead.teamName)
            }
        }

        if (lead.infynityCustomer || lead.infynityCustomerId != null ||
            lead.ksebConsumerNumber != null
        ) {
            item {
                LeadSectionCard(title = "Customer Information") {
                    DetailText(
                        "Infynity Customer",
                        if (lead.infynityCustomer) "Yes" else "No"
                    )
                    DetailText(
                        "Customer ID",
                        lead.infynityCustomerId
                    )
                    DetailText(
                        "KSEB Consumer Number",
                        lead.ksebConsumerNumber
                    )
                }
            }
        }

        if (lead.productNames.isNotEmpty()) {
            item {
                LeadSectionCard(title = "Products") {
                    lead.productNames.forEach { product ->
                        Text(
                            text = "• $product",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (!lead.notes.isNullOrBlank()) {
            item {
                LeadSectionCard(title = "Notes") {
                    Text(
                        text = lead.notes.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            LeadSectionCard(title = "Follow-ups") {
                if (lead.followUps.isEmpty()) {
                    Text(
                        text = "No follow-ups",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    lead.followUps.forEach { followUp ->
                        FollowUpItem(followUp)
                    }
                }
            }
        }

        item {
            LeadSectionCard(title = "History") {
                if (lead.history.isEmpty()) {
                    Text(
                        text = "No history",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    lead.history.forEach { history ->
                        HistoryItem(history)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LeadSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                content()
            }
        )
    }
}

@Composable
private fun DetailText(
    label: String,
    value: String?
) {
    if (!value.isNullOrBlank()) {
        Column(
            modifier = Modifier.padding(vertical = 3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FollowUpItem(
    followUp: FollowUpResponse
) {
    Column(
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = followUp.followUpType.replace('_', ' '),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        DetailText("Scheduled", formatLeadDateTime(followUp.scheduledAt))
        DetailText("Completed", formatLeadDateTime(followUp.completedAt))
        DetailText("Outcome", followUp.outcome)
        DetailText("Notes", followUp.notes)
        DetailText("Staff", followUp.staffName)
    }
}

@Composable
private fun HistoryItem(
    history: LeadHistoryResponse
) {
    Column(
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = buildString {
                if (!history.oldStatus.isNullOrBlank()) {
                    append(history.oldStatus.replace('_', ' '))
                    append(" → ")
                }
                append(history.newStatus.replace('_', ' '))
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        DetailText("Changed At", formatLeadDateTime(history.changedAt))
        DetailText("Changed By", history.changedByName)
        DetailText("Note", history.note)
    }
}


private fun formatLeadDateTime(value: String?): String? {
    if (value.isNullOrBlank()) return value

    val outputFormatter = DateTimeFormatter.ofPattern(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    )

    return try {
        OffsetDateTime.parse(value)
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .format(outputFormatter)
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(value)
                .format(outputFormatter)
        } catch (_: Exception) {
            value
        }
    }
}
