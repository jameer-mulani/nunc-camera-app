package com.nuncsystems.cameraapp.capturer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.common.util.concurrent.ListenableFuture
import com.nuncsystems.cameraapp.databinding.ActivityVideoCapturerBinding
import com.nuncsystems.cameraapp.util.CapturerState
import com.nuncsystems.cameraapp.util.OutputOptionsProvider
import com.nuncsystems.cameraapp.util.isAtLeastP
import com.nuncsystems.cameraapp.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class VideoCapturerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "VideoCapturerActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd:HH:mm:ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        ).apply {
            if (isAtLeastP()) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private lateinit var binding: ActivityVideoCapturerBinding
    private var currentCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var currentTorchState = TorchState.OFF
    private var hasFlashLight: Boolean = false
    private var cameraControl: CameraControl? = null
    private var currentCapturerState = CapturerState.Stopped
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
                showToast("Please grant the required permissions")
            } else {
                startCamera()
            }
        }

    private val torchStateObserver = { state: Int ->
        currentTorchState = state
        binding.flashSelectedState = state == TorchState.ON
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCapturerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        binding.switchCameraButton.setOnClickListener {
            validatePermission(
                Manifest.permission.CAMERA,
                ::switchCamera
            )
        }
        binding.flashButton.setOnClickListener {
            if (!hasFlashLight) {
                showToast("Flash light is not supported on this device")
            } else {
                cameraControl?.enableTorch(currentTorchState == TorchState.OFF)
            }
        }
        binding.videoCaptureButton.setOnClickListener {
            validatePermission(Manifest.permission.CAMERA, ::captureVideo)
        }
        binding.pauseButton.setOnClickListener {
            validatePermission(Manifest.permission.CAMERA){
                emitPauseResumeState()
                when (currentCapturerState) {
                    CapturerState.Resumed -> {
                        binding.chronometer.pause()
                        pauseVideo()
                        currentCapturerState = CapturerState.Paused
                        emitPauseResumeState()
                    }

                    CapturerState.Paused -> {
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
    }

    private fun validatePermission(permission: String, block: () -> Unit) {
        if (PermissionChecker.checkSelfPermission(
                this,
                permission
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            block()
        } else {
            showToast("Please grant the permission")
        }
    }

    private fun emitPauseResumeState() {
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            CameraProviderRunnable(cameraProviderFuture),
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun hasAllPermissionsGranted() =
        REQUIRED_PERMISSIONS.all { eachPermission ->
            ContextCompat.checkSelfPermission(
                this,
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

    private fun pauseVideo() {
        val currentRecording = recording ?: return
        try {
            currentRecording.pause()
        } catch (e: Exception) {
            val message = "Failed to pause video : ${e.message}"
            showToast(message)
            Log.e(TAG, message, e)
        }
    }

    private fun resumeVideo() {
        val currentRecording = recording ?: return
        try {
            currentRecording.resume()
        } catch (e: Exception) {
            val message = "Failed to resume video : ${e.message}"
            showToast(message)
            Log.e(TAG, message, e)
        }
    }

    @SuppressLint("NewApi")
    private fun captureVideo() {
        val videoCapture = videoCapture ?: return
        binding.videoCaptureButton.isEnabled = false
        val ongoingRecording = recording
        if (ongoingRecording != null) {
            //it detected that some last ongoing recording is still going on, lets stop and return from here only
            ongoingRecording.stop()
            recording = null
            return
        }
        val name = SimpleDateFormat(
            FILENAME_FORMAT,
            Locale.US
        ).format(System.currentTimeMillis())
        val outputOptionsProvider = OutputOptionsProvider(this)
        val pendingRecording: PendingRecording = if (isAtLeastP()) {
            if (PermissionChecker.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PermissionChecker.PERMISSION_GRANTED
            ) {
                val outputOption = outputOptionsProvider.getFileOutputOption("$name.mp4")
                videoCapture.output.prepareRecording(this, outputOption)
            } else {
                val message = "Please grant the storage permission"
                showToast(message)
                return
            }
        } else {
            val mediaStoreOutputOptions = outputOptionsProvider.getMediaStoreOutputOption(name)
            videoCapture.output.prepareRecording(this, mediaStoreOutputOptions)
        }

        //lets create a fresh recording instance/session
        recording = pendingRecording
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        this@VideoCapturerActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }.start(ContextCompat.getMainExecutor(this)) { videoRecordEvent ->
                when (videoRecordEvent) {
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

                    is VideoRecordEvent.Finalize -> {
                        if (!videoRecordEvent.hasError()) {
                            //we have successfully recorded vide
                            val mesg =
                                "Video capture successfully : ${videoRecordEvent.outputResults.outputUri}"
                            showToast(mesg)
                            Log.d(TAG, mesg)
                        } else {
                            //we failed here to record
                            recording?.close()
                            recording = null
                            val mesg =
                                "Failed to record video : ErrorCode : ${videoRecordEvent.error}"
                            showToast(mesg)
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
                        if (currentTorchState == TorchState.ON) {
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
                    this@VideoCapturerActivity,
                    currentCameraSelector,
                    preview,
                    videoCapture
                )
                hasFlashLight = camera.cameraInfo.hasFlashUnit()
                if (hasFlashLight) {
                    camera.cameraInfo.torchState.observe(
                        this@VideoCapturerActivity,
                        torchStateObserver
                    )
                }
                cameraControl = camera.cameraControl
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Failed to bind cameraProvider to lifecycle: ${e.message}",
                    e
                )
            }
        }
    }


}