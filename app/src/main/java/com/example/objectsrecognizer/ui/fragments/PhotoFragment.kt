package com.example.objectsrecognizer.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.objectsrecognizer.R
import com.example.objectsrecognizer.data.ImageObject
import com.example.objectsrecognizer.databinding.FragmentPhotoBinding
import com.example.objectsrecognizer.detection.DetectionResultProcessor
import com.example.objectsrecognizer.detection.ImageObjectsDetectorHelper
import com.example.objectsrecognizer.utils.BytesUtils
import com.example.objectsrecognizer.view_models.PhotoFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoFragment : Fragment() {
    private var _binding: FragmentPhotoBinding? = null
    private val binding: FragmentPhotoBinding
        get() = _binding!!

    private val photoFragmentViewModelFactory by lazy {
        PhotoFragmentViewModel.Factory(ImageObjectsDetectorHelper(context = requireContext()))
    }
    private lateinit var photoFragmentViewModel: PhotoFragmentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        photoFragmentViewModel = ViewModelProvider(
            this,
            photoFragmentViewModelFactory
        )[PhotoFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.bind(
            inflater.inflate(R.layout.fragment_photo, container, false)
        )

        initUI()
        observeViewModelDetectionLiveData()
        extractArgumentsAndInitViewModelDetectionAndSetImageToView()

        return binding.root
    }

    /* Init */
    private fun initUI() {
        binding.root.showSkeleton()
    }

    /**
     * Extracts necessary arguments and initializes ViewModel. In case arguments are not present,
     * automatically closes fragment. Eventually, the function sets received bitmap to ImageView.
     * */
    private fun extractArgumentsAndInitViewModelDetectionAndSetImageToView() {
        val args = requireArguments()
        if (!args.containsKey(PHOTO_BYTES_ARGUMENT)) {
            closeFragment()
            return
        }
        val bytes = getBytesFromArguments()
        photoFragmentViewModel.detectImageByImageBytes(bytes)
        setImageToViewFromBytes(bytes)
    }

    private fun setImageToViewFromBytes(bytes: ByteArray) {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = BytesUtils.createBitmapFromByteArray(bytes)
            launch(Dispatchers.Main) {
                binding.photoImageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun getBytesFromArguments(): ByteArray =
        requireArguments().getByteArray(PHOTO_BYTES_ARGUMENT)!!

    private fun closeFragment() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    /* View Model Necessity Functions */
    private fun observeViewModelDetectionLiveData() {
        photoFragmentViewModel.detectionResultLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                setOverlayViewObjects(it)
            }
        }
    }

    private fun setOverlayViewObjects(detectionResult: ImageObjectsDetectorHelper.DetectionResult) {
        val imageObjects: List<ImageObject> =
            DetectionResultProcessor.processDetectionResult(detectionResult)
        binding.photoOverlayView.setImageObjectsAndInvalidate(
            imageObjects,
            detectionResult.imageHeight,
            detectionResult.imageWidth
        )
        binding.root.showOriginal()
    }

    companion object {
        const val PHOTO_BYTES_ARGUMENT = "PHOTO_BYTES_ARGUMENT"
    }
}