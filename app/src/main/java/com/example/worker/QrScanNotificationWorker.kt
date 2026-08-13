package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class QrScanNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val house = inputData.getString(KEY_RESIDENT_HOUSE) ?: "Casa Desconocida"
            val residentName = inputData.getString(KEY_RESIDENT_NAME) ?: "Residente"
            val visitorName = inputData.getString(KEY_VISITOR_NAME) ?: "Visitante"
            val passCode = inputData.getString(KEY_PASS_CODE) ?: "MEDUSA-QR-000"
            val isGranted = inputData.getBoolean(KEY_IS_GRANTED, true)
            val resultReason = inputData.getString(KEY_RESULT_REASON) ?: "Acceso Escaneado"
            val timestampMs = inputData.getLong(KEY_TIMESTAMP_MS, System.currentTimeMillis())

            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeStr = sdf.format(Date(timestampMs))

            sendLocalNotification(
                house = house,
                residentName = residentName,
                visitorName = visitorName,
                passCode = passCode,
                isGranted = isGranted,
                resultReason = resultReason,
                timeStr = timeStr
            )

            Result.success()
        } catch (e: Exception) {
            Log.e("QrScanNotificationWorker", "Error sending local notification via WorkManager", e)
            Result.failure()
        }
    }

    private fun sendLocalNotification(
        house: String,
        residentName: String,
        visitorName: String,
        passCode: String,
        isGranted: Boolean,
        resultReason: String,
        timeStr: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas inmediatas en caseta cuando un visitante escanea su código QR"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusSymbol = if (isGranted) "✅ ACCESO AUTORIZADO" else "🚨 ACCESO DENEGADO"
        val titleText = "🔔 Medusa OS • $house ($visitorName)"
        val shortBodyText = "$statusSymbol: $visitorName escaneó su QR $passCode a las $timeStr"

        val bigTextDetails = """
            🏡 Residencial Medusa OS • $house
            👤 Residente: $residentName
            🎫 Visitante: $visitorName
            🔑 Código QR: $passCode
            📊 Estado: $statusSymbol ($resultReason)
            ⏰ Hora de Escaneo: $timeStr
        """.trimIndent()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titleText)
            .setContentText(shortBodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextDetails))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "medusa_qr_scans_channel"
        const val CHANNEL_NAME = "Escaneos QR de Accesos Medusa"

        const val KEY_RESIDENT_HOUSE = "resident_house"
        const val KEY_RESIDENT_NAME = "resident_name"
        const val KEY_VISITOR_NAME = "visitor_name"
        const val KEY_PASS_CODE = "pass_code"
        const val KEY_IS_GRANTED = "is_granted"
        const val KEY_RESULT_REASON = "result_reason"
        const val KEY_TIMESTAMP_MS = "timestamp_ms"

        fun enqueueNotification(
            context: Context,
            house: String,
            residentName: String,
            visitorName: String,
            passCode: String,
            isGranted: Boolean,
            resultReason: String,
            timestampMs: Long = System.currentTimeMillis()
        ) {
            val inputData = Data.Builder()
                .putString(KEY_RESIDENT_HOUSE, house)
                .putString(KEY_RESIDENT_NAME, residentName)
                .putString(KEY_VISITOR_NAME, visitorName)
                .putString(KEY_PASS_CODE, passCode)
                .putBoolean(KEY_IS_GRANTED, isGranted)
                .putString(KEY_RESULT_REASON, resultReason)
                .putLong(KEY_TIMESTAMP_MS, timestampMs)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<QrScanNotificationWorker>()
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueue(workRequest)
        }
    }
}
