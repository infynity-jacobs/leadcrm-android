package com.infynity.leadcrm.feature.leads

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.infynity.leadcrm.core.network.models.FollowUpResponse
import com.infynity.leadcrm.core.voip.NativeSipManager
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.LeadHistoryResponse

@Composable
fun LeadDetailScreen(
    viewModel: LeadDetailViewModel,
    canDeleteLead: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var pendingDocumentType by remember { mutableStateOf<String?>(null) }
    var viewingDocumentMimeType by remember { mutableStateOf<String?>(null) }
    var isViewingDocument by remember { mutableStateOf(false) }
    var pendingSaveDocumentName by remember { mutableStateOf<String?>(null) }
    var isDownloadingAllDocuments by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val documentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val documentType = pendingDocumentType
        pendingDocumentType = null

        if (uri != null && documentType != null) {
            uploadSelectedLeadDocument(
                context = context,
                uri = uri,
                documentType = documentType,
                onUpload = viewModel::uploadDocument
            )
        }
    }

    LaunchedEffect(uiState.documentFilePath) {
        val filePath = uiState.documentFilePath
        if (!filePath.isNullOrBlank()) {
            try {
                val file = java.io.File(filePath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        uri,
                        viewingDocumentMimeType ?: "*/*"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "No app is available to open this document.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Unable to open document.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isViewingDocument = false
                viewingDocumentMimeType = null
                viewModel.clearDocumentFile()
            }
        }
    }

    val saveDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val filePath = uiState.downloadAllFilePath

        if (uri != null && !filePath.isNullOrBlank()) {
            try {
                val sourceFile = java.io.File(filePath)

                context.contentResolver.openOutputStream(uri)?.use { output ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                } ?: throw java.io.IOException("Unable to open destination")

                Toast.makeText(
                    context,
                    "Documents saved successfully.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Unable to save documents.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        pendingSaveDocumentName = null
        viewModel.clearDownloadAllFile()
    }

    LaunchedEffect(uiState.downloadAllFilePath) {
        val filePath = uiState.downloadAllFilePath
        if (!filePath.isNullOrBlank()) {
            saveDocumentLauncher.launch(
                pendingSaveDocumentName
                    ?: "lead-${uiState.lead?.id ?: "documents"}-documents.pdf"
            )
        }
    }

    LaunchedEffect(uiState.downloadAllFilePath, uiState.documentErrorMessage) {
        if (!uiState.downloadAllFilePath.isNullOrBlank() ||
            !uiState.documentErrorMessage.isNullOrBlank()
        ) {
            isDownloadingAllDocuments = false
        }
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.initiateNativeCall()
        }
    }

    val startNativeCallWithPermission: () -> Unit = {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.initiateNativeCall()
        } else {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

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

            if (canDeleteLead) {
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    enabled = !uiState.isDeleting
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete lead"
                    )
                }
            }

            IconButton(
                onClick = onEdit,
                enabled = !uiState.isDeleting
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit lead"
                )
            }

            IconButton(
                onClick = viewModel::refresh,
                enabled = !uiState.isDeleting
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh lead"
                )
            }
        }

        if (uiState.deleteSuccessful) {
            LaunchedEffect(Unit) {
                onBack()
            }
        }

        if (showDeleteConfirmation && !uiState.isDeleting) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = {
                    Text("Delete Lead?")
                },
                text = {
                    Text(
                        "This will permanently delete this lead and its associated documents. " +
                            "This action cannot be undone."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            viewModel.deleteLead()
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                }
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

                    Button(onClick = viewModel::refresh) {
                        Text("Retry")
                    }
                }
            }

            uiState.lead != null -> {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Overview") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Documents") }
                    )
                }

                if (selectedTab == 0) {
                    LeadDetailContent(
                        lead = uiState.lead!!,
                        isCalling = uiState.isCalling,
                        callMessage = uiState.callMessage,
                        isNativeCalling = uiState.isNativeCalling,
                        nativeCallState = uiState.nativeCallState,
                        nativeCallMessage = uiState.nativeCallMessage,
                        isSpeakerEnabled = uiState.isSpeakerEnabled,
                        onNativeCall = startNativeCallWithPermission,
                        onEndNativeCall = viewModel::endNativeCall,
                        onToggleNativeSpeaker = viewModel::toggleNativeSpeaker,
                        onVoipCall = viewModel::initiateVoipCall
                    )
                } else {
                    LeadDocumentsContent(
                        requirements = uiState.documentRequirements,
                        documents = uiState.documents,
                        isLoading = uiState.isLoadingDocuments,
                        isUploading = uiState.isUploadingDocument,
                        errorMessage = uiState.documentErrorMessage,
                        onRetry = viewModel::loadDocuments,
                        isViewingDocument = isViewingDocument,
                        isDownloadingAllDocuments = isDownloadingAllDocuments,
                        onDownloadAll = {
                            if (!isDownloadingAllDocuments && uiState.documents.isNotEmpty()) {
                                isDownloadingAllDocuments = true
                                viewModel.downloadAllDocuments()
                            }
                        },
                        onViewDocument = { document ->
                            if (!isViewingDocument) {
                                isViewingDocument = true
                                viewingDocumentMimeType =
                                    document.contentType ?: "application/octet-stream"
                                viewModel.downloadDocumentForViewing(document)
                            }
                        },
                        onUpload = { documentType ->
                            pendingDocumentType = documentType
                            documentPicker.launch(
                                arrayOf(
                                    "application/pdf",
                                    "image/jpeg",
                                    "image/png"
                                )
                            )
                        }
                    )
                }

                if (uiState.isDeleting) {
                    Text(
                        text = "Deleting lead...",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LeadDocumentsContent(
    requirements: List<com.infynity.leadcrm.core.network.models.LeadDocumentRequirement>,
    documents: List<com.infynity.leadcrm.core.network.models.LeadDocument>,
    isLoading: Boolean,
    isUploading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    isViewingDocument: Boolean,
    isDownloadingAllDocuments: Boolean,
    onDownloadAll: () -> Unit,
    onViewDocument: (com.infynity.leadcrm.core.network.models.LeadDocument) -> Unit,
    onUpload: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LeadSectionCard(title = "Documents") {
                when {
                    isLoading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }

                    requirements.isEmpty() && documents.isEmpty() -> {
                        Text(
                            text = "No document requirements configured.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    else -> {
                        if (documents.isNotEmpty()) {
                            Button(
                                onClick = onDownloadAll,
                                enabled = !isDownloadingAllDocuments && !isUploading
                            ) {
                                Text(
                                    if (isDownloadingAllDocuments) {
                                        "Preparing PDF..."
                                    } else {
                                        "Download All"
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Uploaded Documents",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            documents.forEach { document ->
                                DocumentRow(
                                    document = document,
                                    isViewing = isViewingDocument,
                                    onView = { onViewDocument(document) }
                                )
                            }
                        }

                        if (requirements.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Document Requirements",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            requirements.forEach { requirement ->
                                val count = documents.count {
                                    it.documentType == requirement.documentType
                                }

                                DocumentRequirementRow(
                                    requirement = requirement,
                                    uploadedCount = count,
                                    isUploading = isUploading,
                                    onUpload = { onUpload(requirement.documentType) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(
    document: com.infynity.leadcrm.core.network.models.LeadDocument,
    isViewing: Boolean,
    onView: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = document.originalFilename,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        DetailText("Type", document.documentType)
        DetailText("Format", document.contentType)
        document.size?.let {
            DetailText("Size", formatDocumentSize(it))
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onView,
            enabled = !isViewing
        ) {
            Text(if (isViewing) "Opening..." else "View")
        }
    }
}

@Composable
private fun DocumentRequirementRow(
    requirement: com.infynity.leadcrm.core.network.models.LeadDocumentRequirement,
    uploadedCount: Int,
    isUploading: Boolean,
    onUpload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = requirement.displayName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = buildString {
                append(if (requirement.required) "Required" else "Optional")
                append(" • ")
                append("$uploadedCount uploaded")
                if (requirement.multipleAllowed) {
                    append(" • Multiple allowed")
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(18.dp)
                    .height(18.dp),
                strokeWidth = 2.dp
            )
        } else if (requirement.multipleAllowed || uploadedCount == 0) {
            Button(
                onClick = onUpload
            ) {
                Text(if (uploadedCount == 0) "Upload" else "Upload Another")
            }
        } else {
            Text(
                text = "Upload limit reached",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private const val MAX_LEAD_DOCUMENT_SIZE = 10L * 1024L * 1024L

private fun uploadSelectedLeadDocument(
    context: android.content.Context,
    uri: Uri,
    documentType: String,
    onUpload: (RequestBody, MultipartBody.Part) -> Unit
) {
    val size = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.SIZE),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getLong(0)
        } else {
            null
        }
    }

    if (size != null && size > MAX_LEAD_DOCUMENT_SIZE) {
        Toast.makeText(
            context,
            "Document must be 10 MB or smaller.",
            Toast.LENGTH_LONG
        ).show()
        return
    }

    val fileName = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            cursor.getString(0)
        } else {
            null
        }
    } ?: "document"

    val contentType = context.contentResolver.getType(uri)
        ?: when {
            fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            fileName.endsWith(".jpg", ignoreCase = true) ||
                fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            else -> null
        }

    if (contentType !in setOf("application/pdf", "image/jpeg", "image/png")) {
        Toast.makeText(
            context,
            "Only PDF, JPEG, and PNG files are supported.",
            Toast.LENGTH_LONG
        ).show()
        return
    }

    val bytes = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        }
    } catch (e: Exception) {
        null
    }

    if (bytes == null) {
        Toast.makeText(
            context,
            "Unable to read the selected document.",
            Toast.LENGTH_LONG
        ).show()
        return
    }

    if (bytes.size.toLong() > MAX_LEAD_DOCUMENT_SIZE) {
        Toast.makeText(
            context,
            "Document must be 10 MB or smaller.",
            Toast.LENGTH_LONG
        ).show()
        return
    }

    val documentTypeBody = documentType
        .toRequestBody("text/plain".toMediaTypeOrNull())

    val fileBody = bytes.toRequestBody(
        contentType!!.toMediaTypeOrNull()
    )

    val filePart = MultipartBody.Part.createFormData(
        "file",
        fileName,
        fileBody
    )

    onUpload(documentTypeBody, filePart)
}

private fun formatDocumentSize(size: Long): String {
    return when {
        size < 1024L -> "$size B"
        size < 1024L * 1024L -> "${size / 1024L} KB"
        else -> String.format(
            Locale.getDefault(),
            "%.1f MB",
            size.toDouble() / (1024L * 1024L)
        )
    }
}

@Composable
private fun LeadDetailContent(
    lead: LeadDetailResponse,
    isCalling: Boolean,
    callMessage: String?,
    isNativeCalling: Boolean,
    nativeCallState: NativeSipManager.State,
    nativeCallMessage: String?,
    isSpeakerEnabled: Boolean,
    onNativeCall: () -> Unit,
    onEndNativeCall: () -> Unit,
    onToggleNativeSpeaker: () -> Unit,
    onVoipCall: () -> Unit
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

                Spacer(modifier = Modifier.height(12.dp))

                LeadContactActions(
                    phone = lead.phone,
                    phone2 = lead.phone2,
                    email = lead.email,
                    isCalling = isCalling,
                    callMessage = callMessage,
                    isNativeCalling = isNativeCalling,
                    nativeCallState = nativeCallState,
                    nativeCallMessage = nativeCallMessage,
                    isSpeakerEnabled = isSpeakerEnabled,
                    onNativeCall = onNativeCall,
                    onEndNativeCall = onEndNativeCall,
                    onToggleNativeSpeaker = onToggleNativeSpeaker,
                    onVoipCall = onVoipCall
                )
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
private fun LeadContactActions(
    phone: String?,
    phone2: String?,
    email: String?,
    isCalling: Boolean,
    callMessage: String?,
    isNativeCalling: Boolean,
    nativeCallState: NativeSipManager.State,
    nativeCallMessage: String?,
    isSpeakerEnabled: Boolean,
    onNativeCall: () -> Unit,
    onEndNativeCall: () -> Unit,
    onToggleNativeSpeaker: () -> Unit,
    onVoipCall: () -> Unit
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        phone?.takeIf { it.isNotBlank() }?.let { number ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNativeCall,
                    enabled = !isNativeCalling && !isCalling,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isNativeCalling) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(18.dp)
                                .height(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call"
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isNativeCalling) "Calling..." else "Call")
                }

                Button(
                    onClick = onVoipCall,
                    enabled = !isCalling && !isNativeCalling,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isCalling) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(18.dp)
                                .height(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "PBX Call"
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isCalling) "Calling..." else "PBX Call")
                }
            }

            Button(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/${formatWhatsAppNumber(number)}")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = "WhatsApp"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp")
            }

            callMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCalling) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            nativeCallMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isNativeCalling) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            if (isNativeCalling) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (nativeCallState == NativeSipManager.State.Connected) {
                        Button(
                            onClick = onToggleNativeSpeaker,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isSpeakerEnabled) "Earpiece" else "Speaker")
                        }
                    }

                    Button(
                        onClick = onEndNativeCall,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "End call"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hang Up")
                    }
                }
            }
        }

        phone2?.takeIf { it.isNotBlank() }?.let { number ->
            Button(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${Uri.encode(number)}")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call phone 2"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Phone 2")
            }
        }

        email?.takeIf { it.isNotBlank() }?.let { address ->
            Button(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("mailto:${Uri.encode(address)}")
                    )
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Email")
            }
        }
    }
}

private fun formatWhatsAppNumber(number: String): String {
    val digits = number.filter { it.isDigit() }

    return when {
        digits.length == 10 -> "91$digits"
        digits.startsWith("91") && digits.length == 12 -> digits
        else -> digits
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
