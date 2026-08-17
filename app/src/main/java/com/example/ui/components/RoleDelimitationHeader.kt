package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserProfile
import com.example.ui.UserRole
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

@Composable
fun RoleDelimitationHeader(
    activeRole: UserRole,
    currentUserProfile: UserProfile? = null,
    onRoleSelected: (UserRole) -> Unit,
    onOpenApiKey: () -> Unit,
    onOpenAuth: () -> Unit = {},
    onOpenNexusSettings: () -> Unit = {},
    onLockPin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SleekBackground)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        // Top Header Info Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2544))
                        .border(1.5.dp, Color(0xFFFFD54F), CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_medusa_logo),
                        contentDescription = "Logo Medusa Alpha",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activeRole == UserRole.ALFHA_SANTIAGO) Color(0xFF10B981) else SleekVioletPrimary
                                )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "MEDUSA ALFHA · DELIMITACIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekVioletPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = activeRole.label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        fontSize = 19.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // PIN Lock button
                IconButton(
                    onClick = onLockPin,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SleekSurfaceVariant)
                        .border(1.dp, SleekBorderSubtle, CircleShape)
                        .semantics {
                            testTag = "lock_pin_header_btn"
                            contentDescription = "Bloquear con PIN de acceso"
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloquear PIN",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Sleek Nexus Customizer Button
                IconButton(
                    onClick = onOpenNexusSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SleekSurfaceVariant)
                        .border(1.dp, SleekBorderSubtle, CircleShape)
                        .semantics {
                            testTag = "nexus_settings_header_btn"
                            contentDescription = "Personalización Sleek Nexus"
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Sleek Nexus Settings",
                        tint = SleekVioletPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Firebase Auth Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (currentUserProfile != null) Color(0xFF064E3B) else SleekSurfaceVariant)
                        .border(
                            1.dp,
                            if (currentUserProfile != null) Color(0xFF10B981) else SleekBorderSubtle,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onOpenAuth() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .semantics { testTag = "firebase_auth_header_btn" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Autenticación Firebase",
                            tint = if (currentUserProfile != null) Color(0xFF34D399) else SleekVioletPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentUserProfile != null) "AUTH: ${currentUserProfile.displayName.take(8)}" else "INICIAR SESIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (currentUserProfile != null) Color(0xFFA7F3D0) else SleekTextPrimary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Key dialog trigger button
                IconButton(
                    onClick = onOpenApiKey,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SleekSurfaceVariant)
                        .border(1.dp, SleekBorderSubtle, CircleShape)
                        .semantics { testTag = "api_key_header_btn" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Configurar Key",
                        tint = SleekVioletPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Active Role Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when (activeRole) {
                                UserRole.ALFHA_SANTIAGO -> Color(0xFF1E1B4B)
                                UserRole.RESIDENTES -> Color(0xFF064E3B)
                                UserRole.GUARDIA -> Color(0xFF1E293B)
                                UserRole.ADMINISTRACION -> Color(0xFF312E81)
                                UserRole.TRABAJADOR -> Color(0xFF422006)
                            }
                        )
                        .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = activeRole.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (activeRole) {
                            UserRole.ALFHA_SANTIAGO -> Color(0xFFA7F3D0)
                            UserRole.RESIDENTES -> Color(0xFF6EE7B7)
                            UserRole.GUARDIA -> Color(0xFF38BDF8)
                            UserRole.ADMINISTRACION -> Color(0xFFC084FC)
                            UserRole.TRABAJADOR -> Color(0xFFFDE047)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Role Selector Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(UserRole.values()) { role ->
                val selected = activeRole == role
                val (chipIcon, chipBg) = when (role) {
                    UserRole.ALFHA_SANTIAGO -> Pair(Icons.Default.AdminPanelSettings, Color(0xFF8B5CF6))
                    UserRole.RESIDENTES -> Pair(Icons.Default.Home, Color(0xFF10B981))
                    UserRole.GUARDIA -> Pair(Icons.Default.Security, Color(0xFF0284C7))
                    UserRole.ADMINISTRACION -> Pair(Icons.Default.Badge, Color(0xFFEC4899))
                    UserRole.TRABAJADOR -> Pair(Icons.Default.Engineering, Color(0xFFD97706))
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) chipBg else SleekSurface)
                        .border(
                            1.dp,
                            if (selected) Color.White.copy(alpha = 0.4f) else SleekBorderSubtle,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onRoleSelected(role) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .semantics {
                            testTag = "role_chip_${role.name.lowercase()}"
                            contentDescription = "Cambiar a perfil ${role.label}"
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = chipIcon,
                        contentDescription = null,
                        tint = if (selected) Color.White else SleekTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = role.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else SleekTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
