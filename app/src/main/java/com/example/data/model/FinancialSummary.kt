package com.example.data.model

data class AccountBalance(
    val code: String,
    val name: String,
    val accountClass: Int,
    val totalDebit: Double = 0.0,
    val totalCredit: Double = 0.0,
    val balance: Double = 0.0 // Débiteur si > 0, Créditeur si < 0
)

data class FinancialSummary(
    val patrimoineNet: Double = 0.0,
    val capaciteEpargne: Double = 0.0,
    val tauxEpargne: Double = 0.0,
    val tresorerieDisponible: Double = 0.0,
    val totalActifs: Double = 0.0,
    val totalDettes: Double = 0.0,
    val totalCharges: Double = 0.0,
    val totalProduits: Double = 0.0,
    val totalLiquidites: Double = 0.0,
    val totalEpargne: Double = 0.0,
    val accountBalances: Map<String, AccountBalance> = emptyMap(),
    val currency: String = "FCFA"
)
