package com.example.objectsrecognizer.taken_photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Class necessary for temporary storing of taken photo. This is used for transferring taken photo
 * between Android components. It's recommended to store the instance of this class as singleton
 * application-level dependency.
 * */
class TakenPhotoStore(
    private val context: Context
) {

    /* Image Load */
    fun loadImageFromCacheDir(): Bitmap? {
        val cacheDir = context.cacheDir
        createMediaDirIfNecessary(cacheDir)
        return getImageBitmapIfPresentOrNull(cacheDir)
    }

    /* Image Store */
    fun storeImageInCacheDir(bitmap: Bitmap) {
        val cacheDir = context.cacheDir
        val bitmapBytes = getBytesFromBitmap(bitmap)
        manageMediaDirAndSaveBytesToTakenPhotoFile(bitmapBytes, cacheDir)
    }

    private fun getBytesFromBitmap(bitmap: Bitmap): ByteArray {
        return openOutputStreamForBitmap(bitmap).use {
            return@use it.toByteArray()
        }
    }

    private fun manageMediaDirAndSaveBytesToTakenPhotoFile(bytes: ByteArray, cacheDir: File) {
        createMediaDirIfNecessary(cacheDir)
        removeTakenPhotoFileIfExists(cacheDir)
        createTakenPhotoFileOutputStream(cacheDir).use {
            it.write(bytes)
        }
    }

    private fun createTakenPhotoFileOutputStream(cacheDir: File): FileOutputStream {
        val pathToTakenPhotoFile = getPathToTakenPhotoFile(cacheDir)
        return FileOutputStream(pathToTakenPhotoFile)
    }

    private fun removeTakenPhotoFileIfExists(cacheDir: File) {
        val takenPhotoFilePath = getPathToTakenPhotoFile(cacheDir)
        val takenPhotoFile = File(takenPhotoFilePath)
        takenPhotoFile.delete()
    }

    private fun getPathToTakenPhotoFile(cacheDir: File): String {
        val pathToMediaDir = getPathToMediaDir(cacheDir)
        return "$pathToMediaDir/$TAKEN_PHOTO_FILENAME"
    }

    private fun getPathToMediaDir(cacheDir: File): String = "$cacheDir/$MEDIA_CACHE_DIR"

    private fun openOutputStreamForBitmap(bitmap: Bitmap): ByteArrayOutputStream {
        return ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
        }
    }

    private fun createMediaDirIfNecessary(cacheDir: File) {
        if (isMediaDirAbsent(cacheDir)) {
            createMediaDirInCacheDir(cacheDir)
        }
    }

    private fun getImageBitmapIfPresentOrNull(cacheDir: File): Bitmap? {
        val file = getMediaFileInCacheDir(cacheDir) ?: return null
        return file.inputStream().use {
            return@use BitmapFactory.decodeStream(it)
        }
    }

    private fun getMediaFileInCacheDir(cacheDir: File): File? =
        cacheDir.listFiles()!!.find {
            it.isFile && it.name == TAKEN_PHOTO_FILENAME
        }

    private fun isMediaDirAbsent(cacheDir: File): Boolean {
        val cacheFiles = cacheDir.listFiles()!!
        return !cacheFiles.any {
            it.isDirectory && it.name == MEDIA_CACHE_DIR
        }
    }

    private fun createMediaDirInCacheDir(cacheDir: File) {
        val mediaDirPath = getPathToMediaDir(cacheDir)
        val mediaDir = File(mediaDirPath)
        mediaDir.mkdir()
    }

    companion object {
        private const val MEDIA_CACHE_DIR = "media"
        private const val TAKEN_PHOTO_FILENAME = "taken_photo"
    }
}