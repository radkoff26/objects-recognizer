package com.example.objectsrecognizer.utils

import android.graphics.Bitmap

object ImageResizingUtils {

    private data class Dimensions(
        val width: Int,
        val height: Int
    )

    @JvmStatic
    fun resizeBitmapToMaxDimension(bitmap: Bitmap, maxDimension: Int): Bitmap {
        if (bitmap.height < maxDimension && bitmap.width < maxDimension) {
            // No reason for resizing
            return bitmap
        }
        val resizedDimensions = getResizedDimensions(bitmap, maxDimension)
        return Bitmap.createScaledBitmap(
            bitmap,
            resizedDimensions.width,
            resizedDimensions.height,
            true
        )
    }

    private fun getResizedDimensions(bitmap: Bitmap, maxDimension: Int): Dimensions {
        val width: Int
        val height: Int
        val aspectRatio: Float = bitmap.width.toFloat() / bitmap.height
        if (bitmap.height > bitmap.width) {
            height = maxDimension
            width = (height * aspectRatio).toInt()
        } else {
            width = maxDimension
            height = (width / aspectRatio).toInt()
        }
        return Dimensions(width, height)
    }
}