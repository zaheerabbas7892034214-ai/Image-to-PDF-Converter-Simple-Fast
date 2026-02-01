package com.zaheer.imagetopdf.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.zaheer.imagetopdf.model.Quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageLoader(private val context: Context) {
    
    suspend fun loadBitmap(uri: Uri, quality: Quality): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            
            // First, decode with inJustDecodeBounds=true to get dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            // Calculate sample size based on quality
            val sampleSize = when (quality) {
                Quality.LOW -> calculateSampleSize(options, 800, 1200)
                Quality.MEDIUM -> calculateSampleSize(options, 1200, 1800)
                Quality.HIGH -> calculateSampleSize(options, 2400, 3600)
            }
            
            // Decode with sample size
            val inputStream2 = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = BitmapFactory.decodeStream(inputStream2, null, finalOptions)
            inputStream2.close()
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun calculateSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
}
