package com.anas.linkchain.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anas.linkchain.LinkChainApp
import com.anas.linkchain.domain.model.HistoryItem
import com.anas.linkchain.domain.model.ItemStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val historyDao = (app as LinkChainApp).database.historyDao()

    val historyItems = historyDao.getAllHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<ItemStatus?>(null)
    val filterStatus: StateFlow<ItemStatus?> = _filterStatus.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: ItemStatus?) {
        _filterStatus.value = status
    }

    fun deleteItem(item: HistoryItem) {
        viewModelScope.launch {
            historyDao.delete(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyDao.clearHistory()
        }
    }
}