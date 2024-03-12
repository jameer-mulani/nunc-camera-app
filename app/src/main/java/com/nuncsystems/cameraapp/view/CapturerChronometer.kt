package com.nuncsystems.cameraapp.view

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import android.widget.Chronometer

class CapturerChronometer(context: Context?, attrs: AttributeSet?) : Chronometer(context, attrs) {

    private var currentBase : Long = SystemClock.elapsedRealtime()

    fun pause(){
        currentBase = base - SystemClock.elapsedRealtime()
        stop()
    }

    fun resume(){
        base = SystemClock.elapsedRealtime() + currentBase
        start()
    }

}