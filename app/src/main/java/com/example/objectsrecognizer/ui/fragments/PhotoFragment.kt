package com.example.objectsrecognizer.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.objectsrecognizer.App
import com.example.objectsrecognizer.R
import com.example.objectsrecognizer.data.ImageObject
import com.example.objectsrecognizer.databinding.FragmentPhotoBinding
import com.example.objectsrecognizer.detection.DetectionResultProcessor
import com.example.objectsrecognizer.detection.ImageObjectsDetectorHelper
import com.example.objectsrecognizer.loader.GalleryPhotoLoaderStrategy
import com.example.objectsrecognizer.loader.PhotoLoaderStrategy
import com.example.objectsrecognizer.loader.TakenPhotoLoaderStrategy
import com.example.objectsrecognizer.utils.ImageResizingUtils
import com.example.objectsrecognizer.view_models.PhotoFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoFragment : Fragment() {
    private var _binding: FragmentPhotoBinding? = null
    private val binding: FragmentPhotoBinding
        get() = _binding!!

    private val photoFragmentViewModelFactory by lazy {
        PhotoFragmentViewModel.Factory(
            (requireActivity().application as App).imageObjectsDetectorHelper
        )
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
        formPhotoLoaderStrategyAndDetectObjectsOnPhotoAndSetImageBitmapSync()

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /* Init */
    private fun initUI() {
        binding.root.showSkeleton()
    }

    /**
     * Extracts necessary arguments, forms [PhotoLoaderStrategy] and enforces [PhotoFragmentViewModel]
     * to detect objects on given photo, also sets bitmap to ImageView. It flows on IO thread.
     * */
    private fun formPhotoLoaderStrategyAndDetectObjectsOnPhotoAndSetImageBitmapSync() {
        lifecycleScope.launch(Dispatchers.IO) {
            formPhotoLoaderStrategyAndDetectObjectsOnPhotoAndSetImageBitmap()
        }
    }

    private fun formPhotoLoaderStrategyAndDetectObjectsOnPhotoAndSetImageBitmap() {
        val photoLoaderStrategy = formStrategyByFragmentArguments()
        val photoBitmap = photoLoaderStrategy.loadPhoto()
        checkNotNull(photoBitmap) { "Chosen photo must be found!" }
        setImageToViewAndResizeIfNecessary(photoBitmap)
        photoFragmentViewModel.detectObjectsOnPhotoByPhotoBitmap(photoBitmap)
    }

    private fun setImageToViewAndResizeIfNecessary(bitmap: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            val resizedBitmap =
                ImageResizingUtils.resizeBitmapToMaxDimension(bitmap, MAX_SIDE_OF_IMAGE)
            launch(Dispatchers.Main.immediate) {
                binding.photoImageView.setImageBitmap(resizedBitmap)
            }
        }
    }

    /* Strategy Formulation Necessity Functions */
    private fun formStrategyByFragmentArguments(): PhotoLoaderStrategy {
        val args = requireArguments()
        failFastIfArgumentsAreAbsent(args)
        val isTakenPhoto = args.getBoolean(IS_TAKEN_PHOTO_ARGUMENT)
        return if (isTakenPhoto) {
            TakenPhotoLoaderStrategy(
                (requireActivity().application as App).takenPhotoStore
            )
        } else {
            val galleryPhotoUri = args.getString(GALLERY_PHOTO_URI_ARGUMENT)!!
            GalleryPhotoLoaderStrategy(requireContext(), Uri.parse(galleryPhotoUri))
        }
    }

    // Fail fast method is necessary to encounter errors as soon as possible
    private fun failFastIfArgumentsAreAbsent(arguments: Bundle) {
        // If there is no argument describing whether the photo was taken or not, then it fails
        if (!arguments.containsKey(IS_TAKEN_PHOTO_ARGUMENT)) {
            failWithAbsentArguments()
        }
        val isTakenPhoto = arguments.getBoolean(IS_TAKEN_PHOTO_ARGUMENT)
        if (!isTakenPhoto) {
            // If the photo wasn't taken and uri wasn't passed to arguments or null was passed
            // Then it also fails
            if (!arguments.containsKey(GALLERY_PHOTO_URI_ARGUMENT)) {
                failWithAbsentArguments()
            } else {
                arguments.getString(GALLERY_PHOTO_URI_ARGUMENT) ?: failWithAbsentArguments()
            }
        }
    }

    private fun failWithAbsentArguments(): Nothing {
        throw IllegalStateException("Fragment has been started with absent arguments!")
    }

    /* View Model Necessity Functions */
    private fun observeViewModelDetectionLiveData() {
        photoFragmentViewModel.detectionResultLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                setOverlayViewObjectsAndShowUi(it)
            }
        }
    }

    private fun setOverlayViewObjectsAndShowUi(detectionResult: ImageObjectsDetectorHelper.DetectionResult) {
        val imageObjects: List<ImageObject> =
            DetectionResultProcessor.processDetectionResult(detectionResult)
        binding.photoOverlayView.setImageObjectsAndInvalidate(
            imageObjects,
            detectionResult.imageHeight,
            detectionResult.imageWidth
        )
        showUi()
    }

    private fun showUi() {
        binding.root.showOriginal()
    }

    companion object {
        private const val MAX_SIDE_OF_IMAGE = 1000
        const val GALLERY_PHOTO_URI_ARGUMENT = "GALLERY_PHOTO_URI_ARGUMENT"
        const val IS_TAKEN_PHOTO_ARGUMENT = "IS_TAKEN_PHOTO_ARGUMENT"
    }
}