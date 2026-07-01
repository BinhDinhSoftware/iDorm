package com.bdsoftware.idorm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import com.bdsoftware.idorm.core.data.repository.InvoiceRepository
import com.bdsoftware.idorm.core.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val isLoggedIn: Boolean) : MainActivityUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val tokenManager: IDormPreferencesDataSource,
    private val notificationRepository: NotificationRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = tokenManager.isLoggedIn
        .map { MainActivityUiState.Success(isLoggedIn = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )

    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val unpaidInvoiceCount: StateFlow<Int> = invoiceRepository.unpaidInvoiceCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    init {
        viewModelScope.launch {
            tokenManager.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    try {
                        notificationRepository.getNotifications()
                        invoiceRepository.getInvoices()
                        invoiceRepository.getInvoiceEWs()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }

        // Synchronize logged-in user profile details with Crashlytics
        viewModelScope.launch {
            tokenManager.userId.collect { id ->
                id?.let {
                    com.bdsoftware.idorm.core.common.util.CrashlyticsUtils.setUserId(it.toString())
                }
            }
        }
        viewModelScope.launch {
            tokenManager.userStudentCode.collect { code ->
                code?.let {
                    com.bdsoftware.idorm.core.common.util.CrashlyticsUtils.setCustomKey("student_code", it)
                }
            }
        }
        viewModelScope.launch {
            tokenManager.userFullName.collect { name ->
                name?.let {
                    com.bdsoftware.idorm.core.common.util.CrashlyticsUtils.setCustomKey("fullname", name)
                }
            }
        }
        viewModelScope.launch {
            tokenManager.userEmail.collect { email ->
                email?.let {
                    com.bdsoftware.idorm.core.common.util.CrashlyticsUtils.setCustomKey("email", email)
                }
            }
        }
        viewModelScope.launch {
            tokenManager.userRoom.collect { room ->
                room?.let {
                    com.bdsoftware.idorm.core.common.util.CrashlyticsUtils.setCustomKey("room", room)
                }
            }
        }
    }
}
