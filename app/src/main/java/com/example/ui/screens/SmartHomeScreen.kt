package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SmartDeviceEntity
import com.example.data.model.ClimateHvacMode
import com.example.data.model.CommunicationProtocol
import com.example.data.model.DeviceType
import com.example.data.model.DiscoveredIotDevice
import com.example.data.model.FanSpeed
import com.example.data.model.SmartScenePreset
import com.example.ui.MedusaViewModel
import com.example.ui.components.FuturisticHandsFreeVoiceDialog
import com.example.ui.voice.VoiceRecognitionManager
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
import java.util.Locale

@Composable
fun SmartHomeScreen(
    viewModel: MedusaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceRecognitionManager(context) }

    val devices by viewModel.smartDevices.collectAsState()
    val isScanning by viewModel.isIotScanning.collectAsState()
    val discoveredDevices by viewModel.discoveredIotDevices.collectAsState()
    val recentActionLog by viewModel.recentIotActionLog.collectAsState()
    val presets = viewModel.availableIotPresets

    var selectedZone by remember { mutableStateOf("TODAS") }
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showScanResultsDialog by remember { mutableStateOf(false) }
    var showVoiceDialog by remember { mutableStateOf(false) }

    val zones = remember(devices) {
        listOf("TODAS") + devices.map { it.zone }.distinct().sorted()
    }

    val filteredDevices = remember(devices, selectedZone) {
        if (selectedZone == "TODAS") devices else devices.filter { it.zone == selectedZone }
    }

    val totalActiveLights = devices.count { it.deviceType == DeviceType.LIGHT.name && it.isOn }
    val activeClimates = devices.count { it.deviceType == DeviceType.CLIMATE.name && it.isOn }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header with Live IoT Telemetry & Protocol Indicators
        item {
            SmartHomeHeader(
                activeLights = totalActiveLights,
                activeClimates = activeClimates,
                isScanning = isScanning,
                onScanClick = {
                    viewModel.scanLocalIotDevices()
                    showScanResultsDialog = true
                },
                onAddClick = { showAddDeviceDialog = true },
                onVoiceClick = { showVoiceDialog = true }
            )
        }

        // 2. Recent IoT Neural Dispatch Log Pill
        if (!recentActionLog.isNullOrBlank()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = recentActionLog ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 3. Quick Scene Presets Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ESCENAS Y PROTOCOLOS INTELIGENTES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekVioletPrimary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Sincronización Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextMuted,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(presets) { preset ->
                        SmartPresetCard(
                            preset = preset,
                            onApply = { viewModel.applyIotPreset(preset) }
                        )
                    }
                }
            }
        }

        // 4. Master All Control & Quick Suggestion Bar
        item {
            MasterAllControlsCard(
                totalDevices = devices.size,
                onTurnAllOn = { viewModel.setMasterPowerAll(true) },
                onTurnAllOff = { viewModel.setMasterPowerAll(false) },
                onSendQuickVoice = { cmd -> viewModel.executeNaturalLanguageIotCommand(cmd) }
            )
        }

        // 5. Condo Zone Filter Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                zones.forEach { zone ->
                    val isSelected = selectedZone == zone
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) SleekVioletPrimary else SleekSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) SleekVioletPrimary else SleekBorderSubtle,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedZone = zone }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .semantics {
                                testTag = "smart_zone_tab_${zone.lowercase().replace(" ", "_")}"
                                contentDescription = "Zona $zone"
                            }
                    ) {
                        Text(
                            text = if (zone == "TODAS") "🌐 Toda la Residencia" else "📍 $zone",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SleekVioletDark else SleekTextSecondary
                        )
                    }
                }
            }
        }

        // 6. Devices List (Grouped by Lights and Climate)
        if (filteredDevices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron dispositivos en esta zona.",
                        color = SleekTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredDevices, key = { it.id }) { device ->
                if (device.deviceType == DeviceType.LIGHT.name) {
                    SmartLightCard(
                        device = device,
                        onTogglePower = { viewModel.toggleIotDevicePower(device) },
                        onUpdateLight = { power, brightness, colorHex ->
                            viewModel.updateIotLightProperties(device, power, brightness, colorHex)
                        },
                        onDelete = { viewModel.deleteIotDevice(device) }
                    )
                } else if (device.deviceType == DeviceType.CLIMATE.name) {
                    SmartClimateCard(
                        device = device,
                        onTogglePower = { viewModel.toggleIotDevicePower(device) },
                        onUpdateClimate = { power, targetTemp, mode, fan ->
                            viewModel.updateIotClimateProperties(device, power, targetTemp, mode, fan)
                        },
                        onDelete = { viewModel.deleteIotDevice(device) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Voice Dialog for Smart Home NLP
    if (showVoiceDialog) {
        FuturisticHandsFreeVoiceDialog(
            voiceManager = voiceManager,
            onSendMessage = { spoken ->
                viewModel.executeNaturalLanguageIotCommand(spoken)
            },
            onDismiss = { showVoiceDialog = false }
        )
    }

    // Add Device Dialog
    if (showAddDeviceDialog) {
        AddSmartDeviceDialog(
            onDismiss = { showAddDeviceDialog = false },
            onAdd = { device ->
                viewModel.addNewIotDevice(device)
                showAddDeviceDialog = false
            }
        )
    }

    // Discovered Devices Sheet Dialog
    if (showScanResultsDialog) {
        ScanDiscoveredDevicesDialog(
            isScanning = isScanning,
            discovered = discoveredDevices,
            onDismiss = { showScanResultsDialog = false },
            onAddDiscovered = { disc ->
                viewModel.addNewIotDevice(
                    SmartDeviceEntity(
                        deviceId = disc.deviceId,
                        name = disc.name,
                        zone = disc.zone,
                        deviceType = disc.type.name,
                        protocol = disc.protocol.name,
                        endpointUrl = if (disc.protocol == CommunicationProtocol.REST_API) disc.endpointOrAddress else "",
                        bleMacOrUuid = if (disc.protocol == CommunicationProtocol.BLUETOOTH_LE) disc.endpointOrAddress else "",
                        isOn = true,
                        isOnline = true
                    )
                )
                showScanResultsDialog = false
            },
            onReScan = { viewModel.scanLocalIotDevices() }
        )
    }
}

// ==================== SUB-COMPONENTS ====================

@Composable
private fun SmartHomeHeader(
    activeLights: Int,
    activeClimates: Int,
    isScanning: Boolean,
    onScanClick: () -> Unit,
    onAddClick: () -> Unit,
    onVoiceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DOMÓTICA & IOT MEDUSA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekVioletPrimary,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4))
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Control REST LAN + Bluetooth LE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "💡 $activeLights Luces activas",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextMuted
                )
                Text(
                    text = "❄️ $activeClimates Climas en marcha",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Voice Control button
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1B4B))
                    .border(1.dp, SleekVioletPrimary, CircleShape)
                    .semantics { testTag = "smart_home_voice_button" }
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Comando de voz IoT",
                    tint = SleekVioletPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Scan IoT Devices button
            IconButton(
                onClick = onScanClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SleekSurfaceVariant)
                    .border(1.dp, SleekBorderSubtle, CircleShape)
                    .semantics { testTag = "smart_home_scan_button" }
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = SleekVioletPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Escanear red IoT",
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Add manual IoT device button
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SleekVioletPrimary)
                    .semantics { testTag = "smart_home_add_button" }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar dispositivo",
                    tint = SleekVioletDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SmartPresetCard(
    preset: SmartScenePreset,
    onApply: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp))
            .clickable { onApply() }
            .padding(12.dp)
            .semantics {
                testTag = "preset_${preset.id.lowercase()}"
                contentDescription = preset.title
            }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = preset.iconEmoji, fontSize = 22.sp)
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(preset.targetColorHex)))
                        .border(1.dp, SleekBorderSubtle, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = preset.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextMuted,
                fontSize = 10.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ACTIVAR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SleekVioletPrimary,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SleekVioletPrimary,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
private fun MasterAllControlsCard(
    totalDevices: Int,
    onTurnAllOn: () -> Unit,
    onTurnAllOff: () -> Unit,
    onSendQuickVoice: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SleekSurfaceVariant)
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint = SleekVioletPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Control Maestro Residencia ($totalDevices disp.)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary,
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onTurnAllOn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekVioletPrimary,
                        contentColor = SleekVioletDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .semantics { testTag = "master_on_button" }
                ) {
                    Text("Encender Todo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onTurnAllOff,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .semantics { testTag = "master_off_button" }
                ) {
                    Text("Apagar Todo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fast NLP suggestion pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "🟣 Luces en violeta",
                "❄️ Clima a 21°C",
                "🌙 Modo Noche",
                "💡 Iluminación al 50%",
                "🍃 Clima Eco"
            )
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekSurface)
                        .border(1.dp, SleekBorderSubtle, RoundedCornerShape(12.dp))
                        .clickable { onSendQuickVoice(suggestion) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 10.sp,
                        color = SleekTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SmartLightCard(
    device: SmartDeviceEntity,
    onTogglePower: () -> Unit,
    onUpdateLight: (isOn: Boolean, brightness: Int, colorHex: String) -> Unit,
    onDelete: () -> Unit
) {
    val currentColor = remember(device.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(device.colorHex))
        } catch (_: Exception) {
            Color(0xFF8B5CF6)
        }
    }

    val glowColor by animateColorAsState(
        targetValue = if (device.isOn) currentColor.copy(alpha = 0.35f) else Color.Transparent,
        label = "lightGlow"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SleekSurface)
            .border(
                1.dp,
                if (device.isOn) currentColor.copy(alpha = 0.5f) else SleekBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .semantics {
                testTag = "device_card_${device.deviceId.lowercase()}"
                contentDescription = "Dispositivo de luz ${device.name}"
            }
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (device.isOn) currentColor.copy(alpha = 0.2f) else SleekSurfaceVariant)
                        .border(1.dp, if (device.isOn) currentColor else SleekBorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (device.isOn) currentColor else SleekTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.zone,
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ProtocolBadge(protocol = device.protocol, endpoint = device.endpointUrl.ifBlank { device.bleMacOrUuid })
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = device.isOn,
                    onCheckedChange = { onTogglePower() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SleekVioletDark,
                        checkedTrackColor = SleekVioletPrimary,
                        uncheckedThumbColor = SleekTextMuted,
                        uncheckedTrackColor = SleekSurfaceVariant
                    ),
                    modifier = Modifier.semantics { testTag = "toggle_${device.deviceId.lowercase()}" }
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar dispositivo",
                        tint = SleekTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Active Controls when turned ON
        AnimatedVisibility(
            visible = device.isOn,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(14.dp))

                // Brightness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Intensidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${device.brightness}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SleekVioletPrimary,
                        fontSize = 12.sp
                    )
                }

                Slider(
                    value = device.brightness.toFloat(),
                    onValueChange = { newBrightness ->
                        onUpdateLight(true, newBrightness.toInt(), device.colorHex)
                    },
                    valueRange = 1f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = SleekVioletPrimary,
                        activeTrackColor = SleekVioletPrimary,
                        inactiveTrackColor = SleekSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "slider_brightness_${device.deviceId.lowercase()}" }
                )

                // Color Palette Selector
                Text(
                    text = "Paleta de Color RGB",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val colors = listOf(
                        "#8B5CF6" to "Violet",
                        "#06B6D4" to "Cyan",
                        "#F59E0B" to "Warm Amber",
                        "#10B981" to "Emerald",
                        "#EC4899" to "Magenta",
                        "#F8FAFC" to "Pure White"
                    )
                    colors.forEach { (hex, name) ->
                        val isSelected = device.colorHex.equals(hex, ignoreCase = true)
                        val colorObj = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorObj)
                                .border(
                                    2.dp,
                                    if (isSelected) Color.White else Color(0x33000000),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onUpdateLight(true, device.brightness, hex)
                                }
                                .semantics {
                                    testTag = "color_${name.lowercase().replace(" ", "_")}_${device.deviceId.lowercase()}"
                                    contentDescription = "Color $name"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (hex == "#F8FAFC" || hex == "#F59E0B") Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartClimateCard(
    device: SmartDeviceEntity,
    onTogglePower: () -> Unit,
    onUpdateClimate: (isOn: Boolean, targetTemp: Float, mode: ClimateHvacMode, fan: FanSpeed) -> Unit,
    onDelete: () -> Unit
) {
    val currentMode = remember(device.hvacMode) {
        try {
            ClimateHvacMode.valueOf(device.hvacMode)
        } catch (_: Exception) {
            ClimateHvacMode.COOL
        }
    }
    val currentFan = remember(device.fanSpeed) {
        try {
            FanSpeed.valueOf(device.fanSpeed)
        } catch (_: Exception) {
            FanSpeed.AUTO
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SleekSurface)
            .border(
                1.dp,
                if (device.isOn) Color(currentMode.colorHex).copy(alpha = 0.5f) else SleekBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .semantics {
                testTag = "climate_card_${device.deviceId.lowercase()}"
                contentDescription = "Termostato ${device.name}"
            }
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (device.isOn) Color(currentMode.colorHex).copy(alpha = 0.2f) else SleekSurfaceVariant)
                        .border(1.dp, if (device.isOn) Color(currentMode.colorHex) else SleekBorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeviceThermostat,
                        contentDescription = null,
                        tint = if (device.isOn) Color(currentMode.colorHex) else SleekTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.zone,
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ProtocolBadge(protocol = device.protocol, endpoint = device.endpointUrl.ifBlank { device.bleMacOrUuid })
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = device.isOn,
                    onCheckedChange = { onTogglePower() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SleekVioletDark,
                        checkedTrackColor = Color(currentMode.colorHex),
                        uncheckedThumbColor = SleekTextMuted,
                        uncheckedTrackColor = SleekSurfaceVariant
                    ),
                    modifier = Modifier.semantics { testTag = "toggle_climate_${device.deviceId.lowercase()}" }
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar clima",
                        tint = SleekTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Active HVAC Controls
        AnimatedVisibility(
            visible = device.isOn,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(14.dp))

                // Target Temperature Dial Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0B0F19))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrement Button
                    IconButton(
                        onClick = {
                            val newTemp = (device.targetTempC - 0.5f).coerceAtLeast(16.0f)
                            onUpdateClimate(true, newTemp, currentMode, currentFan)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SleekSurfaceVariant)
                            .border(1.dp, SleekBorderSubtle, CircleShape)
                            .semantics { testTag = "temp_minus_${device.deviceId.lowercase()}" }
                    ) {
                        Text(text = "−", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    }

                    // Main Temperature Display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f°C", device.targetTempC),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(currentMode.colorHex)
                        )
                        Text(
                            text = "OBJETIVO",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekTextMuted,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    // Increment Button
                    IconButton(
                        onClick = {
                            val newTemp = (device.targetTempC + 0.5f).coerceAtMost(30.0f)
                            onUpdateClimate(true, newTemp, currentMode, currentFan)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SleekSurfaceVariant)
                            .border(1.dp, SleekBorderSubtle, CircleShape)
                            .semantics { testTag = "temp_plus_${device.deviceId.lowercase()}" }
                    ) {
                        Text(text = "+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ambient Sensor Telemetry (Room Temperature & Humidity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sensor: ${String.format(Locale.US, "%.1f°C", device.currentTempC)}",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Humedad: ${device.humidityPercent}%",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // HVAC Modes Selector
                Text(
                    text = "Modo de Operación",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf(
                        ClimateHvacMode.COOL,
                        ClimateHvacMode.HEAT,
                        ClimateHvacMode.ECO,
                        ClimateHvacMode.AUTO,
                        ClimateHvacMode.FAN_ONLY
                    )
                    modes.forEach { mode ->
                        val isSelected = currentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(mode.colorHex) else SleekSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(mode.colorHex) else SleekBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateClimate(true, device.targetTempC, mode, currentFan)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = mode.iconEmoji, fontSize = 14.sp)
                                Text(
                                    text = mode.label.split(" ").first(),
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SleekVioletDark else SleekTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fan Speed Selector
                Text(
                    text = "Velocidad de Ventilación",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FanSpeed.values().forEach { fan ->
                        val isSelected = currentFan == fan
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SleekVioletPrimary else SleekSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) SleekVioletPrimary else SleekBorderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateClimate(true, device.targetTempC, currentMode, fan)
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fan.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SleekVioletDark else SleekTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocolBadge(protocol: String, endpoint: String) {
    val isRest = protocol == CommunicationProtocol.REST_API.name
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isRest) Color(0xFF0F2B36) else Color(0xFF2E1065))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isRest) Icons.Default.Router else Icons.Default.Bluetooth,
                contentDescription = null,
                tint = if (isRest) Color(0xFF06B6D4) else Color(0xFFA855F7),
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (isRest) "REST" else "BLE",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRest) Color(0xFF06B6D4) else Color(0xFFA855F7)
            )
        }
    }
}

// ==================== DIALOGS: ADD & SCANNER ====================

@Composable
private fun AddSmartDeviceDialog(
    onDismiss: () -> Unit,
    onAdd: (SmartDeviceEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("Sala Alpha") }
    var type by remember { mutableStateOf(DeviceType.LIGHT) }
    var protocol by remember { mutableStateOf(CommunicationProtocol.REST_API) }
    var endpoint by remember { mutableStateOf("http://192.168.1.150/api/v1/device") }
    var bleMac by remember { mutableStateOf("A4:C1:38:12:34:56") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        title = {
            Text(
                text = "AGREGAR DISPOSITIVO IOT",
                style = MaterialTheme.typography.titleMedium,
                color = SleekVioletPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Dispositivo") },
                    placeholder = { Text("ej. Foco Comedor") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "input_iot_device_name" },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekVioletPrimary,
                        unfocusedBorderColor = SleekBorder
                    )
                )

                OutlinedTextField(
                    value = zone,
                    onValueChange = { zone = it },
                    label = { Text("Zona / Ubicación") },
                    placeholder = { Text("ej. Terraza, Recámara") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "input_iot_device_zone" },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekVioletPrimary,
                        unfocusedBorderColor = SleekBorder
                    )
                )

                Text(text = "Tipo de Dispositivo", fontSize = 11.sp, color = SleekTextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(DeviceType.LIGHT, DeviceType.CLIMATE).forEach { dType ->
                        val isSelected = type == dType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SleekVioletPrimary else SleekSurfaceVariant)
                                .clickable { type = dType }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${dType.iconEmoji} ${dType.label}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SleekVioletDark else SleekTextSecondary
                            )
                        }
                    }
                }

                Text(text = "Protocolo de Comunicación", fontSize = 11.sp, color = SleekTextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(CommunicationProtocol.REST_API, CommunicationProtocol.BLUETOOTH_LE).forEach { prot ->
                        val isSelected = protocol == prot
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SleekVioletPrimary else SleekSurfaceVariant)
                                .clickable { protocol = prot }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prot.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SleekVioletDark else SleekTextSecondary
                            )
                        }
                    }
                }

                if (protocol == CommunicationProtocol.REST_API) {
                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = { endpoint = it },
                        label = { Text("Endpoint REST (HTTP LAN)") },
                        placeholder = { Text("http://192.168.1.x/api/...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "input_iot_device_endpoint" },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekVioletPrimary,
                            unfocusedBorderColor = SleekBorder
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = bleMac,
                        onValueChange = { bleMac = it },
                        label = { Text("Dirección MAC BLE / UUID") },
                        placeholder = { Text("AA:BB:CC:DD:EE:FF") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "input_iot_device_ble" },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekVioletPrimary,
                            unfocusedBorderColor = SleekBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newDev = SmartDeviceEntity(
                            deviceId = "IOT-CUSTOM-" + (100..999).random(),
                            name = name.trim(),
                            zone = zone.trim().ifBlank { "Condominio Alfa" },
                            deviceType = type.name,
                            protocol = protocol.name,
                            endpointUrl = if (protocol == CommunicationProtocol.REST_API) endpoint.trim() else "",
                            bleMacOrUuid = if (protocol == CommunicationProtocol.BLUETOOTH_LE) bleMac.trim() else "",
                            isOn = true,
                            isOnline = true
                        )
                        onAdd(newDev)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekVioletPrimary,
                    contentColor = SleekVioletDark
                ),
                modifier = Modifier.semantics { testTag = "confirm_add_iot_device_button" }
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

@Composable
private fun ScanDiscoveredDevicesDialog(
    isScanning: Boolean,
    discovered: List<DiscoveredIotDevice>,
    onDismiss: () -> Unit,
    onAddDiscovered: (DiscoveredIotDevice) -> Unit,
    onReScan: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ESCÁNER IOT CONDOMINIAL",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekVioletPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = SleekVioletPrimary,
                        strokeWidth = 2.dp
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Buscando actuadores Bluetooth LE y endpoints REST LAN en la subred local...",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (discovered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isScanning) "Escaneando señales..." else "No se detectaron dispositivos nuevos.",
                            color = SleekTextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    discovered.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SleekSurfaceVariant)
                                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.type.iconEmoji} ${item.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${item.zone} • ${item.protocol.label} (${item.endpointOrAddress})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SleekTextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { onAddDiscovered(item) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekVioletPrimary,
                                    contentColor = SleekVioletDark
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Vincular", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onReScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekSurfaceVariant,
                    contentColor = SleekVioletPrimary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-Escanear", fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = SleekTextSecondary)
            }
        }
    )
}
