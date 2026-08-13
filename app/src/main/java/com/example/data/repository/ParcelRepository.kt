package com.example.data.repository

import android.util.Log
import com.example.data.db.ParcelDao
import com.example.data.db.ParcelEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ParcelRepository(
    private val parcelDao: ParcelDao,
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }
) {
    val allParcels: Flow<List<ParcelEntity>> = parcelDao.getAllParcels()
    val pendingParcels: Flow<List<ParcelEntity>> = parcelDao.getPendingParcels()

    companion object {
        private const val TAG = "ParcelRepository"
        private const val COLLECTION_PARCELS = "parcels"
    }

    init {
        startFirestoreSync()
    }

    /**
     * Escucha cambios en tiempo real desde Firebase Firestore y los sincroniza en la BD local Room
     */
    fun startFirestoreSync() {
        val fs = firestore ?: return
        try {
            fs.collection(COLLECTION_PARCELS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error escuchando cambios de Firestore: ", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        CoroutineScope(Dispatchers.IO).launch {
                            for (doc in snapshot.documents) {
                                try {
                                    val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                                    val houseNumber = doc.getString("houseNumber") ?: ""
                                    val recipientName = doc.getString("recipientName") ?: ""
                                    val carrier = doc.getString("carrier") ?: ""
                                    val description = doc.getString("description") ?: ""
                                    val phone = doc.getString("phone") ?: ""
                                    val status = doc.getString("status") ?: "RECIBIDO"
                                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                    val isNotified = doc.getBoolean("isNotified") ?: false
                                    val photoBase64 = doc.getString("photoBase64") ?: ""

                                    val entity = ParcelEntity(
                                        id = id,
                                        houseNumber = houseNumber,
                                        recipientName = recipientName,
                                        carrier = carrier,
                                        description = description,
                                        phone = phone,
                                        status = status,
                                        timestamp = timestamp,
                                        isNotified = isNotified,
                                        photoBase64 = photoBase64
                                    )
                                    parcelDao.insertParcel(entity)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error al procesar documento de Firestore", e)
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo iniciar la sincronización de Firestore", e)
        }
    }

    suspend fun insertParcel(parcel: ParcelEntity): Long {
        val localId = parcelDao.insertParcel(parcel)
        val parcelWithId = if (parcel.id == 0L) parcel.copy(id = localId) else parcel

        // Sincronizar hacia Firestore
        syncToFirestore(parcelWithId)
        return localId
    }

    suspend fun updateParcelStatus(id: Long, status: String, isNotified: Boolean) {
        parcelDao.updateParcelStatus(id, status, isNotified)

        val fs = firestore ?: return
        try {
            fs.collection(COLLECTION_PARCELS)
                .document(id.toString())
                .update(
                    mapOf(
                        "status" to status,
                        "isNotified" to isNotified
                    )
                )
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar estado en Firestore", e)
        }
    }

    suspend fun deleteParcel(parcel: ParcelEntity) {
        parcelDao.deleteParcel(parcel)

        val fs = firestore ?: return
        try {
            fs.collection(COLLECTION_PARCELS)
                .document(parcel.id.toString())
                .delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar paquete de Firestore", e)
        }
    }

    suspend fun clearAllParcels() {
        parcelDao.clearAllParcels()
    }

    private fun syncToFirestore(parcel: ParcelEntity) {
        val fs = firestore ?: return
        try {
            val parcelMap = hashMapOf(
                "id" to parcel.id,
                "houseNumber" to parcel.houseNumber,
                "recipientName" to parcel.recipientName,
                "carrier" to parcel.carrier,
                "description" to parcel.description,
                "phone" to parcel.phone,
                "status" to parcel.status,
                "timestamp" to parcel.timestamp,
                "isNotified" to parcel.isNotified,
                "photoBase64" to parcel.photoBase64
            )

            fs.collection(COLLECTION_PARCELS)
                .document(parcel.id.toString())
                .set(parcelMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Paquete sincronizado exitosamente con Firestore: ${parcel.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Fallo al sincronizar paquete con Firestore: ${parcel.id}", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante sincronización a Firestore", e)
        }
    }
}
