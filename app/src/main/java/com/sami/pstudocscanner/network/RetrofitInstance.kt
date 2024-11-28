package com.sami.pstudocscanner.network

import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http://www.ocrwebservice.com/"
    private const val USERNAME = "SSAAMMII5"       // Replace with your API username
    private const val LICENSE_CODE = "FBB39182-F2B1-496A-A4E4-CBE9B234BF22" // Replace with your license code

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", Credentials.basic(USERNAME, LICENSE_CODE))
                .build()
            chain.proceed(request)
        }
        .build()

    val api: OCRWebService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(OCRWebService::class.java)
    }
}
