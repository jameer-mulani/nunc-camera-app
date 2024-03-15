package com.nuncsystems.cameraapp.util

import android.os.Build

/**
 * Checks whether given device OS version P OS(Pie API28) AND below.
 */
fun isAtLeastP() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

/**
 * Checks whether given device OS version M(Marshmallow OS 23) above.
 */
fun isGreaterThanM() = Build.VERSION.SDK_INT > Build.VERSION_CODES.M

/**
 * Checks whether given device OS version M(Marshmallow OS23) AND below.
 */
fun isAtLeastM() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.M