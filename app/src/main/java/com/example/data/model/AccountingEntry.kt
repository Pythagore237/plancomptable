package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class AccountingEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val reference: String = "",
    val label: String = "",
    val debitAccountCode: String = "",
    val debitAccountName: String = "",
    val creditAccountCode: String = "",
    val creditAccountName: String = "",
    val amount: Double = 0.0,
    val categoryType: String = "EXPENSE",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
