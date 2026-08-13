package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AccessLogEntity
import com.example.data.db.AccessPassEntity
import com.example.ui.components.MonthlyAccessDashboardCard
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QrScannerScreen(
    accessPasses: List<AccessPassEntity>,
    accessLogs: List<AccessLogEntity>,
    onValidateCode: suspend (String) -> Pair<Boolean, AccessPassEntity?>,
    onDeleteLog: (AccessLogEntity) -> Unit = {},
    onClearLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var manualCodeInput by remember { mutableStateOf("") }
    var isTorchOn by remember { mutableStateOf(false) }
    var isValidating by remember { mutableStateOf(false) }
    var currentValidationResult by remember { mutableStateOf<Pair<Boolean, AccessPassEntity?>?>(null) }
    var scannedCodeTarget by remember { mutableStateOf<String?>(null) }
    var historySearchQuery by remember { mutableStateOf("") }

    // Laser Animation Transition
    val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    fun executeValidation(codeToValidate: String) {
        if (codeToValidate.isBlank()) {
            Toast.makeText(context, "Ingresa o selecciona un código QR", Toast.LENGTH_SHORT).show()
            return
        }
        manualCodeInput = codeToValidate
        scannedCodeTarget = codeToValidate
        isValidating = true

        scope.launch {
            val res = onValidateCode(codeToValidate)
            currentValidationResult = res
            isValidating = false
        }
    }

    val filteredLogs = remember(accessLogs, historySearchQuery) {
        if (historySearchQuery.isBlank()) accessLogs
        else accessLogs.filter { log ->
            log.passCode.contains(historySearchQuery, ignoreCase = true) ||
            log.residentName.contains(historySearchQuery, ignoreCase = true) ||
            log.residentHouse.contains(historySearchQuery, ignoreCase = true) ||
            log.visitorName.contains(historySearchQuery, ignoreCase = true) ||
            log.resultReason.contains(historySearchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Title Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ESCÁNER DE ACCESO Y AUDITORÍA ROOM DB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Validación en Tiempo Real e Historial de Intentos",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = { isTorchOn = !isTorchOn },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isTorchOn) Color(0xFFF59E0B) else SleekSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Flash Toggle",
                        tint = if (isTorchOn) Color.Black else SleekTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Camera Viewfinder Simulation Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF030712))
                    .border(2.dp, if (isTorchOn) Color(0xFFF59E0B) else Color(0xFF1E293B), RoundedCornerShape(24.dp))
                    .semantics {
                        testTag = "qr_camera_viewfinder"
                        contentDescription = "Visor de Cámara de Escáner QR"
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CAM_01 • CASETA PRINCIPAL", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("ROOM DB: PERSISTENTE", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Scanner Target Box
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .align(Alignment.CenterHorizontally)
                            .border(2.dp, Color(0xFF0284C7).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = laserYOffset.dp)
                                .background(Color(0xFF38BDF8))
                        )

                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("REGISTROS EN ROOM: ${accessLogs.size}", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(if (isTorchOn) "ILUMINACIÓN: ACTIVADA" else "APUNTA O SIMULA ESCANEO", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    }
                }
            }
        }

        // Live Visual Result Overlay Banner (Allowed / Denied)
        item {
            val result = currentValidationResult
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (result != null) {
                    val (isGranted, pass) = result

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isGranted) Color(0xFF064E3B) else Color(0xFF450A0A))
                            .border(2.dp, if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                            .semantics { testTag = "scan_result_indicator_banner" }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isGranted) Color(0xFF059669) else Color(0xFFDC2626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Block,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isGranted) "¡ACCESO CONCEDIDO!" else "ACCESO DENEGADO",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGranted) Color(0xFFA7F3D0) else Color(0xFFFECDD3),
                                    fontSize = 16.sp
                                )

                                if (pass != null) {
                                    Text(
                                        text = "${pass.residentName} (${pass.residentHouse})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Tipo: ${pass.accessType} • Código: ${pass.passCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(
                                        text = "El código '${scannedCodeTarget}' no existe en la base de datos local de Room.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Manual Input Validation Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekSurface)
                    .border(1.dp, SleekBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "VALIDACIÓN MANUAL / ESCANEO POR TECLADO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualCodeInput,
                        onValueChange = { manualCodeInput = it },
                        placeholder = { Text("Ej: MEDUSA-QR-XXXXXX", color = SleekTextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "qr_scanner_manual_field" },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0284C7),
                            unfocusedBorderColor = SleekBorderSubtle,
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary
                        )
                    )

                    Button(
                        onClick = { executeValidation(manualCodeInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .semantics { testTag = "qr_scanner_validate_btn" }
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Validar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Quick Test Target Row for active Room Access Passes
        item {
            Text(
                text = "PASES REGISTRADOS EN ROOM DB (TOCA PARA SIMULAR LECTURA)",
                style = MaterialTheme.typography.labelSmall,
                color = SleekTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }

        if (accessPasses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekSurface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay pases registrados en la DB local.", color = SleekTextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(accessPasses, key = { "pass_${it.id}" }) { pass ->
                PassTargetSimRow(
                    pass = pass,
                    onSimulateScan = { executeValidation(pass.passCode) }
                )
            }
        }

        // ==================== DASHBOARD ESTADÍSTICO MENSUAL CON GRÁFICOS (D3.JS) ====================
        item {
            MonthlyAccessDashboardCard(accessLogs = accessLogs)
        }

        // ==================== HISTORIAL DE ACCESOS PERSISTENTE EN ROOM DB ====================
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HISTORIAL DE ACCESOS EN ROOM DB (${accessLogs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        fontSize = 14.sp
                    )
                }

                if (accessLogs.isNotEmpty()) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier
                            .size(32.dp)
                            .semantics { testTag = "clear_access_logs_btn" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Limpiar Historial",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Search Filter for History Logs
        if (accessLogs.isNotEmpty()) {
            item {
                OutlinedTextField(
                    value = historySearchQuery,
                    onValueChange = { historySearchQuery = it },
                    placeholder = { Text("Buscar en historial por código, residente, casa o motivo...", color = SleekTextMuted, fontSize = 11.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SleekTextMuted, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "history_search_input" },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = SleekBorderSubtle,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    )
                )
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekSurface)
                        .border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = SleekTextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (accessLogs.isEmpty()) "Aún no hay intentos de escaneo guardados en Room DB." else "No hay coincidencias con la búsqueda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { "log_${it.id}" }) { logEntity ->
                RoomAccessLogCard(
                    log = logEntity,
                    onDelete = { onDeleteLog(logEntity) }
                )
            }
        }
    }
}

@Composable
private fun PassTargetSimRow(
    pass: AccessPassEntity,
    onSimulateScan: () -> Unit
) {
    val isResident = pass.accessType.contains("RESIDENT") || pass.accessType.contains("PROPIETARIO")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(14.dp))
            .clickable { onSimulateScan() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isResident) Color(0xFF312E81) else Color(0xFF064E3B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                tint = if (isResident) Color(0xFFC084FC) else Color(0xFF6EE7B7),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${pass.residentName} (${pass.residentHouse})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 13.sp
            )
            Text(
                text = "${pass.accessType} • ${pass.passCode}",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextMuted,
                fontSize = 11.sp
            )
        }

        Button(
            onClick = onSimulateScan,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Escanear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RoomAccessLogCard(
    log: AccessLogEntity,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss • dd/MM/yyyy", Locale.getDefault()) }
    val formattedDate = remember(log.timestampMs) { formatter.format(Date(log.timestampMs)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SleekSurface)
            .border(
                width = 1.dp,
                color = if (log.isGranted) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .semantics { testTag = "access_log_item_${log.id}" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Badge Circle
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (log.isGranted) Color(0xFF064E3B) else Color(0xFF450A0A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (log.isGranted) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = null,
                tint = if (log.isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (log.isGranted) "ACCESO CONCEDIDO" else "ACCESO DENEGADO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (log.isGranted) Color(0xFF34D399) else Color(0xFFFCA5A5),
                    fontSize = 12.sp
                )

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Código: ${log.passCode}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 13.sp
            )

            Text(
                text = "Residente: ${log.residentName} • Casa: ${log.residentHouse} • Visita: ${log.visitorName}",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary,
                fontSize = 11.sp
            )

            Text(
                text = "Motivo: ${log.resultReason} • Operador: ${log.scannedByRole}",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextMuted,
                fontSize = 10.sp
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Borrar Registro",
                tint = SleekTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
