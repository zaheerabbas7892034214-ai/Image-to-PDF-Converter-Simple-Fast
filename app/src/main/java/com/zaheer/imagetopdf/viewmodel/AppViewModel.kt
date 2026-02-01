package com.zaheer.imagetopdf.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.imagetopdf.billing.BillingManager
import com.zaheer.imagetopdf.domain.PdfGenerator
import com.zaheer.imagetopdf.model.ImageItem
import com.zaheer.imagetopdf.model.PdfResult
import com.zaheer.imagetopdf.model.PdfSettings
import com.zaheer.imagetopdf.model.Quality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {
    
    private val billingManager = BillingManager(application)
    private val pdfGenerator = PdfGenerator(application)
    
    private val _images = MutableStateFlow<List<ImageItem>>(emptyList())
    val images: StateFlow<List<ImageItem>> = _images.asStateFlow()
    
    private val _pdfSettings = MutableStateFlow(PdfSettings())
    val pdfSettings: StateFlow<PdfSettings> = _pdfSettings.asStateFlow()
    
    private val _pdfResult = MutableStateFlow<PdfResult?>(null)
    val pdfResult: StateFlow<PdfResult?> = _pdfResult.asStateFlow()
    
    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    init {
        viewModelScope.launch {
            billingManager.startConnection()
            billingManager.isPro.collect { isPro ->
                _isProUser.value = isPro
            }
        }
    }
    
    fun addImages(uris: List<Uri>) {
        val newImages = uris.map { uri ->
            ImageItem(
                id = UUID.randomUUID().toString(),
                uri = uri
            )
        }
        
        val currentImages = _images.value
        val maxImages = if (_isProUser.value) Int.MAX_VALUE else 10
        
        if (currentImages.size + newImages.size > maxImages) {
            _message.value = "Free version limited to 10 images"
            val remaining = maxImages - currentImages.size
            if (remaining > 0) {
                _images.value = currentImages + newImages.take(remaining)
            }
        } else {
            _images.value = currentImages + newImages
        }
    }
    
    fun removeImage(imageId: String) {
        _images.value = _images.value.filter { it.id != imageId }
    }
    
    fun rotateImage(imageId: String) {
        _images.value = _images.value.map { image ->
            if (image.id == imageId) {
                image.copy(rotationDegrees = (image.rotationDegrees + 90) % 360)
            } else {
                image
            }
        }
    }
    
    fun reorderImages(fromIndex: Int, toIndex: Int) {
        val list = _images.value.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        _images.value = list
    }
    
    fun updatePdfSettings(settings: PdfSettings) {
        // Check if high quality is requested but user is not Pro
        if (settings.quality == Quality.HIGH && !_isProUser.value) {
            _message.value = "High quality requires Pro"
            return
        }
        _pdfSettings.value = settings
    }
    
    fun generatePdf(fileName: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val result = pdfGenerator.generatePdf(
                    images = _images.value,
                    settings = _pdfSettings.value,
                    fileName = fileName,
                    addWatermark = !_isProUser.value
                )
                _pdfResult.value = result
                if (!_isProUser.value) {
                    _message.value = "Watermark applied (Pro removes it)"
                }
            } catch (e: Exception) {
                _message.value = "Failed to generate PDF: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
    
    fun resetForNewPdf() {
        _images.value = emptyList()
        _pdfResult.value = null
        _pdfSettings.value = PdfSettings()
    }
    
    // Billing methods
    fun purchasePro(activity: android.app.Activity) {
        viewModelScope.launch {
            val success = billingManager.launchPurchaseFlow(activity)
            _message.value = if (success) {
                "Pro unlocked successfully!"
            } else {
                "Purchase failed"
            }
        }
    }
    
    fun restorePurchases() {
        viewModelScope.launch {
            billingManager.restorePurchases()
            _message.value = if (_isProUser.value) {
                "Purchases restored"
            } else {
                "No purchases found to restore"
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
