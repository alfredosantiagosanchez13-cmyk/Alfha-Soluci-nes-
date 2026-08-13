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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MemoryNodeEntity
import com.example.ui.MedusaTab
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceHeader
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletContainer
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletPrimary

@Composable
fun TopNeuralHeader(
    onOpenKeyDialog: () -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SleekBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "NEURAL SYSTEM v4.2",
                style = MaterialTheme.typography.labelSmall,
                color = SleekVioletPrimary.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sistema Medusa OS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onOpenKeyDialog,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SleekSurfaceVariant)
                    .border(1.dp, SleekBorder, CircleShape)
                    .semantics { testTag = "api_key_button" }
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Configurar API Key",
                    tint = SleekVioletPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(1.dp, SleekBorder, CircleShape)
                    .background(SleekSurface)
                    .semantics { testTag = "neural_core_avatar" },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SleekVioletPrimary, SleekVioletDark)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun SynapticAlignmentCard(
    score: Float,
    memoryCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Radial dashed circle core
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .drawWithContent {
                        drawContent()
                        val strokeWidth = 2.dp.toPx()
                        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                        drawCircle(
                            color = SleekVioletPrimary.copy(alpha = 0.4f),
                            radius = size.minDimension / 2 - strokeWidth,
                            style = Stroke(width = strokeWidth, pathEffect = dashPathEffect)
                        )
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(SleekBackground)
                        .border(1.dp, SleekVioletPrimary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🪼",
                        fontSize = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = String.format("%.1f%%", score),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp,
                color = SleekVioletPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "ALINEACIÓN SINÁPTICA & APRENDIZAJE",
                style = MaterialTheme.typography.labelSmall,
                color = SleekTextSecondary.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp
            )
        }
    }
}

@Composable
fun MetricsGridCard(
    memoryCount: Int,
    messageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Long-term Memory node card
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(SleekSurfaceVariant)
                .border(1.dp, SleekBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "MEMORIA LARGO PLAZO",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SleekVioletPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$memoryCount Nodos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SleekTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Meter progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(SleekBorder)
            ) {
                val fillWidthPercent = (memoryCount.toFloat() / 20f).coerceIn(0.15f, 1.0f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fillWidthPercent)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(SleekVioletPrimary)
                )
            }
        }

        // Context Aware status card
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(SleekSurfaceVariant)
                .border(1.dp, SleekBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "CONTEXTO ACTIVO",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SleekVioletPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (messageCount > 0) "Activo ($messageCount)" else "En Espera",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SleekTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(12.dp)
                        .clip(CircleShape)
                        .background(SleekVioletPrimary)
                )
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(12.dp)
                        .clip(CircleShape)
                        .background(SleekVioletPrimary.copy(alpha = 0.5f))
                )
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(12.dp)
                        .clip(CircleShape)
                        .background(SleekVioletPrimary.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
fun MemoryNodeCard(
    node: MemoryNodeEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = when (node.category) {
        "PREFERENCE" -> "✨"
        "DIRECTIVE" -> "🛡️"
        "SECURITY" -> "🔒"
        else -> "👁️"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SleekVioletDark),
            contentAlignment = Alignment.Center
        ) {
            Text(text = categoryIcon, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = node.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextPrimary
                )
                if (node.isUserAdded) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MANUAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = SleekVioletPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = node.detail,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = SleekTextSecondary.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar nodo de memoria",
                tint = SleekTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SleekBottomPillNav(
    activeTab: MedusaTab,
    onTabSelected: (MedusaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(SleekSurfaceVariant)
                .border(1.dp, SleekBorder, RoundedCornerShape(32.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(MedusaTab.CORE_MATRIX, "Matriz", Icons.Default.Psychology),
                Triple(MedusaTab.NEURAL_CHAT, "IA", Icons.Default.AutoAwesome),
                Triple(MedusaTab.QR_SCANNER, "Escáner", Icons.Default.QrCodeScanner),
                Triple(MedusaTab.SMART_PARCEL, "Paquetes", Icons.Default.Inventory2),
                Triple(MedusaTab.MEMORY_VAULT, "Bóveda", Icons.Default.Memory)
            )

            tabs.forEach { (tab, label, icon) ->
                val selected = activeTab == tab
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (selected) SleekVioletPrimary else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .semantics {
                            testTag = "nav_tab_${tab.name.lowercase()}"
                            contentDescription = label
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) SleekVioletDark else SleekTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    if (selected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekVioletDark
                        )
                    }
                }
            }
        }
    }
}
