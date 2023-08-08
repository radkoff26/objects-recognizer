package com.example.objectsrecognizer.ui.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.objectsrecognizer.R
import com.example.objectsrecognizer.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    private lateinit var photoChooseLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.bind(
            inflater.inflate(R.layout.fragment_main, container, false)
        )

        initListeners()
        initPhotoChooseLauncher()

        return binding.root
    }

    override fun onDestroyView() {
        // Avoiding memory leak
        _binding = null
        super.onDestroyView()
    }


    /* Init */
    private fun initListeners() {
        initTakePhotoButtonListener()
        initChoosePhotoButtonListener()
    }

    private fun initPhotoChooseLauncher() {
        photoChooseLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                openPhotoFragmentWithBytesOfImageByUriOnLifecycleScope(it)
            }
        }
    }

    /* Listeners Initialization Functions */
    private fun initTakePhotoButtonListener() {
        binding.takePhotoButton.setOnClickListener {
            openCameraFragment()
        }
    }

    private fun initChoosePhotoButtonListener() {
        binding.choosePhotoButton.setOnClickListener {
            launchChoosePhotoLauncher()
        }
    }

    /* Photo Choosing Functions */
    private fun launchChoosePhotoLauncher() {
        photoChooseLauncher.launch(IMAGE_TYPE)
    }

    private fun openPhotoFragmentWithBytesOfImageByUriOnLifecycleScope(uri: Uri) {
        lifecycleScope.launch {
            openPhotoFragmentWithBytesOfImageByUri(uri)
        }
    }

    private suspend fun openPhotoFragmentWithBytesOfImageByUri(uri: Uri) {
        val imageBytes = getBytesOfImageByUri(uri) ?: return
        openPhotoFragmentWithBytes(imageBytes)
    }

    private suspend fun getBytesOfImageByUri(uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        openContentInputStreamByUri(uri).use { inputStream ->
            if (inputStream == null) {
                return@withContext null
            }
            return@withContext inputStream.readBytes()
        }
    }

    private fun openContentInputStreamByUri(uri: Uri) =
        requireContext().contentResolver.openInputStream(uri)

    /* Open Photo Fragment Functions */
    private fun openPhotoFragmentWithBytes(bytes: ByteArray) {
        val photoFragmentArguments = instantiatePhotoFragmentArguments(bytes)
        findNavController().navigate(R.id.mainFragmentToPhotoFragment, photoFragmentArguments)
    }

    private fun instantiatePhotoFragmentArguments(bytes: ByteArray): Bundle = Bundle().apply {
        putByteArray(PhotoFragment.PHOTO_BYTES_ARGUMENT, bytes)
    }

    /* Open Camera Fragment Functions */
    private fun openCameraFragment() {
        findNavController().navigate(R.id.mainFragmentToCameraFragment)
    }

    companion object {
        private const val IMAGE_TYPE = "image/*"
    }
}