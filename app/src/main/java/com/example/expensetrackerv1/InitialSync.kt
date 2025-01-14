package com.example.expensetrackerv1

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetrackerv1.data.ExpenseDatabase
import com.example.expensetrackerv1.data.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SyncViewModel(
    application: Application,
    private val database: ExpenseDatabase
) : AndroidViewModel(application) {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> get() = _syncState
    private val context: Context = getApplication<Application>().applicationContext

    fun performInitialSync() {
        viewModelScope.launch {
            _syncState.value = SyncState.InProgress
            try {
                val sync = InitialSync(context,database)
                sync.performInitialSync()
                _syncState.value = SyncState.Success
            } catch (e: Exception) {
                println("e")
                _syncState.value = SyncState.Error(e)
            }
        }
    }
}

sealed class SyncState {
    data object Idle : SyncState()
    data object InProgress : SyncState()
    data object Success : SyncState()
    data class Error(val exception: Exception) : SyncState()
}

class SyncViewModelFactory(
    private val application: Application,  // Use Application instead of Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            val database = ExpenseDatabase.getInstance(application.applicationContext)
            return SyncViewModel(application, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class InitialSync(
    private val context: Context,
    private val database: ExpenseDatabase,
    private val title: String = "Initial Sync with Firebase"
) {


    private val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val firestore = FirebaseDatabase()

    // Perform the initial sync
    fun performInitialSync() {
        if (isFirstRun()) {
            CoroutineScope(Dispatchers.IO).launch {
                runFirebaseSync()
                markSyncAsCompleted()
            }
        }
    }

    // Check if it's the first run
    private fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("first_run", true)
    }

    // Mark sync as completed
    private fun markSyncAsCompleted() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("first_run", false)
        editor.apply()
    }

    // Function to sync data with Firebase
    private suspend fun runFirebaseSync() {
        firestore.syncFirebaseData(database.expenseDao(), context, title)
    }
}
