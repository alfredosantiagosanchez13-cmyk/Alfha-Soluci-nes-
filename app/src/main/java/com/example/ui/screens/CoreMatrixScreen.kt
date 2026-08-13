package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AccessLogEntity
import com.example.data.db.AccessPassEntity
import com.example.data.db.MemoryNodeEntity
import com.example.ui.UserRole
import com.example.ui.components.AdminResidentManagerCard
import com.example.ui.components.AlphaRootCommandCard
import com.example.ui.components.GuardAccessConsoleCard
import com.example.ui.components.MemoryNodeCard
import com.example.ui.components.MetricsGridCard
import com.example.ui.components.MonthlyAccessDashboardCard
import com.example.ui.components.ResidentQrGeneratorCard
import com.example.ui.components.SynapticAlignmentCard
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletPrimary

@Composable
fun CoreMatrixScreen(
    activeRole: UserRole,
    synapticScore: Float,
    memoryCount: Int,
    messageCount: Int,
    memories: List<MemoryNodeEntity>,
    accessPasses: List<AccessPassEntity>,
    accessLogs: List<AccessLogEntity> = emptyList(),
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Role Specific Main Card
        item {
            when (activeRole) {
                UserRole.ALFHA_SANTIAGO -> {
                    AlphaRootCommandCard(
                        currentRole = activeRole,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToVault = onNavigateToVault,
                        onPurgeExpiredPasses = onPurgeExpiredPasses
                    )
                }
                UserRole.ADMINISTRACION -> {
                    AdminResidentManagerCard(
                        passes = accessPasses,
                        onCreateResidentPass = onCreateResidentCredential,
                        onDeletePass = onDeletePass
                    )
                }
                UserRole.RESIDENTES -> {
                    ResidentQrGeneratorCard(
                        passes = accessPasses,
                        onCreatePass = onCreatePass,
                        onDeletePass = onDeletePass
                    )
                }
                UserRole.GUARDIA -> {
                    GuardAccessConsoleCard(
                        onValidateCode = onValidatePassCode,
                        onNavigateToParcelScanner = onNavigateToParcel
                    )
                }
            }
        }

        // 2. Secondary Role Contexts for Santiago / Alpha Root Control
        if (activeRole == UserRole.ALFHA_SANTIAGO) {
            item {
                AdminResidentManagerCard(
                    passes = accessPasses,
                    onCreateResidentPass = onCreateResidentCredential,
                    onDeletePass = onDeletePass
                )
            }
            item {
                ResidentQrGeneratorCard(
                    passes = accessPasses,
                    onCreatePass = onCreatePass,
                    onDeletePass = onDeletePass
                )
            }
            item {
                GuardAccessConsoleCard(
                    onValidateCode = onValidatePassCode,
                    onNavigateToParcelScanner = onNavigateToParcel
                )
            }
        }

        // 3. Synaptic Alignment Card Core
        item {
            SynapticAlignmentCard(
                score = synapticScore,
                memoryCount = memoryCount
            )
        }

        // 4. Memory & Context Metrics Grid
        item {
            MetricsGridCard(
                memoryCount = memoryCount,
                messageCount = messageCount
            )
        }

        // 5. Monthly Access Statistics Dashboard
        item {
            MonthlyAccessDashboardCard(
                accessLogs = accessLogs
            )
        }

        // 5. Last Learned Memory Toast Banner
        item {
            AnimatedVisibility(
                visible = lastLearnedMemory != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                lastLearnedMemory?.let { node ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SleekVioletDark)
                            .border(1.dp, SleekVioletPrimary, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SleekVioletPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NUEVA MEMORIA APRENDIDA",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = SleekVioletPrimary
                            )
                            Text(
                                text = "${node.title}: ${node.detail}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                                color = SleekTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 6. Recent Observations Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OBSERVACIONES Y MEMORIA PERSISTENTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    letterSpacing = 1.2.sp
                )
                TextButton(
                    onClick = onNavigateToVault,
                    modifier = Modifier.semantics { testTag = "view_all_vault_button" }
                ) {
                    Text(
                        text = "Ver Bóveda ($memoryCount)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SleekVioletPrimary
                    )
                }
            }
        }

        if (memories.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekSurface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "El núcleo de memoria está listo para registrar datos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextMuted
                    )
                }
            }
        } else {
            items(memories.take(3), key = { it.id }) { node ->
                MemoryNodeCard(
                    node = node,
                    onDelete = { onDeleteMemory(node) }
                )
            }
        }

        // 7. Direct Quick Call To Neural Chat
        item {
            Button(
                onClick = onNavigateToChat,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .semantics { testTag = "open_neural_chat_button" },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekSurface,
                    contentColor = SleekTextPrimary
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consultar con Núcleo Medusa AI...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SleekTextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(SleekVioletPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Iniciar Chat",
                            tint = SleekVioletDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
