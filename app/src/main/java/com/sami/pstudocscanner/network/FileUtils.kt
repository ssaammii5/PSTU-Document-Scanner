package com.sami.pstudocscanner.network

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_file.pdf") // Temporary file in cache directory
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}