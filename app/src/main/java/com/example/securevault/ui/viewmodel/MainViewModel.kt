package com.example.securevault.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.securevault.crypto.AuthManager
import com.example.securevault.crypto.CryptoManager
import com.example.securevault.crypto.ExportImportManager
import com.example.securevault.data.AppDatabase
import com.example.securevault.data.BillRepository
import com.example.securevault.data.CredentialRepository
import com.example.securevault.data.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CredentialRepository
    private val billRepository: BillRepository
    private val noteRepository: NoteRepository
    private val exportImportManager: ExportImportManager
    private val authManager: AuthManager

    val credentials: StateFlow<List<CredentialRepository.DomainCredential>>
    val bills: StateFlow<List<BillRepository.DomainBill>>
    val notes: StateFlow<List<NoteRepository.DomainNote>>

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState
    
    // Auth State
    private val _isUserAuthenticated = MutableStateFlow(false)
    val isUserAuthenticated: StateFlow<Boolean> = _isUserAuthenticated
    
    private val _isSetupRequired = MutableStateFlow(true)
    val isSetupRequired: StateFlow<Boolean> = _isSetupRequired

    init {
        val database = AppDatabase.getDatabase(application)
        val cryptoManager = CryptoManager()
        repository = CredentialRepository(database.credentialDao(), cryptoManager)
        billRepository = BillRepository(database.billDao(), cryptoManager)
        noteRepository = NoteRepository(database.noteDao(), cryptoManager)
        exportImportManager = ExportImportManager(application)
        authManager = AuthManager(application)
        
        _isSetupRequired.value = !authManager.isSetup()
        
        credentials = repository.domainCredentials.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        
        bills = billRepository.domainBills.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        
        notes = noteRepository.domainNotes.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }
    
    fun setupMasterPassword(password: String) {
        if (authManager.setupMasterPassword(password)) {
            _isSetupRequired.value = false
            _isUserAuthenticated.value = true
        } else {
            _uiState.value = UiState.Error("Failed to set password")
        }
    }
    
    fun verifyMasterPassword(password: String) {
        if (authManager.verifyMasterPassword(password)) {
            _isUserAuthenticated.value = true
        } else {
            _uiState.value = UiState.Error("Incorrect Password")
        }
    }

    fun addCredential(title: String, username: String, pass: String, url: String, notes: String) {
        viewModelScope.launch {
            repository.insertCredential(title, username, pass, url, notes)
        }
    }

    fun updateCredential(id: Int, title: String, username: String, pass: String, url: String, notes: String) {
        viewModelScope.launch {
            repository.updateCredential(id, title, username, pass, url, notes)
        }
    }

    fun deleteCredential(id: Int) {
        viewModelScope.launch {
            repository.deleteCredential(id)
        }
    }
    
    // Bill Management
    fun addBill(billName: String, amount: String, notes: String, dueDate: Long, frequency: String) {
        viewModelScope.launch {
            billRepository.insertBill(billName, amount, notes, dueDate, frequency)
        }
    }

    fun updateBill(id: Int, billName: String, amount: String, notes: String, dueDate: Long, frequency: String) {
        viewModelScope.launch {
            billRepository.updateBill(id, billName, amount, notes, dueDate, frequency)
        }
    }

    fun deleteBill(id: Int) {
        viewModelScope.launch {
            billRepository.deleteBill(id)
        }
    }
    
    // Note Management
    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            noteRepository.insertNote(title, content)
        }
    }

    fun updateNote(id: Int, title: String, content: String) {
        viewModelScope.launch {
            noteRepository.updateNote(id, title, content)
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            noteRepository.deleteNote(id)
        }
    }

    fun exportData(uri: Uri, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            
            val currentCredentials = credentials.value.map { 
                ExportImportManager.PlainCredential(it.title, it.username, it.password, it.url, it.notes)
            }
            
            val currentBills = bills.value.map {
                ExportImportManager.PlainBill(it.billName, it.amount, it.notes, it.dueDate, it.frequency)
            }
            
            val currentNotes = notes.value.map {
                ExportImportManager.PlainNote(it.title, it.content)
            }
            
            val success = exportImportManager.exportData(uri, password, currentCredentials, currentBills, currentNotes)
            withContext(Dispatchers.Main) {
                _uiState.value = if (success) UiState.Success("Export Successful") else UiState.Error("Export Failed")
            }
        }
    }

    fun importData(uri: Uri, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            val allData = exportImportManager.importData(uri, password)
            if (allData != null) {
                // Import credentials
                allData.credentials.forEach {
                    repository.insertCredential(it.title, it.username, it.password, it.url, it.notes)
                }
                // Import bills
                allData.bills.forEach {
                    billRepository.insertBill(it.billName, it.amount, it.notes, it.dueDate, it.frequency)
                }
                // Import notes
                allData.notes.forEach {
                    noteRepository.insertNote(it.title, it.content)
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
