package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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

@Composable
fun AdminResidentManagerCard(
    passes: List<AccessPassEntity>,
    onCreateResidentPass: (house: String, residentName: String, level: String, isPermanent: Boolean) -> Unit,
    onDeletePass: (AccessPassEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var searchHouseQuery by remember { mutableStateOf("") }
    var residentNameInput by remember { mutableStateOf("") }
    var houseInput by remember { mutableStateOf("Casa 01") }
    var selectedLevel by remember { mutableStateOf("PROPIETARIO") } // PROPIETARIO, INQUILINO, FAMILIAR, VIP
    var isPermanentPass by remember { mutableStateOf(true) }

    var selectedPassForQrPreview by remember { mutableStateOf<AccessPassEntity?>(null) }

    val filteredPasses = remember(passes, searchHouseQuery) {
        if (searchHouseQuery.isBlank()) {
            passes
        } else {
            passes.filter {
                it.residentHouse.contains(searchHouseQuery, ignoreCase = true) ||
                        it.residentName.contains(searchHouseQuery, ignoreCase = true) ||
                        it.visitorName.contains(searchHouseQuery, ignoreCase = true) ||
                        it.passCode.contains(searchHouseQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .semantics {
                testTag = "admin_resident_manager_card"
                contentDescription = "Módulo de Administración y Credenciales de Residentes"
            }
    ) {
        // Card Header
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
                        .background(Color(0xFF312E81))
                        .border(1.dp, Color(0xFF818CF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = Color(0xFFC084FC),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ADMINISTRACIÓN - CREDANCIALES RESIDENCIALES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC084FC),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Gestión centralizada de accesos e identificaciones QR",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create Resident Credential Section
        Text(
            text = "EMITIR NUEVA CREDENCIAL DIGITAL DE RESIDENTE",
            style = MaterialTheme.typography.labelSmall,
            color = SleekTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = houseInput,
                onValueChange = { houseInput = it },
                label = { Text("Residencia", fontSize = 11.sp, color = SleekTextSecondary) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics { testTag = "admin_house_input" },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC084FC),
                    unfocusedBorderColor = SleekBorderSubtle,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary
                )
            )

            OutlinedTextField(
                value = residentNameInput,
                onValueChange = { residentNameInput = it },
                label = { Text("Nombre Completo del Residente", fontSize = 11.sp, color = SleekTextSecondary) },
                placeholder = { Text("Ej: Santiago (Alfha)", color = SleekTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1.6f)
                    .semantics { testTag = "admin_resident_name_input" },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC084FC),
                    unfocusedBorderColor = SleekBorderSubtle,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Access Level selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val levels = listOf("PROPIETARIO" to "Propietario", "INQUILINO" to "Inquilino", "FAMILIAR" to "Familiar", "VIP" to "Acceso VIP")
            levels.forEach { (lvlKey, lbl) ->
                val selected = selectedLevel == lvlKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color(0xFF6366F1) else SleekSurfaceVariant)
                        .border(1.dp, if (selected) Color.White.copy(alpha = 0.5f) else SleekBorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { selectedLevel = lvlKey }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lbl,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else SleekTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val rName = residentNameInput.ifBlank { "Residente Medusa" }
                onCreateResidentPass(houseInput, rName, selectedLevel, isPermanentPass)
                residentNameInput = ""
                Toast.makeText(context, "¡Credencial de Residente emitida con éxito!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .semantics { testTag = "issue_resident_qr_btn" }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Emitir Credencial QR Permanente", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Search & Directory List
        Text(
            text = "DIRECTORIO Y PASES ACTIVOS (${passes.size})",
            style = MaterialTheme.typography.labelSmall,
            color = SleekTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchHouseQuery,
            onValueChange = { searchHouseQuery = it },
            placeholder = { Text("Buscar por casa, residente o código QR...", color = SleekTextMuted, fontSize = 12.sp) },
            singleLine = true,
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SleekTextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "admin_search_pass_input" },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF818CF8),
                unfocusedBorderColor = SleekBorderSubtle,
                focusedTextColor = SleekTextPrimary,
                unfocusedTextColor = SleekTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredPasses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay registros que coincidan.", color = SleekTextMuted, fontSize = 12.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredPasses.take(5).forEach { pass ->
                    ResidentPassDirectoryRow(
                        pass = pass,
                        onSelectForQr = { selectedPassForQrPreview = pass },
                        onDelete = { onDeletePass(pass) }
                    )
                }
            }
        }

        // Live Selected QR Badge Modal/Card
        selectedPassForQrPreview?.let { pass ->
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0xFF818CF8), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CREDENCIAL RESIDENCIAL DIGITAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFC084FC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        Text(
                            text = pass.accessType,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA7F3D0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    QrCodeCanvas(
                        qrContent = pass.passCode,
                        sizeDp = 180.dp,
                        qrColor = Color(0xFF0F172A),
                        backgroundColor = Color(0xFFFFFFFF)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = pass.residentName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "${pass.residentHouse} • ${pass.passCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Medusa Resident Pass", "Credencial Medusa OS: ${pass.passCode} - ${pass.residentName} (${pass.residentHouse})")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Credencial copiada al portapapeles", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF312E81), contentColor = Color(0xFFC084FC)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar Credencial", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { selectedPassForQrPreview = null },
                            colors = ButtonDefaults.buttonColors(containerColor = SleekSurfaceVariant, contentColor = SleekTextSecondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cerrar", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResidentPassDirectoryRow(
    pass: AccessPassEntity,
    onSelectForQr: () -> Unit,
    onDelete: () -> Unit
) {
    val isResidentType = pass.accessType.contains("RESIDENT") || pass.accessType.contains("PROPIETARIO") || pass.accessType.contains("INQUILINO") || pass.accessType.contains("VIP")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SleekSurfaceVariant)
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onSelectForQr() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isResidentType) Color(0xFF312E81) else Color(0xFF064E3B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                tint = if (isResidentType) Color(0xFFC084FC) else Color(0xFF6EE7B7),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${pass.residentName} (${pass.residentHouse})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 12.sp
            )
            Text(
                text = "${pass.accessType} • ${pass.passCode}",
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextMuted,
                fontSize = 10.sp
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Revocar Credencial",
                tint = SleekTextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
