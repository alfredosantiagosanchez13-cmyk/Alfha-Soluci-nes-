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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UserRole
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekBorderSubtle
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekVioletDark
import com.example.ui.theme.SleekVioletPrimary

@Composable
fun AlphaRootCommandCard(
    currentRole: UserRole,
    onNavigateToChat: () -> Unit,
    onNavigateToVault: () -> Unit,
    onPurgeExpiredPasses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .semantics {
                testTag = "alpha_root_command_card"
                contentDescription = "Panel de Control Alpha Santiago"
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
                        .background(Color(0xFF1E1B4B))
                        .border(1.dp, Color(0xFF8B5CF6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color(0xFFA7F3D0),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (currentRole == UserRole.ALFHA_SANTIAGO) "NÚCLEO COMANDO SANTIAGO (ALPHA)" else "PANEL ADMINISTRATIVO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA7F3D0),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Nivel de acceso supremo • Matriz de Control Medusa",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System Delimitation Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToChat,
                colors = ButtonDefaults.buttonColors(containerColor = SleekVioletPrimary, contentColor = SleekVioletDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .semantics { testTag = "alpha_chat_shortcut" }
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("IA Santiago", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = onNavigateToVault,
                colors = ButtonDefaults.buttonColors(containerColor = SleekSurfaceVariant, contentColor = SleekVioletPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .semantics { testTag = "alpha_vault_shortcut" }
            ) {
                Icon(imageVector = Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Bóveda", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Button(
                onClick = onPurgeExpiredPasses,
                colors = ButtonDefaults.buttonColors(containerColor = SleekSurfaceVariant, contentColor = Color(0xFFFCA5A5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .semantics { testTag = "purge_passes_btn" }
            ) {
                Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Depurar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
