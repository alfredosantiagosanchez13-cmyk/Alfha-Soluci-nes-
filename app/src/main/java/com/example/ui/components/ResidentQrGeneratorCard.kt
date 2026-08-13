package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
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
import com.example.data.db.AccessPassEntity
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResidentQrGeneratorCard(
    passes: List<AccessPassEntity>,
    onCreatePass: (house: String, resident: String, visitor: String, type: String, hours: Int) -> Unit,
    onDeletePass: (AccessPassEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var visitorName by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("Casa 21") }
    var residentName by remember { mutableStateOf("Santiago (Alfha)") }
    var selectedType by remember { mutableStateOf("VISITOR") } // VISITOR, DELIVERY, FAMILY, SERVICE
    var durationHours by remember { mutableStateOf(24) }

    var newlyCreatedPassCode by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .semantics {
                testTag = "resident_qr_generator"
                contentDescription = "Generador de Códigos QR para Residentes"
            }
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF064E3B))
                        .border(1.dp, Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = Color(0xFF6EE7B7),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GENERADOR DE CÓDIGOS QR - RESIDENTES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Genera pases digitales para visitas, repartidores y servicios",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Fields
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = houseNumber,
                onValueChange = { houseNumber = it },
                label = { Text("Residencia", fontSize = 11.sp, color = SleekTextSecondary) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics { testTag = "qr_house_input" },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = SleekBorderSubtle,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary
                )
            )

            OutlinedTextField(
                value = residentName,
                onValueChange = { residentName = it },
                label = { Text("Residente", fontSize = 11.sp, color = SleekTextSecondary) },
                singleLine = true,
                modifier = Modifier
                    .weight(1.3f)
                    .semantics { testTag = "qr_resident_input" },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = SleekBorderSubtle,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = visitorName,
            onValueChange = { visitorName = it },
            label = { Text("Nombre de la Visita / Repartidor", fontSize = 12.sp, color = SleekTextSecondary) },
            placeholder = { Text("Ej: Carlos Mendoza - UberEats", color = SleekTextMuted) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "qr_visitor_input" },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = SleekBorderSubtle,
                focusedTextColor = SleekTextPrimary,
                unfocusedTextColor = SleekTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Access Type Chips
        Text(
            text = "TIPO DE ACCESO:",
            style = MaterialTheme.typography.labelSmall,
            color = SleekTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val types = listOf(
                "VISITOR" to "Visita",
                "DELIVERY" to "Paquetería",
                "FAMILY" to "Familiar",
                "SERVICE" to "Servicio"
            )

            types.forEach { (typeKey, label) ->
                val selected = selectedType == typeKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color(0xFF10B981) else SleekSurfaceVariant)
                        .border(1.dp, if (selected) Color.White.copy(alpha = 0.5f) else SleekBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp)
                        .semantics {
                            testTag = "type_chip_$typeKey"
                            contentDescription = "Seleccionar tipo $label"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color(0xFF022C22) else SleekTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expiration / Duration Selector
        Text(
            text = "TIEMPO DE VALIDEZ Y EXPIRACIÓN CONFIGURADA:",
            style = MaterialTheme.typography.labelSmall,
            color = SleekTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        val durationOptions = listOf(
            1 to "1 hora",
            4 to "4 horas",
            12 to "12 horas",
            24 to "24 horas",
            48 to "48 horas",
            168 to "7 días"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            durationOptions.forEach { (hrs, label) ->
                val selected = durationHours == hrs
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color(0xFF0284C7) else SleekSurfaceVariant)
                        .border(1.dp, if (selected) Color(0xFF38BDF8) else SleekBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(vertical = 7.dp)
                        .semantics {
                            testTag = "duration_chip_${hrs}h"
                            contentDescription = "Configurar expiración $label"
                        }
                        .clickable { durationHours = hrs },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else SleekTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val previewExpDate = remember(durationHours) {
            val sdf = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
            sdf.format(Date(System.currentTimeMillis() + durationHours * 3600 * 1000L))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Expiración estimada: $previewExpDate",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Generate Button
        Button(
            onClick = {
                val vName = visitorName.ifBlank { "Visita Residencial" }
                onCreatePass(houseNumber, residentName, vName, selectedType, durationHours)
                newlyCreatedPassCode = "MEDUSA-QR-GENERATED"
                visitorName = ""
                Toast.makeText(context, "¡Código QR de Acceso Creado!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color(0xFF022C22)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .semantics { testTag = "generate_qr_button" },
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generar Pase QR de Acceso", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Active Generated QR Display Preview if available
        val activePass = passes.firstOrNull()
        if (activePass != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ÚLTIMO CÓDIGO QR GENERADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    QrCodeCanvas(
                        qrContent = activePass.passCode,
                        sizeDp = 170.dp,
                        qrColor = Color(0xFF0F172A),
                        backgroundColor = Color(0xFFFFFFFF)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activePass.passCode,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFA7F3D0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Invitado: ${activePass.visitorName} • ${activePass.residentHouse}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )

                    val activeExpStr = remember(activePass.validUntilTimestamp) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
                        sdf.format(Date(activePass.validUntilTimestamp))
                    }

                    Text(
                        text = "Vence el: $activeExpStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Medusa QR Pass", activePass.passCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B), contentColor = Color(0xFF6EE7B7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.semantics { testTag = "copy_qr_button" }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Código", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val inviteMsg = "🎫 *INVITACIÓN DE ACCESO MEDUSA OS*\n" +
                                        "🏡 *Destino:* ${activePass.residentHouse} (${activePass.residentName})\n" +
                                        "👤 *Visitante:* ${activePass.visitorName}\n" +
                                        "🔑 *Código QR:* `${activePass.passCode}`\n" +
                                        "⏰ *Válido hasta:* $activeExpStr\n" +
                                        "Muestra este código al guardia en caseta."
                                val clip = ClipData.newPlainText("Invitación Medusa OS", inviteMsg)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "¡Texto de invitación copiado para enviar!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.semantics { testTag = "share_invitation_text_button" }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar Invitación", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Passes History List
        if (passes.size > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HISTORIAL DE PASES GENERADOS (${passes.size})",
                style = MaterialTheme.typography.labelSmall,
                color = SleekTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            passes.take(3).forEach { pass ->
                PassItemRow(pass = pass, onDelete = { onDeletePass(pass) })
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun PassItemRow(
    pass: AccessPassEntity,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val expStr = sdf.format(Date(pass.validUntilTimestamp))
    val isExpired = System.currentTimeMillis() > pass.validUntilTimestamp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SleekSurfaceVariant)
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (pass.isUsed) Color(0xFF1E293B) else if (isExpired) Color(0xFF450A0A) else Color(0xFF064E3B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                tint = if (pass.isUsed) Color(0xFF94A3B8) else if (isExpired) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${pass.visitorName} (${pass.residentHouse})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 12.sp
            )
            Text(
                text = "Validez: $expStr • ${pass.passCode}",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextMuted,
                fontSize = 10.sp
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar Pase",
                tint = SleekTextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
