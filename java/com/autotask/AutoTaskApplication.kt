package com.autotask

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AutoTaskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
