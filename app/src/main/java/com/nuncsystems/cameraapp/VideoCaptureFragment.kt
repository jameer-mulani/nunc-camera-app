package com.nuncsystems.cameraapp

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.google.common.util.concurrent.ListenableFuture
import com.nuncsystems.cameraapp.databinding.FragmentVideoCaptureBinding
import com.nuncsystems.cameraapp.databinding.FragmentVideoListBinding
import com.nuncsystems.cameraapp.util.CapturerState
import com.nuncsystems.cameraapp.util.isAtLeastP
import com.nuncsystems.cameraapp.util.showToast
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.log

/**
 * Fragment renders the preview and capture the video.
 */
class VideoCaptureFragment : Fragment() {

    companion object {
        private const val TAG = "VideoCaptureFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd:HH:mm:ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ).apply {
            if (isAtLeastP()) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
        }.toTypedArray()
    }

    private var _binding: FragmentVideoCaptureBinding? = null
    private var currentCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var currentTorchState = TorchState.OFF
    private var hasFlashLight: Boolean = false
    private var cameraControl: CameraControl? = null
    private var currentCapturerState = CapturerState.Stopped
    private val binding get() = _binding!!
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var hasAllPermissionGranted = true
            permissions.entries.forEach { permission ->
                if (permission.key in REQUIRED_PERMISSIONS && !permission.value) {
                    hasAllPermissionGranted = false
                }
            }
            if (!hasAllPermissionGranted) {
                requireContext().showToast("Please grant the required permissions")
            } else {
                startCamera()
            }
        }

    private val torchStateObserver = { state: Int ->
        currentTorchState = state
        binding.flashSelectedState = state == TorchState.ON
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVideoCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchCameraButton.setOnClickListener { switchCamera() }
        binding.flashButton.setOnClickListener {
            if (!hasFlashLight) {
                requireContext().showToast("Flash light is not supported on this device")
            } else {
                cameraControl?.enableTorch(currentTorchState == TorchState.OFF)
            }
        }
        binding.videoCaptureButton.setOnClickListener {
//            val isSelected = it.isSelected
//            it.isSelected = !it.isSelected
//            binding.captureSelectedState = !isSelected
            captureVideo()
        }
        binding.pauseButton.setOnClickListener {
            emitPauseResumeState()
            when(currentCapturerState){
                CapturerState.Resumed -> {
                    binding.chronometer.pause()
                    pauseVideo()
                    currentCapturerState = CapturerState.Paused
                    emitPauseResumeState()
                }
                CapturerState.Paused ->{
                    binding.chronometer.resume()
                    resumeVideo()
                    currentCapturerState = CapturerState.Resumed
                    emitPauseResumeState()
                }
                CapturerState.Stopped -> {
                    //no-ops
                }
            }
        }
    }

    private fun emitPauseResumeState(){
        binding.pauseSelectedState = currentCapturerState == CapturerState.Paused
    }

    override fun onStart() {
        super.onStart()
        if (!hasAllPermissionsGranted()) {
            requestPermissions()
        } else {
            startCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
        cameraProviderFuture.addListener(
            CameraProviderRunnable(cameraProviderFuture),
            ContextCompat.getMainExecutor(requireActivity())
        )
    }

    private fun hasAllPermissionsGranted() = REQUIRED_PERMISSIONS.all { eachPermission ->
        ContextCompat.checkSelfPermission(
            requireContext(),
            eachPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun switchCamera() {
        currentCameraSelector =
            if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        startCamera()
    }

    private fun pauseVideo(){
        val currentRecording = recording ?: return
        try {
            currentRecording.pause()
        }catch (e :Exception){
            val message = "Failed to pause video : ${e.message}"
            requireContext().showToast(message)
            Log.e(TAG, message,e)
        }
    }

    private fun resumeVideo(){
        val currentRecording = recording ?: return
        try {
            currentRecording.resume()
        }catch (e : Exception){
            val message = "Failed to resume video : ${e.message}"
            requireContext().showToast(message)
            Log.e(TAG, message,e)
        }
    }

    private fun captureVideo(){
        val videoCapture = videoCapture ?: return
        binding.videoCaptureButton.isEnabled = false
        val ongoingRecording = recording
        if (ongoingRecording != null){
            //it detected that some last ongoing recording is still going on, lets stop and return from here only
            ongoingRecording.stop()
            recording = null
            return
        }

        //lets create a fresh recording instance/session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (!isAtLeastP()){
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/NUNC-Camera")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
            requireActivity().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues)
            .build()

        recording = videoCapture.output.prepareRecording(requireActivity(), mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED){
                    withAudioEnabled()
                }
            }.start(ContextCompat.getMainExecutor(requireActivity())){videoRecordEvent->
                when(videoRecordEvent){
                    is VideoRecordEvent.Start -> {
                        binding.chronometer.apply {
                            base = SystemClock.elapsedRealtime()
                            start()
                            currentCapturerState = CapturerState.Resumed
                        }
                        binding.run {
                            captureSelectedState = true
                            videoCaptureButton.isEnabled = true
                        }
                    }

                    is VideoRecordEvent.Finalize ->{
                        if (!videoRecordEvent.hasError()){
                            //we have successfully recorded vide
                            val mesg = "Video capture successfully : ${videoRecordEvent.outputResults.outputUri}"
                            requireContext().showToast(mesg)
                            Log.d(TAG, mesg)
                        }else{
                            //we failed here to record
                            recording?.close()
                            recording = null
                            val mesg = "Failed to record video : ErrorCode : ${videoRecordEvent.error}"
                            requireContext().showToast(mesg)
                            Log.e(TAG, mesg)
                        }
                        currentCapturerState = CapturerState.Stopped
                        binding?.run {
                            captureSelectedState = false
                            videoCaptureButton.isEnabled = true
                        }
                        binding.chronometer.apply {
                            base = SystemClock.elapsedRealtime()
                            stop()
                        }
                        if (currentTorchState == TorchState.ON){
                            cameraControl?.enableTorch(false)
                        }
                    }
                }
            }
    }

    //Instance of CameraProviderRunnable will initialize ProcessCameraProvider and bind it to view lifecycle
    //If any use case bindings already present then it unbind it all first and then tries to bind to lifecycle again.
    private inner class CameraProviderRunnable(val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>) :
        Runnable {
        override fun run() {
            //cameraProvider instance will help to bind the camera to the lifecycle of activity/fragment
            val cameraProvider = cameraProviderFuture.get()

            //build the Preview for Camera.
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            //init recorder
            val recorder =
                Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)


            try {
                //unbind previously attached use cases if any
                cameraProvider.unbindAll()
                //bind the use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    requireActivity(),
                    currentCameraSelector,
                    preview,
                    videoCapture
                )
                hasFlashLight = camera.cameraInfo.hasFlashUnit()
                if (hasFlashLight) {
                    camera.cameraInfo.torchState.observe(requireActivity(), torchStateObserver)
                }
                cameraControl = camera.cameraControl
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind cameraProvider to lifecycle: ${e.message}", e)
            }
        }
    }
}