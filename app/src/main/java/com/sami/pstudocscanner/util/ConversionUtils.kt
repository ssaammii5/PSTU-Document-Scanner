package com.sami.pstudocscanner.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaType
import com.sami.pstudocscanner.network.RetrofitInstance
import com.sami.pstudocscanner.network.getFileFromUri

fun convertToWord(uri: Uri, context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Step 1: Get the file from Uri
            val file = getFileFromUri(context, uri)
            if (file == null || !file.exists()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to access file.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // Step 2: Prepare the file for upload
            val requestFile = file.asRequestBody("application/pdf".toMediaType())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // Step 3: Call the OCR API
            val response = RetrofitInstance.api.processDocument(body)
            if (response.isSuccessful) {
                val ocrResponse = response.body()
                if (!ocrResponse?.ErrorMessage.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${ocrResponse.ErrorMessage}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val downloadUrl = ocrResponse?.OutputFileUrl
                    if (!downloadUrl.isNullOrEmpty()) {
                        // Step 4: Use DownloadManager to download the file
                        val request = DownloadManager.Request(Uri.parse(downloadUrl))
                            .setTitle("Downloading DOC File")
                            .setDescription("Your converted Word file is being downloaded.")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "converted_file.doc")
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true)

                        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        downloadManager.enqueue(request)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Download started.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Download URL not found.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to process document.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
