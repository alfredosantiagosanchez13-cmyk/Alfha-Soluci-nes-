package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun AuthDialog(
    currentUserProfile: UserProfile?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSignInEmail: (String, String) -> Unit,
    onRegisterEmail: (String, String, String, UserRole, Int?) -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onResetPassword: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (currentUserProfile != null) 2 else 0) }

    // Form inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var houseNumberStr by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.RESIDENTES) }
    var resetSuccessMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SleekSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SleekVioletPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AUTENTICACIÓN FIREBASE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        fontSize = 16.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab Header
                TabRow(
                    selectedTabIndex = if (currentUserProfile != null) 0 else selectedTab,
                    containerColor = SleekBackground,
                    contentColor = SleekVioletPrimary,
                    indicator = { tabPositions ->
                        if (tabPositions.isNotEmpty() && selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = SleekVioletPrimary
                            )
                        }
                    }
                ) {
                    if (currentUserProfile != null) {
                        Tab(
                            selected = true,
                            onClick = { },
                            text = { Text("Mi Perfil", fontWeight = FontWeight.Bold) }
                        )
                    } else {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.semantics { testTag = "auth_tab_login" },
                            text = { Text("Iniciar Sesión", fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.semantics { testTag = "auth_tab_register" },
                            text = { Text("Registrarse", fontSize = 12.sp) }
                        )
                    }
                }

                // Error Message Banner
                errorMessage?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF7F1D1D))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = err,
                            color = Color(0xFFFECACA),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }
                }

                resetSuccessMsg?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF064E3B))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = msg,
                            color = Color(0xFFA7F3D0),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = SleekVioletPrimary)
                    }
                } else if (currentUserProfile != null) {
                    // Logged in User Profile View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekBackground)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = SleekVioletPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUserProfile.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextPrimary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = currentUserProfile.email,
                                    color = SleekTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rol Asignado:", fontSize = 12.sp, color = SleekTextMuted)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SleekVioletPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentUserProfile.role.badge,
                                    color = SleekVioletPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        currentUserProfile.houseNumber?.let { house ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Casa / Residencia:", fontSize = 12.sp, color = SleekTextMuted)
                                Text("Casa #$house", fontSize = 12.sp, color = SleekTextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("UID Firebase:", fontSize = 11.sp, color = SleekTextMuted)
                            Text(
                                text = currentUserProfile.uid.take(12) + "...",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "auth_signout_btn" }
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar Sesión Segura")
                        }
                    }
                } else if (selectedTab == 0) {
                    // LOGIN TAB
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "login_email_input" },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekVioletPrimary,
                                unfocusedBorderColor = SleekBorder
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "login_password_input" },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekVioletPrimary,
                                unfocusedBorderColor = SleekBorder
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    if (email.isNotBlank()) {
                                        onResetPassword(email)
                                        resetSuccessMsg = "Se envió enlace de recuperación a $email"
                                    } else {
                                        resetSuccessMsg = "Ingresa tu correo primero"
                                    }
                                }
                            ) {
                                Text("¿Olvidaste tu contraseña?", fontSize = 11.sp, color = SleekVioletPrimary)
                            }
                        }

                        Button(
                            onClick = { onSignInEmail(email, password) },
                            enabled = email.isNotBlank() && password.length >= 6,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekVioletPrimary,
                                contentColor = SleekVioletDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "login_submit_btn" }
                        ) {
                            Text("Ingresar", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Google Sign-In Option
                        Button(
                            onClick = onGoogleSignIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekSurfaceVariant,
                                contentColor = SleekTextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SleekBorderSubtle, RoundedCornerShape(24.dp))
                                .semantics { testTag = "google_signin_btn" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = SleekVioletPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continuar con Google", fontSize = 13.sp)
                        }
                    }
                } else {
                    // REGISTER TAB
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Nombre Completo") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "register_name_input" },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekVioletPrimary,
                                unfocusedBorderColor = SleekBorder
                            )
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Correo Electrónico") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "register_email_input" },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekVioletPrimary,
                                unfocusedBorderColor = SleekBorder
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña (mínimo 6 caracteres)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "register_password_input" },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekVioletPrimary,
                                unfocusedBorderColor = SleekBorder
                            )
                        )

                        // Role Selector
                        Text("Selecciona tu rol:", style = MaterialTheme.typography.labelSmall, color = SleekTextMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                UserRole.RESIDENTES to "Residente",
                                UserRole.ADMINISTRACION to "Admin",
                                UserRole.GUARDIA to "Guardia"
                            ).forEach { (role, label) ->
                                val isSel = selectedRole == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) SleekVioletPrimary else SleekSurfaceVariant)
                                        .clickable { selectedRole = role }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) SleekVioletDark else SleekTextSecondary
                                    )
                                }
                            }
                        }

                        if (selectedRole == UserRole.RESIDENTES) {
                            OutlinedTextField(
                                value = houseNumberStr,
                                onValueChange = { houseNumberStr = it.filter { c -> c.isDigit() } },
                                label = { Text("Número de Casa / Casa #") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "register_house_input" },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekVioletPrimary,
                                    unfocusedBorderColor = SleekBorder
                                )
                            )
                        }

                        Button(
                            onClick = {
                                val houseNum = houseNumberStr.toIntOrNull()
                                onRegisterEmail(email, password, displayName, selectedRole, houseNum)
                            },
                            enabled = email.isNotBlank() && password.length >= 6 && displayName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekVioletPrimary,
                                contentColor = SleekVioletDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "register_submit_btn" }
                        ) {
                            Text("Crear Cuenta", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { testTag = "auth_dialog_close_btn" }
            ) {
                Text("Cerrar", color = SleekTextSecondary)
            }
        }
    )
}
