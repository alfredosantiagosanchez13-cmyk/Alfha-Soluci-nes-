package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.UserRole
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import kotlinx.coroutines.launch

/**
 * Pantalla de desbloqueo por PIN y selector de clave original de Medusa OS.
 * Replica el diseño espacial azul marino con medusa luminosa y teclado numérico de 4 dígitos.
 */
@Composable
fun PrototypePinLockScreen(
    onUnlockSuccess: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Breathing jellyfish animation
    val infiniteTransition = rememberInfiniteTransition(label = "jellyfish_pulse")
    val jellyScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jellyScale"
    )

    fun handleKeyTap(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isError = false
            errorMessage = null

            if (newPin.length == 4) {
                // Validate PIN
                when (newPin) {
                    "9999", "0000", "2026" -> {
                        onUnlockSuccess(UserRole.ALFHA_SANTIAGO)
                    }
                    "1234", "1010" -> {
                        onUnlockSuccess(UserRole.GUARDIA)
                    }
                    "4321", "2020" -> {
                        onUnlockSuccess(UserRole.ADMINISTRACION)
                    }
                    "7777", "1313" -> {
                        onUnlockSuccess(UserRole.RESIDENTES)
                    }
                    "1111" -> {
                        onUnlockSuccess(UserRole.TRABAJADOR)
                    }
                    else -> {
                        isError = true
                        errorMessage = "PIN no reconocido. Usa 9999 (Alfha), 1234 (Caseta), 4321 (Admin), 7777 (Residente) o 1111 (Trabajador)."
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
            errorMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070914),
                        Color(0xFF0F142B),
                        Color(0xFF090D1E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Official Medusa Alpha Crest Emblem with glowing pulse
            Box(
                modifier = Modifier
                    .scale(jellyScale)
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFD54F).copy(alpha = 0.25f),
                                Color(0xFF00E5FF).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFF00E5FF))), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_medusa_logo),
                    contentDescription = "Escudo Oficial Medusa Alpha",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "MEDUSA ALFHA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD54F),
                letterSpacing = 2.sp,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Authentic Motto Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TIEMPO ",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD54F),
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "= ",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Text(
                    text = "FAMILIA",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Inteligencia que protege · Tiempo que transforma",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF90CAF9),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4 PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) Color(0xFFFFB300) else Color.Transparent
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) Color(0xFFFFB300) else Color(0xFFFFB300).copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Error notice
            AnimatedVisibility(
                visible = isError && errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Keypad Grid (1..9, 0, Backspace)
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(64.dp))
                            } else if (key == "DEL") {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E2442))
                                        .border(1.dp, Color(0xFF2C3560), CircleShape)
                                        .clickable { handleBackspace() }
                                        .semantics {
                                            testTag = "pin_keypad_del"
                                            contentDescription = "Borrar dígito de PIN"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Borrar",
                                        tint = Color(0xFF90CAF9),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B213D))
                                        .border(1.dp, Color(0xFF2C3560), CircleShape)
                                        .clickable { handleKeyTap(key) }
                                        .semantics {
                                            testTag = "pin_keypad_$key"
                                            contentDescription = "Dígito $key"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Fast Demo Role Switchers (Acceso Rápido por Clave)
            Text(
                text = "CLAVES PREESTABLECIDAS DE DEMOSTRACIÓN",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7E8BAE),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val demoPins = listOf(
                    Triple("👑 Súper Alfha", "9999", UserRole.ALFHA_SANTIAGO),
                    Triple("🛡️ Caseta", "1234", UserRole.GUARDIA),
                    Triple("🏢 Admin", "4321", UserRole.ADMINISTRACION),
                    Triple("🏠 Residente", "7777", UserRole.RESIDENTES),
                    Triple("👷 Trabajador", "1111", UserRole.TRABAJADOR)
                )

                items(demoPins) { (label, pinCode, role) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF171D36))
                            .border(1.dp, Color(0xFF2E3A6B), RoundedCornerShape(16.dp))
                            .clickable {
                                onUnlockSuccess(role)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .semantics { testTag = "demo_pin_${pinCode}" }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "PIN: $pinCode",
                                fontSize = 9.sp,
                                color = Color(0xFFFFB300),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
