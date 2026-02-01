package com.zaheer.imagetopdf.domain

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.zaheer.imagetopdf.model.ImageItem
import com.zaheer.imagetopdf.model.PdfResult
import com.zaheer.imagetopdf.model.PdfSettings
import com.zaheer.imagetopdf.model.Quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class PdfGenerator(private val context: Context) {
    
    private val imageLoader = ImageLoader(context)
    
    suspend fun generatePdf(
        images: List<ImageItem>,
        settings: PdfSettings,
        fileName: String,
        addWatermark: Boolean
    ): PdfResult = withContext(Dispatchers.IO) {
        
        val pdfDocument = PdfDocument()
        val pageWidth = settings.pageSize.width
        val pageHeight = settings.pageSize.height
        val margin = settings.margin
        
        images.forEachIndexed { index, imageItem ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Load and draw image
            val bitmap = imageLoader.loadBitmap(imageItem.uri, settings.quality)
            if (bitmap != null) {
                val rotatedBitmap = rotateBitmap(bitmap, imageItem.rotationDegrees)
                drawImageOnCanvas(canvas, rotatedBitmap, pageWidth, pageHeight, margin)
                
                if (addWatermark) {
                    drawWatermark(canvas, pageWidth, pageHeight)
                }
                
                rotatedBitmap.recycle()
            }
            
            pdfDocument.finishPage(page)
        }
        
        // Save PDF to MediaStore
        val pdfUri = savePdfToMediaStore(pdfDocument, fileName)
        pdfDocument.close()
        
        val fileSize = getFileSize(pdfUri)
        
        PdfResult(
            uri = pdfUri,
            fileName = "$fileName.pdf",
            fileSizeBytes = fileSize
        )
    }
    
    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return source
        
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
    
    private fun drawImageOnCanvas(
        canvas: Canvas,
        bitmap: Bitmap,
        pageWidth: Int,
        pageHeight: Int,
        margin: Int
    ) {
        val availableWidth = pageWidth - 2 * margin
        val availableHeight = pageHeight - 2 * margin
        
        val imageWidth = bitmap.width.toFloat()
        val imageHeight = bitmap.height.toFloat()
        val imageAspectRatio = imageWidth / imageHeight
        
        val availableAspectRatio = availableWidth.toFloat() / availableHeight.toFloat()
        
        val (scaledWidth, scaledHeight) = if (imageAspectRatio > availableAspectRatio) {
            // Image is wider, fit to width
            availableWidth.toFloat() to (availableWidth / imageAspectRatio)
        } else {
            // Image is taller, fit to height
            (availableHeight * imageAspectRatio) to availableHeight.toFloat()
        }
        
        val left = margin + (availableWidth - scaledWidth) / 2
        val top = margin + (availableHeight - scaledHeight) / 2
        
        val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
        canvas.drawBitmap(bitmap, null, destRect, null)
    }
    
    private fun drawWatermark(canvas: Canvas, pageWidth: Int, pageHeight: Int) {
        val paint = Paint().apply {
            color = Color.argb(50, 128, 128, 128)
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        val watermarkText = "Image to PDF - Free Version"
        val x = pageWidth / 2f
        val y = pageHeight - 50f
        
        canvas.drawText(watermarkText, x, y, paint)
    }
    
    private fun savePdfToMediaStore(pdfDocument: PdfDocument, fileName: String): Uri {
        val pdfFileName = if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf"
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/ImageToPDF")
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            ) ?: throw IllegalStateException("Failed to create MediaStore entry")
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            uri
        } else {
            // Use legacy external storage for older versions
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val appDir = File(documentsDir, "ImageToPDF")
            appDir.mkdirs()
            
            val file = File(appDir, pdfFileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            Uri.fromFile(file)
        }
    }
    
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
