package com.example.di

import android.app.Application
import android.content.Context
import com.example.MainActivity
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiRepository
import com.example.data.db.MedusaDatabase
import com.example.data.repository.AiLearningContextRepository
import com.example.data.repository.AiMemoryRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.ParcelRepository
import com.example.ui.MedusaViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * Componente principal de Inyección de Dependencias Dagger / Hilt para el Sistema Medusa.
 * Administra el ciclo de vida Singleton de la base de datos Room, los repositorios de memoria
 * y el servicio de IA generativa Gemini.
 */
@Singleton
@Component(
    modules = [
        ContextModule::class,
        DatabaseModule::class,
        NetworkModule::class,
        RepositoryModule::class
    ]
)
interface AppComponent {

    fun inject(activity: MainActivity)
    fun inject(application: Application)

    // Base de datos y DAOs
    fun medusaDatabase(): MedusaDatabase

    // Servicios de IA
    fun geminiApiService(): GeminiApiService
    fun geminiRepository(): GeminiRepository
    fun googleAiGeminiRepository(): com.example.data.repository.GoogleAiGeminiRepository

    // Repositorios de datos y memoria
    fun aiMemoryRepository(): AiMemoryRepository
    fun aiLearningContextRepository(): AiLearningContextRepository
    fun parcelRepository(): ParcelRepository
    fun firebaseAuthRepository(): FirebaseAuthRepository

    // ViewModel Factory helper
    fun medusaViewModel(): MedusaViewModel

    @Component.Factory
    interface Factory {
        fun create(
            contextModule: ContextModule,
            databaseModule: DatabaseModule,
            networkModule: NetworkModule,
            repositoryModule: RepositoryModule
        ): AppComponent
    }
}
