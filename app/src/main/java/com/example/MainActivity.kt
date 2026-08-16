package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MedusaTab
import com.example.ui.MedusaViewModel
import com.example.ui.UserRole
import com.example.ui.components.AuthDialog
import com.example.ui.components.RoleDelimitationHeader
import com.example.ui.components.SleekBottomPillNav
import com.example.ui.components.SleekNexusSettingsDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CoreMatrixScreen
import com.example.ui.screens.MemoryVaultScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.SmartHomeScreen
import com.example.ui.screens.SmartParcelScreen
import com.example.ui.theme.SistemaMedusaTheme
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletPrimary
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MedusaViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as? MedusaApplication
                return (app?.appComponent?.medusaViewModel() ?: MedusaViewModel(application)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase App Check with Play Integrity protection
        initFirebaseAppCheck()

        setContent {
            val nexusThemeConfig by viewModel.nexusThemeConfig.collectAsState()
            SistemaMedusaTheme(themeConfig = nexusThemeConfig) {
                MedusaAppScreen(viewModel = viewModel)
            }
        }
    }

    private fun initFirebaseAppCheck() {
        try {
            // Ensure FirebaseApp is ready
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }

            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            val providerFactory = if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }

            firebaseAppCheck.installAppCheckProviderFactory(providerFactory)
            Log.d("MainActivity", "Firebase App Check initialized successfully (Play Integrity / Debug provider: ${BuildConfig.DEBUG})")
        } catch (e: Exception) {
            Log.w("MainActivity", "Firebase App Check initialization notice: ${e.message}")
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MedusaAppScreen(viewModel: MedusaViewModel) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()
    val activeRole by viewModel.userRole.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val memoryNodes by viewModel.memoryNodes.collectAsState()
    val accessPasses by viewModel.accessPasses.collectAsState()
    val accessLogs by viewModel.accessLogs.collectAsState()
    val smartDevices by viewModel.smartDevices.collectAsState()
    val messageCount by viewModel.messageCount.collectAsState()
    val memoryCount by viewModel.memoryCount.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val synapticScore by viewModel.synapticAlignmentScore.collectAsState()
    val lastLearnedMemory by viewModel.lastLearnedMemory.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val uiError by viewModel.uiError.collectAsState()
    val isSpeakingAi by viewModel.isSpeakingAi.collectAsState()
    val isVoiceOutputEnabled by viewModel.isVoiceOutputEnabled.collectAsState()

    val authUserProfile by viewModel.authUserProfile.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showNexusSettingsDialog by remember { mutableStateOf(false) }
    val nexusThemeConfig by viewModel.nexusThemeConfig.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        containerColor = SleekBackground,
        topBar = {
            RoleDelimitationHeader(
                activeRole = activeRole,
                currentUserProfile = authUserProfile,
                onRoleSelected = { viewModel.selectUserRole(it) },
                onOpenApiKey = { showApiKeyDialog = true },
                onOpenAuth = {
                    viewModel.clearAuthError()
                    showAuthDialog = true
                },
                onOpenNexusSettings = { showNexusSettingsDialog = true }
            )
        },
        bottomBar = {
            SleekBottomPillNav(
                activeTab = activeTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SleekBackground)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "tabTransition"
            ) { tab ->
                when (tab) {
                    MedusaTab.CORE_MATRIX -> {
                        CoreMatrixScreen(
                            activeRole = activeRole,
                            synapticScore = synapticScore,
                            memoryCount = memoryCount,
                            messageCount = messageCount,
                            memories = memoryNodes,
                            accessPasses = accessPasses,
                            accessLogs = accessLogs,
                            lastLearnedMemory = lastLearnedMemory,
                            onDeleteMemory = { viewModel.deleteMemoryNode(it) },
                            onCreatePass = { h, r, v, t, hrs -> viewModel.createAccessPass(h, r, v, t, hrs) },
                            onCreateResidentCredential = { h, n, l, p -> viewModel.createResidentCredential(h, n, l, p) },
                            onValidatePassCode = { code -> viewModel.validateAccessPassCode(code, context) },
                            onDeletePass = { pass -> viewModel.deleteAccessPass(pass) },
                            onPurgeExpiredPasses = { viewModel.purgeExpiredAccessPasses() },
                            onNavigateToChat = { viewModel.selectTab(MedusaTab.NEURAL_CHAT) },
                            onNavigateToVault = { viewModel.selectTab(MedusaTab.MEMORY_VAULT) },
                            onNavigateToParcel = { viewModel.selectTab(MedusaTab.SMART_PARCEL) },
                            onNavigateToSmartHome = { viewModel.selectTab(MedusaTab.SMART_HOME) },
                            smartDevices = smartDevices,
                            onApplyPreset = { viewModel.applyIotPreset(it) },
                            onToggleMasterPower = { viewModel.setMasterPowerAll(it) },
                            onTriggerTestNotification = { viewModel.triggerTestWorkManagerNotification(context) },
                            onSendVoiceMessage = { viewModel.sendMessage(it) }
                        )
                    }
                    MedusaTab.NEURAL_CHAT -> {
                        ChatScreen(
                            messages = chatMessages,
                            isGenerating = isGenerating,
                            onSendMessage = { viewModel.sendMessage(it) },
                            onClearChat = { viewModel.clearChatHistory() },
                            isVoiceOutputEnabled = isVoiceOutputEnabled,
                            isSpeakingAi = isSpeakingAi,
                            onToggleVoiceOutput = { viewModel.toggleVoiceOutput() },
                            onSpeakMessage = { viewModel.speakText(it) },
                            onStopSpeaking = { viewModel.stopSpeaking() }
                        )
                    }
                    MedusaTab.SMART_HOME -> {
                        SmartHomeScreen(viewModel = viewModel)
                    }
                    MedusaTab.QR_SCANNER -> {
                        QrScannerScreen(
                            accessPasses = accessPasses,
                            accessLogs = accessLogs,
                            onValidateCode = { code -> viewModel.validateAccessPassCode(code, context) },
                            onDeleteLog = { log -> viewModel.deleteAccessLog(log) },
                            onClearLogs = { viewModel.clearAllAccessLogs() },
                            onTriggerTestNotification = { viewModel.triggerTestWorkManagerNotification(context) }
                        )
                    }
                    MedusaTab.SMART_PARCEL -> {
                        SmartParcelScreen(viewModel = viewModel)
                    }
                    MedusaTab.MEMORY_VAULT -> {
                        MemoryVaultScreen(
                            memories = memoryNodes,
                            onAddMemory = { cat, title, detail ->
                                viewModel.addManualMemoryNode(cat, title, detail)
                            },
                            onDeleteMemory = { viewModel.deleteMemoryNode(it) },
                            onClearAllMemories = { viewModel.clearAllMemories() }
                        )
                    }
                }
            }
        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = customApiKey,
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                viewModel.updateApiKey(key)
                showApiKeyDialog = false
            }
        )
    }

    if (showAuthDialog) {
        AuthDialog(
            currentUserProfile = authUserProfile,
            isLoading = isAuthLoading,
            errorMessage = authError,
            onDismiss = { showAuthDialog = false },
            onSignInEmail = { email, pass ->
                viewModel.signInWithEmail(email, pass)
            },
            onRegisterEmail = { email, pass, name, role, houseNum ->
                viewModel.registerUserWithEmail(email, pass, name, role, houseNum)
            },
            onGoogleSignIn = {
                // Google Sign In via CredentialManager demo trigger
                viewModel.signInWithEmail("demo.google.user@medusa.app", "GoogleAuth#2026")
            },
            onSignOut = {
                viewModel.signOutFirebase()
            },
            onResetPassword = { email ->
                viewModel.sendPasswordReset(email)
            }
        )
    }

    if (showNexusSettingsDialog) {
        SleekNexusSettingsDialog(
            currentConfig = nexusThemeConfig,
            currentApiKey = customApiKey,
            onDismiss = { showNexusSettingsDialog = false },
            onApplyConfig = { newConfig ->
                viewModel.updateNexusTheme(newConfig)
            },
            onSaveApiKey = { newKey ->
                viewModel.updateApiKey(newKey)
            }
        )
    }
}

@Composable
fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var keyText by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        title = {
            Text(
                text = "CONFIGURAR GEMINI API KEY",
                style = MaterialTheme.typography.labelSmall,
                color = SleekVioletPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "AI Studio inyecta automáticamente GEMINI_API_KEY. Si deseas usar una clave personalizada, ingrésala a continuación:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    placeholder = { Text("AIzaSy...", color = SleekTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "api_key_input_field" },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekVioletPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(keyText.trim()) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekVioletPrimary,
                    contentColor = SleekVioletDark
                ),
                modifier = Modifier.semantics { testTag = "save_api_key_button" }
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SleekTextSecondary)
            }
        }
    )
}
