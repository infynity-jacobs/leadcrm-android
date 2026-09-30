package com.infynity.leadcrm.feature.leads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
import com.infynity.leadcrm.core.network.UserResponse
import com.infynity.leadcrm.core.network.models.LeadAreaResponse
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.TeamResponse
import com.infynity.leadcrm.core.network.models.UpdateLeadRequest

@Composable
fun EditLeadScreen(
    viewModel: LeadEditViewModel,
    currentUser: UserResponse,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var initializedLeadId by remember { mutableStateOf<Int?>(null) }
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
    var atCustomerLocation by remember { mutableStateOf(false) }
    var customerLocation by remember { mutableStateOf(CustomerLocationData()) }
    var teamId by remember { mutableStateOf<Int?>(null) }
    var assignedToId by remember { mutableStateOf<Int?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(uiState.lead?.id) {
        val lead = uiState.lead ?: return@LaunchedEffect

        if (initializedLeadId != lead.id) {
            firstName = lead.firstName
            lastName = lead.lastName.orEmpty()
            phone = lead.phone.orEmpty()
            phone2 = lead.phone2.orEmpty()
            email = lead.email.orEmpty()
            company = lead.company.orEmpty()
            source = lead.source.orEmpty()
            placeArea = lead.placeArea.orEmpty()
            referredBy = lead.referredBy.orEmpty()
            infynityCustomer = lead.infynityCustomer
            infynityCustomerId = lead.infynityCustomerId.orEmpty()
            ksebConsumerNumber = lead.ksebConsumerNumber.orEmpty()
            notes = lead.notes.orEmpty()
            atCustomerLocation = lead.atCustomerLocation
            customerLocation = CustomerLocationData(
                latitude = lead.customerLatitude?.toString().orEmpty(),
                longitude = lead.customerLongitude?.toString().orEmpty(),
                accuracy = lead.customerLocationAccuracy?.toString().orEmpty(),
                capturedAt = lead.customerLocationCapturedAt.orEmpty()
            )
            teamId = lead.teamId
            assignedToId = lead.assignedToId
            initializedLeadId = lead.id
        }
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
                text = "Edit Lead",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
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

                    Button(onClick = viewModel::loadLead) {
                        Text("Retry")
                    }
                }
            }

            uiState.lead != null -> {
                EditLeadForm(
                    firstName = firstName,
                    onFirstNameChange = { firstName = it },
                    lastName = lastName,
                    onLastNameChange = { lastName = it },
                    phone = phone,
                    onPhoneChange = { phone = it },
                    phone2 = phone2,
                    onPhone2Change = { phone2 = it },
                    email = email,
                    onEmailChange = { email = it },
                    company = company,
                    onCompanyChange = { company = it },
                    source = source,
                    onSourceChange = { source = it },
                    placeArea = placeArea,
                    onPlaceAreaChange = { placeArea = it },
                    areaSearchResults = uiState.areaSearchResults,
                    isSearchingAreas = uiState.isSearchingAreas,
                    areaSearchError = uiState.areaSearchError,
                    onSearchAreas = viewModel::searchAreas,
                    referredBy = referredBy,
                    referralOptions = uiState.referralOptions.map { it.value },
                    onReferredByChange = { referredBy = it },
                    infynityCustomer = infynityCustomer,
                    onInfynityCustomerChange = {
                        infynityCustomer = it
                        if (!it) infynityCustomerId = ""
                        validationError = null
                    },
                    infynityCustomerId = infynityCustomerId,
                    onInfynityCustomerIdChange = {
                        infynityCustomerId = it.take(10)
                        validationError = null
                    },
                    ksebConsumerNumber = ksebConsumerNumber,
                    onKsebConsumerNumberChange = { ksebConsumerNumber = it },
                    atCustomerLocation = atCustomerLocation,
                    customerLocation = customerLocation,
                    onAtCustomerLocationChange = { atCustomerLocation = it },
                    onCustomerLocationChange = { customerLocation = it },
                    teams = availableTeams,
                    teamId = teamId,
                    onTeamChange = { teamId = it; assignedToId = null },
                    assignees = uiState.assignees,
                    assignedToId = assignedToId,
                    onAssigneeChange = { assignedToId = it },
                    showAssignment = canAssign,
                    notes = notes,
                    onNotesChange = { notes = it },
                    isSaving = uiState.isSaving,
                    errorMessage = uiState.errorMessage,
                    validationError = validationError,
                    onCancel = onBack,
                    onSave = {
                        val cleanCustomerId = infynityCustomerId.trim()
                        validationError = when {
                            firstName.isBlank() -> "First name is required."
                            infynityCustomer && cleanCustomerId.isBlank() ->
                                "Infynity Customer ID is required."
                            infynityCustomer && cleanCustomerId.length > 10 ->
                                "Infynity Customer ID must be no more than 10 characters."
                            atCustomerLocation &&
                                (customerLocation.latitude.isBlank() ||
                                 customerLocation.longitude.isBlank() ||
                                 customerLocation.capturedAt.isBlank()) ->
                                "Please capture the customer location using “Use My Location” before saving."
                            else -> null
                        }
                        if (validationError == null) viewModel.save(
                            UpdateLeadRequest(
                                firstName = firstName.trim(),
                                lastName = lastName.trim().ifBlank { null },
                                email = email.trim().ifBlank { null },
                                phone = phone.trim().ifBlank { null },
                                phone2 = phone2.trim().ifBlank { null },
                                infynityCustomer = infynityCustomer,
                                infynityCustomerId = cleanCustomerId.takeIf { infynityCustomer },
                                ksebConsumerNumber = ksebConsumerNumber.trim().ifBlank { null },
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
                                teamId = if (canAssign) teamId else -1,
                                assignedToId = if (canAssign) assignedToId else -1
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun EditLeadForm(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    phone2: String,
    onPhone2Change: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    company: String,
    onCompanyChange: (String) -> Unit,
    source: String,
    onSourceChange: (String) -> Unit,
    placeArea: String,
    onPlaceAreaChange: (String) -> Unit,
    areaSearchResults: List<LeadAreaResponse>,
    isSearchingAreas: Boolean,
    areaSearchError: String?,
    onSearchAreas: (String) -> Unit,
    referredBy: String,
    referralOptions: List<String>,
    onReferredByChange: (String) -> Unit,
    infynityCustomer: Boolean,
    onInfynityCustomerChange: (Boolean) -> Unit,
    infynityCustomerId: String,
    onInfynityCustomerIdChange: (String) -> Unit,
    ksebConsumerNumber: String,
    onKsebConsumerNumberChange: (String) -> Unit,
    atCustomerLocation: Boolean,
    customerLocation: CustomerLocationData,
    onAtCustomerLocationChange: (Boolean) -> Unit,
    onCustomerLocationChange: (CustomerLocationData) -> Unit,
    teams: List<TeamResponse>,
    teamId: Int?,
    onTeamChange: (Int?) -> Unit,
    assignees: List<UserResponse>,
    assignedToId: Int?,
    onAssigneeChange: (Int?) -> Unit,
    showAssignment: Boolean,
    notes: String,
    onNotesChange: (String) -> Unit,
    isSaving: Boolean,
    errorMessage: String?,
    validationError: String?,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            EditSectionCard(title = "Contact") {
                EditTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = "First Name",
                    enabled = !isSaving,
                    required = true
                )

                EditTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = "Last Name",
                    enabled = !isSaving
                )

                EditTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = "Phone",
                    enabled = !isSaving
                )

                EditTextField(
                    value = phone2,
                    onValueChange = onPhone2Change,
                    label = "Phone 2",
                    enabled = !isSaving
                )

                EditTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    enabled = !isSaving
                )
            }
        }

        item {
            EditSectionCard(title = "Lead") {
                EditTextField(
                    value = company,
                    onValueChange = onCompanyChange,
                    label = "Company",
                    enabled = !isSaving
                )

                EditTextField(
                    value = source,
                    onValueChange = onSourceChange,
                    label = "Source",
                    enabled = !isSaving
                )

                PlaceAreaSelector(
                    value = placeArea,
                    onValueChange = onPlaceAreaChange,
                    onSearch = onSearchAreas,
                    areas = areaSearchResults,
                    isSearching = isSearchingAreas,
                    errorMessage = areaSearchError,
                    enabled = !isSaving
                )

                val referralChoices = buildList {
                    add("" to "-- None --")
                    referralOptions.forEach { add(it to it) }
                    if (referredBy.isNotBlank() && referralOptions.none { it == referredBy }) {
                        add(referredBy to "$referredBy (inactive)")
                    }
                }
                LeadDropdownField(
                    "Referred By", referredBy, referralChoices, onReferredByChange, !isSaving
                )
            }
        }

        item {
            EditSectionCard(title = "Customer Information") {
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
                        onCheckedChange = onInfynityCustomerChange,
                        enabled = !isSaving
                    )
                }

                if (infynityCustomer) EditTextField(
                    value = infynityCustomerId,
                    onValueChange = { onInfynityCustomerIdChange(it.take(10)) },
                    label = "Infynity Customer ID *",
                    enabled = infynityCustomer && !isSaving
                )

                EditTextField(
                    value = ksebConsumerNumber,
                    onValueChange = onKsebConsumerNumberChange,
                    label = "KSEB Consumer Number",
                    enabled = !isSaving
                )

                CustomerLocationFields(
                    enabled = !isSaving,
                    checked = atCustomerLocation,
                    location = customerLocation,
                    onCheckedChange = onAtCustomerLocationChange,
                    onLocationChange = onCustomerLocationChange
                )
            }
        }

        if (showAssignment) item {
            EditSectionCard(title = "Assignment") {
                LeadDropdownField(
                    "Team",
                    teamId?.toString().orEmpty(),
                    listOf("" to "-- No Team --") +
                        teams.map { it.id.toString() to it.name },
                    { onTeamChange(it.toIntOrNull()) },
                    !isSaving
                )
                LeadDropdownField(
                    "Assigned To",
                    assignedToId?.toString().orEmpty(),
                    listOf("" to "-- Unassigned --") +
                        assignees.map { it.id.toString() to it.fullName },
                    { onAssigneeChange(it.toIntOrNull()) },
                    !isSaving
                )
            }
        }

        item {
            EditSectionCard(title = "Notes") {
                val keyboardController = LocalSoftwareKeyboardController.current

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
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
                    enabled = !isSaving
                )
            }
        }

        item {
            if (!validationError.isNullOrBlank()) {
                Text(validationError, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving && firstName.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp)
                        )
                    } else {
                        Text("Save")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    required: Boolean = false
) {
    val keyboardController = LocalSoftwareKeyboardController.current

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
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        ),
        enabled = enabled
    )
}

@Composable
private fun EditSectionCard(
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
