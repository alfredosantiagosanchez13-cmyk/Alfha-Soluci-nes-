package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletPrimary

enum class PrototypeModuleType(
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val isOperating: Boolean = true
) {
    // Caseta / Vigilancia
    ASISTENCIA("Asistencia", "Entrada/salida de personal con GPS y foto", "📝"),
    ACCESOS("Accesos", "Verificación QR de residentes/visitantes", "🚪"),
    FICHA_RESIDENTE("Ficha de Residente", "Familia, vehículos, personal, paquetería y accesos por casa", "🪪"),
    VISITANTES("Visitantes", "Registro y control de visitas", "👥"),
    PAQUETERIA("Paquetería", "Recepción y entrega inteligente con WhatsApp", "📦"),
    PLACAS("Placas", "Consulta rápida de placas registradas", "🚗"),
    INCIDENTES("Incidentes", "Reporte y seguimiento de incidentes", "🚨"),
    RONDINES("Rondines", "Checkpoints de recorrido de guardias", "🚶", isOperating = false),
    GUARDIAS("Guardias", "Entrega de turno y relevo de caseta", "🔄"),
    BITACORA("Bitácora", "Consignas y reglas digitalizadas de caseta", "📋"),

    // Administración
    DIRECTORIO("Directorio", "Directorio general de contactos", "📁"),
    RESIDENTES("Residentes", "Padrón de residentes por condominio", "🏠"),
    VEHICULOS("Vehículos", "Control vehicular y placas registradas", "🚙"),
    REPORTES("Reportes", "Asistencia, tardanzas y exportación CSV", "📈"),
    TRABAJOS_ESPECIALES("Trabajos Especiales", "Programación, checklist de recursos y ejecución de mantenimiento especial", "🌿"),
    ESTADISTICAS("Estadísticas", "Panel ejecutivo en tiempo real", "📊"),
    HISTORIAL_ACTIVIDAD("Historial de Actividad", "Quién hizo qué acción y cuándo", "🕵️"),
    AUDITORIAS("Auditorías", "Revisión de cumplimiento operativo", "🔍"),
    RIESGOS("Riesgos", "Detección temprana de riesgos", "⚠️"),
    IA_MEDUSA("IA Medusa", "Reportes ejecutivos generados con IA", "🤖"),
    CONFIGURACION("Configuración", "Panel Medusa Admin y generación de QR", "⚙️")
}

@Composable
fun PrototypeModuleDialog(
    module: PrototypeModuleType,
    onDismiss: () -> Unit,
    onRegisterAttendance: (name: String, action: String, gps: String, note: String) -> Unit = { _, _, _, _ -> },
    onRegisterVisitor: (name: String, house: String, plates: String) -> Unit = { _, _, _ -> },
    onReportIncident: (title: String, priority: String, detail: String) -> Unit = { _, _, _ -> },
    onRecordPatrol: (checkpoint: String) -> Unit = {},
    onGenerateAiReport: ((String) -> Unit) -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToQr: () -> Unit = {},
    onNavigateToParcel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var input1 by remember { mutableStateOf("") }
    var input2 by remember { mutableStateOf("") }
    var input3 by remember { mutableStateOf("") }
    var actionDoneMessage by remember { mutableStateOf<String?>(null) }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var aiReportResult by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, SleekBorder, RoundedCornerShape(24.dp)),
        containerColor = SleekSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = module.iconEmoji, fontSize = 24.sp)
                    Column {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 17.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (module.isOperating) Color(0xFF10B981).copy(alpha = 0.15f)
                                    else Color(0xFFFFB300).copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (module.isOperating) "● OPERANDO" else "● ATENCIÓN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (module.isOperating) Color(0xFF10B981) else Color(0xFFFFB300)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = SleekTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = module.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SleekTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                if (actionDoneMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = actionDoneMessage.orEmpty(),
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                when (module) {
                    PrototypeModuleType.ASISTENCIA -> {
                        OutlinedTextField(
                            value = input1,
                            onValueChange = { input1 = it },
                            label = { Text("Nombre del Colaborador / Guardia", fontSize = 12.sp) },
                            placeholder = { Text("Ej: Carlos Méndez") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SleekTextPrimary,
                                unfocusedTextColor = SleekTextPrimary,
                                focusedBorderColor = SleekVioletPrimary,
                                unfocusedBorderColor = SleekBorder
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val name = input1.ifBlank { "Personal de Turno" }
                                    onRegisterAttendance(name, "Entrada Asistencia", "GPS: Lat 20.6767, Lng -103.3475", "Foto de verificación OK")
                                    actionDoneMessage = "¡Entrada registrada con GPS y foto validada!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Entrada", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    val name = input1.ifBlank { "Personal de Turno" }
                                    onRegisterAttendance(name, "Salida Asistencia", "GPS: Lat 20.6767, Lng -103.3475", "Relevo completado")
                                    actionDoneMessage = "¡Salida registrada con éxito!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Salida", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    PrototypeModuleType.VISITANTES -> {
                        OutlinedTextField(
                            value = input1,
                            onValueChange = { input1 = it },
                            label = { Text("Nombre del Visitante", fontSize = 12.sp) },
                            placeholder = { Text("Ej: Mario Bros") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = input2,
                            onValueChange = { input2 = it },
                            label = { Text("Casa de Destino", fontSize = 12.sp) },
                            placeholder = { Text("Ej: Casa 13") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = input3,
                            onValueChange = { input3 = it },
                            label = { Text("Placas / Vehículo", fontSize = 12.sp) },
                            placeholder = { Text("Ej: JNZ-8821") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (input1.isNotBlank() && input2.isNotBlank()) {
                                    onRegisterVisitor(input1, input2, input3.ifBlank { "Peatonal" })
                                    actionDoneMessage = "Visitante $input1 registrado para $input2."
                                    input1 = ""
                                    input2 = ""
                                    input3 = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Registrar e Ingresar Visita", fontWeight = FontWeight.Bold)
                        }
                    }

                    PrototypeModuleType.PLACAS -> {
                        OutlinedTextField(
                            value = input1,
                            onValueChange = { input1 = it },
                            label = { Text("Número de Placa", fontSize = 12.sp) },
                            placeholder = { Text("Ej: JNZ-8821") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val plate = input1.uppercase().trim()
                                actionDoneMessage = if (plate.contains("JNZ") || plate.contains("13") || plate.contains("ABC")) {
                                    "🟢 PLACA AUTORIZADA: Residente Casa 13 (Familia Paraíso)"
                                } else {
                                    "🟡 PLACA NO REGISTRADA: Requiere pase de visitante o autorización de residente."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consultar Placa", fontWeight = FontWeight.Bold)
                        }
                    }

                    PrototypeModuleType.INCIDENTES -> {
                        OutlinedTextField(
                            value = input1,
                            onValueChange = { input1 = it },
                            label = { Text("Título del Incidente", fontSize = 12.sp) },
                            placeholder = { Text("Ej: Ruido excesivo / Vehículo mal estacionado") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = input2,
                            onValueChange = { input2 = it },
                            label = { Text("Detalle y Ubicación", fontSize = 12.sp) },
                            placeholder = { Text("Ej: Casa 24 con música alta después de las 23:00") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (input1.isNotBlank()) {
                                    onReportIncident(input1, "ALTA", input2.ifBlank { "Sin detalle" })
                                    actionDoneMessage = "Incidente reportado y guardado en la memoria central."
                                    input1 = ""
                                    input2 = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Enviar Reporte de Incidente", fontWeight = FontWeight.Bold)
                        }
                    }

                    PrototypeModuleType.RONDINES -> {
                        Text(
                            text = "Ruta de Rondín: Caseta -> Parque Infantil -> Alberca -> Perímetro Norte",
                            fontSize = 12.sp,
                            color = SleekTextPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    onRecordPatrol("Parque Infantil")
                                    actionDoneMessage = "Checkpoint 'Parque Infantil' verificado y marcado."
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Check Parque", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    onRecordPatrol("Perímetro Norte")
                                    actionDoneMessage = "Checkpoint 'Perímetro Norte' verificado y marcado."
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Check Perímetro", fontSize = 11.sp)
                            }
                        }
                    }

                    PrototypeModuleType.IA_MEDUSA -> {
                        if (isGeneratingAi) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SleekVioletPrimary)
                                Text("Consultando Núcleo Gemini IA...", fontSize = 12.sp, color = SleekTextSecondary)
                            }
                        } else if (aiReportResult != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SleekSurfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = aiReportResult.orEmpty(),
                                    fontSize = 12.sp,
                                    color = SleekTextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                isGeneratingAi = true
                                onGenerateAiReport { report ->
                                    isGeneratingAi = false
                                    aiReportResult = report
                                    actionDoneMessage = "Reporte ejecutivo sintetizado con IA."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generar Reporte Ejecutivo con IA", fontWeight = FontWeight.Bold)
                        }
                    }

                    PrototypeModuleType.ACCESOS -> {
                        Button(
                            onClick = {
                                onDismiss()
                                onNavigateToQr()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Abrir Escáner y Validador QR", fontWeight = FontWeight.Bold)
                        }
                    }

                    PrototypeModuleType.PAQUETERIA -> {
                        Button(
                            onClick = {
                                onDismiss()
                                onNavigateToParcel()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Abrir Gestión de Paquetería IA", fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        // Generic administrative / information module view
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SleekSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Base de Datos Sincronizada",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "Los datos de este módulo están respaldados localmente en SQLite / Room DB y coordinados por el Núcleo Medusa OS.",
                                    fontSize = 11.sp,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                actionDoneMessage = "Módulo '${module.title}' sincronizado y validado."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sincronizar y Exportar Estado", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Listo", color = SleekTextPrimary, fontSize = 12.sp)
            }
        }
    )
}
