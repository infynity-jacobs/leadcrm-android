package com.infynity.leadcrm.feature.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableSelector(
    label: String,
    selectedText: String?,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelected: (T) -> Unit,
    enabled: Boolean = true
) {
    var showSheet by remember {
        mutableStateOf(false)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    val filteredItems = items.filter {
        itemLabel(it).contains(
            searchText,
            ignoreCase = true
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedText ?: "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(label)
                },
                readOnly = true,
                enabled = enabled
            )

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = enabled) {
                        showSheet = true
                    }
            )
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    searchText = ""
                },
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Search $label")
                        },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                        ),
                        singleLine = true
                    )

                    LazyColumn {
                        items(filteredItems) { item ->
                            Text(
                                text = itemLabel(item),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelected(item)
                                        showSheet = false
                                        searchText = ""
                                    }
                                    .padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
