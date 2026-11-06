package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class BudgetItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val accountCode: String = "",
    val targetAccountCode: String = "",
    val title: String = "",
    val monthlyLimit: Double = 0.0,
    val periodMonthYear: String = "ALL", // Format "YYYY-MM" or "ALL"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
