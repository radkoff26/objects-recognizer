package com.example.objectsrecognizer.view_models

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.example.objectsrecognizer.detection.ImageObjectsDetectorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoFragmentViewModel(
    private val objectsDetectorHelper: ImageObjectsDetectorHelper
) : ViewModel() {
    private val _detectionResultLiveData: MutableLiveData<ImageObjectsDetectorHelper.DetectionResult?> =
        MutableLiveData<ImageObjectsDetectorHelper.DetectionResult?>(null)
    val detectionResultLiveData: LiveData<ImageObjectsDetectorHelper.DetectionResult?> =
        _detectionResultLiveData

    fun detectObjectsOnPhotoByPhotoBitmap(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            detectObjectsWithBitmapAndUpdateLiveData(bitmap)
        }
    }

    private fun detectObjectsWithBitmapAndUpdateLiveData(bitmap: Bitmap) {
        val detectionResult = objectsDetectorHelper.detectObjectsOnImage(bitmap)
        _detectionResultLiveData.postValue(detectionResult)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val objectsDetectorHelper: ImageObjectsDetectorHelper) :
        ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PhotoFragmentViewModel(objectsDetectorHelper) as T
        }
    }
}