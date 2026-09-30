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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.core.network.models.CreateLeadRequest

@Composable
fun LeadCreateScreen(
    viewModel: LeadCreateViewModel,
    currentUser: UserResponse,
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
    var atCustomerLocation by remember { mutableStateOf(false) }
    var customerLocation by remember { mutableStateOf(CustomerLocationData()) }
    var teamId by remember {
        mutableStateOf(if (currentUser.role == "team_leader") currentUser.teamId else null)
    }
    var assignedToId by remember { mutableStateOf<Int?>(null) }

    val canAssign = currentUser.role in setOf(
        "super_admin", "site_admin", "marketing_manager", "team_leader"
    )
    val availableTeams = if (currentUser.role == "team_leader") {
        val memberships = currentUser.teamMemberships.mapNotNull { it.teamId }.toSet()
        uiState.teams.filter { it.id in memberships || it.id == currentUser.teamId }
    } else {
        uiState.teams.filter { it.isActive }
    }

    LaunchedEffect(currentUser.id) { viewModel.loadFormOptions() }
    LaunchedEffect(teamId, canAssign) {
        if (canAssign) viewModel.loadAssignees(teamId)
    }

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

                    val referralChoices = buildList {
                        add("" to "-- None --")
                        uiState.referralOptions.filter { it.isActive }
                            .forEach { add(it.value to it.value) }
                        if (referredBy.isNotBlank() &&
                            uiState.referralOptions.none { it.value == referredBy }) {
                            add(referredBy to "$referredBy (inactive)")
                        }
                    }
                    LeadDropdownField(
                        "Referred By",
                        referredBy,
                        referralChoices,
                        { referredBy = it },
                        !uiState.isSaving
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
                            onCheckedChange = {
                                infynityCustomer = it
                                if (!it) infynityCustomerId = ""
                                validationError = null
                            },
                            enabled = !uiState.isSaving
                        )
                    }

                    if (infynityCustomer) LeadCreateTextField(
                        value = infynityCustomerId,
                        onValueChange = {
                            infynityCustomerId = it.take(10)
                            validationError = null
                        },
                        label = "Infynity Customer ID *",
                        enabled = !uiState.isSaving
                    )

                    LeadCreateTextField(
                        value = ksebConsumerNumber,
                        onValueChange = { ksebConsumerNumber = it },
                        label = "KSEB Consumer Number",
                        enabled = !uiState.isSaving
                    )

                    CustomerLocationFields(
                        enabled = !uiState.isSaving,
                        checked = atCustomerLocation,
                        location = customerLocation,
                        onCheckedChange = { atCustomerLocation = it },
                        onLocationChange = { customerLocation = it }
                    )
                }
            }

            if (canAssign) item {
                LeadCreateSectionCard(title = "Assignment") {
                    LeadDropdownField(
                        "Team",
                        teamId?.toString().orEmpty(),
                        listOf("" to "-- No Team --") +
                            availableTeams.map { it.id.toString() to it.name },
                        { teamId = it.toIntOrNull(); assignedToId = null },
                        !uiState.isSaving
                    )
                    LeadDropdownField(
                        "Assigned To",
                        assignedToId?.toString().orEmpty(),
                        listOf("" to "-- Unassigned --") +
                            uiState.assignees.map { it.id.toString() to it.fullName },
                        { assignedToId = it.toIntOrNull() },
                        !uiState.isSaving
                    )
                }
            }

            item {
                LeadCreateSectionCard(title = "Notes") {
                    val keyboardController = LocalSoftwareKeyboardController.current

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes") },
                        minLines = 4,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
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

                                infynityCustomer && cleanInfynityCustomerId.length > 10 ->
                                    "Infynity Customer ID must be no more than 10 characters."

                                atCustomerLocation &&
                                    (customerLocation.latitude.isBlank() ||
                                     customerLocation.longitude.isBlank() ||
                                     customerLocation.capturedAt.isBlank()) ->
                                    "Please capture the customer location using “Use My Location” before saving."

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
                                            .takeIf { infynityCustomer },
                                        ksebConsumerNumber = ksebConsumerNumber
                                            .trim()
                                            .ifBlank { null },
                                        company = company.trim().ifBlank { null },
                                        source = source.trim().ifBlank { null },
                                        placeArea = placeArea.trim().ifBlank { null },
                                        referredBy = referredBy.trim().ifBlank { null },
                                        notes = notes.trim().ifBlank { null },
                                        atCustomerLocation = atCustomerLocation,
                                        customerLatitude = customerLocation.latitude
                                            .toDoubleOrNull().takeIf { atCustomerLocation },
                                        customerLongitude = customerLocation.longitude
                                            .toDoubleOrNull().takeIf { atCustomerLocation },
                                        customerLocationAccuracy = customerLocation.accuracy
                                            .toDoubleOrNull().takeIf { atCustomerLocation },
                                        customerLocationCapturedAt = customerLocation.capturedAt
                                            .takeIf { atCustomerLocation && it.isNotBlank() },
                                        teamId = teamId,
                                        assignedToId = assignedToId,
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
