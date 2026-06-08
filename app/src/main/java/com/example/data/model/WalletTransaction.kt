package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // earning (commission earnings), commission (deduction), load (add cash), withdraw
    val desc: String,
    val amount: Double,
    val timeStr: String,
    val dateStr: String,
    val status: String, // completed, pending
    val commission: Double = 0.0
)
