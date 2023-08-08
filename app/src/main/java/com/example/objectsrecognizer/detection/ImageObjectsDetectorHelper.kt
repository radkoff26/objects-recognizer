package com.example.objectsrecognizer.detection

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import org.tensorflow.lite.task.gms.vision.detector.Detection
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector

class ImageObjectsDetectorHelper(
    private var threshold: Float = 0.5f,
    private var numThreads: Int = 2,
    private var maxResults: Int = 5,
    private val context: Context
) {

    init {
        if (!TfLiteVision.isInitialized()) {
            TfLiteVision.initialize(context)
        }
    }

    private var objectDetector: ObjectDetector? = null

    fun clearObjectDetector() {
        objectDetector = null
    }

    /* Setting Object Detector Function */
    private fun setupObjectDetector() {
        if (!TfLiteVision.isInitialized()) {
            Log.e(TAG, "setupObjectDetector: TfLiteVision is not initialized yet")
            return
        }

        val optionsBuilder = instantiateOptionsBuilder()

        try {
            objectDetector =
                ObjectDetector.createFromFileAndOptions(
                    context,
                    MODEL_ASSETS_NAME,
                    optionsBuilder.build()
                )
        } catch (e: Exception) {
            Log.e(TAG, "TFLite failed to load model with error: " + e.message, e)
        }
    }

    private fun instantiateOptionsBuilder() = ObjectDetector.ObjectDetectorOptions.builder()
        .setScoreThreshold(threshold)
        .setMaxResults(maxResults)
        .setBaseOptions(
            BaseOptions.builder()
                .setNumThreads(numThreads)
                .build()
        )

    /* Detection Necessity Functions */
    fun detectObjectsOnImage(image: Bitmap): DetectionResult? {
        // Initializing TfLite if not
        if (!TfLiteVision.isInitialized()) {
            Log.e(TAG, "detect: TfLiteVision is not initialized yet")
            return null
        }

        // Initializing objectDetector if not
        if (objectDetector == null) {
            setupObjectDetector()
        }

        // Returning detections (confident that everything's ready)
        return detectObjectsAndGetDetectionResult(image)
    }

    private fun detectObjectsAndGetDetectionResult(image: Bitmap): DetectionResult? {
        val imageProcessor = ImageProcessor.Builder().build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val results = objectDetector?.detect(tensorImage) ?: return null

        return DetectionResult(
            results,
            tensorImage.height,
            tensorImage.width
        )
    }

    data class DetectionResult(
        val results: List<Detection>,
        val imageHeight: Int,
        val imageWidth: Int
    )

    companion object {
        private const val MODEL_ASSETS_NAME = "tflite_detection_model.tflite"
        const val TAG = "ObjectDetectionHelperClass"
    }
}