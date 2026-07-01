package com.bdsoftware.idorm.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.domain.GetStudentInfoUseCase
import com.bdsoftware.idorm.core.model.StudentProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bdsoftware.idorm.core.common.util.toUserMessage

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getStudentInfoUseCase: GetStudentInfoUseCase
) : ViewModel() {

    sealed interface ProfileUiState {
        data object Loading : ProfileUiState
        data class Success(val profile: StudentProfile) : ProfileUiState
        data class Error(val message: String) : ProfileUiState
    }

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = getStudentInfoUseCase()
            _uiState.value = result.fold(
                onSuccess = { ProfileUiState.Success(it) },
                onFailure = { ProfileUiState.Error(it.toUserMessage("Không thể tải thông tin")) }
            )
        }
    }
}
