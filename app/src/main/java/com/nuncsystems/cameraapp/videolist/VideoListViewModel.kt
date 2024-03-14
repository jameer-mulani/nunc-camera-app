package com.nuncsystems.cameraapp.videolist

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nuncsystems.cameraapp.model.RecordedVideo
import com.nuncsystems.cameraapp.usecase.OsPAndAboveRecordedVideoLoadUseCase
import com.nuncsystems.cameraapp.usecase.OsPAndBelowRecordedVideoLoadUseCase
import com.nuncsystems.cameraapp.util.isAtLeastP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoListViewModel @Inject constructor(
    private val osPAndBelowRecordedVideoLoadUseCase: OsPAndBelowRecordedVideoLoadUseCase,
    private val osPAndAboveRecordedVideoLoadUseCase: OsPAndAboveRecordedVideoLoadUseCase,
    private val contentResolver: ContentResolver,
    private val filePathForOs28AndBelow : File
) : ViewModel() {
    companion object {
        private const val TAG = "VideoListViewModel"
    }

    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }
    private val _videoListLiveData: MutableLiveData<VideoListScreenState> = MutableLiveData()
    val videoListLiveData: LiveData<VideoListScreenState> = _videoListLiveData

    fun loadData() {
        viewModelScope.launch {
            val data = loadDataInternal()
            _videoListLiveData.value = data
        }
    }

    @SuppressLint("NewApi")
    private suspend fun loadDataInternal(): VideoListScreenState {
        return try {
            val list = withContext(Dispatchers.IO + coroutineExceptionHandler) {
                if (isAtLeastP()) {
                    return@withContext osPAndBelowRecordedVideoLoadUseCase(filePathForOs28AndBelow)
                } else {
                    osPAndAboveRecordedVideoLoadUseCase(contentResolver)
                }
            }
            VideoListScreenState(recordedVideos = list, errorMessage = null)
        }catch (e : Exception){
            Log.e(TAG, "loadData: ${e.message}", e )
            VideoListScreenState(recordedVideos = emptyList(), errorMessage = e.message)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

}

data class VideoListScreenState(val recordedVideos : List<RecordedVideo> = emptyList(), val errorMessage : String? = null)