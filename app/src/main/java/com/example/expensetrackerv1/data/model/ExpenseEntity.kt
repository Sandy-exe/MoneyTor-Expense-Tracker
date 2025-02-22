package com.example.expensetrackerv1.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "expense_table")
@Serializable
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val title: String,
    val amount: Double,
    val date: String,
    val type: String,
){
    // No-argument constructor needed for Firestore deserialization
    constructor() : this(id = null, title = "", amount = 0.0, date = "", type = "")
}