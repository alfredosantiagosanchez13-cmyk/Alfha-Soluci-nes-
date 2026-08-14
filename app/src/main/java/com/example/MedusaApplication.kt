package com.example

import android.app.Application
import android.util.Log
import com.example.di.AppComponent
import com.example.di.ContextModule
import com.example.di.DaggerAppComponent
import com.example.di.DatabaseModule
import com.example.di.NetworkModule
import com.example.di.RepositoryModule
import com.google.firebase.FirebaseApp
import net.sqlcipher.database.SQLiteDatabase

class MedusaApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize SQLCipher native binaries
        try {
            SQLiteDatabase.loadLibs(this)
            Log.d("MedusaApplication", "SQLCipher loaded successfully")
        } catch (e: Throwable) {
            Log.w("MedusaApplication", "SQLCipher pre-init notice: ${e.message}")
        }

        // 2. Initialize FirebaseApp safely
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("MedusaApplication", "FirebaseApp initialized successfully")
            }
        } catch (e: Throwable) {
            Log.w("MedusaApplication", "FirebaseApp initialization handled: ${e.message}")
        }

        // 3. Initialize Dagger / Hilt Dependency Injection Component
        try {
            appComponent = DaggerAppComponent.factory().create(
                contextModule = ContextModule(this),
                databaseModule = DatabaseModule,
                networkModule = NetworkModule,
                repositoryModule = RepositoryModule
            )
            Log.d("MedusaApplication", "AppComponent initialized successfully with Room & AI Services")
        } catch (e: Throwable) {
            Log.e("MedusaApplication", "Error initializing AppComponent", e)
        }
    }
}
