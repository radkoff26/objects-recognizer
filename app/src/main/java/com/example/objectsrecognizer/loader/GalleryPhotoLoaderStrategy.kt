package com.example.objectsrecognizer.loader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

class GalleryPhotoLoaderStrategy(
    private val context: Context,
    private val uri: Uri
): PhotoLoaderStrategy {

    override fun loadPhoto(): Bitmap? = context.contentResolver.openInputStream(uri)?.use {
        return@use BitmapFactory.decodeStream(it)
    }
}