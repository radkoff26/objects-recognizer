package com.example.objectsrecognizer.loader

import android.graphics.Bitmap
import com.example.objectsrecognizer.taken_photo.TakenPhotoStore

class TakenPhotoLoaderStrategy(
    private val takenPhotoStore: TakenPhotoStore
): PhotoLoaderStrategy {

    override fun loadPhoto(): Bitmap? {
        return takenPhotoStore.loadImageFromCacheDir()
    }
}