package com.example.snapkit.camera

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.snapkit.R
import com.example.snapkit.databinding.FragmentCameraViewBinding
import com.example.snapkit.utils.PermissionUtilsCallbacks
import com.example.snapkit.utils.getAlertDialog
import com.example.snapkit.utils.hasPermissions
import com.example.snapkit.utils.requestForPermissions
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult

class CameraViewFragment : Fragment() {
    private lateinit var binding: FragmentCameraViewBinding
    private lateinit var viewModel: CameraViewModel
    private lateinit var camera: CameraView

    // A flag to check if the user is coming back from the permission Dialog prompt.
    private var fromDialog = false

    // Permissions needed for this fragment.
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Setup data binding expressions and objects.
        binding = FragmentCameraViewBinding.inflate(layoutInflater)
        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)
        binding.viewModel = viewModel

        if (hasPermissions(requireContext(), *permissions)) {
            initCameraView()
        }
        //Set system ui
        setupSystemWindows()
        // Observe the values from the viewModel.
        initObservers()
        // Set onClickListeners for states/events that doesn't need to be tracked by the View Model.
        setOnClickListeners()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        ViewCompat.requestApplyInsets(binding.cameraLayout)
        checkPermissionsForCamera()
    }

    /**
     * Before opening the camera check if user has the permissions.
     */
    private fun checkPermissionsForCamera() {
        // Permissions need to be requested at runtime if API level >= 23
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val permsGranted = hasPermissions(requireContext(), *permissions)
            if (!permsGranted) {
                val cameraAlertDialog = getAlertDialog(requireContext())
                cameraAlertDialog.setMessage(getString(R.string.camera_dialog_message))
                cameraAlertDialog.setTitle(R.string.permissions_dialog_title)
                val permCallBacks = object : PermissionUtilsCallbacks() {
                    override fun onPermissionsPermanentlyDenied() {
                        cameraAlertDialog.show()
                        fromDialog = true
                    }

                    override fun onAllPermissionsAccepted() = viewModel.cameraNotInitialized()
                }
                // Request user for permission before opening the camera.
                requestForPermissions(requireActivity(), permCallBacks, *permissions)
            }
            // Check if user came from the permanently denied dialog and has allowed all permissions.
            else if (fromDialog && permsGranted) {
                viewModel.cameraNotInitialized()
            }
        }
    }

    /**
     * Sets up the CameraView and apply any other settings to the camera functionality.
     */
    private fun initCameraView() {
        // Initialize the object with data binding.
        camera = binding.camera
        camera.setLifecycleOwner(viewLifecycleOwner)
        camera.addCameraListener(object : CameraListener() {
            //TODO: Handle when user interrupts snapshot process via home button or swiping up on the home button.
            override fun onPictureTaken(result: PictureResult) {
                viewModel.storeFile(result)
            }
        })
    }

    /**
     * Set onClickListeners for events/states that don't need to be tracked by the CameraViewModel.
     */
    private fun setOnClickListeners() {
        // Set a listener to toggle Front/Back facing camera.
        binding.cameraFacingButton.setOnClickListener {
            camera.toggleFacing()
        }
    }

    /**
     * Subscribe observers to CameraViewModel's data/states.
     */
    private fun initObservers() {
        // Tell this fragment to observe the capture image state and call the image capture function.
        viewModel.captureImageState.observe(viewLifecycleOwner, Observer { captureState ->
            if (captureState == true) {
                // Start image capture.
                camera.takePicture()
                viewModel.onCaptureButtonFinished()
            }
        })

        // Ensure that CameraView is only initialized once after permissions have been granted.
        viewModel.isCameraInitialized.observe(viewLifecycleOwner, Observer { isInitialized ->
            if (!isInitialized && hasPermissions(requireContext(), *permissions)) {
                initCameraView()
                viewModel.cameraInitialized()
            }
        })

        // Handle navigation when user clicks the gallery button.
        viewModel.navigateToGallery.observe(viewLifecycleOwner, Observer { navigateToGallery ->
            if (navigateToGallery) {
                val navController = findNavController()
                navController.navigate(R.id.action_cameraViewFragment2_to_imageGalleryFragment)
                viewModel.onGalleryButtonFinished()
            }

        })

        // Tell the fragment to start the save file animation.
        viewModel.savingFile.observe(viewLifecycleOwner, Observer { saving ->
            if (saving == true) {
                //TODO: Animation
            }
        })
    }

    private fun setupSystemWindows() {
        val topPadding = binding.cameraLayout.paddingTop
        val botPadding = binding.cameraLayout.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.cameraLayout) { view, windowInsets ->
            view.updatePadding(
                top = windowInsets.systemWindowInsetTop + topPadding,
                bottom = windowInsets.systemWindowInsetBottom + botPadding
            )
            windowInsets
        }
    }
}