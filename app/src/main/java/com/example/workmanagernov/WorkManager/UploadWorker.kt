package com.example.workmanagernov.WorkManager

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.*
import com.example.workmanagernov.MainActivity
import com.example.workmanagernov.R
import com.example.workmanagernov.data.remote.ImgurApiService
import com.example.workmanagernov.domain.UploadResponsev2
import com.example.workmanagernov.util.Constants
import com.example.workmanagernov.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

interface ImgurUploader {
    val imgurApi: ImgurApiService
    suspend fun uploadFile(uri: Uri, title: String? = null): Resource<UploadResponsev2>
    fun copyStreamToFile(uri: Uri): File
}

class UploadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), ImgurUploader {

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            createNotificationChannel()
            // Get the input
            val imageUriInput =
                inputData.getString(Constants.KEY_IMAGE_URI)
                    ?: return@withContext Result.failure()
            // Do the work
            val response = uploadFile(Uri.parse(imageUriInput))
            when (response) {
                is Resource.Error -> {
                    Result.failure()
                }
                is Resource.Success -> {
                    // Create the output of the work
                    val imageResponse = response.data.data.link
                    // workDataOf (part of KTX) converts a list of pairs to a [Data] object.
                    val outputData = workDataOf(Constants.KEY_IMAGE_URI to imageResponse)
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(outputData.getString("IMAGE_URI")))
                    val pendingIntent = PendingIntent.getActivity(
                        applicationContext,
                        2,
                        intent,
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                    )
                    var builder = NotificationCompat.Builder(applicationContext, "NOTIFICATION_CHANNEL_PREMIUM")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Success")
                        .setContentText("See Image Now")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .build()
                    val notificationManager2 =
                        applicationContext.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager2.notify(0, builder)
                    Result.success(outputData)
                }
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }


    override val imgurApi: ImgurApiService by lazy { ImgurApiService.getInstance() }

    override suspend fun uploadFile(uri: Uri, title: String?): Resource<UploadResponsev2> {
        return try {
            val file = copyStreamToFile(uri)
            val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val filePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val response = imgurApi.uploadFile(
                image = filePart,
            )
            if (response.isSuccessful) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error("Unknown network Exception.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Something happened")
        }
    }

    override fun copyStreamToFile(uri: Uri): File {
        val outputFile = File.createTempFile("temp", null)
        applicationContext.contentResolver
            .openInputStream(uri)?.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
        return outputFile
    }

    fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("NOTIFICATION_CHANNEL_PREMIUM", "title", importance)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = applicationContext.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}