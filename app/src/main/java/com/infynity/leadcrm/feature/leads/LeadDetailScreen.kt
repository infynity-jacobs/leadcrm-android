package com.infynity.leadcrm.feature.leads

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.infynity.leadcrm.R
import com.infynity.leadcrm.core.network.models.FollowUpResponse
import com.infynity.leadcrm.core.voip.NativeSipManager
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.LeadHistoryResponse
import com.infynity.leadcrm.core.network.models.LeadProduct
import com.infynity.leadcrm.core.network.models.LeadProductCreateRequest
import com.infynity.leadcrm.core.network.models.LeadProductUpdateRequest
import com.infynity.leadcrm.core.network.models.Product
import com.infynity.leadcrm.core.network.models.TaskResponse

@Composable
fun LeadDetailScreen(
    viewModel: LeadDetailViewModel,
    canDeleteLead: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onTaskSelected: (Int) -> Unit,
    onCreateTask: () -> Unit
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
                        onVoipCall = viewModel::initiateVoipCall,
                        products = uiState.products,
                        availableProducts = uiState.availableProducts,
                        isLoadingProducts = uiState.isLoadingProducts,
                        isSavingProduct = uiState.isSavingProduct,
                        productErrorMessage = uiState.productErrorMessage,
                        onLoadProducts = viewModel::loadProducts,
                        onAddProduct = viewModel::addLeadProduct,
                        onUpdateProduct = viewModel::updateLeadProduct,
                        onDeleteProduct = viewModel::deleteLeadProduct,
                        onClearProductError = viewModel::clearProductError,
                        tasks = uiState.tasks,
                        isLoadingTasks = uiState.isLoadingTasks,
                        taskErrorMessage = uiState.taskErrorMessage,
                        onLoadTasks = viewModel::loadTasks,
                        onTaskSelected = onTaskSelected,
                        onCreateTask = onCreateTask
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
            LeadSectionCard(
                title = "Documents",
                trailingAction = if (documents.isNotEmpty()) {
                    {
                        OutlinedButton(
                            onClick = onDownloadAll,
                            enabled = !isDownloadingAllDocuments && !isUploading,
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 0.dp
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                if (isDownloadingAllDocuments) {
                                    "Preparing..."
                                } else {
                                    "Download All"
                                }
                            )
                        }
                    }
                } else {
                    null
                }
            ) {
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = document.originalFilename,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = onView,
                    enabled = !isViewing,
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 0.dp
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (isViewing) "Opening..." else "View")
                }
            }

            DetailText("Type", document.documentType)
            DetailText("Format", document.contentType)
            document.size?.let {
                DetailText("Size", formatDocumentSize(it))
            }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = requirement.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
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
            }

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(18.dp)
                        .height(18.dp),
                    strokeWidth = 2.dp
                )
            } else if (requirement.multipleAllowed || uploadedCount == 0) {
                OutlinedButton(
                    onClick = onUpload,
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 0.dp
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        if (uploadedCount == 0) "Upload" else "Upload Another"
                    )
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

private fun formatLeadProductMoney(
    currency: String?,
    amount: Int
): String {
    val symbol = when (currency?.uppercase()) {
        null, "", "INR" -> "₹"
        else -> currency
    }
    return "$symbol ${String.format(Locale.getDefault(), "%,d", amount)}"
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
    onVoipCall: () -> Unit,
    products: List<LeadProduct>,
    availableProducts: List<Product>,
    isLoadingProducts: Boolean,
    isSavingProduct: Boolean,
    productErrorMessage: String?,
    onLoadProducts: () -> Unit,
    onAddProduct: (LeadProductCreateRequest) -> Unit,
    onUpdateProduct: (Int, LeadProductUpdateRequest) -> Unit,
    onDeleteProduct: (Int) -> Unit,
    onClearProductError: () -> Unit,
    tasks: List<TaskResponse>,
    isLoadingTasks: Boolean,
    taskErrorMessage: String?,
    onLoadTasks: () -> Unit,
    onTaskSelected: (Int) -> Unit,
    onCreateTask: () -> Unit
) {
    var productEditorOpen by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<LeadProduct?>(null) }
    var removeProduct by remember { mutableStateOf<LeadProduct?>(null) }

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
                if (lead.status.isNotBlank()) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = lead.status.replace('_', ' '),
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 5.dp
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                InlineDetailText("Company", lead.company)
                InlineDetailText("Source", lead.source)
                InlineDetailText("Place / Area", lead.placeArea)
                InlineDetailText("Referred By", lead.referredBy)
                InlineDetailText("Assigned To", lead.assignedToName)
                InlineDetailText("Team", lead.teamName)
            }
        }

        if (lead.infynityCustomer || lead.infynityCustomerId != null ||
            lead.ksebConsumerNumber != null
        ) {
            item {
                LeadSectionCard(title = "Customer Information") {
                    val context = LocalContext.current

                    Text(
                        text = "Infynity Customer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (lead.infynityCustomer) "Yes" else "No",
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 5.dp
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    InlineDetailText(
                        "Customer ID",
                        lead.infynityCustomerId
                    )
                    InlineDetailText(
                        "KSEB Consumer Number",
                        lead.ksebConsumerNumber
                    )

                    val customerLatitude = lead.customerLatitude
                    val customerLongitude = lead.customerLongitude

                    if (lead.atCustomerLocation ||
                        customerLatitude != null ||
                        customerLongitude != null
                    ) {
                        Text(
                            text = "Customer Location",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        val hasCoordinates =
                            customerLatitude != null && customerLongitude != null

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .then(
                                    if (hasCoordinates) {
                                        Modifier.clickable {
                                            val lat = customerLatitude
                                            val lon = customerLongitude
                                            val geoUri = Uri.parse(
                                                "geo:$lat,$lon?q=$lat,$lon(Customer Location)"
                                            )
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                geoUri
                                            )
                                            runCatching {
                                                context.startActivity(intent)
                                            }
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(bottom = 6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Customer Location • Tap to open in map",
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 5.dp
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Lat. ${customerLatitude?.toString().orEmpty()}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Long. ${customerLongitude?.toString().orEmpty()}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Text(
                                text = "Accuracy ${
                                    lead.customerLocationAccuracy?.let {
                                        "%.2f metres".format(it)
                                    }.orEmpty()
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        if (products.isNotEmpty() || availableProducts.isNotEmpty() || isLoadingProducts) {
            item {
                LeadSectionCard(
                    title = "Products",
                    trailingAction = {
                        OutlinedButton(
                            onClick = {
                                editingProduct = null
                                onClearProductError()
                                if (availableProducts.isEmpty()) {
                                    onLoadProducts()
                                }
                                productEditorOpen = true
                            },
                            enabled = !isSavingProduct,
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 0.dp
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("+ Add Product")
                        }
                    }
                ) {
                    if (isLoadingProducts) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    productErrorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (products.isEmpty() && !isLoadingProducts) {
                        Text(
                            text = "No products assigned",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        products.forEach { product ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = product.productName
                                            ?: "Product #${product.productId}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    product.sku?.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            text = "SKU: $it",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        Text(
                                            text = "Quantity: ${product.quantity}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Text(
                                            text = "Interest: ${
                                                product.interestStatus.replaceFirstChar {
                                                    it.uppercase()
                                                }
                                            }",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    product.quotedPrice?.let {
                                        InlineDetailText(
                                            label = "Quoted Price",
                                            value = formatLeadProductMoney(product.currency, it)
                                        )
                                    }

                                    product.notes?.takeIf { it.isNotBlank() }?.let {
                                        InlineDetailText(
                                            label = "Notes",
                                            value = it
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                editingProduct = product
                                                onClearProductError()
                                                productEditorOpen = true
                                            },
                                            enabled = !isSavingProduct,
                                            contentPadding = PaddingValues(
                                                horizontal = 12.dp,
                                                vertical = 0.dp
                                            ),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Edit")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedButton(
                                            onClick = {
                                                removeProduct = product
                                            },
                                            enabled = !isSavingProduct,
                                            contentPadding = PaddingValues(
                                                horizontal = 12.dp,
                                                vertical = 0.dp
                                            ),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Remove")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            LeadSectionCard(
                title = "Tasks",
                trailingAction = {
                    OutlinedButton(
                        onClick = onCreateTask,
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 0.dp
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("+ New Task")
                    }
                }
            ) {
                if (isLoadingTasks) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                taskErrorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    TextButton(onClick = onLoadTasks) {
                        Text("Retry")
                    }
                }

                if (tasks.isEmpty() && !isLoadingTasks && taskErrorMessage == null) {
                    Text(
                        text = "No tasks",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    tasks.forEach { task ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onTaskSelected(task.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                task.statusName?.takeIf { it.isNotBlank() }?.let {
                                    InlineDetailText("Status", it)
                                }

                                InlineDetailText("Priority", task.priority)
                                InlineDetailText(
                                    "Type",
                                    task.taskType.replace('_', ' ')
                                )
                                task.dueDate?.takeIf { it.isNotBlank() }?.let {
                                    InlineDetailText("Due", formatLeadDateTime(it))
                                }
                            }
                        }
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

    if (removeProduct != null && !isSavingProduct) {
        val productToRemove = removeProduct

        AlertDialog(
            onDismissRequest = {
                removeProduct = null
            },
            title = {
                Text("Remove Product?")
            },
            text = {
                Text(
                    "Remove ${
                        productToRemove?.productName
                            ?: "this product"
                    } from this lead?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToRemove?.let {
                            onDeleteProduct(it.id)
                        }
                        removeProduct = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        removeProduct = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (productEditorOpen) {
        LeadProductEditorDialog(
            editingProduct = editingProduct,
            availableProducts = availableProducts,
            isSaving = isSavingProduct,
            errorMessage = productErrorMessage,
            onDismiss = {
                if (!isSavingProduct) {
                    productEditorOpen = false
                    editingProduct = null
                    onClearProductError()
                }
            },
            onAdd = { request ->
                onAddProduct(request)
            },
            onUpdate = { request ->
                editingProduct?.let { product ->
                    onUpdateProduct(product.id, request)
                }
            }
        )
    }
}

@Composable
private fun LeadProductEditorDialog(
    editingProduct: LeadProduct?,
    availableProducts: List<Product>,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onAdd: (LeadProductCreateRequest) -> Unit,
    onUpdate: (LeadProductUpdateRequest) -> Unit
) {
    var selectedProductId by remember(editingProduct?.id) {
        mutableStateOf(editingProduct?.productId)
    }
    var quantityText by remember(editingProduct?.id) {
        mutableStateOf(editingProduct?.quantity?.toString() ?: "1")
    }
    var interestStatus by remember(editingProduct?.id) {
        mutableStateOf(editingProduct?.interestStatus ?: "interested")
    }
    var quotedPriceText by remember(editingProduct?.id) {
        mutableStateOf(editingProduct?.quotedPrice?.toString() ?: "")
    }
    var notesText by remember(editingProduct?.id) {
        mutableStateOf(editingProduct?.notes.orEmpty())
    }
    var validationError by remember(editingProduct?.id) {
        mutableStateOf<String?>(null)
    }
    var saveSubmitted by remember(editingProduct?.id) {
        mutableStateOf(false)
    }
    var saveStarted by remember(editingProduct?.id) {
        mutableStateOf(false)
    }

    LaunchedEffect(isSaving, errorMessage, saveSubmitted) {
        if (isSaving) {
            saveStarted = true
        } else if (
            saveSubmitted &&
            saveStarted &&
            errorMessage == null
        ) {
            onDismiss()
        }
    }

    var productPickerOpen by remember { mutableStateOf(false) }
    var productSearchText by remember { mutableStateOf("") }
    var interestMenuExpanded by remember { mutableStateOf(false) }

    val selectedProduct = availableProducts.firstOrNull {
        it.id == selectedProductId
    }

    val filteredProducts = remember(
        availableProducts,
        productSearchText
    ) {
        val query = productSearchText.trim()
        if (query.isBlank()) {
            availableProducts
        } else {
            availableProducts.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                    product.sku?.contains(query, ignoreCase = true) == true
            }
        }
    }

    if (productPickerOpen) {
        AlertDialog(
            onDismissRequest = {
                if (!isSaving) {
                    productPickerOpen = false
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            title = {
                Text(
                    "Select Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = productSearchText,
                        onValueChange = {
                            productSearchText = it
                        },
                        label = {
                            Text("Search product or SKU")
                        },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp)
                    ) {
                        if (filteredProducts.isEmpty()) {
                            item {
                                Text(
                                    text = "No matching products",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        } else {
                            items(
                                items = filteredProducts,
                                key = { it.id }
                            ) { product ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable(enabled = !isSaving) {
                                            selectedProductId = product.id
                                            productPickerOpen = false
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 11.dp
                                        )
                                    ) {
                                        Text(
                                            text = product.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        product.sku?.takeIf {
                                            it.isNotBlank()
                                        }?.let {
                                            Text(
                                                text = "SKU: $it",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!isSaving) {
                            productPickerOpen = false
                        }
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = {
            Text(
                if (editingProduct == null) "Add Product"
                else "Edit Product",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                if (editingProduct == null) {
                    OutlinedButton(
                        onClick = {
                            productSearchText = ""
                            productPickerOpen = true
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedProduct?.name
                                ?: "Select Product *"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text(
                        text = selectedProduct?.name
                            ?: editingProduct.productName
                            ?: "Product #${editingProduct.productId}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        if (it.all(Char::isDigit)) {
                            quantityText = it
                        }
                    },
                    label = { Text("Quantity *") },
                    singleLine = true,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = interestStatus.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    label = { Text("Interest") },
                    readOnly = true,
                    enabled = !isSaving,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSaving) {
                            interestMenuExpanded = true
                        },
                    trailingIcon = {
                        Text(
                            text = "▼",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                if (interestMenuExpanded) {
                    AlertDialog(
                        onDismissRequest = {
                            interestMenuExpanded = false
                        },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        title = {
                            Text(
                                "Interest",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        text = {
                            Column {
                                listOf(
                                    "interested",
                                    "quoted",
                                    "not_interested"
                                ).forEach { status ->
                                    val selected = interestStatus == status

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable {
                                                interestStatus = status
                                                interestMenuExpanded = false
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ) {
                                        Text(
                                            text = status.replaceFirstChar {
                                                it.uppercase()
                                            },
                                            modifier = Modifier.padding(
                                                horizontal = 14.dp,
                                                vertical = 12.dp
                                            ),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (selected) {
                                                FontWeight.SemiBold
                                            } else {
                                                FontWeight.Normal
                                            },
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    interestMenuExpanded = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quotedPriceText,
                    onValueChange = {
                        if (it.all(Char::isDigit)) {
                            quotedPriceText = it
                        }
                    },
                    label = { Text("Quoted Price (optional)") },
                    singleLine = true,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = {
                        notesText = it
                    },
                    label = { Text("Notes (optional)") },
                    enabled = !isSaving,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                validationError?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isSaving) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    val quantity = quantityText.toIntOrNull()
                    val quotedPrice = quotedPriceText
                        .takeIf { it.isNotBlank() }
                        ?.toIntOrNull()

                    validationError = when {
                        editingProduct == null &&
                            selectedProductId == null ->
                            "Please select a product."

                        quantity == null || quantity < 1 ->
                            "Quantity must be at least 1."

                        quotedPriceText.isNotBlank() &&
                            quotedPrice == null ->
                            "Quoted price must be a valid number."

                        quotedPrice != null && quotedPrice < 0 ->
                            "Quoted price cannot be negative."

                        else -> null
                    }

                    if (validationError != null) {
                        return@TextButton
                    }

                    saveSubmitted = true

                    if (editingProduct == null) {
                        onAdd(
                            LeadProductCreateRequest(
                                productId = selectedProductId!!,
                                quantity = quantity!!,
                                interestStatus = interestStatus,
                                quotedPrice = quotedPrice,
                                notes = notesText
                                    .takeIf { it.isNotBlank() }
                            )
                        )
                    } else {
                        onUpdate(
                            LeadProductUpdateRequest(
                                quantity = quantity!!,
                                interestStatus = interestStatus,
                                quotedPrice = quotedPrice,
                                notes = notesText
                                    .takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
            ) {
                Text(
                    if (editingProduct == null) "Add"
                    else "Save"
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSaving,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        phone?.takeIf { it.isNotBlank() }?.let { number ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Phone",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    SmallContactAction(
                        icon = Icons.Default.Call,
                        label = if (isNativeCalling) "Calling" else "Call",
                        enabled = !isNativeCalling && !isCalling,
                        onClick = onNativeCall
                    )

                    SmallContactAction(
                        icon = Icons.Default.Call,
                        label = if (isCalling) "Calling" else "PBX",
                        enabled = !isCalling && !isNativeCalling,
                        onClick = onVoipCall
                    )

                    SmallContactAction(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        label = "WhatsApp",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/${formatWhatsAppNumber(number)}")
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        phone2?.takeIf { it.isNotBlank() }?.let { number ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Phone 2",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    SmallContactAction(
                        icon = Icons.Default.Call,
                        label = "Phone 2",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:${Uri.encode(number)}")
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        email?.takeIf { it.isNotBlank() }?.let { address ->
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    SmallContactAction(
                        icon = Icons.Default.Email,
                        label = "Email",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_SENDTO,
                                Uri.parse("mailto:${Uri.encode(address)}")
                            )
                            context.startActivity(intent)
                        }
                    )
                }
            }
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (nativeCallState == NativeSipManager.State.Connected) {
                    SmallContactAction(
                        icon = Icons.Default.VolumeUp,
                        label = if (isSpeakerEnabled) "Earpiece" else "Speaker",
                        onClick = onToggleNativeSpeaker
                    )
                }

                SmallContactAction(
                    icon = Icons.Default.Call,
                    label = "Hang Up",
                    actionColor = MaterialTheme.colorScheme.error,
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.55f),
                    onClick = onEndNativeCall
                )
            }
        }
    }
}

@Composable
private fun SmallContactAction(
    icon: ImageVector? = null,
    painter: Painter? = null,
    label: String,
    enabled: Boolean = true,
    actionColor: Color? = null,
    borderColor: Color? = null,
    onClick: () -> Unit
) {
    val resolvedActionColor = actionColor ?: MaterialTheme.colorScheme.primary
    val resolvedBorderColor =
        borderColor ?: resolvedActionColor.copy(alpha = 0.55f)

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.height(30.dp),
        contentPadding = PaddingValues(
            horizontal = 8.dp,
            vertical = 0.dp
        ),
        border = BorderStroke(
            1.dp,
            resolvedBorderColor
        )
    ) {
        when {
            painter != null -> {
                Icon(
                    painter = painter,
                    contentDescription = label,
                    modifier = Modifier.size(16.dp)
                )
            }

            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = resolvedActionColor,
            maxLines = 1
        )
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
    trailingAction: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    trailingAction?.invoke()
                }

                Spacer(modifier = Modifier.height(10.dp))

                content()
            }
        )
    }
}

@Composable
private fun InlineDetailText(
    label: String,
    value: String?
) {
    if (value.isNullOrBlank()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = followUp.followUpType
                    .replace('_', ' ')
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            DetailText(
                "Scheduled",
                formatLeadDateTime(followUp.scheduledAt)
            )
            DetailText(
                "Completed",
                formatLeadDateTime(followUp.completedAt)
            )
            DetailText(
                "Outcome",
                followUp.outcome
            )
            DetailText(
                "Staff",
                followUp.staffName
            )
            DetailText(
                "Notes",
                followUp.notes
            )
        }
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
