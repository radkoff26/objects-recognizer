package com.example.objectsrecognizer.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.objectsrecognizer.R
import com.example.objectsrecognizer.databinding.FragmentMainBinding

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
                openPhotoFragmentWithImageUri(it)
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

    /* Open Photo Fragment Functions */
    private fun openPhotoFragmentWithImageUri(uri: Uri) {
        val photoFragmentArguments = instantiatePhotoFragmentArguments(uri.toString())
        findNavController().navigate(R.id.mainFragmentToPhotoFragment, photoFragmentArguments)
    }

    private fun instantiatePhotoFragmentArguments(stringUri: String): Bundle = Bundle().apply {
        // The photo hasn't just been taken, so false is passed
        putBoolean(PhotoFragment.IS_TAKEN_PHOTO_ARGUMENT, false)
        putString(PhotoFragment.GALLERY_PHOTO_URI_ARGUMENT, stringUri)
    }

    /* Open Camera Fragment Functions */
    private fun openCameraFragment() {
        findNavController().navigate(R.id.mainFragmentToCameraFragment)
    }

    companion object {
        private const val IMAGE_TYPE = "image/*"
    }
}