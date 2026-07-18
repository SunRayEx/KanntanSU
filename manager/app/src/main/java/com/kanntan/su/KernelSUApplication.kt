package com.kanntan.su

import android.app.Application

/**
 * Global application instance for KanntanSU
 * 
 * This is initialized in onCreate() and provides
 * access to the Android context for package manager queries,
 * shared preferences, etc.
 * 
 * Used throughout the app by:
 * - Natives.kt for JNI context
 * - ViewModels for SharedPreferences
 * - Repository implementations
 */
lateinit var ksuApp: KernelSUApplication

/**
 * KanntanSU Application Class
 * 
 * This is the Application class for KanntanSU. It initializes the
 * global ksuApp reference used throughout the app for accessing
 * Android context and system services.
 * 
 * This class should be registered in AndroidManifest.xml:
 * <application android:name=".KernelSUApplication" ...>
 */
class KernelSUApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize global context reference
        ksuApp = this
    }
}
