package com.nuncsystems.cameraapp.model

import android.net.Uri

data class RecordedVideo(val name: String, val filePath : String = "", val contentUri : Uri? = null)
