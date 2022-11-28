package com.example.workmanagernov

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // provide custom configuration
        val myConfig = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

        // initialize WorkManager
        WorkManager.initialize(this, myConfig)
    }
}