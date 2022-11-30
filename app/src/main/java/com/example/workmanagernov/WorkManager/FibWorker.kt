package com.example.workmanagernov.WorkManager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.workmanagernov.util.Constants.FIBONACCI
import com.example.workmanagernov.util.Constants.KEY_IMAGE_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class FibWorker(context: Context, workerParams: WorkerParameters): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val link = inputData.getString(KEY_IMAGE_URI) ?: ""
        val fib = fib(5)
        delay(5000L)
        val outputData = workDataOf(FIBONACCI to fib)
        Result.success(outputData)
    }

    private fun fib(n: Int): Int {
        return if (n <= 1) n else fib(n - 1) + fib(n - 2)
    }
}