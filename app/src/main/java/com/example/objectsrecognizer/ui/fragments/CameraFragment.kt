package com.example.objectsrecognizer.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.objectsrecognizer.App
import com.example.objectsrecognizer.R
import com.example.objectsrecognizer.databinding.FragmentCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding: FragmentCameraBinding
        get() = _binding!!

    private val imageCapture: ImageCapture = ImageCapture.Builder().build()
    private val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.bind(
            inflater.inflate(R.layout.fragment_camera, container, false)
        )

        requestPermissionIfNecessaryOrStartCameraImmediately()
        setTakeImageButtonClickListener()

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /* Listener Setting */
    private fun setTakeImageButtonClickListener() {
        binding.takePhotoButton.setOnClickListener {
            takePicture()
        }
    }

    /* Permission Request */
    private fun requestPermissionIfNecessaryOrStartCameraImmediately() {
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                if (it) {
                    startCamera()
                } else {
                    closeFragment()
                }
            }.launch(Manifest.permission.CAMERA)
        } else {
            startCamera()
        }
    }

    /* Fragment Management */
    private fun openPhotoFragment() {
        val args = Bundle().apply {
            putBoolean(PhotoFragment.IS_TAKEN_PHOTO_ARGUMENT, true)
        }
        findNavController().navigate(R.id.cameraFragmentToPhotoFragment, args)
    }

    private fun closeFragment() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    /* Camera Management */
    private fun startCamera() {
        val cameraProviderListenableFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderListenableFuture.addListener(
            {
                val cameraProvider = cameraProviderListenableFuture.get()

                val preview = buildPreview()

                try {
                    rebindCameraProviderWithUseCases(
                        cameraProvider,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    notifyUserAboutCameraStartErrorAndCloseTheFragment()
                }
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun buildPreview(): Preview =
        Preview.Builder()
            .build()
            .apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }

    private fun rebindCameraProviderWithUseCases(
        cameraProvider: ProcessCameraProvider,
        cameraSelector: CameraSelector,
        vararg useCases: UseCase
    ) {
        cameraProvider.unbindAll()
        val useCaseGroup = buildUseCaseGroup(useCases.toList())
        cameraProvider.bindToLifecycle(
            viewLifecycleOwner,
            cameraSelector,
            useCaseGroup
        )
    }

    private fun buildUseCaseGroup(useCaseList: List<UseCase>): UseCaseGroup =
        UseCaseGroup.Builder().apply {
            useCaseList.forEach {
                addUseCase(it)
            }
        }.build()

    private fun takePicture() {
        lifecycleScope.launch(Dispatchers.IO) {
            val fileOptions = getOutputFileOptionsWithTakenPhotoFileBuiltIn()
            launch(Dispatchers.Main.immediate) {
                imageCapture.takePicture(
                    fileOptions,
                    ContextCompat.getMainExecutor(requireContext()),
                    object : OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            openPhotoFragment()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            toastErrorWhileTakingPicture()
                        }
                    }
                )
            }
        }
    }

    private fun getOutputFileOptionsWithTakenPhotoFileBuiltIn(): OutputFileOptions {
        val file =
            (requireActivity().application as App).takenPhotoStore.getNewSavedTakenPhotoFileToWriteTo()
        return OutputFileOptions.Builder(file).build()
    }

    /* Error Notification */
    private fun notifyUserAboutCameraStartErrorAndCloseTheFragment() {
        toastCameraStartError()
        closeFragment()
    }

    private fun toastCameraStartError() {
        Toast.makeText(requireContext(), R.string.error_while_starting_camera, Toast.LENGTH_SHORT)
            .show()
    }

    private fun toastErrorWhileTakingPicture() {
        Toast.makeText(requireContext(), R.string.error_while_taking_picture, Toast.LENGTH_SHORT)
            .show()
    }
}