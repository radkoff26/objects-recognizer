package com.example.objectsrecognizer

import android.app.Application
import com.example.objectsrecognizer.detection.ImageObjectsDetectorHelper
import com.example.objectsrecognizer.taken_photo.TakenPhotoStore
import org.tensorflow.lite.task.gms.vision.TfLiteVision

class App: Application() {

    val takenPhotoStore: TakenPhotoStore by lazy {
        TakenPhotoStore(this)
    }

    val imageObjectsDetectorHelper: ImageObjectsDetectorHelper by lazy {
        ImageObjectsDetectorHelper(context = this)
    }

    override fun onCreate() {
        super.onCreate()
        TfLiteVision.initialize(this)
    }
}