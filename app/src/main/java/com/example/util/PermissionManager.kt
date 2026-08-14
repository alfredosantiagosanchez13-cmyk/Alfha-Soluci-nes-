package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Utility class and state holder to centralize runtime verification and requests for
 * critical device permissions: CAMERA, RECORD_AUDIO, and POST_NOTIFICATIONS.
 * Integrates Google Accompanist Permissions for seamless Jetpack Compose UI state management.
 */
object PermissionManager {

    val REQUIRED_PERMISSIONS: List<String> = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val REQUIRED_HARDWARE_PERMISSIONS: Array<String> = REQUIRED_PERMISSIONS.toTypedArray()

    /**
     * Checks if CAMERA permission is currently granted.
     */
    fun hasCameraPermission(context: Context): Boolean {
        return isPermissionGranted(context, Manifest.permission.CAMERA)
    }

    /**
     * Checks if RECORD_AUDIO permission is currently granted.
     */
    fun hasAudioPermission(context: Context): Boolean {
        return isPermissionGranted(context, Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Checks if POST_NOTIFICATIONS permission is granted (or not required on Android < 13).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
    }

    /**
     * Generic permission check against ContextCompat.
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifies if all essential hardware and notification permissions are granted.
     */
    fun hasAllPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { isPermissionGranted(context, it) }
    }

    /**
     * Safely executes an action requiring CAMERA if granted, otherwise invokes onDenied.
     */
    inline fun runWithCamera(
        context: Context,
        onGranted: () -> Unit,
        crossinline onDenied: (missingPermission: String) -> Unit = {}
    ) {
        if (hasCameraPermission(context)) {
            onGranted()
        } else {
            onDenied(Manifest.permission.CAMERA)
        }
    }

    /**
     * Safely executes an action requiring RECORD_AUDIO if granted, otherwise invokes onDenied.
     */
    inline fun runWithAudio(
        context: Context,
        onGranted: () -> Unit,
        crossinline onDenied: (missingPermission: String) -> Unit = {}
    ) {
        if (hasAudioPermission(context)) {
            onGranted()
        } else {
            onDenied(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * Safely executes an action requiring POST_NOTIFICATIONS if granted, otherwise invokes onDenied.
     */
    inline fun runWithNotification(
        context: Context,
        onGranted: () -> Unit,
        crossinline onDenied: (missingPermission: String) -> Unit = {}
    ) {
        if (hasNotificationPermission(context)) {
            onGranted()
        } else {
            onDenied(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Data snapshot class summarizing current hardware and notification access states.
     */
    data class PermissionSummary(
        val isCameraGranted: Boolean,
        val isAudioGranted: Boolean,
        val isNotificationGranted: Boolean
    ) {
        val areAllGranted: Boolean get() = isCameraGranted && isAudioGranted && isNotificationGranted
    }

    /**
     * Returns a summary snapshot of current permissions.
     */
    fun getSummary(context: Context): PermissionSummary {
        return PermissionSummary(
            isCameraGranted = hasCameraPermission(context),
            isAudioGranted = hasAudioPermission(context),
            isNotificationGranted = hasNotificationPermission(context)
        )
    }
}

/**
 * Wrapper class around Accompanist [MultiplePermissionsState] providing quick getters
 * and rationale checks for CAMERA, RECORD_AUDIO, and POST_NOTIFICATIONS.
 */
@OptIn(ExperimentalPermissionsApi::class)
class AccompanistPermissionStateHolder(
    val multiplePermissionsState: MultiplePermissionsState,
    val cameraPermissionState: PermissionState,
    val audioPermissionState: PermissionState,
    val notificationPermissionState: PermissionState?
) {
    val isCameraGranted: Boolean
        get() = cameraPermissionState.status.isGranted

    val isAudioGranted: Boolean
        get() = audioPermissionState.status.isGranted

    val isNotificationGranted: Boolean
        get() = notificationPermissionState?.status?.isGranted ?: true

    val allPermissionsGranted: Boolean
        get() = multiplePermissionsState.allPermissionsGranted

    val shouldShowCameraRationale: Boolean
        get() = cameraPermissionState.status.shouldShowRationale

    val shouldShowAudioRationale: Boolean
        get() = audioPermissionState.status.shouldShowRationale

    val shouldShowNotificationRationale: Boolean
        get() = notificationPermissionState?.status?.shouldShowRationale ?: false

    val shouldShowAnyRationale: Boolean
        get() = multiplePermissionsState.shouldShowRationale

    fun launchCameraPermissionRequest() {
        cameraPermissionState.launchPermissionRequest()
    }

    fun launchAudioPermissionRequest() {
        audioPermissionState.launchPermissionRequest()
    }

    fun launchNotificationPermissionRequest() {
        notificationPermissionState?.launchPermissionRequest()
    }

    fun launchMultiplePermissionsRequest() {
        multiplePermissionsState.launchMultiplePermissionRequest()
    }
}

/**
 * Accompanist Permissions helper composable that configures Accompanist [MultiplePermissionsState]
 * for CAMERA, RECORD_AUDIO, and POST_NOTIFICATIONS.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberAccompanistPermissionManager(): AccompanistPermissionStateHolder {
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = PermissionManager.REQUIRED_PERMISSIONS
    )

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    val audioPermissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    return remember(
        multiplePermissionsState,
        cameraPermissionState,
        audioPermissionState,
        notificationPermissionState
    ) {
        AccompanistPermissionStateHolder(
            multiplePermissionsState = multiplePermissionsState,
            cameraPermissionState = cameraPermissionState,
            audioPermissionState = audioPermissionState,
            notificationPermissionState = notificationPermissionState
        )
    }
}

/**
 * Standard ActivityResultContracts state holder wrapper for Jetpack Compose.
 */
class PermissionStateHolder internal constructor(
    private val context: Context,
    private val launchSinglePermission: (String) -> Unit,
    private val launchMultiplePermissions: (Array<String>) -> Unit,
    private val permissionMapState: Map<String, Boolean>
) {
    val isCameraGranted: Boolean
        get() = permissionMapState[Manifest.permission.CAMERA]
            ?: PermissionManager.hasCameraPermission(context)

    val isAudioGranted: Boolean
        get() = permissionMapState[Manifest.permission.RECORD_AUDIO]
            ?: PermissionManager.hasAudioPermission(context)

    val isNotificationGranted: Boolean
        get() = permissionMapState[Manifest.permission.POST_NOTIFICATIONS]
            ?: PermissionManager.hasNotificationPermission(context)

    val allGranted: Boolean
        get() = isCameraGranted && isAudioGranted && isNotificationGranted

    fun requestCameraPermission() {
        launchSinglePermission(Manifest.permission.CAMERA)
    }

    fun requestAudioPermission() {
        launchSinglePermission(Manifest.permission.RECORD_AUDIO)
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launchSinglePermission(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun requestAllPermissions() {
        launchMultiplePermissions(PermissionManager.REQUIRED_HARDWARE_PERMISSIONS)
    }

    fun isPermissionGranted(permission: String): Boolean {
        return permissionMapState[permission] ?: PermissionManager.isPermissionGranted(context, permission)
    }
}

/**
 * Standard Composable helper that remembers a [PermissionStateHolder].
 */
@Composable
fun rememberPermissionManager(
    onPermissionsResult: (Map<String, Boolean>) -> Unit = {}
): PermissionStateHolder {
    val context = LocalContext.current
    val permissionMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            put(Manifest.permission.CAMERA, PermissionManager.hasCameraPermission(context))
            put(Manifest.permission.RECORD_AUDIO, PermissionManager.hasAudioPermission(context))
            put(Manifest.permission.POST_NOTIFICATIONS, PermissionManager.hasNotificationPermission(context))
        }
    }

    var pendingSinglePermission by remember { mutableStateOf<String?>(null) }

    val singleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        pendingSinglePermission?.let { perm ->
            permissionMap[perm] = isGranted
        }
        onPermissionsResult(permissionMap.toMap())
    }

    val multipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (perm, granted) ->
            permissionMap[perm] = granted
        }
        onPermissionsResult(permissionMap.toMap())
    }

    return remember(context) {
        PermissionStateHolder(
            context = context,
            launchSinglePermission = { perm ->
                pendingSinglePermission = perm
                singleLauncher.launch(perm)
            },
            launchMultiplePermissions = { perms ->
                multipleLauncher.launch(perms)
            },
            permissionMapState = permissionMap
        )
    }
}
