package com.nuncsystems.cameraapp.videolist

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.nuncsystems.cameraapp.model.RecordedVideo
import java.io.File

interface RecordedVideoLoader {
    fun loadData(): List<RecordedVideo>
}

class OsPAndBelowRecordedVideoLoader : RecordedVideoLoader {
    override fun loadData(): List<RecordedVideo> {
        val downloadDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val mimeTypeFilter = { f: File ->
            val extension = MimeTypeMap.getFileExtensionFromUrl(f.path)

        }
        val files = downloadDirectory.listFiles()
        val recordedFiles = files?.map { f: File ->
            RecordedVideo(name = f.name, filePath = f.path)
        }
        return recordedFiles ?: emptyList()
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
class OsPAndAboveRecordedVideoLoader(private val contentResolver: ContentResolver/*, private val uri : Uri, private val projection : Array<String>*/) :
    RecordedVideoLoader {
    companion object {
        private const val TAG = "OsPAboveRecVidLoader"
    }

    override fun loadData(): List<RecordedVideo> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATA
        )
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val cursor = contentResolver.query(
            collection,
            projection,
            null,
            null,
            null
        )
        val recordedVideos = mutableListOf<RecordedVideo>()
        cursor?.let {
            if (it.moveToNext()){
                val displayNameIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val mimeTypeIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)

                do {
                    val name = it.getStringOrNull(displayNameIndex) ?: continue
                    val mimeType = it.getStringOrNull(mimeTypeIndex) ?: continue
                    val data = it.getStringOrNull(dataIndex) ?: continue
                    val id = it.getLongOrNull(idIndex) ?: continue
                    val contentUri = ContentUris.appendId(
                        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL).buildUpon(), id).build()
                    recordedVideos.add(RecordedVideo(name = name, filePath = data, contentUri = contentUri))
                } while (it.moveToNext())
            }
        }
        try {
            cursor?.close()
        } catch (e: Exception) {
            Log.e(TAG, "loadData error: ${e.message}", e)
        }
        return recordedVideos
    }

}