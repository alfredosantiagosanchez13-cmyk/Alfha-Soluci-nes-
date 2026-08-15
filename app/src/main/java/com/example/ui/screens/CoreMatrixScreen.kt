package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AccessLogEntity
import com.example.data.db.AccessPassEntity
import com.example.data.db.MemoryNodeEntity
import com.example.data.db.SmartDeviceEntity
import com.example.data.model.SmartScenePreset
import com.example.ui.UserRole
import com.example.ui.components.AdminResidentManagerCard
import com.example.ui.components.AlphaRootCommandCard
import com.example.ui.components.ApkInstallerGuideCard
import com.example.ui.components.D3MemoryDashboard
import com.example.ui.components.FuturisticHandsFreeVoiceDialog
import com.example.ui.components.GuardAccessConsoleCard
import com.example.ui.components.MemoryNodeCard
import com.example.ui.components.MetricsGridCard
import com.example.ui.components.MonthlyAccessDashboardCard
import com.example.ui.components.ResidentQrGeneratorCard
import com.example.ui.components.SmartHomeSummaryCard
import com.example.ui.components.SynapticAlignmentCard
import com.example.ui.components.WorkManagerNotificationStatusCard
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletPrimary
import com.example.ui.voice.VoiceRecognitionManager
import com.example.worker.QrScanNotificationWorker

@Composable
fun CoreMatrixScreen(
    activeRole: UserRole,
    synapticScore: Float,
    memoryCount: Int,
    messageCount: Int,
    memories: List<MemoryNodeEntity>,
    accessPasses: List<AccessPassEntity>,
    accessLogs: List<AccessLogEntity> = emptyList(),
    lastLearnedMemory: MemoryNodeEntity?,
    onDeleteMemory: (MemoryNodeEntity) -> Unit,
    onCreatePass: (house: String, resident: String, visitor: String, type: String, hours: Int) -> Unit,
    onCreateResidentCredential: (house: String, name: String, level: String, isPermanent: Boolean) -> Unit,
    onValidatePassCode: suspend (String) -> Pair<Boolean, AccessPassEntity?>,
    onDeletePass: (AccessPassEntity) -> Unit,
    onPurgeExpiredPasses: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToParcel: () -> Unit,
    onNavigateToSmartHome: () -> Unit = {},
    smartDevices: List<SmartDeviceEntity> = emptyList(),
    onApplyPreset: (SmartScenePreset) -> Unit = {},
    onToggleMasterPower: (Boolean) -> Unit = {},
    onTriggerTestNotification: () -> Unit = {},
    onSendVoiceMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showVoiceDialog by remember { mutableStateOf(false) }
    val voiceManager = remember { VoiceRecognitionManager(context) }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Role Specific Main Card
        item {
            when (activeRole) {
                UserRole.ALFHA_SANTIAGO -> {
                    AlphaRootCommandCard(
                        currentRole = activeRole,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToVault = onNavigateToVault,
                        onPurgeExpiredPasses = onPurgeExpiredPasses
                    )
                }
                UserRole.ADMINISTRACION -> {
                    AdminResidentManagerCard(
                        passes = accessPasses,
                        onCreateResidentPass = onCreateResidentCredential,
                        onDeletePass = onDeletePass
                    )
                }
                UserRole.RESIDENTES -> {
                    ResidentQrGeneratorCard(
                        passes = accessPasses,
                        onCreatePass = onCreatePass,
                        onDeletePass = onDeletePass
                    )
                }
                UserRole.GUARDIA -> {
                    GuardAccessConsoleCard(
                        onValidateCode = onValidatePassCode,
                        onNavigateToParcelScanner = onNavigateToParcel
                    )
                }
            }
        }

        // Voice Command Action Pill Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(SleekSurfaceVariant, SleekVioletDark)
                        )
                    )
                    .border(1.dp, SleekVioletPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { showVoiceDialog = true }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SleekVioletPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Comando de Voz",
                                tint = SleekVioletDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "INTERFAZ VOCAL MANOS LIBRES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Text(
                                text = "Toca para dictar preguntas, registros o instrucciones",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekVioletPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "HABLAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekVioletPrimary
                        )
                    }
                }
            }
        }

        // 2. Secondary Role Contexts for Santiago / Alpha Root Control
        if (activeRole == UserRole.ALFHA_SANTIAGO) {
            item {
                AdminResidentManagerCard(
                    passes = accessPasses,
                    onCreateResidentPass = onCreateResidentCredential,
                    onDeletePass = onDeletePass
                )
            }
            item {
                ResidentQrGeneratorCard(
                    passes = accessPasses,
                    onCreatePass = onCreatePass,
                    onDeletePass = onDeletePass
                )
            }
            item {
                GuardAccessConsoleCard(
                    onValidateCode = onValidatePassCode,
                    onNavigateToParcelScanner = onNavigateToParcel
                )
            }
        }

        // 3. Synaptic Alignment Card Core
        item {
            SynapticAlignmentCard(
                score = synapticScore,
                memoryCount = memoryCount
            )
        }

        // 4. Memory & Context Metrics Grid
        item {
            MetricsGridCard(
                memoryCount = memoryCount,
                messageCount = messageCount
            )
        }

        // 4b. Interactive D3.js AI Learning & Memory Analytics
        item {
            D3MemoryDashboard(
                memories = memories
            )
        }

        // 4c. Smart Home IoT Condominial Control Summary Card
        item {
            SmartHomeSummaryCard(
                devices = smartDevices,
                onNavigateToSmartHome = onNavigateToSmartHome,
                onApplyPreset = onApplyPreset,
                onToggleMasterPower = onToggleMasterPower
            )
        }

        // 5. Monthly Access Statistics Dashboard
        item {
            MonthlyAccessDashboardCard(
                accessLogs = accessLogs
            )
        }

        // 6. WorkManager Local Notification System Card
        item {
            WorkManagerNotificationStatusCard(
                onTriggerTestNotification = onTriggerTestNotification
            )
        }

        // 7. APK Installer Mobile Guide Card
        item {
            ApkInstallerGuideCard()
        }

        // 5. Last Learned Memory Toast Banner
        item {
            AnimatedVisibility(
                visible = lastLearnedMemory != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                lastLearnedMemory?.let { node ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SleekVioletDark)
                            .border(1.dp, SleekVioletPrimary, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SleekVioletPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NUEVA MEMORIA APRENDIDA",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = SleekVioletPrimary
                            )
                            Text(
                                text = "${node.title}: ${node.detail}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                color = SleekTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 6. Recent Observations Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OBSERVACIONES Y MEMORIA PERSISTENTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    letterSpacing = 1.2.sp
                )
                TextButton(
                    onClick = onNavigateToVault,
                    modifier = Modifier.semantics { testTag = "view_all_vault_button" }
                ) {
                    Text(
                        text = "Ver Bóveda ($memoryCount)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekVioletPrimary
                    )
                }
            }
        }

        if (memories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "El núcleo de memoria está listo para registrar datos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextMuted
                    )
                }
            }
        } else {
            items(memories.take(3), key = { it.id }) { node ->
                MemoryNodeCard(
                    node = node,
                    onDelete = { onDeleteMemory(node) }
                )
            }
        }

        // 7. Direct Quick Call To Neural Chat
        item {
            Button(
                onClick = onNavigateToChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .semantics { testTag = "open_neural_chat_button" },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekSurface,
                    contentColor = SleekTextPrimary
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consultar con Núcleo Medusa AI...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(SleekVioletPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Iniciar Chat",
                            tint = SleekVioletDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showVoiceDialog) {
        FuturisticHandsFreeVoiceDialog(
            voiceManager = voiceManager,
            onSendMessage = { spokenText ->
                onSendVoiceMessage(spokenText)
                onNavigateToChat()
            },
            onDismiss = { showVoiceDialog = false }
        )
    }
}
