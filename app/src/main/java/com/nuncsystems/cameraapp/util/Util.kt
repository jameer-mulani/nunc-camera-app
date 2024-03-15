package com.nuncsystems.cameraapp.util

import android.content.Context
import android.widget.Toast

/**
 * Extension function on [Context] to show the Toast message.
 */
fun Context.showToast(message : String, duration : Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, message, duration).show()
}