package com.zaheer.imagetopdf.model

import android.net.Uri

data class ImageItem(
    val id: String,
    val uri: Uri,
    val rotationDegrees: Float = 0f
)

data class PdfSettings(
    val pageSize: PageSize = PageSize.A4,
    val margin: Int = 20,
    val quality: Quality = Quality.MEDIUM
)

enum class PageSize(val width: Int, val height: Int) {
    A4(595, 842),
    LETTER(612, 792),
    LEGAL(612, 1008)
}

enum class Quality {
    LOW, MEDIUM, HIGH
}

data class PdfResult(
    val uri: Uri,
    val fileName: String,
    val fileSizeBytes: Long
)
