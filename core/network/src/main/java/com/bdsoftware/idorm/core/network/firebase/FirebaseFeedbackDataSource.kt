package com.bdsoftware.idorm.core.network.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFeedbackDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun submitFeedback(
        studentCode: String,
        studentName: String,
        email: String,
        description: String,
        status: String
    ) {
        val feedbackRef = firestore.collection("idorm_feedbacks").document()
        val feedbackId = feedbackRef.id
        val nowIso = Instant.now().toString()

        val feedbackData = hashMapOf(
            "id" to feedbackId,
            "studentCode" to studentCode,
            "studentName" to studentName,
            "email" to email,
            "description" to description,
            "images" to emptyList<String>(),
            "status" to status,
            "createdAt" to nowIso,
            "updatedAt" to nowIso,
            "deletedAt" to null,
            "createdBy" to studentCode,
            "updatedBy" to studentCode
        )

        feedbackRef.set(feedbackData).await()
        Log.d("FirebaseFeedbackDataSource", "Feedback submitted: $feedbackId")
    }
}
