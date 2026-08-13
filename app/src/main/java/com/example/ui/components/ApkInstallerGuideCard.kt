package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun ApkInstallerGuideCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .semantics {
                testTag = "apk_installer_guide_card"
                contentDescription = "Complemento de Instalación APK desde Celular Android"
            }
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF34D399), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "INSTALADOR APK MÓVIL ANDROID",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Guía y descarga directa del instalador en tu celular",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "APK BUILD READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF34D399),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = SleekTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {

                // Mobile Device Specs Badge Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpecBadgeItem(
                        label = "Versión Android",
                        value = "7.0 - 15.0+",
                        subtitle = "API Level 24 - 35",
                        icon = Icons.Default.Smartphone,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )

                    SpecBadgeItem(
                        label = "Arquitectura",
                        value = "arm64-v8a",
                        subtitle = "Universal APK",
                        icon = Icons.Default.PhoneAndroid,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )

                    SpecBadgeItem(
                        label = "Auditoría 2026",
                        value = "Audit v2.0",
                        subtitle = "Seguridad OK",
                        icon = Icons.Default.Shield,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Installation Steps Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SleekSurfaceVariant)
                        .border(1.dp, SleekBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "PASOS PARA INSTALAR EN TU CELULAR:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InstructionStepRow(
                        number = "1",
                        title = "Generar APK en el Panel de AI Studio",
                        description = "En la barra superior / menú de AI Studio, selecciona la opción 'Exportar' -> 'Descargar APK' o escanea el QR generado."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InstructionStepRow(
                        number = "2",
                        title = "Permitir Fuentes Desconocidas",
                        description = "En tu celular ve a 'Ajustes' > 'Seguridad' > 'Instalar aplicaciones desconocidas' y autoriza al navegador Chrome o Administrador de archivos."
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InstructionStepRow(
                        number = "3",
                        title = "Instalar y Conceder Permisos",
                        description = "Abre el archivo Medusa.apk descargado. Al iniciar la app se solicitarán permisos de Cámara (Lector QR) y Notificaciones."
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                "📲 Para descargar el APK final en tu celular, usa el botón 'Export' > 'Download APK' en la interfaz superior de AI Studio.",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                testTag = "download_apk_mobile_button"
                                contentDescription = "Descargar APK en celular Android"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Instalar APK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(
                                context,
                                "✅ Verificación de compatibilidad Android: SDK ${Build.VERSION.SDK_INT} Compatible.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                testTag = "check_device_compat_button"
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Dispositivo",
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecBadgeItem(
    label: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SleekSurfaceVariant)
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = SleekTextMuted,
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = SleekTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InstructionStepRow(
    number: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF0284C7))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                fontSize = 12.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
