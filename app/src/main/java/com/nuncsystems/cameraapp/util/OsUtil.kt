package com.nuncsystems.cameraapp.util

import android.os.Build

fun isAtLeastP() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

fun isGreaterThanM() = Build.VERSION.SDK_INT > Build.VERSION_CODES.M

fun isAtLeastM() = Build.VERSION.SDK_INT <= Build.VERSION_CODES.M