package com.nuncsystems.cameraapp.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import java.io.File

/**
 * Provides recorded videos output options.
 */
class OutputOptionsProvider(private val context: Context) {
    /**
     * Returns the [FileOutputOptions] for recorded file, it records the file onto Downloads folder
     * Useful for OS P and below device.
     * @param filename name of file for the recording video
     * @return instance of [FileOutputOptions]
     */
    fun getFileOutputOption(filename: String): FileOutputOptions {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            filename
        )
        return FileOutputOptions.Builder(file).build()
    }

    /**
     * Returns the [MediaStoreOutputOptions] for recorded file, it records the file and store it into the [MediaStore]
     * Useful for OS Q and above device.
     * @param filename name of file for the recording video
     * @return instance of [MediaStoreOutputOptions]
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getMediaStoreOutputOption(filename: String): MediaStoreOutputOptions {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        return MediaStoreOutputOptions.Builder(
            context.contentResolver, MediaStore.Downloads.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues)
            .build()
    }


}