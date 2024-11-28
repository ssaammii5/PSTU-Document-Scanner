package com.sami.pstudocscanner.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface OCRWebService {
    @Multipart
    @POST("restservices/processDocument")
    suspend fun processDocument(
        @Part file: MultipartBody.Part,
        @Query("language") language: String = "english",
        @Query("outputformat") outputFormat: String = "doc",
        @Query("gettext") getText: Boolean = false
    ): Response<OCRResponse>
}

data class OCRResponse(
    val ErrorMessage: String?,
    val OutputFileUrl: String?,
    val AvailablePages: Int,
    val ProcessedPages: Int
)
