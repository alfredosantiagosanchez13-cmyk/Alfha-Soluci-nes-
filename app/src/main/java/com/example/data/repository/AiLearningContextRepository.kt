package com.example.data.repository

import com.alfredo.medusaalfha.data.repository.AiLearningContextRepository as CoreAiLearningContextRepository
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiRepository
import com.example.data.api.RetrofitClient
import com.example.data.db.InteractionDao
import com.example.data.db.MemoryDao
import com.example.data.db.MemoryNodeDao

/**
 * Proxy de compatibilidad en el paquete com.example.data.repository
 * para AiLearningContextRepository.
 */
typealias AiLearningContextRepository = CoreAiLearningContextRepository
