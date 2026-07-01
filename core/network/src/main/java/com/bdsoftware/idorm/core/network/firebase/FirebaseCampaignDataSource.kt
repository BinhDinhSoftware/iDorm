package com.bdsoftware.idorm.core.network.firebase

import android.util.Log
import com.bdsoftware.idorm.core.model.Banner
import com.bdsoftware.idorm.core.model.Campaign
import com.bdsoftware.idorm.core.model.PromotionalAd
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCampaignDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getLatestCampaign(): Campaign? {
        return try {
            val querySnapshot = firestore.collection("idorm_promotional_ads")
                .get()
                .await()

            val doc = querySnapshot.documents.firstOrNull { doc ->
                if (doc.get("deletedAt") != null) return@firstOrNull false
                val status = doc.getString("status") ?: "active"
                if (status != "active") return@firstOrNull false
                val displayScreen = doc.getString("displayScreen") ?: "home"
                val position = doc.getString("position") ?: "middle"
                displayScreen == "home" && position == "popup"
            }

            Log.d("FirebaseCampaignDataSource", "Active campaign doc: ${doc?.data}")
            if (doc != null) {
                Campaign(
                    Id = doc.getString("id") ?: doc.id,
                    BannerUrl = doc.getString("bannerUrl") ?: "",
                    UrlRedirect = doc.getString("urlRedirect"),
                    status = doc.getString("status") ?: "active",
                    deletedAt = doc.getString("deletedAt")
                )
            } else {
                Log.d("FirebaseCampaignDataSource", "No active campaign found")
                null
            }
        } catch (e: Exception) {
            Log.d("FirebaseCampaignDataSource", "Error fetching campaign: ${e.message}")
            null
        }
    }

    suspend fun getBanners(): List<Banner> {
        return try {
            val querySnapshot = firestore.collection("idorm_banners")
                .orderBy("sort")
                .get()
                .await()
                
            querySnapshot.documents.mapNotNull { doc ->
                if (doc.get("deletedAt") != null) return@mapNotNull null
                val status = doc.getString("status") ?: "active"
                if (status != "active") return@mapNotNull null
                Banner(
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    badgeText = doc.getString("badgeText") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    sort = doc.getLong("sort")?.toInt() ?: 0,
                    status = status,
                    clicks = doc.getLong("clicks") ?: 0L,
                    views = doc.getLong("views") ?: 0L,
                    endDate = doc.getString("endDate"),
                    createdAt = doc.getString("createdAt"),
                    updatedAt = doc.getString("updatedAt"),
                    deletedAt = doc.getString("deletedAt")
                )
            }
        } catch (e: Exception) {
            Log.d("FirebaseCampaignDataSource", "Error fetching banners: ${e.message}")
            emptyList()
        }
    }

    suspend fun getPromotionalAds(): List<PromotionalAd> {
        return try {
            val querySnapshot = firestore.collection("idorm_promotional_ads")
                .orderBy("sort")
                .get()
                .await()
                
            querySnapshot.documents.mapNotNull { doc ->
                if (doc.get("deletedAt") != null) return@mapNotNull null
                val status = doc.getString("status") ?: "active"
                if (status != "active") return@mapNotNull null
                val displayScreen = doc.getString("displayScreen") ?: "home"
                val position = doc.getString("position") ?: "middle"
                if (displayScreen != "home" || position != "middle") return@mapNotNull null

                PromotionalAd(
                    Id = doc.getString("id") ?: doc.id,
                    displayScreen = displayScreen,
                    position = position,
                    Title = doc.getString("title"),
                    Description = doc.getString("description"),
                    ActionText = doc.getString("actionText") ?: "Xem ngay",
                    ActionTextPosition = doc.getString("actionTextPosition") ?: "START",
                    ActionStyle = doc.getString("actionStyle") ?: "DEFAULT",
                    Sort = doc.getLong("sort")?.toInt() ?: 0,
                    BannerUrl = doc.getString("bannerUrl") ?: "",
                    GradientStart = doc.getString("gradientStart"),
                    GradientEnd = doc.getString("gradientEnd"),
                    Overlay = doc.getLong("overlay")?.toInt() ?: 40,
                    UrlRedirect = doc.getString("urlRedirect"),
                    status = status,
                    clicks = doc.getLong("clicks") ?: 0L,
                    views = doc.getLong("views") ?: 0L,
                    startDate = doc.getString("startDate"),
                    endDate = doc.getString("endDate"),
                    createdAt = doc.getString("createdAt"),
                    updatedAt = doc.getString("updatedAt"),
                    deletedAt = doc.getString("deletedAt")
                )
            }
        } catch (e: Exception) {
            Log.d("FirebaseCampaignDataSource", "Error fetching promotional ads: ${e.message}")
            emptyList()
        }
    }
}
