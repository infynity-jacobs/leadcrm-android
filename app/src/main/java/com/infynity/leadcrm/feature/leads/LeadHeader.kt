package com.infynity.leadcrm.feature.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.LeadDetailResponse

@Composable
fun LeadHeader(
    lead: LeadDetailResponse
) {
    val name = listOfNotNull(
        lead.firstName,
        lead.lastName
    ).joinToString(" ")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = createInitials(
                    lead.firstName,
                    lead.lastName
                ),
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer
                    )
                    .padding(20.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            lead.company
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            val statusStyle = leadStatusStyle(
                lead.status
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = statusStyle.background
            ) {
                Text(
                    text = lead.status
                        .replace("_", " ")
                        .replaceFirstChar {
                            it.uppercase()
                        },
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                    color = statusStyle.foreground,
                    fontWeight = FontWeight.SemiBold
                )
            }

            lead.phone
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = "☎  $it",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
        }
    }
}

private fun createInitials(
    firstName: String,
    lastName: String?
): String {

    val first =
        firstName.firstOrNull()?.uppercase()
            ?: ""

    val second =
        lastName
            ?.firstOrNull()
            ?.uppercase()
            ?: ""

    return first + second
}
