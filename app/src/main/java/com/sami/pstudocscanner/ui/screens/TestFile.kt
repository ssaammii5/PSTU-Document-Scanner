package com.sami.pstudocscanner.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sami.pstudocscanner.network.RetrofitInstance
import com.sami.pstudocscanner.network.OCRResponse
import com.sami.pstudocscanner.network.getFileFromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Composable
fun OOCRScreen() {
    val context = LocalContext.current // Get the context using LocalContext
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var responseUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fileUri = uri
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { filePickerLauncher.launch("application/pdf") }) {
            Text("Select PDF File")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                fileUri?.let { uri ->
                    isLoading = true
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val file = getFileFromUri(context, uri) // Use the context here
                            if (file != null) {
                                val response = processDocument(file)
                                if (response?.ErrorMessage.isNullOrEmpty()) {
                                    responseUrl = response?.OutputFileUrl ?: ""
                                    errorMessage = ""
                                } else {
                                    errorMessage = response?.ErrorMessage.orEmpty()
                                }
                            } else {
                                errorMessage = "Failed to process the file."
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Unknown error"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            enabled = fileUri != null
        ) {
            Text("Upload and Convert")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage.isNotEmpty()) {
            Text("Error: $errorMessage", color = MaterialTheme.colors.error)
        } else if (responseUrl.isNotEmpty()) {
            Text("Download URL: $responseUrl")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                downloadFile(context, responseUrl)
            }) {
                Text("Download File")
            }
        }
    }
}

suspend fun processDocument(file: File): OCRResponse? {
    val requestFile = file.asRequestBody("application/pdf".toMediaType())
    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
    val response = RetrofitInstance.api.processDocument(file = body)

    return if (response.isSuccessful) {
        response.body()
    } else {
        null
    }
}

/**
 * Function to download the file using Android's DownloadManager.
 */
fun downloadFile(context: Context, fileUrl: String) {
    val request = DownloadManager.Request(Uri.parse(fileUrl))
        .setTitle("Downloading DOC File")
        .setDescription("Please wait while the file is being downloaded.")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "converted_file.doc")
        .setAllowedOverMetered(true) // Allow download over metered networks
        .setAllowedOverRoaming(true) // Allow download over roaming

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)
}
