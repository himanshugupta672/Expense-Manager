package com.example.budgettracker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val  id:Int,
    val lable: String,
    val amount: Double,
    val description: String) {
}