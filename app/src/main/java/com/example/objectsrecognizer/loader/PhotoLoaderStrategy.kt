package com.example.objectsrecognizer.loader

import android.graphics.Bitmap

interface PhotoLoaderStrategy {

    fun loadPhoto(): Bitmap?
}