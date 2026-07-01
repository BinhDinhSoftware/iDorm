package com.bdsoftware.idorm.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.data.repository.WifiAuthRepository
import com.bdsoftware.idorm.core.data.repository.WifiConnectionManager
import com.bdsoftware.idorm.core.data.repository.UserRepository
import com.bdsoftware.idorm.core.data.repository.NotificationRepository
import com.bdsoftware.idorm.core.data.repository.CampaignRepository
import com.bdsoftware.idorm.core.model.Campaign
import com.bdsoftware.idorm.core.model.Banner
import com.bdsoftware.idorm.core.model.PromotionalAd
import com.bdsoftware.idorm.core.network.model.NotificationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenManager: IDormPreferencesDataSource,
    private val wifiAuthRepository: WifiAuthRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val campaignRepository: CampaignRepository,
    private val wifiConnectionManager: WifiConnectionManager
) : ViewModel() {

    val isWifiActive: StateFlow<Boolean> = wifiAuthRepository.isWifiActive
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _currentWifiSsid = MutableStateFlow<String?>(null)
    val currentWifiSsid: StateFlow<String?> = _currentWifiSsid.asStateFlow()

    private val _isAwingConnected = MutableStateFlow(false)
    val isAwingConnected: StateFlow<Boolean> = _isAwingConnected.asStateFlow()

    fun refreshWifiStatus() {
        _currentWifiSsid.value = wifiConnectionManager.getCurrentWifiSsid()
        _isAwingConnected.value = wifiConnectionManager.isConnectedToAwing()
    }

    data class UserProfile(
        val fullName: String = "",
        val room: String = "",
        val avatarUrl: String = "",
        val mobile: String = "",
        val studentCode: String = ""
    )

    val userProfile: StateFlow<UserProfile> = combine(
        tokenManager.userFullName,
        tokenManager.userRoom,
        tokenManager.userAvatarUrl,
        tokenManager.userMobile,
        tokenManager.userStudentCode
    ) { fullName, room, avatarUrl, mobile, studentCode ->
        UserProfile(
            fullName = fullName.orEmpty(),
            room = room.orEmpty(),
            avatarUrl = avatarUrl.orEmpty(),
            mobile = mobile.orEmpty(),
            studentCode = studentCode.orEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    val notifications: StateFlow<List<NotificationData>> = notificationRepository.notifications
        .map { list -> list.take(3) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _latestCampaign = MutableStateFlow<Campaign?>(null)

    val campaignToShow: StateFlow<Campaign?> = combine(
        _latestCampaign,
        tokenManager.dismissedCampaignId
    ) { campaign, dismissedId ->
        if (campaign != null && campaign.Id != dismissedId) campaign else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val ads: StateFlow<List<PromotionalAd>> = MutableStateFlow(emptyList())
    val banners: StateFlow<List<Banner>> = MutableStateFlow(emptyList())

    init {
        refreshWifiStatus()
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.fetchUserProfileIfNeeded()
                notificationRepository.getNotifications()
                _latestCampaign.value = campaignRepository.getLatestCampaign()
                (ads as MutableStateFlow).value = campaignRepository.getPromotionalAds()
                (banners as MutableStateFlow).value = campaignRepository.getBanners()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi tải dữ liệu dashboard ban đầu", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        refreshWifiStatus()
        viewModelScope.launch {
            try {
                userRepository.fetchUserProfile(force = true)
                notificationRepository.getNotifications()
                _latestCampaign.value = campaignRepository.getLatestCampaign()
                (ads as MutableStateFlow).value = campaignRepository.getPromotionalAds()
                (banners as MutableStateFlow).value = campaignRepository.getBanners()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi refresh dashboard", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun dismissCampaign(campaignId: String) {
        viewModelScope.launch {
            tokenManager.dismissCampaign(campaignId)
        }
    }
}
