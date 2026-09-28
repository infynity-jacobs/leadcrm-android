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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.models.CreateLeadRequest

@Composable
fun LeadCreateScreen(
    viewModel: LeadCreateViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var phone2 by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var placeArea by remember { mutableStateOf("") }
    var referredBy by remember { mutableStateOf("") }
    var infynityCustomer by remember { mutableStateOf(false) }
    var infynityCustomerId by remember { mutableStateOf("") }
    var ksebConsumerNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.saveSuccessful) {
        if (uiState.saveSuccessful) {
            viewModel.clearSaveSuccess()
            onBack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Lead",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LeadCreateSectionCard(title = "Contact") {
                    LeadCreateTextField(
                        value = firstName,
                        onValueChange = {
                            firstName = it
                            validationError = null
                        },
                        label = "First Name",
                        enabled = !uiState.isSaving,
                        required = true
                    )

                    LeadCreateTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = "Last Name",
                        enabled = !uiState.isSaving
                    )

                    LeadCreateTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone",
                        enabled = !uiState.isSaving
                    )

                    LeadCreateTextField(
                        value = phone2,
                        onValueChange = { phone2 = it },
                        label = "Phone 2",
                        enabled = !uiState.isSaving
                    )

                    LeadCreateTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        enabled = !uiState.isSaving
                    )
                }
            }

            item {
                LeadCreateSectionCard(title = "Lead") {
                    LeadCreateTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = "Company",
                        enabled = !uiState.isSaving
                    )

                    LeadCreateTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = "Source",
                        enabled = !uiState.isSaving
                    )

                    PlaceAreaSelector(
                        value = placeArea,
                        onValueChange = { placeArea = it },
                        onSearch = viewModel::searchAreas,
                        areas = uiState.areaSearchResults,
                        isSearching = uiState.isSearchingAreas,
                        errorMessage = uiState.areaSearchError,
                        enabled = !uiState.isSaving
                    )

                    LeadCreateTextField(
                        value = referredBy,
                        onValueChange = { referredBy = it },
                        label = "Referred By",
                        enabled = !uiState.isSaving
                    )
                }
            }

            item {
                LeadCreateSectionCard(title = "Customer Information") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Infynity Customer",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = infynityCustomer,
                            onCheckedChange = { infynityCustomer = it },
                            enabled = !uiState.isSaving
                        )
                    }

                    LeadCreateTextField(
                        value = infynityCustomerId,
                        onValueChange = { infynityCustomerId = it },
                        label = "Customer ID",
                        enabled = !uiState.isSaving
                    )

                    LeadCreateTextField(
                        value = ksebConsumerNumber,
                        onValueChange = { ksebConsumerNumber = it },
                        label = "KSEB Consumer Number",
                        enabled = !uiState.isSaving
                    )
                }
            }

            item {
                LeadCreateSectionCard(title = "Notes") {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes") },
                        minLines = 4,
                        enabled = !uiState.isSaving
                    )
                }
            }

            item {
                validationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val cleanFirstName = firstName.trim()

                            val cleanInfynityCustomerId = infynityCustomerId.trim()

                            validationError = when {
                                cleanFirstName.isBlank() ->
                                    "First name is required."

                                infynityCustomer && cleanInfynityCustomerId.isBlank() ->
                                    "Infynity Customer ID is required."

                                else ->
                                    null
                            }

                            if (validationError == null) {
                                viewModel.save(
                                    CreateLeadRequest(
                                        firstName = cleanFirstName,
                                        lastName = lastName.trim().ifBlank { null },
                                        email = email.trim().ifBlank { null },
                                        phone = phone.trim().ifBlank { null },
                                        phone2 = phone2.trim().ifBlank { null },
                                        infynityCustomer = infynityCustomer,
                                        infynityCustomerId = cleanInfynityCustomerId
                                            .ifBlank { null },
                                        ksebConsumerNumber = ksebConsumerNumber
                                            .trim()
                                            .ifBlank { null },
                                        company = company.trim().ifBlank { null },
                                        source = source.trim().ifBlank { null },
                                        placeArea = placeArea.trim().ifBlank { null },
                                        referredBy = referredBy.trim().ifBlank { null },
                                        notes = notes.trim().ifBlank { null },
                                        status = "new"
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp)
                            )
                        } else {
                            Text("Create Lead")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LeadCreateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    required: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        label = {
            Text(if (required) "$label *" else label)
        },
        singleLine = true,
        enabled = enabled
    )
}

@Composable
private fun LeadCreateSectionCard(
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
