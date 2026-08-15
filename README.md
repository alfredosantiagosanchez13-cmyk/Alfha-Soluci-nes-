# 🪼 Medusa-Alfha — Plataforma Inteligente Condominial & Domótica IoT

[![Android CI & Release](https://github.com/alfhaseguridad070/Medusa-Alfha/actions/workflows/android.yml/badge.svg)](https://github.com/alfhaseguridad070/Medusa-Alfha/actions/workflows/android.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Gradle](https://img.shields.io/badge/Gradle-8.11.1-green.svg)](https://gradle.org)
[![JDK](https://img.shields.io/badge/JDK-17%20(LTS)-orange.svg)](https://adoptium.net)

**Medusa-Alfha** es un ecosistema integral Android de seguridad residencial, control de accesos por código QR de alta resolución, escaneo OCR de paquetería, persistencia criptográfica local con Room Database, y automatización domótica inteligente (REST LAN + Bluetooth LE 5.0) impulsada por IA Neural (Google Gemini).

---

## 📌 Tabla de Contenidos
1. [Requisitos Previos](#-requisitos-previos)
2. [Configuración de Variables de Entorno (.env)](#-configuración-de-variables-de-entorno-env)
3. [Inicialización y Subida a GitHub](#-inicialización-y-subida-a-github)
4. [Compilación y Generación del APK con Gradle](#-compilación-y-generación-del-apk-con-gradle)
5. [Despliegue Continuo con GitHub Actions (CI/CD)](#-despliegue-continuo-con-github-actions-cicd)
6. [Arquitectura y Módulos del Sistema](#-arquitectura-y-módulos-del-sistema)
7. [Permisos y Seguridad](#-permisos-y-seguridad)

---

## 🛠️ Requisitos Previos

Asegúrate de contar con el siguiente entorno de desarrollo:

- **JDK:** Java Development Kit **17 (LTS)** (se recomienda Eclipse Temurin 17 u OpenJDK 17).
  - Verifica tu versión instalada: `java -version`
  - Variable de entorno `JAVA_HOME` apuntando al directorio de JDK 17.
- **Android Studio:** Ladybug (2024.2.1+), Koala o Hedgehog.
- **Gradle:** 8.11.1+ (Gestionado vía Kotlin DSL `.gradle.kts` y Version Catalog `libs.versions.toml`).
- **Android SDK:**
  - `compileSdk`: **35** (Android 15)
  - `targetSdk`: **35** (Android 15)
  - `minSdk`: **26** (Android 8.0 Oreo)

---

## 🔑 Configuración de Variables de Entorno (`.env`)

El proyecto utiliza el **Secrets Gradle Plugin** para inyectar claves de API en tiempo de compilación sin exponer credenciales sensibles en el repositorio de Git.

### 1. Crear el archivo `.env` local

En la raíz del proyecto, copia la plantilla:

```bash
cp .env.example .env
```

### 2. Configurar tus credenciales

Abre el archivo `.env` y añade tu clave de API de **Google Gemini**:

```properties
# Clave de API de Google Gemini (Obtenla en https://aistudio.google.com/)
GEMINI_API_KEY=AIzaSyTuClaveDeApiAqui
```

> 🔒 **Seguridad:** El archivo `.env` está expresamente excluido en `.gitignore` para evitar filtraciones en repositorios públicos.

---

## 🚀 Inicialización y Subida a GitHub

Sigue estos pasos para subir el código a tu repositorio de GitHub:

```bash
# 1. Abre tu terminal en el directorio raíz del proyecto
cd /ruta/hacia/Medusa-Alfha

# 2. Inicializa el repositorio Git (si no está inicializado)
git init

# 3. Agrega todos los archivos al seguimiento
git add .

# 4. Crea el commit inicial
git commit -m "feat: Lanzamiento de Medusa-Alfha con Domótica IoT y Soporte GitHub CI"

# 5. Define la rama principal como main
git branch -M main

# 6. Vincula tu repositorio remoto en GitHub
git remote add origin https://github.com/alfhaseguridad070/Medusa-Alfha.git

# 7. Sube tu código a GitHub
git push -u origin main
```

---

## 📦 Compilación y Generación del APK con Gradle

### Compilar APK en modo Debug:

```bash
# En sistemas Linux / macOS:
./gradlew assembleDebug --no-daemon

# O directamente con la herramienta gradle instalada:
gradle assembleDebug --no-daemon
```

El binario ejecutable `.apk` se generará en la ruta:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### Ejecutar Pruebas Unitarias en JVM:

```bash
gradle :app:testDebugUnitTest
```

### Limpiar artefactos de compilación (opcional):

```bash
gradle clean
```

---

## 🤖 Despliegue Continuo con GitHub Actions (CI/CD)

El repositorio incluye un flujo automatizado en `.github/workflows/android.yml` listo para producción.

### ¿Qué hace el flujo de CI/CD?
1. **Compilación Continua:** Se activa en cada `push` o `pull_request` a la rama `main`.
2. **Generación y Guardado de Artefacto:** Compila el APK en Ubuntu con JDK 17 y sube el archivo como artefacto descargable en la pestaña **Actions** de GitHub.
3. **Releases Automáticos:** Al crear y subir un tag de versión (por ejemplo `v1.0.0`), compila el APK y crea automáticamente un **GitHub Release** con el archivo `.apk` adjunto listo para instalación.

### Cómo publicar una nueva versión con Release:

```bash
# Crear un tag de versión
git tag -a v1.0.0 -m "Release Medusa-Alfha v1.0.0 con Domótica IoT"

# Subir el tag a GitHub (activará el Release en GitHub Actions)
git push origin v1.0.0
```

---

## 🏛️ Arquitectura y Módulos del Sistema

La aplicación sigue el patrón **MVVM** (Model-View-ViewModel) con **Clean Architecture**, Jetpack Compose y Material Design 3:

```text
app/src/main/java/com/example/
├── data/
│   ├── db/          # Room Database, Entidades y DAOs (Pases, Paquetes, IoT, Memoria)
│   ├── model/       # Modelos de dominio y enums de protocolos
│   └── repository/  # SmartHomeService, GeminiRepository, ParcelRepository, AuthRepo
├── di/              # Inyección de dependencias modular (Hilt / Constructor DI)
├── ui/
│   ├── components/  # Componentes reutilizables M3 (Tarjetas, Diálogos de Voz, Gráficos D3)
│   ├── screens/     # Pantallas: CoreMatrix, SmartHome, NeuralChat, QrScanner, SmartParcel, Vault
│   ├── theme/       # Paleta Sleek Nexus (Violeta Eléctrico, Cyan, Fondo OLED 0x050814)
│   └── voice/       # VoiceRecognitionManager para comandos de voz en tiempo real
└── MedusaApplication.kt
```

### Módulos Principales:
1. **Matriz Central (`CoreMatrixScreen`):** Monitoreo en vivo de telemetría de caseta, estadísticas mensuales de visitas y estado del sistema.
2. **Domótica IoT (`SmartHomeScreen`):** Control bidireccional de iluminación RGB y climatización HVAC mediante protocolos híbridos **REST API LAN** y **Bluetooth Low Energy (BLE 5.0)** con interpretación por lenguaje natural (NLP).
3. **Chat Asistencial Neural (`NeuralChatScreen`):** Comunicación directa con Google Gemini multimodal y memoria asociativa de residentes.
4. **Escáner y Generador QR (`QrScannerScreen`):** Generación de pases vectoriales y validación de visitas en caseta.
5. **Recepción de Paquetería (`SmartParcelScreen`):** Registro de paquetería de Amazon, MercadoLibre, DHL, etc., con notificaciones locales periódicas vía Android WorkManager.
6. **Bóveda de Memoria (`MemoryVaultScreen`):** Historial inmutable y base de datos relacional cifrada en Room.

---

## 🛡️ Permisos y Seguridad

Declarados en `AndroidManifest.xml`:
- `INTERNET` & `ACCESS_NETWORK_STATE`: Sincronización REST LAN y llamadas seguras a Gemini API.
- `BLUETOOTH`, `BLUETOOTH_ADMIN`, `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`: Control domótico periférico BLE 5.0.
- `CAMERA`: Escáner de pases de acceso QR.
- `RECORD_AUDIO`: Comandos de voz manos libres con reconocimiento neural.
- `POST_NOTIFICATIONS`: Alertas de seguridad y paquetería pendiente en segundo plano.

---

## 📄 Licencia

Desarrollado y mantenido para la administración y seguridad del condominio residencial **Alfha / Medusa**. Todos los derechos reservados.
