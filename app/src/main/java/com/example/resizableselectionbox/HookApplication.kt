package com.example.resizableselectionbox
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
class HookApplication :Application(){
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val TOKEN = "aq75cw7kvkm9y1i9o4e2"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}


