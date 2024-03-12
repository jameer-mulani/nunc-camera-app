package com.nuncsystems.cameraapp.videolist

import android.annotation.SuppressLint
import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuncsystems.cameraapp.model.RecordedVideo
import com.nuncsystems.cameraapp.util.isAtLeastP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(): ViewModel() {
    companion object {
        private const val TAG = "VideoListViewModel"
    }

    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    private val _videoListLiveData: MutableLiveData<List<RecordedVideo>> = MutableLiveData()
    val videoListLiveData: LiveData<List<RecordedVideo>> = _videoListLiveData
    var contentResolver: ContentResolver? = null
    fun loadData() {
        viewModelScope.launch {
            val data = loadDataInternal()
            _videoListLiveData.value = data
        }
    }

    @SuppressLint("NewApi")
    private suspend fun loadDataInternal(): List<RecordedVideo> {
        return withContext(Dispatchers.IO + coroutineExceptionHandler) {
            if (isAtLeastP()) {
                val osPAndBelowRecordedVideoLoader = OsPAndBelowRecordedVideoLoader()
                val list = osPAndBelowRecordedVideoLoader.loadData()
                return@withContext list
            } else {
                if (contentResolver != null) {
                    val osPAndAboveRecordedVideoLoader =
                        OsPAndAboveRecordedVideoLoader(contentResolver = contentResolver!!)
                    val list = osPAndAboveRecordedVideoLoader.loadData()
                    list
                } else {
                    emptyList<RecordedVideo>()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

}