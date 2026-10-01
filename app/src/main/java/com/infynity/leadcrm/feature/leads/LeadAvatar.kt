package com.infynity.leadcrm.feature.leads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private fun leadInitials(name: String): String {
    val parts = name
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    return when {
        parts.size >= 2 ->
            "${parts.first().first()}${parts.last().first()}".uppercase()

        parts.size == 1 ->
            parts.first().take(2).uppercase()

        else -> "?"
    }
}

@Composable
fun LeadAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = leadInitials(name),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
