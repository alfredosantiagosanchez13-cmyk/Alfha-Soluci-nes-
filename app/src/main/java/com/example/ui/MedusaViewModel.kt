package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiRepository
import com.example.data.db.AccessLogEntity
import com.example.data.db.AccessPassEntity
import com.example.data.db.ChatMessageEntity
import com.example.data.db.MedusaDatabase
import com.example.data.db.MemoryNodeEntity
import com.example.data.db.ParcelEntity
import com.example.data.model.PackageScanResult
import com.example.data.repository.ParcelRepository
import com.example.worker.QrScanNotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class MedusaTab {
    CORE_MATRIX,
    NEURAL_CHAT,
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

class MedusaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MedusaDatabase.getDatabase(application)
    private val chatDao = db.chatMessageDao()
    private val memoryDao = db.memoryNodeDao()
    private val parcelRepository = ParcelRepository(db.parcelDao())
    private val accessPassDao = db.accessPassDao()
    private val accessLogDao = db.accessLogDao()
    private val geminiRepo = GeminiRepository()

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

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError: StateFlow<String?> = _uiError.asStateFlow()

    private val _lastLearnedMemory = MutableStateFlow<MemoryNodeEntity?>(null)
    val lastLearnedMemory: StateFlow<MemoryNodeEntity?> = _lastLearnedMemory.asStateFlow()

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
                            category = "PREFERENCE",
                            title = "Estética Medusa OS",
                            detail = "Preferencia confirmada por interfaz oscura futurista Sleek Nexus con brillo violeta.",
                            confidenceScore = 0.99f,
                            isUserAdded = false
                        ),
                        MemoryNodeEntity(
                            category = "DIRECTIVE",
                            title = "Protocolo Nexus v4.2",
                            detail = "Respuesta analítica, concisa y leal al usuario. Prioridad máxima en persistencia de contexto.",
                            confidenceScore = 0.98f,
                            isUserAdded = false
                        ),
                        MemoryNodeEntity(
                            category = "FACT",
                            title = "Sistema Medusa OS",
                            detail = "Núcleo de inteligencia con base de datos local Room para persistencia de conversaciones y memoria a largo plazo.",
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
        _customApiKey.value = key
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

            // 3. Generate response from Gemini API
            val result = geminiRepo.generateResponse(
                prompt = trimmed,
                chatHistory = historyList,
                memories = memoryList,
                customApiKey = _customApiKey.value,
                userRoleLabel = _userRole.value.label
            )

            result.onSuccess { responseText ->
                // Save AI response to Room
                val aiMsg = ChatMessageEntity(sender = "MEDUSA", content = responseText)
                chatDao.insertMessage(aiMsg)

                // 4. Try automatic long-term memory extraction
                val newMemoryNode = geminiRepo.extractMemoryNode(
                    userMessage = trimmed,
                    aiResponse = responseText,
                    apiKey = _customApiKey.value
                )

                if (newMemoryNode != null) {
                    memoryDao.insertMemory(newMemoryNode)
                    _lastLearnedMemory.value = newMemoryNode
                }

            }.onFailure { error ->
                val fallbackResponse = "⚠️ [Núcleo Medusa - Error de Conexión]: ${error.localizedMessage ?: "No se pudo sincronizar con la red neural."}\n\nNota: Verifica tu API Key o conexión a internet. Los mensajes siguen respaldados en tu base de datos Room local."
                val errorMsg = ChatMessageEntity(sender = "MEDUSA", content = fallbackResponse)
                chatDao.insertMessage(errorMsg)
                _uiError.value = error.localizedMessage
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

            // Auto send notification via WhatsApp if context and phone provided
            if (context != null && phone.isNotBlank()) {
                sendWhatsAppNotice(context, newId, houseNumber, recipientName, carrier, description, phone)
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
        phone: String
    ) {
        val cleanPhone = phone.filter { it.isDigit() }
        val formattedPhone = if (cleanPhone.length == 10) "52$cleanPhone" else cleanPhone

        val message = """
            📦 *AVISO DE PAQUETERÍA - CASETA DE VIGILANCIA*
            
            Hola *${recipientName.ifBlank { "Residente" }}* ($houseNumber),
            
            Le informamos que ha llegado un paquete a caseta:
            • *Empresa:* $carrier
            • *Descripción:* $description
            • *Estado:* Listo para recolección
            
            _Notificado automáticamente por Sistema Medusa OS IA_
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
                Pair(true, "Acceso Autorizado por Room DB")
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
}
