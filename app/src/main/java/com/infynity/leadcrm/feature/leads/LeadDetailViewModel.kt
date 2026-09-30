package com.infynity.leadcrm.feature.leads

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infynity.leadcrm.core.network.models.LeadDetailResponse
import com.infynity.leadcrm.core.network.models.LeadDocument
import com.infynity.leadcrm.core.network.models.LeadDocumentRequirement
import com.infynity.leadcrm.core.network.models.LeadProduct
import com.infynity.leadcrm.core.network.models.LeadProductCreateRequest
import com.infynity.leadcrm.core.network.models.LeadProductUpdateRequest
import com.infynity.leadcrm.core.network.models.Product
import com.infynity.leadcrm.core.voip.NativeSipManager
import com.infynity.leadcrm.data.repository.LeadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LeadDetailUiState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isCalling: Boolean = false,
    val isNativeCalling: Boolean = false,
    val nativeCallState: NativeSipManager.State = NativeSipManager.State.Idle,
    val nativeCallMessage: String? = null,
    val isSpeakerEnabled: Boolean = false,
    val lead: LeadDetailResponse? = null,
    val documentRequirements: List<LeadDocumentRequirement> = emptyList(),
    val documents: List<LeadDocument> = emptyList(),
    val products: List<LeadProduct> = emptyList(),
    val availableProducts: List<Product> = emptyList(),
    val isLoadingProducts: Boolean = false,
    val isSavingProduct: Boolean = false,
    val productErrorMessage: String? = null,
    val isLoadingDocuments: Boolean = false,
    val isUploadingDocument: Boolean = false,
    val documentErrorMessage: String? = null,
    val documentFilePath: String? = null,
    val downloadAllFilePath: String? = null,
    val errorMessage: String? = null,
    val callMessage: String? = null,
    val callSuccessful: Boolean = false,
    val deleteSuccessful: Boolean = false
)

class LeadDetailViewModel(
    private val repository: LeadRepository,
    private val leadId: Int,
    appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadDetailUiState())
    val uiState: StateFlow<LeadDetailUiState> = _uiState.asStateFlow()

    private val appContext = appContext.applicationContext
    private val nativeSipManager = NativeSipManager(appContext)

    init {
        viewModelScope.launch {
            nativeSipManager.state.collect { state ->
                _uiState.value = _uiState.value.copy(
                    nativeCallState = state,
                    isNativeCalling = state == NativeSipManager.State.Calling ||
                        state == NativeSipManager.State.Connected ||
                        state == NativeSipManager.State.Ending
                )
            }
        }

        viewModelScope.launch {
            nativeSipManager.message.collect { message ->
                _uiState.value = _uiState.value.copy(
                    nativeCallMessage = message
                )
            }
        }

        viewModelScope.launch {
            nativeSipManager.speakerEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(isSpeakerEnabled = enabled)
            }
        }

        loadLead()
    }

    fun loadLead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val lead = repository.getLead(leadId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lead = lead,
                    errorMessage = null
                )
                loadProducts()
                loadDocuments()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unable to load lead"
                )
            }
        }
    }

    fun refresh() {
        loadLead()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingProducts = true,
                productErrorMessage = null
            )

            try {
                val products = repository.getLeadProducts(leadId)
                val availableProducts = repository.getProducts(activeOnly = true)

                _uiState.value = _uiState.value.copy(
                    isLoadingProducts = false,
                    products = products,
                    availableProducts = availableProducts,
                    productErrorMessage = null
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isLoadingProducts = false,
                    productErrorMessage = "Unable to load products: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingProducts = false,
                    productErrorMessage = e.message ?: "Unable to load products"
                )
            }
        }
    }

    fun addLeadProduct(request: LeadProductCreateRequest) {
        if (_uiState.value.isSavingProduct) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSavingProduct = true,
                productErrorMessage = null
            )

            try {
                repository.addLeadProduct(
                    leadId = leadId,
                    request = request
                )

                val lead = repository.getLead(leadId)
                val products = repository.getLeadProducts(leadId)
                val availableProducts = repository.getProducts(activeOnly = true)

                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    lead = lead,
                    products = products,
                    availableProducts = availableProducts,
                    productErrorMessage = null
                )

                loadDocuments()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    productErrorMessage = "Unable to add product: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    productErrorMessage = e.message ?: "Unable to add product"
                )
            }
        }
    }

    fun updateLeadProduct(
        itemId: Int,
        request: LeadProductUpdateRequest
    ) {
        if (_uiState.value.isSavingProduct) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSavingProduct = true,
                productErrorMessage = null
            )

            try {
                repository.updateLeadProduct(
                    leadId = leadId,
                    itemId = itemId,
                    request = request
                )

                val products = repository.getLeadProducts(leadId)

                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    products = products,
                    productErrorMessage = null
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    productErrorMessage = "Unable to update product: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    productErrorMessage = e.message ?: "Unable to update product"
                )
            }
        }
    }

    fun deleteLeadProduct(itemId: Int) {
        if (_uiState.value.isSavingProduct) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSavingProduct = true,
                productErrorMessage = null
            )

            try {
                repository.deleteLeadProduct(
                    leadId = leadId,
                    itemId = itemId
                )

                val lead = repository.getLead(leadId)
                val products = repository.getLeadProducts(leadId)

                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    lead = lead,
                    products = products,
                    productErrorMessage = null
                )

                loadDocuments()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    productErrorMessage = "Unable to remove product: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSavingProduct = false,
                    productErrorMessage = e.message ?: "Unable to remove product"
                )
            }
        }
    }

    fun clearProductError() {
        _uiState.value = _uiState.value.copy(
            productErrorMessage = null
        )
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingDocuments = true,
                documentErrorMessage = null
            )

            try {
                val response = repository.getLeadDocumentRequirements(leadId)
                val documents = repository.getLeadDocuments(leadId)

                _uiState.value = _uiState.value.copy(
                    isLoadingDocuments = false,
                    documentRequirements = response.requirements.sortedBy { it.displayOrder },
                    documents = documents,
                    documentErrorMessage = null
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isLoadingDocuments = false,
                    documentErrorMessage = "Unable to load documents: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingDocuments = false,
                    documentErrorMessage = e.message ?: "Unable to load documents"
                )
            }
        }
    }

    fun uploadDocument(
        documentType: okhttp3.RequestBody,
        file: okhttp3.MultipartBody.Part
    ) {
        if (_uiState.value.isUploadingDocument) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploadingDocument = true,
                documentErrorMessage = null
            )

            try {
                repository.uploadLeadDocument(
                    leadId = leadId,
                    documentType = documentType,
                    file = file
                )

                val response = repository.getLeadDocumentRequirements(leadId)
                val documents = repository.getLeadDocuments(leadId)

                _uiState.value = _uiState.value.copy(
                    isUploadingDocument = false,
                    documentRequirements = response.requirements.sortedBy { it.displayOrder },
                    documents = documents,
                    documentErrorMessage = null
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isUploadingDocument = false,
                    documentErrorMessage = "Unable to upload document: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingDocument = false,
                    documentErrorMessage = e.message ?: "Unable to upload document"
                )
            }
        }
    }

    fun downloadDocumentForViewing(document: LeadDocument) {
        if (_uiState.value.documentFilePath != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                documentFilePath = null,
                documentErrorMessage = null
            )

            try {
                val responseBody = repository.viewLeadDocument(
                    leadId = leadId,
                    documentId = document.id
                )

                val documentsDir = java.io.File(appContext.cacheDir, "documents").apply {
                    mkdirs()
                }

                documentsDir.listFiles()?.forEach { file ->
                    if (file.isFile) file.delete()
                }

                val safeName = document.originalFilename
                    .replace(Regex("[^A-Za-z0-9._-]"), "_")
                    .ifBlank { "document" }

                val outputFile = java.io.File(documentsDir, safeName)

                responseBody.byteStream().use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    documentFilePath = outputFile.absolutePath,
                    documentErrorMessage = null
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    documentFilePath = null,
                    documentErrorMessage = "Unable to view document: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    documentFilePath = null,
                    documentErrorMessage = e.message ?: "Unable to view document"
                )
            }
        }
    }

    fun downloadAllDocuments() {
        if (_uiState.value.downloadAllFilePath != null) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                downloadAllFilePath = null,
                documentErrorMessage = null
            )

            try {
                val responseBody = repository.downloadAllLeadDocuments(leadId)

                val documentsDir = java.io.File(appContext.cacheDir, "documents").apply {
                    mkdirs()
                }

                val outputFile = java.io.File(
                    documentsDir,
                    "lead-${leadId}-documents.pdf"
                )

                responseBody.byteStream().use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    downloadAllFilePath = outputFile.absolutePath,
                    documentErrorMessage = null
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    downloadAllFilePath = null,
                    documentErrorMessage =
                        "Unable to download documents: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    downloadAllFilePath = null,
                    documentErrorMessage =
                        e.message ?: "Unable to download documents"
                )
            }
        }
    }

    fun clearDownloadAllFile() {
        _uiState.value = _uiState.value.copy(downloadAllFilePath = null)
    }

    fun clearDocumentFile() {
        _uiState.value = _uiState.value.copy(documentFilePath = null)
    }

    fun initiateNativeCall() {
        if (_uiState.value.isNativeCalling) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                nativeCallMessage = null
            )

            try {
                val response = repository.startNativeCall(leadId)

                val phone = response.phone
                if (phone.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        nativeCallMessage = "No dialable phone number was returned."
                    )
                    return@launch
                }

                val sipConfig = NativeSipManager.SipConfig(
                    username = response.sipUsername.orEmpty(),
                    password = response.sipPassword.orEmpty(),
                    domain = response.sipDomain.orEmpty(),
                    port = response.sipPort ?: 5063,
                    transport = response.sipTransport ?: "tls"
                )

                nativeSipManager.start(sipConfig)
                nativeSipManager.call(phone)

            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    nativeCallMessage = "Native call failed: HTTP ${e.code()}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    nativeCallMessage = e.message ?: "Unable to start native call"
                )
            }
        }
    }

    fun endNativeCall() {
        nativeSipManager.hangUp()
    }

    fun toggleNativeSpeaker() {
        nativeSipManager.toggleSpeaker()
    }

    override fun onCleared() {
        nativeSipManager.stop()
        super.onCleared()
    }

    fun initiateVoipCall() {
        if (_uiState.value.isCalling) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCalling = true,
                callMessage = null,
                callSuccessful = false
            )

            try {
                val response = repository.initiateVoipCall(leadId)

                _uiState.value = _uiState.value.copy(
                    isCalling = false,
                    callMessage = response.message ?: "Call initiated.",
                    callSuccessful = response.ok
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400 -> "This lead does not have a valid phone number."
                    403 -> "You do not have permission to call this lead."
                    404 -> "This lead no longer exists."
                    409 -> "VOIP is not available for your CRM account."
                    502 -> "Unable to initiate the VOIP call. Please try again."
                    else -> "Unable to initiate VOIP call. Please try again."
                }

                _uiState.value = _uiState.value.copy(
                    isCalling = false,
                    callMessage = message,
                    callSuccessful = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCalling = false,
                    callMessage = e.message ?: "Unable to initiate VOIP call.",
                    callSuccessful = false
                )
            }
        }
    }

    fun deleteLead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                errorMessage = null,
                deleteSuccessful = false
            )

            try {
                repository.deleteLead(leadId)

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccessful = true
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    403 -> "You do not have permission to delete this lead."
                    404 -> "This lead no longer exists."
                    else -> "Unable to delete lead. Please try again."
                }

                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = e.message ?: "Unable to delete lead"
                )
            }
        }
    }

    fun clearDeleteSuccess() {
        _uiState.value = _uiState.value.copy(
            deleteSuccessful = false
        )
    }
}
