package com.infynity.leadcrm.feature.leads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.LeadAreaResponse

@Composable
fun PlaceAreaSelector(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    areas: List<LeadAreaResponse>,
    isSearching: Boolean,
    errorMessage: String?,
    enabled: Boolean
) {
    var showSuggestions by remember { mutableStateOf(false) }
    val enteredValue = value.trim()
    val hasExactMatch = areas.any {
        it.name.equals(enteredValue, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                showSuggestions = true
                onSearch(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && enabled) {
                        showSuggestions = true
                        onSearch(value)
                    }
                },
            label = { Text("Place / Area") },
            placeholder = { Text("Search or enter a new Area") },
            singleLine = true,
            enabled = enabled
        )

        if (showSuggestions) {
            Spacer(modifier = Modifier.height(4.dp))

            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp)
                )
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!isSearching && errorMessage == null) {
                areas.take(8).forEach { area ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                onValueChange(area.name)
                                showSuggestions = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Text(
                            text = area.name,
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 12.dp
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (enteredValue.isNotBlank() && !hasExactMatch) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Use \"$enteredValue\" as a new shared Area",
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
