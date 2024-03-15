package com.nuncsystems.cameraapp.model

import android.net.Uri

/**
 * Simple model class represent the recorded video.
 */
data class RecordedVideo(val name: String, val filePath : String = "", val contentUri : Uri? = null)
