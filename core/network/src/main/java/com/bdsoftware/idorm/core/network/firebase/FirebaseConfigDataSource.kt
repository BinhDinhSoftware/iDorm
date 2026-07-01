package com.bdsoftware.idorm.core.network.firebase

import android.util.Log
import com.bdsoftware.idorm.core.model.AppConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConfigDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getAppConfig(): AppConfig? {
        return try {
            val doc = firestore.collection("idorm_settings")
                .document("app_config")
                .get()
                .await()

            if (doc != null && doc.exists()) {
                AppConfig(
                    androidForceUpdate = doc.getBoolean("androidForceUpdate") ?: false,
                    androidLatestVersion = doc.getString("androidLatestVersion") ?: "1.0.0",
                    androidMinVersion = doc.getString("androidMinVersion") ?: "1.0.0",
                    maintenanceMessage = doc.getString("maintenanceMessage") ?: "",
                    maintenanceMode = doc.getBoolean("maintenanceMode") ?: false
                )
            } else {
                Log.d("FirebaseConfigDataSource", "App config document does not exist")
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseConfigDataSource", "Error fetching app config: ${e.message}", e)
            null
        }
    }
}
