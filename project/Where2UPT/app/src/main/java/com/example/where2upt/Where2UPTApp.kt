package com.example.where2upt

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class Where2UPTApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}