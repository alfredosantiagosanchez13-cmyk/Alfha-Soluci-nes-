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
import com.example.data.db.ParcelEntity
import com.example.data.db.SmartDeviceEntity
import com.example.data.model.SmartScenePreset
import com.example.ui.MedusaTab
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
import com.example.ui.components.PrototypeMedusaOsDashboard
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
    parcels: List<ParcelEntity> = emptyList(),
    personalPresenteCount: Int = 0,
    incidentesAbiertosCount: Int = 0,
    paquetesPendientesCount: Int = 0,
    visitantesDentroCount: Int = 0,
    rondinesCompletosText: String = "3/4",
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
    onLockScreen: () -> Unit = {},
    onRegisterAttendance: (name: String, action: String, gps: String, note: String) -> Unit = { _, _, _, _ -> },
    onRegisterVisitor: (name: String, house: String, plates: String) -> Unit = { _, _, _ -> },
    onReportIncident: (title: String, priority: String, detail: String) -> Unit = { _, _, _ -> },
    onRecordPatrol: (checkpoint: String) -> Unit = {},
    onGenerateAiReport: ((String) -> Unit) -> Unit = {},
    smartDevices: List<SmartDeviceEntity> = emptyList(),
    onApplyPreset: (SmartScenePreset) -> Unit = {},
    onToggleMasterPower: (Boolean) -> Unit = {},
    onTriggerTestNotification: () -> Unit = {},
    onSendVoiceMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    PrototypeMedusaOsDashboard(
        activeRole = activeRole,
        personalPresenteCount = personalPresenteCount,
        incidentesAbiertosCount = incidentesAbiertosCount,
        paquetesPendientesCount = paquetesPendientesCount,
        visitantesDentroCount = visitantesDentroCount,
        rondinesCompletosText = rondinesCompletosText,
        accessPasses = accessPasses,
        parcels = parcels,
        onLockScreen = onLockScreen,
        onNavigateTab = { tab ->
            when (tab) {
                MedusaTab.NEURAL_CHAT -> onNavigateToChat()
                MedusaTab.MEMORY_VAULT -> onNavigateToVault()
                MedusaTab.SMART_PARCEL -> onNavigateToParcel()
                MedusaTab.SMART_HOME -> onNavigateToSmartHome()
                MedusaTab.QR_SCANNER -> onNavigateToParcel()
                MedusaTab.CORE_MATRIX -> {}
            }
        },
        onRegisterAttendance = onRegisterAttendance,
        onRegisterVisitor = onRegisterVisitor,
        onReportIncident = onReportIncident,
        onRecordPatrol = onRecordPatrol,
        onGenerateAiReport = onGenerateAiReport,
        modifier = modifier
    )
}
