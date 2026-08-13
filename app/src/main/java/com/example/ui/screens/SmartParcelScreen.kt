package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.db.ParcelEntity
import com.example.data.model.PackageScanResult
import com.example.data.model.ResidentDirectory
import com.example.ui.MedusaViewModel
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
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmartParcelScreen(
    viewModel: MedusaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val parcels by viewModel.parcels.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingPackage.collectAsState()
    val scanResult by viewModel.lastScanResult.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredParcels = remember(parcels, searchQuery) {
        if (searchQuery.isBlank()) {
            parcels
        } else {
            val q = searchQuery.trim().lowercase()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            parcels.filter { parcel ->
                val fullDate = dateFormat.format(Date(parcel.timestamp)).lowercase()
                val shortDate = dateOnlyFormat.format(Date(parcel.timestamp)).lowercase()
                parcel.recipientName.lowercase().contains(q) ||
                parcel.houseNumber.lowercase().contains(q) ||
                parcel.carrier.lowercase().contains(q) ||
                parcel.description.lowercase().contains(q) ||
                parcel.status.lowercase().contains(q) ||
                fullDate.contains(q) ||
                shortDate.contains(q)
            }
        }
    }

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var base64Photo by remember { mutableStateOf("") }
    var showLiveCameraX by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher for CameraX runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            showLiveCameraX = true
        }
    }

    // Camera preview fallback launcher
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val bytes = outputStream.toByteArray()
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            base64Photo = b64
            viewModel.analyzePackagePhoto(b64)
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                capturedBitmap = bitmap
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val bytes = outputStream.toByteArray()
                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                base64Photo = b64
                viewModel.analyzePackagePhoto(b64)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Header
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "PAQUETERÍA INTELIGENTE IA",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekVioletPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Escáner & Aviso",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekVioletDark)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "☁️ Room + Firestore Sync",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekVioletPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Captura con la cámara CameraX la etiqueta del paquete. La IA detectará automáticamente la casa, destinatario y notificará por WhatsApp.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary
                )
            }
        }

        // Live CameraX view or Action trigger card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SleekSurface, SleekSurfaceVariant)
                        )
                    )
                    .border(1.dp, SleekBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (showLiveCameraX && hasCameraPermission) {
                        CameraXLivePreviewView(
                            onImageCaptured = { bitmap ->
                                showLiveCameraX = false
                                capturedBitmap = bitmap
                                val outputStream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                                val bytes = outputStream.toByteArray()
                                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                base64Photo = b64
                                viewModel.analyzePackagePhoto(b64)
                            },
                            onClose = { showLiveCameraX = false }
                        )
                    } else {
                        if (capturedBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, SleekBorder, RoundedCornerShape(16.dp))
                            ) {
                                Image(
                                    bitmap = capturedBitmap!!.asImageBitmap(),
                                    contentDescription = "Foto de paquete escaneado",
                                    modifier = Modifier.fillMaxSize()
                                )

                                IconButton(
                                    onClick = {
                                        capturedBitmap = null
                                        base64Photo = ""
                                        viewModel.dismissScanResult()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(32.dp)
                                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar foto",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (isAnalyzing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = SleekVioletPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Analizando etiqueta con IA...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 14.sp,
                                    color = SleekVioletPrimary
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (hasCameraPermission) {
                                            showLiveCameraX = true
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                    .height(52.dp)
                                    .semantics { testTag = "camerax_button" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SleekVioletPrimary,
                                        contentColor = SleekVioletDark
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Cámara CameraX",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .semantics { testTag = "pick_gallery_button" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SleekSurfaceVariant,
                                        contentColor = SleekTextPrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = SleekVioletPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Galería",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Extracted Scan Result Confirmation Card
        if (scanResult != null) {
            item {
                ScanConfirmationCard(
                    result = scanResult!!,
                    onConfirmNotify = { house, recipient, carrier, desc, phone ->
                        viewModel.confirmAndSaveParcel(
                            houseNumber = house,
                            recipientName = recipient,
                            carrier = carrier,
                            description = desc,
                            phone = phone,
                            photoBase64 = base64Photo,
                            context = context
                        )
                        capturedBitmap = null
                        base64Photo = ""
                    },
                    onDismiss = { viewModel.dismissScanResult() }
                )
            }
        }

        // Section Title & Search Field: Historic Packages
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "REGISTRO DE PAQUETES RECIBIDOS (${parcels.size})"
                        else "RESULTADOS DE BÚSQUEDA (${filteredParcels.size} DE ${parcels.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekVioletPrimary,
                        letterSpacing = 1.sp
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Buscar por residente, casa, guía o fecha (dd/mm/aaaa)...",
                            fontSize = 12.sp,
                            color = SleekTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar paquetes",
                            tint = SleekVioletPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = SleekTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "parcel_search_input" },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekSurface,
                        unfocusedContainerColor = SleekSurface,
                        focusedBorderColor = SleekVioletPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    )
                )
            }
        }

        if (filteredParcels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SleekSurface)
                        .border(1.dp, SleekBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (searchQuery.isBlank()) "📦" else "🔍", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No hay paquetes registrados." else "Sin resultados.",
                            style = MaterialTheme.typography.titleMedium,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "Escanea la primera entrega tomando una foto a la etiqueta con CameraX." else "No se encontraron paquetes con '$searchQuery'. Intenta con el nombre, casa o fecha.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextMuted
                        )
                    }
                }
            }
        } else {
            items(filteredParcels, key = { it.id }) { parcel ->
                ParcelItemCard(
                    parcel = parcel,
                    onSendWhatsApp = {
                        viewModel.sendWhatsAppNotice(
                            context = context,
                            parcelId = parcel.id,
                            houseNumber = parcel.houseNumber,
                            recipientName = parcel.recipientName,
                            carrier = parcel.carrier,
                            description = parcel.description,
                            phone = parcel.phone
                        )
                    },
                    onMarkDelivered = { viewModel.markParcelDelivered(parcel.id) },
                    onDelete = { viewModel.deleteParcel(parcel) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * CameraX Live Preview Composable with Image Capture capabilities
 */
@Composable
fun CameraXLivePreviewView(
    onImageCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SleekVioletPrimary, RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()

                            imageCapture = capture

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            Log.e("CameraX", "Error initializing CameraX provider", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Close button overlay
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar visor de cámara",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val capture = imageCapture
                if (capture != null && !isCapturing) {
                    isCapturing = true
                    capture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                try {
                                    val buffer = image.planes[0].buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    image.close()
                                    if (bitmap != null) {
                                        onImageCaptured(bitmap)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isCapturing = false
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraX", "Error capturing picture", exception)
                                isCapturing = false
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SleekVioletPrimary,
                contentColor = SleekVioletDark
            ),
            shape = RoundedCornerShape(14.dp),
            enabled = !isCapturing
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = SleekVioletDark,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Capturando foto...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📸 Capturar Paquete",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ScanConfirmationCard(
    result: PackageScanResult,
    onConfirmNotify: (house: String, recipient: String, carrier: String, desc: String, phone: String) -> Unit,
    onDismiss: () -> Unit
) {
    var houseNumber by remember(result) { mutableStateOf(result.houseNumber) }
    var recipientName by remember(result) { mutableStateOf(result.recipientName) }
    var carrier by remember(result) { mutableStateOf(result.carrier) }
    var description by remember(result) { mutableStateOf(result.description) }
    var phone by remember(result) { mutableStateOf(result.matchedPhone) }

    // Dynamically update phone if user changes house number manually
    fun updateContactForHouse(newHouseStr: String) {
        houseNumber = newHouseStr
        val contact = ResidentDirectory.findContactByHouse(newHouseStr)
        if (contact != null) {
            phone = contact.phone
            if (recipientName.isBlank() || recipientName == "Destinatario no visible") {
                recipientName = contact.name
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, SleekVioletPrimary, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SleekVioletPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DETECCIÓN IA COMPLETADA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SleekVioletPrimary,
                        fontSize = 13.sp
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = SleekTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = houseNumber,
                onValueChange = { updateContactForHouse(it) },
                label = { Text("Número de Casa / Depto") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = SleekVioletPrimary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekVioletPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                label = { Text("Nombre del Residente / Destinatario") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SleekVioletPrimary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekVioletPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = carrier,
                    onValueChange = { carrier = it },
                    label = { Text("Empresa") },
                    leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SleekVioletPrimary) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekVioletPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono WhatsApp") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SleekVioletPrimary) },
                    modifier = Modifier.weight(1.2f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekVioletPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción Paquete") },
                leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = SleekVioletPrimary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekVioletPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onConfirmNotify(houseNumber, recipientName, carrier, description, phone) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics { testTag = "confirm_notify_button" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekVioletPrimary,
                    contentColor = SleekVioletDark
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (phone.isNotBlank()) "📱 Guardar y Notificar por WhatsApp" else "💾 Guardar Registro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ParcelItemCard(
    parcel: ParcelEntity,
    onSendWhatsApp: () -> Unit,
    onMarkDelivered: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedTime = remember(parcel.timestamp) { dateFormat.format(Date(parcel.timestamp)) }

    val isDelivered = parcel.status == "ENTREGADO"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SleekBorderSubtle, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isDelivered) SleekSurfaceVariant.copy(alpha = 0.5f) else SleekSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDelivered) SleekSurfaceVariant else SleekVioletDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = parcel.houseNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDelivered) SleekTextMuted else SleekVioletPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = parcel.carrier,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextPrimary,
                        fontSize = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (parcel.isNotified) {
                        Text(
                            text = "WhatsApp Enviado",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = SleekVioletPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = SleekTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Residente: ${parcel.recipientName.ifBlank { "Sin asignar" }}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SleekTextPrimary,
                fontSize = 13.sp
            )

            Text(
                text = parcel.description,
                style = MaterialTheme.typography.bodySmall,
                color = SleekTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = SleekTextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isDelivered && parcel.phone.isNotBlank()) {
                        IconButton(
                            onClick = onSendWhatsApp,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SleekVioletDark)
                                .border(1.dp, SleekVioletPrimary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Aviso WhatsApp",
                                tint = SleekVioletPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (!isDelivered) {
                        Button(
                            onClick = onMarkDelivered,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekSurfaceVariant,
                                contentColor = SleekTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SleekVioletPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Entregado", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ENTREGADO A RESIDENTE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = SleekTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
