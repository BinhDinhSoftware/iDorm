package com.bdsoftware.idorm.core.data.repository

import com.bdsoftware.idorm.core.model.Banner
import com.bdsoftware.idorm.core.model.Campaign
import com.bdsoftware.idorm.core.model.PromotionalAd
import com.bdsoftware.idorm.core.network.firebase.FirebaseCampaignDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampaignRepository @Inject constructor(
    private val firebaseDataSource: FirebaseCampaignDataSource
) {
    
    suspend fun getLatestCampaign(): Campaign? {
        return firebaseDataSource.getLatestCampaign()
    }

    suspend fun getPromotionalAds(): List<PromotionalAd> {
        return firebaseDataSource.getPromotionalAds()
    }

    suspend fun getBanners(): List<Banner> {
        return firebaseDataSource.getBanners()
    }
}
