package com.nuncsystems.cameraapp.videolist

import android.content.Context
import android.net.Uri
import com.nuncsystems.cameraapp.model.RecordedVideo
import com.nuncsystems.cameraapp.util.ContentProviderLiveData

/**
 * Livedata for observing content provider onto given uri.
 */
class VideoListContentProviderLiveData(private val context: Context, private val uri: Uri) :
    ContentProviderLiveData<List<RecordedVideo>>(context, uri) {
    override fun getResult(uris: Uri?): List<RecordedVideo> {
        return listOf(RecordedVideo(name = uris.toString()))
    }
}