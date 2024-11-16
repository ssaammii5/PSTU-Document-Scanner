package com.sami.pstudocscanner.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sami.pstudocscanner.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OCRScanningScreen() {
    val context = LocalContext.current
    val capturedImageUris = remember { mutableStateListOf<Uri>() }
    var currentImageUri by remember { mutableStateOf<Uri?>(null) }
    var recognizedText by remember { mutableStateOf("") }
    val outputDirectory = context.getExternalFilesDir(null)
    val coroutineScope = rememberCoroutineScope()

    // Launcher to request permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Launcher to capture an image
    val captureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && currentImageUri != null) {
                capturedImageUris.add(currentImageUri!!)
                currentImageUri = null
            } else {
                Toast.makeText(context, "Image capture failed", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "OCR Scanning",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Image Display Section
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(capturedImageUris) { uri ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .border(2.dp, Color.Red, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = {
                            capturedImageUris.remove(uri)
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.Red
                        )
                    }
                }
            }
        }

        // Process Image Button
        Button(
            onClick = {
                coroutineScope.launch {
                    recognizedText = ""
                    for (uri in capturedImageUris) {
                        val bitmap =
                            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                        if (bitmap != null) {
                            try {
                                val result = recognizeText(bitmap)
                                recognizedText += result + "\n"
                            } catch (e: Exception) {
                                e.printStackTrace()
                                recognizedText += "Failed to recognize text: ${e.message}\n"
                            }
                        } else {
                            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Process Image",
                tint = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Process Image", color = Color.White)
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Preview Text Section
        Text(
            text = "Recognized Text",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Makes the text scrollable
            ) {
                Text(
                    text = if (recognizedText.isNotEmpty()) recognizedText else "No text recognized yet",
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    val clipboardManager =
                        context.getSystemService(android.content.ClipboardManager::class.java)
                    val clip =
                        android.content.ClipData.newPlainText("Recognized Text", recognizedText)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Copy to Clipboard",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Clip", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    try {
                        val translateIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                recognizedText
                            ) // Pass the recognized text directly
                            type = "text/plain"
                            `package` = "com.google.android.apps.translate" // Explicitly target Google Translate
                        }
                        context.startActivity(translateIntent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Google Translate app not installed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Translate",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    try {
                        // Directory for saving the Word file
                        val outputDirectory = context.getExternalFilesDir(null)
                        val fileName =
                            "PSTU Doc ${
                                SimpleDateFormat(
                                    "yyyyMMdd_HHmmss",
                                    Locale.getDefault()
                                ).format(Date())
                            }.docx"
                        val file = File(outputDirectory, fileName)

                        // Create a Word file and write the recognized text into it
                        FileOutputStream(file).use { fos ->
                            XWPFDocument().apply {
                                val paragraph = createParagraph()
                                val run = paragraph.createRun()
                                run.setText(recognizedText)
                                write(fos) // Write the Word document
                            }
                        }

                        Toast.makeText(
                            context,
                            "Word file saved: ${file.absolutePath}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Open the file using an Intent
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.file_provider",
                            file
                        )
                        val intent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_VIEW
                            setDataAndType(
                                uri,
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                            )
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            "Error creating Word file: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Convert to Word",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Word", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Capture Image Button
        Button(
            onClick = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
                if (context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    currentImageUri = createImageFileUri(context, outputDirectory)
                    currentImageUri?.let { uri -> captureLauncher.launch(uri) }
                } else {
                    Toast.makeText(context, "Camera permission not granted", Toast.LENGTH_SHORT)
                        .show()
                }
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Capture Image",
                tint = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Capture Image", color = Color.White)
        }
    }
}

fun createImageFileUri(context: android.content.Context, outputDirectory: File?): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = outputDirectory ?: context.getExternalFilesDir(null)
    storageDir?.exists()?.let { if (!it) storageDir?.mkdirs() }
    return try {
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        FileProvider.getUriForFile(context, "${context.packageName}.file_provider", file)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { text ->
            continuation.resume(text.text) {}
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
            continuation.resume("Failed to recognize text: ${e.message}") {}
        }
}

@Preview(showBackground = true)
@Composable
fun OCRScanningScreenPreview() {
    OCRScanningScreen()
}
