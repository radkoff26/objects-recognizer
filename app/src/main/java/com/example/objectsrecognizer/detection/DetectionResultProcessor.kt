package com.example.objectsrecognizer.detection

import com.example.objectsrecognizer.data.ImageObject
import com.example.objectsrecognizer.data.ObjectBounds

object DetectionResultProcessor {

    @JvmStatic
    fun processDetectionResult(detectionResult: ImageObjectsDetectorHelper.DetectionResult): List<ImageObject> {
        return mapDetectionResultToImageObjects(detectionResult)
    }

    @JvmStatic
    private fun mapDetectionResultToImageObjects(
        detectionResult: ImageObjectsDetectorHelper.DetectionResult
    ): List<ImageObject> =
        detectionResult.results.map {
            ImageObject(
                it.categories[0].label,
                ObjectBounds(
                    it.boundingBox.left,
                    it.boundingBox.top,
                    it.boundingBox.right,
                    it.boundingBox.bottom
                )
            )
        }
}