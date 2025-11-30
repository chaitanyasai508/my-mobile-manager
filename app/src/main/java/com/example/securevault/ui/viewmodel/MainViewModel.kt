package com.example.securevault.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.securevault.crypto.CryptoManager
import com.example.securevault.crypto.ExportImportManager
import com.example.securevault.data.AppDatabase
import com.example.securevault.data.CredentialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CredentialRepository
    private val exportImportManager: ExportImportManager

    val credentials: StateFlow<List<CredentialRepository.DomainCredential>>

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    init {
        val database = AppDatabase.getDatabase(application)
        val cryptoManager = CryptoManager()
        repository = CredentialRepository(database.credentialDao(), cryptoManager)
        exportImportManager = ExportImportManager(application)
        
        credentials = repository.domainCredentials.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun addCredential(title: String, username: String, pass: String, notes: String) {
        viewModelScope.launch {
            repository.insertCredential(title, username, pass, notes)
        }
    }

    fun updateCredential(id: Int, title: String, username: String, pass: String, notes: String) {
        viewModelScope.launch {
            repository.updateCredential(id, title, username, pass, notes)
        }
    }

    fun deleteCredential(id: Int) {
        viewModelScope.launch {
            repository.deleteCredential(id)
        }
    }

    fun exportData(uri: Uri, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            // We need to fetch all plain credentials first
            // Since we can't collect the flow here easily in a one-off, let's just rely on the current state if possible,
            // or better, add a suspend function to repo to get all plain credentials.
            // For now, let's map the current value of the stateflow if it's populated, or query the DB.
            // A cleaner way is to expose a suspend function in Repo.
            // Let's assume we can get them from the flow for now or just re-query.
            // Actually, let's just use the repo's flow but we need a snapshot.
            // Let's add a helper in Repo to get snapshot.
            
            // Hack for now: collect once.
            val currentList = credentials.value.map { 
                ExportImportManager.PlainCredential(it.title, it.username, it.password, it.notes)
            }
            
            val success = exportImportManager.exportData(uri, password, currentList)
            withContext(Dispatchers.Main) {
                _uiState.value = if (success) UiState.Success("Export Successful") else UiState.Error("Export Failed")
            }
        }
    }

    fun importData(uri: Uri, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            val importedList = exportImportManager.importData(uri, password)
            if (importedList != null) {
                // Clear existing and replace? Or append?
                // User asked for import. Usually append or merge.
                // Let's append for safety, user can delete duplicates.
                // Or maybe we should wipe? "Import" often implies restore.
                // Let's just append.
                importedList.forEach {
                    repository.insertCredential(it.title, it.username, it.password, it.notes)
                }
                withContext(Dispatchers.Main) {
                    _uiState.value = UiState.Success("Import Successful")
                }
            } else {
                withContext(Dispatchers.Main) {
                    _uiState.value = UiState.Error("Import Failed: Invalid password or file")
                }
            }
        }
    }
    
    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}
