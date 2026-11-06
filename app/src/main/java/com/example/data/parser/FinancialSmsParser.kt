package com.example.data.parser

import com.example.data.model.AccountingEntry
import com.example.data.model.OperationPresetType
import java.util.UUID
import java.util.regex.Pattern

data class ParsedSmsTransaction(
    val detectedType: OperationPresetType,
    val amount: Double,
    val currency: String,
    val label: String,
    val debitAccountCode: String,
    val debitAccountName: String,
    val creditAccountCode: String,
    val creditAccountName: String,
    val reference: String,
    val rawText: String
)

object FinancialSmsParser {

    fun parse(smsText: String, defaultCurrency: String = "FCFA"): ParsedSmsTransaction? {
        val trimmed = smsText.trim()
        if (trimmed.isBlank()) return null

        val lower = trimmed.lowercase()

        // 1. Detect Channel / Wallet
        val isOrange = lower.contains("orange") || lower.contains("om ") || lower.contains("om:") || lower.contains("mp2")
        val isMtn = lower.contains("mtn") || lower.contains("y'ello") || lower.contains("momo")
        val isBank = lower.contains("banque") || lower.contains("bank") || lower.contains("compte") || lower.contains("carte") || lower.contains("virement")

        val walletAccountCode = when {
            isMtn -> "532" // MTN Mobile Money
            isOrange -> "531" // Orange Money
            isBank -> "521" // Comptes bancaires
            else -> "531" // Default Mobile Money
        }
        val walletAccountName = when {
            isMtn -> "MTN Mobile Money"
            isOrange -> "Orange Money"
            isBank -> "Comptes bancaires"
            else -> "Orange Money"
        }

        // 2. Extract Amount
        val amountPattern = Pattern.compile("([0-9]{1,3}(?:[\\s.,][0-9]{3})*|[0-9]+)(?:[.,][0-9]{1,2})?\\s*(fcfa|xaf|xof|eur|usd|cfa|f cfa|frs)?", Pattern.CASE_INSENSITIVE)
        val matcher = amountPattern.matcher(trimmed)
        var extractedAmount: Double? = null
        var extractedCurrency = defaultCurrency

        while (matcher.find()) {
            val numStr = matcher.group(1)?.replace(" ", "")?.replace(",", "")?.replace(".", "")
            val currStr = matcher.group(2)
            val parsed = numStr?.toDoubleOrNull()
            if (parsed != null && parsed > 50 && (extractedAmount == null || currStr != null)) {
                extractedAmount = parsed
                if (currStr != null) {
                    extractedCurrency = currStr.uppercase().replace("FRS", "FCFA").replace("CFA", "FCFA").replace("F FCFA", "FCFA")
                }
            }
        }

        val amount = extractedAmount ?: 0.0
        if (amount <= 0.0) return null

        // 3. Extract Reference
        val refPattern = Pattern.compile("(?:ref|txnid|id|transaction id|trans id)[:\\s]+([a-zA-Z0-9._\\-]+)", Pattern.CASE_INSENSITIVE)
        val refMatcher = refPattern.matcher(trimmed)
        val reference = if (refMatcher.find()) refMatcher.group(1) ?: "SMS-${System.currentTimeMillis() % 10000}" else "SMS-${System.currentTimeMillis() % 10000}"

        // 4. Direction (Reception vs Payment / Transfer)
        val isReceived = lower.contains("recu") || lower.contains("reçu") || lower.contains("credited") || lower.contains("credit de") || lower.contains("depot de") || lower.contains("dépôt")
        val isTransferToOwn = lower.contains("epargne") || lower.contains("urgence") || lower.contains("coffre")

        if (isReceived) {
            // Check if Salary or general revenue
            val isSalary = lower.contains("salaire") || lower.contains("paie") || lower.contains("remuneration")
            val debitCode = walletAccountCode
            val debitName = walletAccountName
            val creditCode = if (isSalary) "701" else "751"
            val creditName = if (isSalary) "Salaire" else "Autres revenus"
            val label = if (isSalary) "Encaissement Salaire (via $walletAccountName)" else "Réception fonds (via $walletAccountName)"

            return ParsedSmsTransaction(
                detectedType = OperationPresetType.REVENU_SALAIRE,
                amount = amount,
                currency = extractedCurrency,
                label = label,
                debitAccountCode = debitCode,
                debitAccountName = debitName,
                creditAccountCode = creditCode,
                creditAccountName = creditName,
                reference = reference,
                rawText = trimmed
            )
        } else {
            // Outgoing payment / expense / transfer
            if (isTransferToOwn) {
                return ParsedSmsTransaction(
                    detectedType = OperationPresetType.VIREMENT_EPARGNE,
                    amount = amount,
                    currency = extractedCurrency,
                    label = "Virement vers Fonds d'urgence",
                    debitAccountCode = "551",
                    debitAccountName = "Fonds d'urgence",
                    creditAccountCode = walletAccountCode,
                    creditAccountName = walletAccountName,
                    reference = reference,
                    rawText = trimmed
                )
            }

            // Categorize expense based on keywords
            val (expenseCode, expenseName, defaultLabel) = when {
                lower.contains("supermarche") || lower.contains("marche") || lower.contains("resto") || lower.contains("boulangerie") || lower.contains("food") || lower.contains("carrefour") || lower.contains("alimentation") ->
                    Triple("621", "Alimentation", "Achat alimentation")
                lower.contains("taxi") || lower.contains("yango") || lower.contains("uber") || lower.contains("carburant") || lower.contains("total") || lower.contains("shell") || lower.contains("tradex") || lower.contains("ola") ->
                    Triple("631", "Transport", "Frais de transport / carburant")
                lower.contains("eneo") || lower.contains("camwater") || lower.contains("cie") || lower.contains("sodeci") || lower.contains("electricite") || lower.contains("eau") ->
                    Triple("611", "Eau et électricité", "Facture eau / électricité")
                lower.contains("pharmacie") || lower.contains("hopital") || lower.contains("clinique") || lower.contains("medecin") || lower.contains("sante") ->
                    Triple("651", "Santé", "Dépense de santé / pharmacie")
                lower.contains("canal") || lower.contains("netflix") || lower.contains("cinema") || lower.contains("soiree") || lower.contains("bar") ->
                    Triple("661", "Loisirs et divertissements", "Loisirs / divertissement")
                lower.contains("forfait") || lower.contains("credit") || lower.contains("data") || lower.contains("recharge") || lower.contains("internet") || lower.contains("fibre") || lower.contains("megas") ->
                    Triple("691", "Télécommunications", "Recharge crédit / forfait internet")
                lower.contains("loyer") || lower.contains("bail") ->
                    Triple("601", "Logement", "Règlement loyer")
                lower.contains("formation") || lower.contains("ecole") || lower.contains("cours") || lower.contains("certification") ->
                    Triple("671", "Éducation et formations", "Formation / éducation")
                lower.contains("ordi") || lower.contains("laptop") || lower.contains("smartphone") || lower.contains("iphone") || lower.contains("samsung") ->
                    Triple("217", "Matériel informatique", "Achat matériel informatique")
                else ->
                    Triple("699", "Autres dépenses", "Règlement / transfert débit")
            }

            val isImmobilisation = expenseCode.startsWith("2")
            val presetType = if (isImmobilisation) OperationPresetType.ACHAT_IMMOBILISATION else OperationPresetType.DEPENSE_COURANTE

            return ParsedSmsTransaction(
                detectedType = presetType,
                amount = amount,
                currency = extractedCurrency,
                label = "$defaultLabel (via $walletAccountName)",
                debitAccountCode = expenseCode,
                debitAccountName = expenseName,
                creditAccountCode = walletAccountCode,
                creditAccountName = walletAccountName,
                reference = reference,
                rawText = trimmed
            )
        }
    }
}
