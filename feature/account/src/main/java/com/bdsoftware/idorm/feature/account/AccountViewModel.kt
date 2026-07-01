package com.bdsoftware.idorm.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.datastore.IDormPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val tokenManager: IDormPreferencesDataSource
) : ViewModel() {

    data class UserProfile(
        val fullName: String = "",
        val room: String = "",
        val avatarUrl: String = "",
        val mobile: String = "",
        val studentCode: String = ""
    )

    val userProfile: StateFlow<UserProfile?> = combine(
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
        initialValue = null
    )
}
