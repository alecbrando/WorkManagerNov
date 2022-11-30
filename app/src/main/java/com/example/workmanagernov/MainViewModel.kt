package com.example.workmanagernov

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.example.workmanagernov.WorkManager.FibWorker
import com.example.workmanagernov.WorkManager.UploadWorker
import com.example.workmanagernov.util.Constants
import java.util.*

class MainViewModel: ViewModel() {
    private val _workInfoData: MutableLiveData<WorkInfo> = MutableLiveData()
    val workInfoData: LiveData<WorkInfo> = _workInfoData
    lateinit var livedata: LiveData<WorkInfo>
    val observer = object: Observer<WorkInfo> {
        override fun onChanged(t: WorkInfo?) {
            Log.d("TEST", t.toString())
        }
    }

    fun startWorkManager(context: Context, image: Uri?) {
        val imageData = workDataOf(Constants.KEY_IMAGE_URI to image.toString())
        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(imageData)
            .build()
        val fibWorker = OneTimeWorkRequest.from(FibWorker::class.java)
        WorkManager.getInstance(context)
            .beginUniqueWork(
                Constants.UPLOAD_IMAGE,
                ExistingWorkPolicy.REPLACE,
                uploadWorkRequest
            ).then(fibWorker).enqueue()
        livedata = WorkManager.getInstance(context).getWorkInfoByIdLiveData(fibWorker.id)
        livedata.observeForever(observer)
    }

    override fun onCleared() {
        super.onCleared()
        livedata.removeObserver(observer)
    }
}