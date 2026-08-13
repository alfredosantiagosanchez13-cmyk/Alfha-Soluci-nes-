# 🛡️ Medusa-Alfha - Alfha Soluciones

Sistema de seguridad residencial / control de acceso con QR, botón de pánico y bitácora de incidentes.

[![Build](https://github.com/alfredosantiagosanchez13-cmyk/Alfha-Soluci-nes-/actions/workflows/android.yml/badge.svg)](https://github.com/alfredosantiagosanchez13-cmyk/Alfha-Soluci-nes-/actions/workflows/android.yml)

## Características
- **Control de Acceso:** `CameraScannerView` + `QrGeneratorDialog` + `PassVerificationSheet`
- **Botón de Pánico:** `PulsingPanicButton` + `PanicFloorPlanView` con plano
- **Incidentes por Voz:** `VoiceIncidentLoggerComponent` con ForegroundService
- **Amenidades y Reportes:** `AmenityBookingSection`, `PdfReportDialog`, `PersistentChatHistoryView`
- **Seguridad:** Biometric, Room encriptado, Security-Crypto

## Stack Técnico
Kotlin, Jetpack Compose, CameraX, Room, Biometric, ZXing, Firebase Firestore, Material3

## Estado del Build
**Build actual: FIXED**
- ✅ `namespace = com.alfredo.medusaalfha` corregido (antes com.example)
- ✅ `gradlew` restaurado para GitHub Actions
- ✅ Error `Line 137 Expecting }` corregido - faltaba cierre de bloque android {}
- ✅ targetSdk 35, compileSdk 36
- 🔧 Pendiente: migrar a setup-java@v4 y encriptar Room

## Instalación
```bash
git clone https://github.com/alfredosantiagosanchez13-cmyk/Alfha-Soluci-nes-.git
cd Alfha-Soluci-nes-
./gradlew :app:assembleDebug
# APK en app/build/outputs/apk/debug/app-debug.apk
