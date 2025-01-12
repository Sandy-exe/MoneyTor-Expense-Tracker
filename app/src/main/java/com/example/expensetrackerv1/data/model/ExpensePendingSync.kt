package com.example.expensetrackerv1.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "expense_pending_sync_table")
data class ExpensePendingSync(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val title: String,
    val amount: Double,
    val date: String,
    val type: String,
    val syncOperation: String,
    val syncStatus: Boolean
)