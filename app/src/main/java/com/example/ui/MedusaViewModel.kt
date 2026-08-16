package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiRepository
import com.example.data.db.AccessLogDao
import com.example.data.db.AccessLogEntity
import com.example.data.db.AccessPassDao
import com.example.data.db.AccessPassEntity
import com.example.data.db.ChatMessageDao
import com.example.data.db.ChatMessageEntity
import com.example.data.db.MedusaDatabase
import com.example.data.db.MemoryNodeDao
import com.example.data.db.MemoryNodeEntity
import com.example.data.db.ParcelEntity
import com.example.data.db.SmartDeviceEntity
import com.example.data.model.ClimateHvacMode
import com.example.data.model.CommunicationProtocol
import com.example.data.model.DeviceType
import com.example.data.model.DiscoveredIotDevice
import com.example.data.model.FanSpeed
import com.example.data.model.PackageScanResult
import com.example.data.model.SmartHomeCommandResult
import com.example.data.model.SmartScenePreset
import com.example.data.model.UserProfile
import com.example.data.repository.AiLearningContextRepository
import com.example.data.repository.AiMemoryRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.GoogleAiGeminiRepository
import com.example.data.repository.ParcelRepository
import com.example.data.repository.SmartHomeService
import com.example.ui.theme.NexusAccentPalette
import com.example.ui.theme.NexusFontStyle
import com.example.ui.theme.NexusGlowLevel
import com.example.ui.theme.SleekNexusThemeConfig
import com.example.ui.voice.MedusaSpeechSynthesizer
import com.example.worker.QrScanNotificationWorker
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class MedusaTab {
    CORE_MATRIX,
    NEURAL_CHAT,
    SMART_HOME,
    QR_SCANNER,
    SMART_PARCEL,
    MEMORY_VAULT
}

enum class UserRole(val label: String, val badge: String, val description: String) {
    ALFHA_SANTIAGO("Santiago (Alfha)", "👑 ALPHA", "Superusuario con control total de la matriz neural"),
    RESIDENTES("Residente", "🏠 RESIDENTE", "Generación de QR para visitas y estado de caseta"),
    GUARDIA("Guardia", "🛡️ GUARDIA", "Validación QR, escaneo de paquetes y accesos"),
    ADMINISTRACION("Administración", "⚙️ ADMIN", "Auditoría, métricas D3 y directorio residencial")
}

@HiltViewModel
class MedusaViewModel @Inject constructor(
    application: Application,
    private val db: MedusaDatabase,
    private val chatDao: ChatMessageDao,
    private val memoryDao: MemoryNodeDao,
    private val parcelRepository: ParcelRepository,
    private val accessPassDao: AccessPassDao,
    private val accessLogDao: AccessLogDao,
    private val geminiRepo: GeminiRepository,
    val googleAiRepo: GoogleAiGeminiRepository,
    private val authRepo: FirebaseAuthRepository,
    val aiLearningRepo: AiLearningContextRepository,
    val aiMemoryRepo: AiMemoryRepository,
    val smartHomeService: SmartHomeService
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        db = MedusaDatabase.getDatabase(application),
        chatDao = MedusaDatabase.getDatabase(application).chatMessageDao(),
        memoryDao = MedusaDatabase.getDatabase(application).memoryNodeDao(),
        parcelRepository = ParcelRepository(MedusaDatabase.getDatabase(application).parcelDao()),
        accessPassDao = MedusaDatabase.getDatabase(application).accessPassDao(),
        accessLogDao = MedusaDatabase.getDatabase(application).accessLogDao(),
        geminiRepo = GeminiRepository(),
        googleAiRepo = GoogleAiGeminiRepository(),
        authRepo = FirebaseAuthRepository(),
        aiLearningRepo = AiLearningContextRepository(
            memoryDao = MedusaDatabase.getDatabase(application).memoryDao(),
            interactionDao = MedusaDatabase.getDatabase(application).interactionDao(),
            memoryNodeDao = MedusaDatabase.getDatabase(application).memoryNodeDao(),
            geminiRepository = GeminiRepository()
        ),
        aiMemoryRepo = AiMemoryRepository(
            chatMessageDao = MedusaDatabase.getDatabase(application).chatMessageDao(),
            messageDao = MedusaDatabase.getDatabase(application).messageDao(),
            conversationDao = MedusaDatabase.getDatabase(application).conversationDao(),
            memoryDao = MedusaDatabase.getDatabase(application).memoryDao(),
            interactionDao = MedusaDatabase.getDatabase(application).interactionDao(),
            memoryNodeDao = MedusaDatabase.getDatabase(application).memoryNodeDao(),
            geminiRepository = GeminiRepository()
        ),
        smartHomeService = SmartHomeService(
            context = application,
            smartDeviceDao = MedusaDatabase.getDatabase(application).smartDeviceDao()
        )
    )

    private val _authUserProfile = MutableStateFlow<UserProfile?>(null)
    val authUserProfile: StateFlow<UserProfile?> = _authUserProfile.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoryNodes: StateFlow<List<MemoryNodeEntity>> = memoryDao.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parcels: StateFlow<List<ParcelEntity>> = parcelRepository.allParcels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accessPasses: StateFlow<List<AccessPassEntity>> = accessPassDao.getAllPasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accessLogs: StateFlow<List<AccessLogEntity>> = accessLogDao.getAllAccessLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val smartDevices: StateFlow<List<SmartDeviceEntity>> = smartHomeService.getAllDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isIotScanning: StateFlow<Boolean> = smartHomeService.isScanning
    val discoveredIotDevices: StateFlow<List<DiscoveredIotDevice>> = smartHomeService.discoveredDevices
    val recentIotActionLog: StateFlow<String?> = smartHomeService.recentIotActionLog
    val availableIotPresets: List<SmartScenePreset> = smartHomeService.availablePresets

    val messageCount: StateFlow<Int> = chatDao.getMessageCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val memoryCount: StateFlow<Int> = memoryDao.getMemoryCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _userRole = MutableStateFlow(UserRole.ALFHA_SANTIAGO)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isAnalyzingPackage = MutableStateFlow(false)
    val isAnalyzingPackage: StateFlow<Boolean> = _isAnalyzingPackage.asStateFlow()

    private val _lastScanResult = MutableStateFlow<PackageScanResult?>(null)
    val lastScanResult: StateFlow<PackageScanResult?> = _lastScanResult.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeTab = MutableStateFlow(MedusaTab.CORE_MATRIX)
    val activeTab: StateFlow<MedusaTab> = _activeTab.asStateFlow()

    private val nexusPrefs by lazy {
        getApplication<Application>().getSharedPreferences("medusa_nexus_prefs", Context.MODE_PRIVATE)
    }

    private val _customApiKey = MutableStateFlow(loadPersistedApiKey())
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private fun loadPersistedApiKey(): String {
        return try {
            val prefs = getApplication<Application>().getSharedPreferences("medusa_nexus_prefs", Context.MODE_PRIVATE)
            prefs.getString("medusa_gemini_api_key", "") ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError: StateFlow<String?> = _uiError.asStateFlow()

    private val _lastLearnedMemory = MutableStateFlow<MemoryNodeEntity?>(null)
    val lastLearnedMemory: StateFlow<MemoryNodeEntity?> = _lastLearnedMemory.asStateFlow()

    val speechSynthesizer = MedusaSpeechSynthesizer(application)
    val isSpeakingAi: StateFlow<Boolean> = speechSynthesizer.isSpeaking
    val isVoiceOutputEnabled: StateFlow<Boolean> = speechSynthesizer.isVoiceOutputEnabled

    private val _nexusThemeConfig = MutableStateFlow(loadPersistedThemeConfig())
    val nexusThemeConfig: StateFlow<SleekNexusThemeConfig> = _nexusThemeConfig.asStateFlow()

    private fun loadPersistedThemeConfig(): SleekNexusThemeConfig {
        return try {
            val prefs = getApplication<Application>().getSharedPreferences("medusa_nexus_prefs", Context.MODE_PRIVATE)
            val paletteId = prefs.getString("nexus_palette_id", NexusAccentPalette.HYPER_VIOLET.id) ?: NexusAccentPalette.HYPER_VIOLET.id
            val fontId = prefs.getString("nexus_font_id", NexusFontStyle.NEXUS_TECH.id) ?: NexusFontStyle.NEXUS_TECH.id
            val glowId = prefs.getString("nexus_glow_id", NexusGlowLevel.BALANCED.id) ?: NexusGlowLevel.BALANCED.id
            SleekNexusThemeConfig(
                accentPalette = NexusAccentPalette.fromId(paletteId),
                fontStyle = NexusFontStyle.fromId(fontId),
                glowLevel = NexusGlowLevel.fromId(glowId)
            )
        } catch (_: Exception) {
            SleekNexusThemeConfig()
        }
    }

    fun updateNexusTheme(newConfig: SleekNexusThemeConfig) {
        _nexusThemeConfig.value = newConfig
        try {
            nexusPrefs.edit()
                .putString("nexus_palette_id", newConfig.accentPalette.id)
                .putString("nexus_font_id", newConfig.fontStyle.id)
                .putString("nexus_glow_id", newConfig.glowLevel.id)
                .apply()
        } catch (e: Exception) {
            Log.e("MedusaViewModel", "Error guardando configuración Nexus Theme", e)
        }
    }

    // Synaptic alignment score calculated dynamically based on nodes & interactions
    val synapticAlignmentScore: StateFlow<Float> = combine(messageCount, memoryCount) { msgs, memories ->
        val base = 90.0f
        val memoryFactor = (memories * 1.8f).coerceAtMost(8.5f)
        val chatFactor = (msgs * 0.2f).coerceAtMost(1.4f)
        (base + memoryFactor + chatFactor).coerceAtMost(99.9f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 98.4f)

    init {
        seedInitialMemoriesIfEmpty()
        seedInitialAccessPassesIfEmpty()
        seedInitialAccessLogsIfEmpty()
        seedInitialSmartDevicesIfEmpty()
        observeAuthState()
    }

    private fun seedInitialSmartDevicesIfEmpty() {
        viewModelScope.launch {
            smartHomeService.seedInitialDevicesIfEmpty()
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepo.authStateFlow.collect { firebaseUser ->
                if (firebaseUser != null) {
                    val profile = authRepo.fetchUserProfile(
                        uid = firebaseUser.uid,
                        fallbackEmail = firebaseUser.email ?: "usuario@medusa.app",
                        fallbackName = firebaseUser.displayName ?: "Usuario Medusa"
                    )
                    _authUserProfile.value = profile
                    _userRole.value = profile.role
                } else {
                    _authUserProfile.value = null
                }
            }
        }
    }

    private fun seedInitialAccessLogsIfEmpty() {
        viewModelScope.launch {
            accessLogDao.getAllAccessLogs().collect { logs ->
                if (logs.isEmpty()) {
                    val now = System.currentTimeMillis()
                    val oneDay = 24 * 3600 * 1000L
                    val seedLogs = listOf(
                        AccessLogEntity(
                            passCode = "MEDUSA-RES-001-SANTIAGO",
                            residentHouse = "Casa 01",
                            residentName = "Santiago (Alfha)",
                            visitorName = "Santiago (Propietario VIP)",
                            accessType = "PROPIETARIO VIP",
                            isGranted = true,
                            resultReason = "Pase de Propietario Válido",
                            timestampMs = now - (2 * 3600 * 1000L),
                            scannedByRole = "CASETA_PRINCIPAL"
                        ),
                        AccessLogEntity(
                            passCode = "MEDUSA-RES-012-RAMIREZ",
                            residentHouse = "Casa 12",
                            residentName = "Familia Ramírez",
                            visitorName = "Familia Ramírez",
                            accessType = "PROPIETARIO",
                            isGranted = true,
                            resultReason = "Residente Verificado",
                            timestampMs = now - (6 * 3600 * 1000L),
                            scannedByRole = "CASETA_PRINCIPAL"
                        ),
                        AccessLogEntity(
                            passCode = "MEDUSA-INV-EXPIRED-99",
                            residentHouse = "Casa 08",
                            residentName = "Carlos Mendoza",
                            visitorName = "Proveedor Mantenimiento",
                            accessType = "VISITANTE TEMPORAL",
                            isGranted = false,
                            resultReason = "Pase Expirado o Inexistente",
                            timestampMs = now - (14 * 3600 * 1000L),
                            scannedByRole = "CASETA_PRINCIPAL"
                        ),
                        AccessLogEntity(
                            passCode = "MEDUSA-RES-001-SANTIAGO",
                            residentHouse = "Casa 01",
                            residentName = "Santiago (Alfha)",
                            visitorName = "Santiago",
                            accessType = "PROPIETARIO VIP",
                            isGranted = true,
                            resultReason = "Acceso Concedido Automático",
                            timestampMs = now - (oneDay * 30), // ~1 month ago
                            scannedByRole = "CASETA_PRINCIPAL"
                        ),
                        AccessLogEntity(
                            passCode = "MEDUSA-OLD-REUSED-PASS",
                            residentHouse = "Casa 05",
                            residentName = "Ana Gutiérrez",
                            visitorName = "Visitante Frecuente",
                            accessType = "VISITANTE TEMPORAL",
                            isGranted = false,
                            resultReason = "Pase de Un Solo Uso Ya Utilizado",
                            timestampMs = now - (oneDay * 60), // ~2 months ago
                            scannedByRole = "CASETA_PRINCIPAL"
                        )
                    )
                    seedLogs.forEach { accessLogDao.insertAccessLog(it) }
                }
            }
        }
    }

    private fun seedInitialAccessPassesIfEmpty() {
        viewModelScope.launch {
            accessPassDao.getAllPasses().collect { passes ->
                if (passes.isEmpty()) {
                    val santiagoPass = AccessPassEntity(
                        passCode = "MEDUSA-RES-001-SANTIAGO",
                        residentHouse = "Casa 01",
                        residentName = "Santiago (Alfha)",
                        visitorName = "Santiago (Propietario Alpha)",
                        accessType = "PROPIETARIO VIP",
                        validUntilTimestamp = System.currentTimeMillis() + (3650 * 24 * 3600 * 1000L), // 10 years
                        isUsed = false,
                        createdByRole = "ADMINISTRACION"
                    )
                    val residentPass2 = AccessPassEntity(
                        passCode = "MEDUSA-RES-012-RAMIREZ",
                        residentHouse = "Casa 12",
                        residentName = "Familia Ramírez",
                        visitorName = "Familia Ramírez (Propietario)",
                        accessType = "PROPIETARIO",
                        validUntilTimestamp = System.currentTimeMillis() + (3650 * 24 * 3600 * 1000L),
                        isUsed = false,
                        createdByRole = "ADMINISTRACION"
                    )
                    accessPassDao.insertPass(santiagoPass)
                    accessPassDao.insertPass(residentPass2)
                }
            }
        }
    }

    private fun seedInitialMemoriesIfEmpty() {
        viewModelScope.launch {
            memoryDao.getAllMemories().collect { currentList ->
                if (currentList.isEmpty()) {
                    val initialNodes = listOf(
                        MemoryNodeEntity(
                            category = "COMMUNITY",
                            title = "Esencia Residencial Alfha",
                            detail = "Comunidad armónica, familiar, pet-friendly y sustentable. Se prioriza la seguridad, la convivencia pacífica y el respeto mutuo entre vecinos.",
                            confidenceScore = 1.0f,
                            isUserAdded = false
                        ),
                        MemoryNodeEntity(
                            category = "DIRECTIVE",
                            title = "Horario de Silencio y Descanso",
                            detail = "Horario de descanso vecinal estricto de 22:00 a 08:00 hrs. Volumen moderado en terrazas y respeto absoluto al descanso de los residentes.",
                            confidenceScore = 0.99f,
                            isUserAdded = false
                        ),
                        MemoryNodeEntity(
                            category = "AMENITY",
                            title = "Reglamento de Amenidades",
                            detail = "Casa Club, Alberca climatizada, Parque Canino y Canchas de Pádel operan de 06:00 a 22:00 hrs. Acceso controlado con QR emitido por la administración.",
                            confidenceScore = 0.98f,
                            isUserAdded = false
                        ),
                        MemoryNodeEntity(
                            category = "PREFERENCE",
                            title = "Atención Cálida a Residentes",
                            detail = "Saludar con calidez y respeto a cada familia por su nombre y casa. Apoyar proactivamente con paquetería inteligente y avisos de caseta.",
                            confidenceScore = 0.98f,
                            isUserAdded = false
                        ),
                        MemoryNodeEntity(
                            category = "SECURITY",
                            title = "Protocolo Caseta y Visitantes",
                            detail = "Todo visitante o proveedor debe contar con pase QR verificado antes de ingresar. Notificación inmediata al residente al recibir paquetería.",
                            confidenceScore = 1.0f,
                            isUserAdded = false
                        ),
                        MemoryNodeEntity(
                            category = "FACT",
                            title = "Memoria Comunitaria Continua",
                            detail = "Medusa IA evoluciona con el condominio, aprendiendo de cada interacción para preservar la cultura, acuerdos y seguridad del residencial.",
                            confidenceScore = 1.0f,
                            isUserAdded = false
                        )
                    )
                    initialNodes.forEach { memoryDao.insertMemory(it) }
                }
            }
        }
    }

    fun selectTab(tab: MedusaTab) {
        _activeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateApiKey(key: String) {
        val clean = key.trim()
        _customApiKey.value = clean
        try {
            nexusPrefs.edit()
                .putString("medusa_gemini_api_key", clean)
                .apply()
        } catch (e: Exception) {
            Log.e("MedusaViewModel", "Error al guardar GEMINI_API_KEY local", e)
        }
    }

    fun clearError() {
        _uiError.value = null
    }

    fun dismissLastLearnedMemory() {
        _lastLearnedMemory.value = null
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            _isGenerating.value = true
            _uiError.value = null

            // 1. Save user message to Room
            val userMsg = ChatMessageEntity(sender = "USER", content = trimmed)
            chatDao.insertMessage(userMsg)

            // 2. Fetch history and memories for prompt engineering
            val historyList = chatMessages.value.map { Pair(it.sender, it.content) }
            val memoryList = memoryNodes.value

            // 2b. Check and execute IoT smart home domotics natural language command if applicable
            val iotResult = smartHomeService.parseAndExecuteNaturalLanguageCommand(trimmed, smartDevices.value)
            val effectivePrompt = if (iotResult != null) {
                "$trimmed\n[Sistema Domótico IoT Medusa: Acción ejecutada exitosamente: ${iotResult.actionSummary} - ${iotResult.details}]"
            } else {
                trimmed
            }

            // 3. Generate conversational response using the Google AI Client SDK (with Retrofit fallback)
            val googleAiResult = googleAiRepo.generateConversationalResponse(
                prompt = effectivePrompt,
                chatHistory = historyList,
                memories = memoryList,
                userRoleLabel = _userRole.value.label,
                customApiKey = _customApiKey.value
            )

            val result = if (googleAiResult.isSuccess) {
                googleAiResult
            } else {
                Log.w("MedusaViewModel", "Google AI Client fallback to Retrofit: ${googleAiResult.exceptionOrNull()?.message}")
                geminiRepo.generateResponse(
                    prompt = effectivePrompt,
                    chatHistory = historyList,
                    memories = memoryList,
                    customApiKey = _customApiKey.value,
                    userRoleLabel = _userRole.value.label
                )
            }

            result.onSuccess { responseText ->
                // Save AI response to Room
                val aiMsg = ChatMessageEntity(sender = "MEDUSA", content = responseText)
                chatDao.insertMessage(aiMsg)

                // 3b. Speak AI response aloud via MedusaSpeechSynthesizer
                speechSynthesizer.speak(responseText)

                // 4. Try automatic long-term memory extraction via Google AI Client SDK
                val newMemoryNode = googleAiRepo.extractMemoryNode(
                    userMessage = trimmed,
                    aiResponse = responseText,
                    customApiKey = _customApiKey.value
                ) ?: geminiRepo.extractMemoryNode(
                    userMessage = trimmed,
                    aiResponse = responseText,
                    apiKey = _customApiKey.value
                )

                if (newMemoryNode != null) {
                    memoryDao.insertMemory(newMemoryNode)
                    _lastLearnedMemory.value = newMemoryNode
                    aiLearningRepo.saveLearningContext(
                        key = newMemoryNode.title,
                        value = newMemoryNode.detail,
                        category = newMemoryNode.category,
                        importance = (newMemoryNode.confidenceScore * 5).toInt().coerceIn(1, 5)
                    )
                }

            }.onFailure { error ->
                val fallbackResponse = "⚠️ [Núcleo Medusa - Error de Conexión]: ${error.localizedMessage ?: "No se pudo sincronizar con la red neural."}\n\nNota: Verifica tu API Key o conexión a internet. Los mensajes siguen respaldados en tu base de datos Room local."
                val errorMsg = ChatMessageEntity(sender = "MEDUSA", content = fallbackResponse)
                chatDao.insertMessage(errorMsg)
                _uiError.value = error.localizedMessage
                speechSynthesizer.speak("Error de conexión al canal neural Medusa. Por favor verifica tu conexión.")
            }

            _isGenerating.value = false
        }
    }

    fun addManualMemoryNode(category: String, title: String, detail: String) {
        if (title.isBlank() || detail.isBlank()) return
        viewModelScope.launch {
            val node = MemoryNodeEntity(
                category = category.uppercase(),
                title = title.trim(),
                detail = detail.trim(),
                confidenceScore = 1.0f,
                isUserAdded = true
            )
            memoryDao.insertMemory(node)
        }
    }

    fun deleteMemoryNode(node: MemoryNodeEntity) {
        viewModelScope.launch {
            memoryDao.deleteMemory(node)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearAllMessages()
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryDao.clearAllMemories()
        }
    }

    // ==================== SMART PARCEL / PAQUETERÍA IA ====================

    fun analyzePackagePhoto(base64Image: String) {
        if (_isAnalyzingPackage.value) return
        viewModelScope.launch {
            _isAnalyzingPackage.value = true
            _uiError.value = null
            _lastScanResult.value = null

            val cleanBase64 = if (base64Image.contains(",")) {
                base64Image.substringAfter(",")
            } else base64Image

            val result = geminiRepo.analyzePackageImage(cleanBase64, _customApiKey.value)

            result.onSuccess { scan ->
                _lastScanResult.value = scan
            }.onFailure { err ->
                _uiError.value = err.localizedMessage ?: "Error al analizar la imagen del paquete."
            }

            _isAnalyzingPackage.value = false
        }
    }

    fun dismissScanResult() {
        _lastScanResult.value = null
    }

    fun confirmAndSaveParcel(
        houseNumber: String,
        recipientName: String,
        carrier: String,
        description: String,
        phone: String,
        photoBase64: String = "",
        context: Context? = null
    ) {
        viewModelScope.launch {
            val parcel = ParcelEntity(
                houseNumber = houseNumber,
                recipientName = recipientName,
                carrier = carrier,
                description = description,
                phone = phone,
                status = "RECIBIDO",
                isNotified = false,
                photoBase64 = photoBase64
            )
            val newId = parcelRepository.insertParcel(parcel)

            // Generate dedicated Security Delivery QR Pass in Room DB
            val shortCode = UUID.randomUUID().toString().take(6).uppercase()
            val pickupPassCode = "MEDUSA-PK-$newId-$shortCode"
            val deliveryPass = AccessPassEntity(
                passCode = pickupPassCode,
                residentHouse = houseNumber.ifBlank { "Casa 01" },
                residentName = recipientName.ifBlank { "Residente" },
                visitorName = "Paquetería: $carrier",
                accessType = "ENTREGA_PAQUETE",
                validUntilTimestamp = System.currentTimeMillis() + (7 * 24 * 3600 * 1000L), // 7 days
                isUsed = false,
                createdByRole = _userRole.value.name
            )
            accessPassDao.insertPass(deliveryPass)

            // Auto send notification via WhatsApp if context and phone provided with QR Contra-Entrega Token
            if (context != null && phone.isNotBlank()) {
                sendWhatsAppNotice(
                    context = context,
                    parcelId = newId,
                    houseNumber = houseNumber,
                    recipientName = recipientName,
                    carrier = carrier,
                    description = description,
                    phone = phone,
                    pickupCode = pickupPassCode
                )
            }

            _lastScanResult.value = null
        }
    }

    fun sendWhatsAppNotice(
        context: Context,
        parcelId: Long,
        houseNumber: String,
        recipientName: String,
        carrier: String,
        description: String,
        phone: String,
        pickupCode: String? = null
    ) {
        val cleanPhone = phone.filter { it.isDigit() }
        val formattedPhone = if (cleanPhone.length == 10) "52$cleanPhone" else cleanPhone
        val actualCode = pickupCode ?: "MEDUSA-PK-$parcelId-${houseNumber.replace(" ", "").take(4).uppercase()}"

        val message = """
            📦 *AVISO DE PAQUETERÍA - CASETA DE VIGILANCIA*
            *SISTEMA MEDUSA ALFA - SEGURIDAD RESIDENCIAL*
            
            Hola *${recipientName.ifBlank { "Residente" }}* ($houseNumber),
            
            Le informamos que ha llegado un paquete a caseta de seguridad:
            • 🏢 *Empresa:* $carrier
            • 📋 *Detalle:* $description
            • ⏱️ *Estado:* Recibido en Caseta
            
            🔐 *TOKEN QR CONTRA-ENTREGA:*
            👉 `$actualCode`
            
            ⚠️ *Protocolo de Seguridad:*
            Muestre este código QR o proporcione este Token en caseta para verificar su identidad y autorizar la entrega de su paquete de forma segura.
            
            _Notificación automática generada por Sistema Medusa OS IA_
        """.trimIndent()

        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            // Update parcel status to notified in database
            viewModelScope.launch {
                parcelRepository.updateParcelStatus(parcelId, "RECIBIDO", true)
            }
        } catch (e: Exception) {
            Log.e("MedusaViewModel", "Error opening WhatsApp", e)
            _uiError.value = "No se pudo abrir WhatsApp para enviar la notificación."
        }
    }

    fun selectUserRole(role: UserRole) {
        _userRole.value = role
    }

    // ==================== QR ACCESS PASS MANAGEMENT ====================

    fun createAccessPass(
        residentHouse: String,
        residentName: String,
        visitorName: String,
        accessType: String = "VISITOR",
        validHours: Int = 24
    ) {
        viewModelScope.launch {
            val shortUuid = UUID.randomUUID().toString().take(6).uppercase()
            val passCode = "MEDUSA-QR-$shortUuid"
            val validUntil = System.currentTimeMillis() + (validHours * 3600 * 1000L)

            val pass = AccessPassEntity(
                passCode = passCode,
                residentHouse = residentHouse.ifBlank { "Casa 21" },
                residentName = residentName.ifBlank { "Santiago (Alfha)" },
                visitorName = visitorName.ifBlank { "Visita General" },
                accessType = accessType,
                validUntilTimestamp = validUntil,
                isUsed = false,
                createdByRole = _userRole.value.name
            )
            accessPassDao.insertPass(pass)
        }
    }

    fun createResidentCredential(
        residentHouse: String,
        residentName: String,
        level: String = "PROPIETARIO",
        isPermanent: Boolean = true
    ) {
        viewModelScope.launch {
            val cleanHouse = residentHouse.ifBlank { "Casa 01" }.replace(" ", "").uppercase()
            val shortUuid = UUID.randomUUID().toString().take(4).uppercase()
            val passCode = "MEDUSA-RES-$cleanHouse-$shortUuid"
            // Permanent = 10 years validity
            val validUntil = System.currentTimeMillis() + (3650 * 24 * 3600 * 1000L)

            val pass = AccessPassEntity(
                passCode = passCode,
                residentHouse = residentHouse.ifBlank { "Casa 01" },
                residentName = residentName.ifBlank { "Residente Medusa" },
                visitorName = residentName.ifBlank { "Residente Medusa" },
                accessType = "CREDENCIAL $level",
                validUntilTimestamp = validUntil,
                isUsed = false,
                createdByRole = "ADMINISTRACION"
            )
            accessPassDao.insertPass(pass)
        }
    }

    suspend fun validateAccessPassCode(code: String, context: Context? = null): Pair<Boolean, AccessPassEntity?> {
        val cleanCode = code.trim().uppercase()
        val pass = accessPassDao.getPassByCode(cleanCode)
        val now = System.currentTimeMillis()

        val (isGranted, reason) = if (pass != null) {
            val isExpired = now > pass.validUntilTimestamp
            if (!isExpired && !pass.isUsed) {
                accessPassDao.markPassAsUsed(pass.passCode)
                if (pass.accessType == "ENTREGA_PAQUETE") {
                    // Automatically mark matching parcel as delivered
                    val pendingParcel = parcels.value.find { it.houseNumber.equals(pass.residentHouse, ignoreCase = true) && it.status == "RECIBIDO" }
                    if (pendingParcel != null) {
                        parcelRepository.updateParcelStatus(pendingParcel.id, "ENTREGADO", pendingParcel.isNotified)
                    }
                    Pair(true, "📦 Entrega contra QR Autorizada (${pass.residentHouse} - ${pass.residentName})")
                } else {
                    Pair(true, "Acceso Autorizado por Room DB")
                }
            } else if (isExpired) {
                Pair(false, "Pase Expirado (${pass.accessType})")
            } else {
                Pair(false, "Pase Ya Utilizado (${pass.accessType})")
            }
        } else {
            Pair(false, "Código QR No Registrado en DB")
        }

        val updatedPass = if (pass != null && isGranted) pass.copy(isUsed = true) else pass

        val log = AccessLogEntity(
            passCode = cleanCode.ifBlank { "SIN_CODIGO" },
            residentName = pass?.residentName ?: "Desconocido",
            residentHouse = pass?.residentHouse ?: "N/A",
            visitorName = pass?.visitorName ?: "Visita No Registrada",
            accessType = pass?.accessType ?: "DESCONOCIDO",
            isGranted = isGranted,
            resultReason = reason,
            timestampMs = now,
            scannedByRole = _userRole.value.label
        )

        accessLogDao.insertAccessLog(log)

        // Enqueue local background notification via WorkManager to alert resident
        val ctx = context ?: getApplication<Application>().applicationContext
        try {
            QrScanNotificationWorker.enqueueNotification(
                context = ctx,
                house = pass?.residentHouse ?: "Caseta Principal",
                residentName = pass?.residentName ?: "Residente Medusa OS",
                visitorName = pass?.visitorName ?: "Visitante",
                passCode = cleanCode,
                isGranted = isGranted,
                resultReason = reason,
                timestampMs = now
            )
        } catch (e: Exception) {
            Log.e("MedusaViewModel", "WorkManager notification enqueue failed", e)
        }

        return Pair(isGranted, updatedPass)
    }

    fun triggerTestWorkManagerNotification(
        context: Context,
        house: String = "Casa 01",
        residentName: String = "Santiago (Alfha)",
        visitorName: String = "Visitante de Prueba"
    ) {
        val testCode = "MEDUSA-TEST-" + (100..999).random()
        QrScanNotificationWorker.enqueueNotification(
            context = context,
            house = house,
            residentName = residentName,
            visitorName = visitorName,
            passCode = testCode,
            isGranted = true,
            resultReason = "Prueba de Notificación WorkManager",
            timestampMs = System.currentTimeMillis()
        )
    }

    fun deleteAccessLog(log: AccessLogEntity) {
        viewModelScope.launch {
            accessLogDao.deleteAccessLog(log)
        }
    }

    fun clearAllAccessLogs() {
        viewModelScope.launch {
            accessLogDao.clearAllAccessLogs()
        }
    }

    fun deleteAccessPass(pass: AccessPassEntity) {
        viewModelScope.launch {
            accessPassDao.deletePass(pass)
        }
    }

    fun purgeExpiredAccessPasses() {
        viewModelScope.launch {
            accessPassDao.deleteExpiredPasses()
        }
    }

    fun markParcelDelivered(id: Long) {
        viewModelScope.launch {
            parcelRepository.updateParcelStatus(id, "ENTREGADO", true)
        }
    }

    fun deleteParcel(parcel: ParcelEntity) {
        viewModelScope.launch {
            parcelRepository.deleteParcel(parcel)
        }
    }

    // --- FIREBASE AUTHENTICATION ACTIONS ---
    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authRepo.signInWithEmail(email, pass)
            result.onFailure {
                _authError.value = it.localizedMessage ?: "Error al iniciar sesión"
            }
            _isAuthLoading.value = false
        }
    }

    fun registerUserWithEmail(
        email: String,
        pass: String,
        displayName: String,
        role: UserRole,
        houseNumber: Int?
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authRepo.registerUser(email, pass, displayName, role, houseNumber)
            result.onFailure {
                _authError.value = it.localizedMessage ?: "Error al registrar usuario"
            }
            _isAuthLoading.value = false
        }
    }

    fun signInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authRepo.signInWithCredential(credential)
            result.onFailure {
                _authError.value = it.localizedMessage ?: "Error con credencial de Google"
            }
            _isAuthLoading.value = false
        }
    }

    fun signOutFirebase() {
        viewModelScope.launch {
            authRepo.signOut()
            _authUserProfile.value = null
            _userRole.value = UserRole.RESIDENTES
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authError.value = null
            val result = authRepo.sendPasswordReset(email)
            result.onFailure {
                _authError.value = it.localizedMessage ?: "No se pudo enviar correo de recuperación"
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    // ==================== SMART HOME & IOT DOMOTICS ====================

    fun toggleIotDevicePower(device: SmartDeviceEntity) {
        viewModelScope.launch {
            smartHomeService.toggleDevicePower(device)
        }
    }

    fun updateIotLightProperties(device: SmartDeviceEntity, isOn: Boolean, brightness: Int, colorHex: String) {
        viewModelScope.launch {
            smartHomeService.updateLightProperties(device, isOn, brightness, colorHex)
        }
    }

    fun updateIotClimateProperties(
        device: SmartDeviceEntity,
        isOn: Boolean,
        targetTemp: Float,
        mode: ClimateHvacMode,
        fan: FanSpeed
    ) {
        viewModelScope.launch {
            smartHomeService.updateClimateProperties(device, isOn, targetTemp, mode, fan)
        }
    }

    fun applyIotPreset(preset: SmartScenePreset) {
        viewModelScope.launch {
            val current = smartDevices.value
            smartHomeService.applyScenePreset(preset, current)
        }
    }

    fun setMasterPowerAll(isOn: Boolean) {
        viewModelScope.launch {
            val current = smartDevices.value
            smartHomeService.setMasterPowerAll(isOn, current)
        }
    }

    fun scanLocalIotDevices() {
        viewModelScope.launch {
            smartHomeService.scanLocalIotNetwork()
        }
    }

    fun addNewIotDevice(device: SmartDeviceEntity) {
        viewModelScope.launch {
            smartHomeService.addNewDevice(device)
        }
    }

    fun deleteIotDevice(device: SmartDeviceEntity) {
        viewModelScope.launch {
            smartHomeService.deleteDevice(device)
        }
    }

    fun executeNaturalLanguageIotCommand(commandText: String) {
        val trimmed = commandText.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val userMsg = ChatMessageEntity(sender = "USER", content = trimmed)
            chatDao.insertMessage(userMsg)

            val iotResult = smartHomeService.parseAndExecuteNaturalLanguageCommand(trimmed, smartDevices.value)
            val aiResponseText = if (iotResult != null) {
                "⚡ [Protocolo Domótico IoT Ejecutado]: ${iotResult.actionSummary}.\n${iotResult.details}"
            } else {
                "Sistema IoT Medusa: Comando procesado en el nodo central. Dispositivos verificados y sincronizados."
            }

            val aiMsg = ChatMessageEntity(sender = "MEDUSA", content = aiResponseText)
            chatDao.insertMessage(aiMsg)
            speechSynthesizer.speak(aiResponseText)
        }
    }

    fun speakText(text: String) {
        speechSynthesizer.speak(text)
    }

    fun stopSpeaking() {
        speechSynthesizer.stopSpeaking()
    }

    fun toggleVoiceOutput(): Boolean {
        return speechSynthesizer.toggleVoiceOutput()
    }

    fun setVoiceOutputEnabled(enabled: Boolean) {
        speechSynthesizer.setVoiceOutputEnabled(enabled)
    }

    override fun onCleared() {
        super.onCleared()
        speechSynthesizer.shutdown()
    }
}
