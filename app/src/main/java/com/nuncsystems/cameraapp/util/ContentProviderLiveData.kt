package com.nuncsystems.cameraapp.util

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import androidx.lifecycle.MutableLiveData

abstract class ContentProviderLiveData<T>(private val context: Context, private val uri : Uri) : MutableLiveData<T>() {

    private lateinit var observer: ContentObserver

    override fun onActive() {
        super.onActive()
        observer = object : ContentObserver(null){
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                postValue(getResult(uri))
            }
        }
        context.contentResolver.registerContentObserver(uri, true, observer)
    }

    override fun onInactive() {
        super.onInactive()
        context.contentResolver.unregisterContentObserver(observer)
    }
    abstract fun getResult(uri : Uri?) : T
}