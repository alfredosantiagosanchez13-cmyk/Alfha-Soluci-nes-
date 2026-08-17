package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.AccessPassEntity
import com.example.data.db.ParcelEntity
import com.example.ui.MedusaTab
import com.example.ui.UserRole
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletPrimary

/**
 * Panel de Control Principal auténtico del prototipo Medusa OS.
 * Implementa la separación exacta por roles (Caseta, Administración, Residente y Súper Alfha).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrototypeMedusaOsDashboard(
    activeRole: UserRole,
    personalPresenteCount: Int,
    incidentesAbiertosCount: Int,
    paquetesPendientesCount: Int,
    visitantesDentroCount: Int,
    rondinesCompletosText: String,
    accessPasses: List<AccessPassEntity>,
    parcels: List<ParcelEntity>,
    onLockScreen: () -> Unit,
    onNavigateTab: (MedusaTab) -> Unit,
    onRegisterAttendance: (name: String, action: String, gps: String, note: String) -> Unit,
    onRegisterVisitor: (name: String, house: String, plates: String) -> Unit,
    onReportIncident: (title: String, priority: String, detail: String) -> Unit,
    onRecordPatrol: (checkpoint: String) -> Unit,
    onGenerateAiReport: ((String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedModuleForDialog by remember { mutableStateOf<PrototypeModuleType?>(null) }

    // Residente form state
    var selectedCondominio by remember { mutableStateOf("Condominio Paraíso Real") }
    var condoDropdownExpanded by remember { mutableStateOf(false) }
    var residentHouseNumber by remember { mutableStateOf("13") }
    var residentAccessCode by remember { mutableStateOf("MEDUSA-7777") }
    var isResidentHouseEntered by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070913),
                        Color(0xFF0C1021),
                        Color(0xFF070913)
                    )
                )
            ),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Top System Bar (Sleek Glassmorphic Neon Header)
        item {
            PrototypeSystemHeader(
                activeRole = activeRole,
                onLock = onLockScreen
            )
        }

        // If Residente Role and not yet logged into house
        if (activeRole == UserRole.RESIDENTES && !isResidentHouseEntered) {
            item {
                ResidentPortalLoginCard(
                    selectedCondominio = selectedCondominio,
                    condoDropdownExpanded = condoDropdownExpanded,
                    onCondoExpandedChange = { condoDropdownExpanded = it },
                    onCondoSelected = { selectedCondominio = it },
                    houseNumber = residentHouseNumber,
                    onHouseChange = { residentHouseNumber = it },
                    accessCode = residentAccessCode,
                    onCodeChange = { residentAccessCode = it },
                    onEnterHouse = { isResidentHouseEntered = true }
                )
            }
        } else {
            // 2. Medusa OS Sub-Header Title, Official Emblem & Status
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Official Logo Emblem in Glowing Glass Chamber
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFFD54F).copy(alpha = 0.35f),
                                        Color(0xFF00E5FF).copy(alpha = 0.2f),
                                        Color(0x00070913)
                                    )
                                )
                            )
                            .border(
                                width = 1.8.dp,
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFFFFD54F),
                                        Color(0xFF00E5FF),
                                        Color(0xFFB388FF),
                                        Color(0xFFFFD54F)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_medusa_logo),
                            contentDescription = "Escudo Medusa Alpha",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "MEDUSA ALFHA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F),
                        letterSpacing = 3.sp,
                        fontSize = 23.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Authentic Motto Banner with Neon Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF151D3B).copy(alpha = 0.85f),
                                        Color(0xFF1E284F).copy(alpha = 0.85f),
                                        Color(0xFF151D3B).copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFFD54F).copy(alpha = 0.5f),
                                        Color(0xFF00E5FF).copy(alpha = 0.6f),
                                        Color(0xFFFFD54F).copy(alpha = 0.5f)
                                    )
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "TIEMPO ",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "= ",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "FAMILIA",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "INTELIGENCIA QUE PROTEGE · TIEMPO QUE TRANSFORMA",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF90A4AE),
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location Glass Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF141B36).copy(alpha = 0.8f))
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.4f),
                                        Color(0xFFFFD54F).copy(alpha = 0.3f)
                                    )
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Text(
                                text = "Seguridad Paraíso · Alpha Medusa OS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5 Pilares Ribbon in Medusa Tactical Capsules
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val pilares = listOf(
                            Pair("🔒 Seguridad Inteligente", MedusaAlphaPalette.ElectricBlue),
                            Pair("📡 Monitoreo Avanzado", Color(0xFFB388FF)),
                            Pair("🧠 Análisis Estratégico", MedusaAlphaPalette.InstitutionalGold),
                            Pair("🎯 Respuesta Efectiva", MedusaAlphaPalette.StatusCritical),
                            Pair("⏳ Devolvemos Tiempo", MedusaAlphaPalette.StatusOperating)
                        )
                        items(pilares) { (pilar, color) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MedusaAlphaPalette.DarkGraphite.copy(alpha = 0.9f))
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                color.copy(alpha = 0.6f),
                                                MedusaAlphaPalette.InstitutionalGold.copy(alpha = 0.2f),
                                                color.copy(alpha = 0.2f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = pilar,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }

            // 3. Welcome & 5 Real-Time KPI Cards
            item {
                PrototypeKpiDashboardCard(
                    activeRole = activeRole,
                    personalPresente = personalPresenteCount,
                    incidentesAbiertos = incidentesAbiertosCount,
                    paquetesPendientes = paquetesPendientesCount,
                    visitantesDentro = visitantesDentroCount,
                    rondinesCompletos = rondinesCompletosText
                )
            }

            // 4. CASETA / VIGILANCIA SECTION
            // Visible for Súper Alfha, Guardia, Administración, and (only Asistencia) for Trabajador
            val showCasetaSection = activeRole == UserRole.ALFHA_SANTIAGO ||
                    activeRole == UserRole.GUARDIA ||
                    activeRole == UserRole.ADMINISTRACION ||
                    activeRole == UserRole.TRABAJADOR

            if (showCasetaSection) {
                item {
                    MedusaSectionHeader(
                        iconGlyph = "🛡",
                        title = "CASETA / VIGILANCIA",
                        accentColor = MedusaAlphaPalette.ElectricBlue
                    )
                }

                item {
                    val casetaModules = if (activeRole == UserRole.TRABAJADOR) {
                        listOf(PrototypeModuleType.ASISTENCIA)
                    } else {
                        listOf(
                            PrototypeModuleType.ASISTENCIA,
                            PrototypeModuleType.ACCESOS,
                            PrototypeModuleType.FICHA_RESIDENTE,
                            PrototypeModuleType.VISITANTES,
                            PrototypeModuleType.PAQUETERIA,
                            PrototypeModuleType.PLACAS,
                            PrototypeModuleType.INCIDENTES,
                            PrototypeModuleType.RONDINES,
                            PrototypeModuleType.GUARDIAS,
                            PrototypeModuleType.BITACORA
                        )
                    }

                    PrototypeModulesGrid(
                        modules = casetaModules,
                        onModuleClick = { module ->
                            selectedModuleForDialog = module
                        }
                    )
                }
            }

            // 5. ADMINISTRACIÓN SECTION
            // Visible strictly for Súper Alfha and Administración
            val showAdminSection = activeRole == UserRole.ALFHA_SANTIAGO || activeRole == UserRole.ADMINISTRACION

            if (showAdminSection) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    MedusaSectionHeader(
                        iconGlyph = "🏛",
                        title = "ADMINISTRACIÓN",
                        accentColor = MedusaAlphaPalette.InstitutionalGold
                    )
                }

                item {
                    val adminModules = listOf(
                        PrototypeModuleType.DIRECTORIO,
                        PrototypeModuleType.RESIDENTES,
                        PrototypeModuleType.VEHICULOS,
                        PrototypeModuleType.REPORTES,
                        PrototypeModuleType.TRABAJOS_ESPECIALES,
                        PrototypeModuleType.ESTADISTICAS,
                        PrototypeModuleType.HISTORIAL_ACTIVIDAD,
                        PrototypeModuleType.AUDITORIAS,
                        PrototypeModuleType.RIESGOS,
                        PrototypeModuleType.IA_MEDUSA,
                        PrototypeModuleType.CONFIGURACION
                    )

                    PrototypeModulesGrid(
                        modules = adminModules,
                        onModuleClick = { module ->
                            selectedModuleForDialog = module
                        }
                    )
                }
            }

            // 6. RESIDENTE HOUSE CONSOLE (If Resident is logged in)
            if (activeRole == UserRole.RESIDENTES && isResidentHouseEntered) {
                item {
                    ResidentHouseConsoleCard(
                        houseNumber = residentHouseNumber,
                        condominio = selectedCondominio,
                        passes = accessPasses,
                        parcels = parcels,
                        onNavigateToChat = { onNavigateTab(MedusaTab.NEURAL_CHAT) },
                        onNavigateToQr = { onNavigateTab(MedusaTab.QR_SCANNER) }
                    )
                }
            }

            // 7. Footer Motto
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "\"MEDUSA OBSERVA · MEDUSA PROTEGE · MEDUSA RECUERDA\"",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            }
        }
    }

    // Modal when a module is tapped
    selectedModuleForDialog?.let { module ->
        PrototypeModuleDialog(
            module = module,
            onDismiss = { selectedModuleForDialog = null },
            onRegisterAttendance = onRegisterAttendance,
            onRegisterVisitor = onRegisterVisitor,
            onReportIncident = onReportIncident,
            onRecordPatrol = onRecordPatrol,
            onGenerateAiReport = onGenerateAiReport,
            onNavigateToChat = { onNavigateTab(MedusaTab.NEURAL_CHAT) },
            onNavigateToQr = { onNavigateTab(MedusaTab.QR_SCANNER) },
            onNavigateToParcel = { onNavigateTab(MedusaTab.SMART_PARCEL) }
        )
    }
}

@Composable
fun PrototypeSystemHeader(
    activeRole: UserRole,
    onLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roleBadgeText = when (activeRole) {
        UserRole.ALFHA_SANTIAGO -> "Santiago (Alfha) · Coordinador"
        UserRole.GUARDIA -> "Seguridad Paraíso · Guardia"
        UserRole.ADMINISTRACION -> "Administración · Admin"
        UserRole.RESIDENTES -> "Residente Paraíso · Residente"
        UserRole.TRABAJADOR -> "Mantenimiento · Operador"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MedusaAlphaPalette.PanelTitanium.copy(alpha = 0.98f),
                        MedusaAlphaPalette.DarkGraphite.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        MedusaAlphaPalette.ElectricBlue.copy(alpha = 0.45f),
                        MedusaAlphaPalette.InstitutionalGold.copy(alpha = 0.40f),
                        MedusaAlphaPalette.ElectricBlue.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MedusaAlphaPalette.DarkGraphite)
                        .border(
                            1.2.dp,
                            Brush.linearGradient(
                                listOf(
                                    MedusaAlphaPalette.InstitutionalGold,
                                    MedusaAlphaPalette.ElectricBlue
                                )
                            ),
                            CircleShape
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_medusa_logo),
                        contentDescription = "Medusa Alpha Emblem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MEDUSA ALFHA",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MedusaAlphaPalette.InstitutionalGold,
                            letterSpacing = 1.2.sp
                        )
                        MedusaStatusIndicator(
                            status = MedusaOperationalStatus.OPERANDO,
                            customLabel = "ACTIVO"
                        )
                    }
                    Text(
                        text = "TIEMPO = FAMILIA · SISTEMA NEURAL",
                        fontSize = 9.sp,
                        color = Color(0xFF80D8FF),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MedusaAlphaPalette.PanelSurface)
                        .border(
                            1.dp,
                            MedusaAlphaPalette.InstitutionalGold.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = roleBadgeText,
                        fontSize = 10.sp,
                        color = MedusaAlphaPalette.InstitutionalGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onLock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedusaAlphaPalette.PanelTitanium,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .border(
                            1.dp,
                            MedusaAlphaPalette.ElectricBlue.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(text = "Salir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PrototypeKpiDashboardCard(
    activeRole: UserRole,
    personalPresente: Int,
    incidentesAbiertos: Int,
    paquetesPendientes: Int,
    visitantesDentro: Int,
    rondinesCompletos: String,
    modifier: Modifier = Modifier
) {
    val greetingName = when (activeRole) {
        UserRole.ALFHA_SANTIAGO -> "Santiago"
        UserRole.GUARDIA -> "Seguridad"
        UserRole.ADMINISTRACION -> "Administrador"
        UserRole.RESIDENTES -> "Residente"
        UserRole.TRABAJADOR -> "Trabajador"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .medusaFrame(
                accentColor = MedusaAlphaPalette.ElectricBlue,
                chamferRadius = 18.dp,
                showCornerNodes = true,
                showCircuitTraces = true
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Buenos días, $greetingName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedusaAlphaPalette.TextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Matriz Operativa en Tiempo Real",
                        fontSize = 11.sp,
                        color = Color(0xFF80D8FF),
                        letterSpacing = 0.5.sp
                    )
                }

                MedusaStatusIndicator(
                    status = MedusaOperationalStatus.OPERANDO,
                    customLabel = "EN LÍNEA"
                )
            }

            // KPI Grid (2 columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiTile(
                    emoji = "👮",
                    count = personalPresente.toString(),
                    label = "Personal presente",
                    neonColor = MedusaAlphaPalette.StatusOperating,
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    emoji = "🚨",
                    count = incidentesAbiertos.toString(),
                    label = "Incidentes abiertos",
                    neonColor = if (incidentesAbiertos > 0) MedusaAlphaPalette.StatusCritical else MedusaAlphaPalette.StatusOperating,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiTile(
                    emoji = "📦",
                    count = paquetesPendientes.toString(),
                    label = "Paquetes pendientes",
                    neonColor = MedusaAlphaPalette.InstitutionalGold,
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    emoji = "👥",
                    count = visitantesDentro.toString(),
                    label = "Visitantes dentro",
                    neonColor = MedusaAlphaPalette.ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            KpiTile(
                emoji = "📍",
                count = rondinesCompletos,
                label = "Rondines completos hoy",
                neonColor = Color(0xFFB388FF),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun KpiTile(
    emoji: String,
    count: String,
    label: String,
    neonColor: Color = MedusaAlphaPalette.StatusOperating,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .medusaFrame(
                accentColor = neonColor,
                chamferRadius = 12.dp,
                showCornerNodes = false,
                showCircuitTraces = false
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Column {
                Text(
                    text = count,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = neonColor
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MedusaAlphaPalette.TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SectionHeaderTitle(
    icon: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD54F),
            letterSpacing = 1.5.sp,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFD54F).copy(alpha = 0.5f),
                            Color(0xFF00E5FF).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun PrototypeModulesGrid(
    modules: List<PrototypeModuleType>,
    onModuleClick: (PrototypeModuleType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Group in pairs of 2
        modules.chunked(2).forEach { rowModules ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowModules.forEach { module ->
                    val moduleNeonColor = when (module) {
                        PrototypeModuleType.ACCESOS, PrototypeModuleType.VISITANTES -> MedusaAlphaPalette.ElectricBlue
                        PrototypeModuleType.ASISTENCIA, PrototypeModuleType.GUARDIAS -> MedusaAlphaPalette.StatusOperating
                        PrototypeModuleType.PAQUETERIA, PrototypeModuleType.VEHICULOS, PrototypeModuleType.PLACAS -> MedusaAlphaPalette.InstitutionalGold
                        PrototypeModuleType.INCIDENTES, PrototypeModuleType.REPORTES -> MedusaAlphaPalette.StatusCritical
                        PrototypeModuleType.RONDINES, PrototypeModuleType.BITACORA -> Color(0xFFB388FF)
                        else -> MedusaAlphaPalette.TechBlue
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .medusaFrame(
                                accentColor = moduleNeonColor,
                                chamferRadius = 14.dp,
                                showCornerNodes = true,
                                showCircuitTraces = true
                            )
                            .clickable { onModuleClick(module) }
                            .padding(14.dp)
                            .semantics { testTag = "prototype_module_${module.name.lowercase()}" }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MedusaAlphaPalette.DarkGraphite)
                                        .border(
                                            1.dp,
                                            Brush.linearGradient(
                                                listOf(
                                                    moduleNeonColor.copy(alpha = 0.7f),
                                                    MedusaAlphaPalette.InstitutionalGold.copy(alpha = 0.3f)
                                                )
                                            ),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = module.iconEmoji, fontSize = 20.sp)
                                }

                                MedusaStatusIndicator(
                                    status = if (module.isOperating) MedusaOperationalStatus.OPERANDO else MedusaOperationalStatus.ATENCION
                                )
                            }

                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MedusaAlphaPalette.TextPrimary,
                                fontSize = 13.sp,
                                maxLines = 1
                            )

                            Text(
                                text = module.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MedusaAlphaPalette.TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 13.sp,
                                maxLines = 2
                            )
                        }
                    }
                }

                // If odd number in row
                if (rowModules.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentPortalLoginCard(
    selectedCondominio: String,
    condoDropdownExpanded: Boolean,
    onCondoExpandedChange: (Boolean) -> Unit,
    onCondoSelected: (String) -> Unit,
    houseNumber: String,
    onHouseChange: (String) -> Unit,
    accessCode: String,
    onCodeChange: (String) -> Unit,
    onEnterHouse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val condoOptions = listOf("Condominio Paraíso Real", "Condominio Vista Hermosa", "Condominio Las Palmas")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gold Medusa Shield with Official Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFD54F).copy(alpha = 0.35f), Color(0x00000000))
                    )
                )
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFF00E5FF))), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_medusa_logo),
                contentDescription = "Escudo Medusa Alpha Residente",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MEDUSA ALFHA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD54F),
                letterSpacing = 2.sp
            )
            Text(
                text = "INTELIGENCIA QUE PROTEGE, TIEMPO QUE TRANSFORMA",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF90A4AE),
                fontSize = 9.sp,
                letterSpacing = 0.6.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161E3D))
                    .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = "SEGURIDAD INTELIGENTE", fontSize = 9.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161E3D))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = "TIEMPO = FAMILIA", fontSize = 9.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        }

        // Form Card "INGRESA A TU CASA" with Medusa Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .medusaFrame(
                    accentColor = MedusaAlphaPalette.InstitutionalGold,
                    chamferRadius = 18.dp,
                    showCornerNodes = true,
                    showCircuitTraces = true
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔐", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INGRESA A TU RESIDENCIA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MedusaAlphaPalette.InstitutionalGold,
                        fontSize = 14.sp
                    )
                }

                // Condominio selector
                Column {
                    Text(text = "CONDOMINIO", fontSize = 10.sp, color = Color(0xFF80D8FF), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = condoDropdownExpanded,
                        onExpandedChange = onCondoExpandedChange
                    ) {
                        OutlinedTextField(
                            value = selectedCondominio,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = condoDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MedusaAlphaPalette.InstitutionalGold,
                                unfocusedBorderColor = Color(0xFF2C3558)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = condoDropdownExpanded,
                            onDismissRequest = { onCondoExpandedChange(false) }
                        ) {
                            condoOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        onCondoSelected(option)
                                        onCondoExpandedChange(false)
                                    }
                                )
                            }
                        }
                    }
                }

                // House number
                Column {
                    Text(text = "NÚMERO DE CASA", fontSize = 10.sp, color = Color(0xFF80D8FF), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = houseNumber,
                        onValueChange = onHouseChange,
                        placeholder = { Text("Ej: 13", color = Color(0xFF546E7A)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MedusaAlphaPalette.InstitutionalGold,
                            unfocusedBorderColor = Color(0xFF2C3558)
                        )
                    )
                }

                // Access code
                Column {
                    Text(text = "CÓDIGO DE ACCESO", fontSize = 10.sp, color = Color(0xFF80D8FF), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = accessCode,
                        onValueChange = onCodeChange,
                        placeholder = { Text("El que te dio administración", color = Color(0xFF546E7A)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MedusaAlphaPalette.InstitutionalGold,
                            unfocusedBorderColor = Color(0xFF2C3558)
                        )
                    )
                }

                Button(
                    onClick = onEnterHouse,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MedusaAlphaPalette.InstitutionalGold,
                        contentColor = Color(0xFF121626)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics { testTag = "enter_resident_house_button" }
                ) {
                    Text(text = "ENTRAR A MI RESIDENCIA", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }

                Text(
                    text = "¿No tienes código? Pídelo con la administración de tu condominio. Cada casa tiene uno.",
                    fontSize = 10.sp,
                    color = Color(0xFF78909C),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ResidentHouseConsoleCard(
    houseNumber: String,
    condominio: String,
    passes: List<AccessPassEntity>,
    parcels: List<ParcelEntity>,
    onNavigateToChat: () -> Unit,
    onNavigateToQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .medusaFrame(
                accentColor = MedusaAlphaPalette.InstitutionalGold,
                chamferRadius = 18.dp,
                showCornerNodes = true,
                showCircuitTraces = true
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Casa $houseNumber",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = condominio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MedusaAlphaPalette.InstitutionalGold
                    )
                }

                MedusaStatusIndicator(
                    status = MedusaOperationalStatus.OPERANDO,
                    customLabel = "RESIDENTE ACTIVO"
                )
            }

            // Quick resident actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigateToQr,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF13172C))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pase QR", fontSize = 11.sp, color = Color(0xFF13172C), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToChat,
                    colors = ButtonDefaults.buttonColors(containerColor = MedusaAlphaPalette.ElectricBlue),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF13172C))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("IA Medusa", fontSize = 11.sp, color = Color(0xFF13172C), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
