package com.example.objectsrecognizer.taken_photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

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

    /* Image Management */
    /**
     * Function needed for creating new empty file for the taken photo.
     * @return instance of an empty [File] in which photo can be written.
     * */
    fun getNewSavedTakenPhotoFileToWriteTo(): File {
        val cacheDir = context.cacheDir
        createMediaDirIfNecessary(cacheDir)
        removeTakenPhotoFileIfExists(cacheDir)
        val pathToTakenPhotoFile = getPathToTakenPhotoFile(cacheDir)
        return File(pathToTakenPhotoFile).apply {
            createNewFile()
        }
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

    private fun createMediaDirIfNecessary(cacheDir: File) {
        if (isMediaDirAbsent(cacheDir)) {
            createMediaDirInCacheDir(cacheDir)
        }
    }

    private fun getImageBitmapIfPresentOrNull(cacheDir: File): Bitmap? {
        val file = getTakenPhotoFileInCacheDir(cacheDir) ?: return null
        return file.inputStream().use {
            return@use BitmapFactory.decodeStream(it)
        }
    }

    private fun getTakenPhotoFileInCacheDir(cacheDir: File): File? {
        val mediaDir = getMediaDir(cacheDir)
        return mediaDir.listFiles()!!.find {
            it.isFile && it.name == TAKEN_PHOTO_FILENAME
        }
    }

    private fun getMediaDir(cacheDir: File): File {
        val pathToMediaDir = getPathToMediaDir(cacheDir)
        return File(pathToMediaDir)
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