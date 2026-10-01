package com.infynity.leadcrm.feature.leads.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.LeadResponse
import com.infynity.leadcrm.feature.leads.LeadAvatar
import com.infynity.leadcrm.feature.leads.leadStatusStyle

@Composable
fun LeadCard(
    lead: LeadResponse,
    onClick: () -> Unit
) {
    val name = listOfNotNull(
        lead.firstName,
        lead.lastName?.takeIf { it.isNotBlank() }
    ).joinToString(" ")

    val statusStyle = leadStatusStyle(
        lead.status
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            LeadAvatar(
                name = name
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                lead.company
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                Text(
                    text = lead.status
                        .replace("_", " ")
                        .replaceFirstChar {
                            it.uppercase()
                        },
                    modifier = Modifier
                        .padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = statusStyle.foreground
                )

                lead.phone
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        Text(
                            text = "☎ $it",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
            }
        }
    }
}
