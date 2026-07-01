package com.bdsoftware.idorm.feature.rent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bdsoftware.idorm.core.data.repository.RentRepository
import com.bdsoftware.idorm.core.network.model.NetworkRentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RentHistoryViewModel @Inject constructor(
    private val rentRepository: RentRepository
) : ViewModel() {

    private val _rentList = MutableStateFlow<List<NetworkRentItem>>(emptyList())
    val rentList: StateFlow<List<NetworkRentItem>> = _rentList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRentHistory()
    }

    fun loadRentHistory() {
        if (_isLoading.value) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _rentList.value = rentRepository.getRentList()
            } catch (e: Exception) {
                Log.e("RentHistoryViewModel", "Lỗi tải lịch sử thuê", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
