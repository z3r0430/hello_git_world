package com.example.notilog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notilog.data.AppDatabase
import com.example.notilog.data.FilterMode
import com.example.notilog.data.FilterSettings
import com.example.notilog.data.FilterSettingsStore
import com.example.notilog.data.NotificationDao
import com.example.notilog.data.NotificationEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val dao: NotificationDao,
    private val filterStore: FilterSettingsStore,
) : ViewModel() {

    val entries: StateFlow<List<NotificationEntry>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val knownConversations: StateFlow<List<String>> =
        dao.observeConversations()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<FilterSettings> =
        filterStore.settings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FilterSettings())

    fun setRecordingEnabled(enabled: Boolean) =
        viewModelScope.launch { filterStore.setRecordingEnabled(enabled) }

    fun setMode(mode: FilterMode) =
        viewModelScope.launch { filterStore.setMode(mode) }

    fun addSelected(name: String) =
        viewModelScope.launch { filterStore.addSelected(name) }

    fun removeSelected(name: String) =
        viewModelScope.launch { filterStore.removeSelected(name) }

    fun clearLog() = viewModelScope.launch { dao.clear() }

    fun deleteEntry(id: Long) = viewModelScope.launch { dao.delete(id) }

    class Factory(
        private val database: AppDatabase,
        private val filterStore: FilterSettingsStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(database.notificationDao(), filterStore) as T
    }
}
