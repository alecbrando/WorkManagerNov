package com.example.workmanagernov.data.remote

import com.example.workmanagernov.domain.UploadResponsev2
import com.example.workmanagernov.util.Constants.BASE_URL
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImgurApiService {
    /**
     * An anonymous image upload endpoint:
     * https://apidocs.imgur.com/?version=latest#c85c9dfc-7487-4de2-9ecd-66f727cf3139
     */
    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Header("Authorization") auth: String = "Client-ID $CLIENT_ID",
        @Part image: MultipartBody.Part?,
    ): Response<UploadResponsev2>

    companion object {
        fun getInstance(): ImgurApiService {
            return Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ImgurApiService::class.java)
        }

        const val CLIENT_ID = ""
    }
}
