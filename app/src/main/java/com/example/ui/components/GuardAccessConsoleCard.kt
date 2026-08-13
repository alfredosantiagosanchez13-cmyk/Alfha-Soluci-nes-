package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.data.db.AccessPassEntity
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import kotlinx.coroutines.launch

@Composable
fun GuardAccessConsoleCard(
    onValidateCode: suspend (String) -> Pair<Boolean, AccessPassEntity?>,
    onNavigateToParcelScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var qrCodeInput by remember { mutableStateOf("") }
    var validationResult by remember { mutableStateOf<Pair<Boolean, AccessPassEntity?>?>(null) }
    var isValidating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .semantics {
                testTag = "guard_access_console"
                contentDescription = "Consola de Validación de Accesos para Guardia"
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
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF0284C7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CASETA DE VIGILANCIA - VALIDACIÓN QR",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Escanea o ingresa códigos QR presentados en pluma",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner Input
        OutlinedTextField(
            value = qrCodeInput,
            onValueChange = {
                qrCodeInput = it
                validationResult = null
            },
            label = { Text("Código de Pase QR (ej: MEDUSA-QR-XXXX)", fontSize = 12.sp, color = SleekTextSecondary) },
            placeholder = { Text("Escanea o escribe el código...", color = SleekTextMuted) },
            singleLine = true,
            leadingIcon = {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF38BDF8))
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "guard_qr_input_field" },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0284C7),
                unfocusedBorderColor = SleekBorderSubtle,
                focusedTextColor = SleekTextPrimary,
                unfocusedTextColor = SleekTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (qrCodeInput.isBlank()) {
                        Toast.makeText(context, "Ingresa un código QR para validar", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isValidating = true
                        val res = onValidateCode(qrCodeInput)
                        validationResult = res
                        isValidating = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .semantics { testTag = "validate_qr_btn" }
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Validar Pase", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = onNavigateToParcelScanner,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B), contentColor = Color(0xFF38BDF8)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .semantics { testTag = "guard_parcel_btn" }
            ) {
                Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Paquetería IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Validation Banner Result Display
        val result = validationResult
        if (result != null) {
            val (approved, pass) = result
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (approved) Color(0xFF064E3B) else Color(0xFF450A0A))
                    .border(1.dp, if (approved) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (approved) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (approved) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (approved) "¡ACCESO AUTORIZADO!" else "ACCESO DENEGADO / EXPIRADO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (approved) Color(0xFFA7F3D0) else Color(0xFFFECDD3),
                            fontSize = 14.sp
                        )

                        if (pass != null) {
                            Text(
                                text = "Visita: ${pass.visitorName} → ${pass.residentHouse} (${pass.residentName})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Código QR no registrado en el sistema Medusa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
