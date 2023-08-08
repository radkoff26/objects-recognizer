package com.example.objectsrecognizer.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BytesUtils {

    @JvmStatic
    fun createBitmapFromByteArray(byteArray: ByteArray): Bitmap =
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}