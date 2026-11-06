package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Account
import com.example.data.model.AccountBalance
import com.example.data.model.AccountingEntry
import com.example.data.model.BudgetItem
import com.example.data.model.FinancialSummary
import com.example.data.model.OperationPresetType
import com.example.data.parser.FinancialSmsParser
import com.example.data.parser.ParsedSmsTransaction
import com.example.data.repository.AccountingRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AccountingUiState(
    val entries: List<AccountingEntry> = emptyList(),
    val accounts: List<Account> = Account.DEFAULT_ACCOUNTS,
    val budgets: List<BudgetItem> = emptyList(),
    val summary: FinancialSummary = FinancialSummary(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedPeriodMonthYear: String = getCurrentMonthYear(),
    val stagedSms: ParsedSmsTransaction? = null
)

private fun getCurrentMonthYear(): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return sdf.format(Date())
}

class AccountingViewModel(
    private val repository: AccountingRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(getCurrentMonthYear())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _stagedSms = MutableStateFlow<ParsedSmsTransaction?>(null)
    val stagedSms: StateFlow<ParsedSmsTransaction?> = _stagedSms.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val entriesFlow = repository.observeEntries(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Entries observe error: ${error.message}", error)
            emit(emptyList())
        }

    private val accountsFlow = repository.observeAccounts(currentUserId)
        .catch { error ->
            Log.w(TAG, "Accounts observe error: ${error.message}", error)
            emit(Account.DEFAULT_ACCOUNTS)
        }

    private val budgetsFlow = repository.observeBudgets(currentUserId)
        .catch { error ->
            Log.w(TAG, "Budgets observe error: ${error.message}", error)
            emit(emptyList())
        }

    val uiState: StateFlow<AccountingUiState> = combine(
        entriesFlow,
        accountsFlow,
        budgetsFlow,
        _selectedMonth,
        _stagedSms
    ) { entries, accounts, budgets, month, staged ->
        val summary = calculateFinancialSummary(entries, accounts)
        AccountingUiState(
            entries = entries,
            accounts = accounts,
            budgets = budgets,
            summary = summary,
            isLoading = false,
            errorMessage = null,
            selectedPeriodMonthYear = month,
            stagedSms = staged
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AccountingUiState(isLoading = true)
    )

    fun selectPeriod(monthYear: String) {
        _selectedMonth.value = monthYear
    }

    fun parseSms(smsText: String) {
        val result = FinancialSmsParser.parse(smsText, defaultCurrency = "FCFA")
        _stagedSms.value = result
        if (result == null) {
            _statusMessage.value = "Impossible de détecter automatiquement une transaction dans ce SMS."
        }
    }

    fun clearStagedSms() {
        _stagedSms.value = null
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun addEntry(
        dateMillis: Long,
        reference: String,
        label: String,
        debitAccountCode: String,
        debitAccountName: String,
        creditAccountCode: String,
        creditAccountName: String,
        amount: Double,
        categoryType: String,
        note: String
    ) {
        viewModelScope.launch {
            val entry = AccountingEntry(
                userId = currentUserId,
                dateMillis = dateMillis,
                reference = reference.ifBlank { "ECR-${System.currentTimeMillis() % 100000}" },
                label = label,
                debitAccountCode = debitAccountCode,
                debitAccountName = debitAccountName,
                creditAccountCode = creditAccountCode,
                creditAccountName = creditAccountName,
                amount = amount,
                categoryType = categoryType,
                note = note
            )
            val result = repository.addEntry(currentUserId, entry)
            if (result.isSuccess) {
                _statusMessage.value = "Écriture comptable enregistrée avec succès !"
                _stagedSms.value = null
            } else {
                _statusMessage.value = "Erreur lors de l'enregistrement: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            val result = repository.deleteEntry(currentUserId, entryId)
            if (result.isSuccess) {
                _statusMessage.value = "Écriture supprimée."
            } else {
                _statusMessage.value = "Erreur de suppression."
            }
        }
    }

    fun saveAccount(code: String, name: String, accountClass: Int, description: String) {
        viewModelScope.launch {
            val acc = Account(
                id = "acc_$code",
                userId = currentUserId,
                code = code,
                name = name,
                accountClass = accountClass,
                description = description,
                isCustom = true
            )
            val result = repository.saveAccount(currentUserId, acc)
            if (result.isSuccess) {
                _statusMessage.value = "Compte $code enregistré dans le plan comptable."
            }
        }
    }

    fun saveBudget(accountCode: String, targetAccountCode: String, title: String, monthlyLimit: Double, period: String) {
        viewModelScope.launch {
            val budget = BudgetItem(
                id = "budget_${accountCode}_${targetAccountCode}",
                userId = currentUserId,
                accountCode = accountCode,
                targetAccountCode = targetAccountCode,
                title = title,
                monthlyLimit = monthlyLimit,
                periodMonthYear = period
            )
            repository.saveBudget(currentUserId, budget)
            _statusMessage.value = "Budget / objectif enregistré !"
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            repository.deleteBudget(currentUserId, budgetId)
        }
    }

    fun seedDemoDataIfEmpty() {
        viewModelScope.launch {
            val current = uiState.value.entries
            if (current.isNotEmpty()) return@launch

            val now = System.currentTimeMillis()
            val dayMillis = 86400000L

            val demoList = listOf(
                AccountingEntry(
                    dateMillis = now - 5 * dayMillis,
                    reference = "VIR-SALAIRE-01",
                    label = "Salaire mensuel Novembre",
                    debitAccountCode = "521",
                    debitAccountName = "Comptes bancaires",
                    creditAccountCode = "701",
                    creditAccountName = "Salaire",
                    amount = 450000.0,
                    categoryType = "REVENUE",
                    note = "Virement société"
                ),
                AccountingEntry(
                    dateMillis = now - 4 * dayMillis,
                    reference = "OM-EPARGNE-01",
                    label = "Épargne Fonds d'urgence",
                    debitAccountCode = "551",
                    debitAccountName = "Fonds d'urgence",
                    creditAccountCode = "521",
                    creditAccountName = "Comptes bancaires",
                    amount = 80000.0,
                    categoryType = "SAVINGS_TRANSFER",
                    note = "Règle 1 : L'épargne n'est pas une charge"
                ),
                AccountingEntry(
                    dateMillis = now - 4 * dayMillis,
                    reference = "MOMO-TOPUP",
                    label = "Alimentation Orange Money",
                    debitAccountCode = "531",
                    debitAccountName = "Orange Money",
                    creditAccountCode = "521",
                    creditAccountName = "Comptes bancaires",
                    amount = 100000.0,
                    categoryType = "INTERNAL_TRANSFER",
                    note = "Règle 2 : Transfert entre propres comptes"
                ),
                AccountingEntry(
                    dateMillis = now - 3 * dayMillis,
                    reference = "LOY-NOV26",
                    label = "Paiement Loyer mensuel",
                    debitAccountCode = "601",
                    debitAccountName = "Logement",
                    creditAccountCode = "521",
                    creditAccountName = "Comptes bancaires",
                    amount = 120000.0,
                    categoryType = "EXPENSE",
                    note = "Quittance propriétaire"
                ),
                AccountingEntry(
                    dateMillis = now - 2 * dayMillis,
                    reference = "OM-COURSES-01",
                    label = "Courses Supermarché Carrefour",
                    debitAccountCode = "621",
                    debitAccountName = "Alimentation",
                    creditAccountCode = "531",
                    creditAccountName = "Orange Money",
                    amount = 38500.0,
                    categoryType = "EXPENSE",
                    note = "Provisions du mois"
                ),
                AccountingEntry(
                    dateMillis = now - 2 * dayMillis,
                    reference = "OM-ENEO-01",
                    label = "Facture Électricité ENEO",
                    debitAccountCode = "611",
                    debitAccountName = "Eau et électricité",
                    creditAccountCode = "531",
                    creditAccountName = "Orange Money",
                    amount = 14200.0,
                    categoryType = "EXPENSE",
                    note = "Facture reçue par SMS"
                ),
                AccountingEntry(
                    dateMillis = now - 1 * dayMillis,
                    reference = "OM-IMMO-01",
                    label = "Achat Ordinateur portable professionnel",
                    debitAccountCode = "217",
                    debitAccountName = "Matériel informatique",
                    creditAccountCode = "531",
                    creditAccountName = "Orange Money",
                    amount = 250000.0,
                    categoryType = "ASSET_PURCHASE",
                    note = "Règle 5 : Bien durable, augmente le patrimoine"
                ),
                AccountingEntry(
                    dateMillis = now - 1 * dayMillis,
                    reference = "PRET-PAUL",
                    label = "Dépannage accordé à Paul",
                    debitAccountCode = "411",
                    debitAccountName = "Créances sur des tiers",
                    creditAccountCode = "531",
                    creditAccountName = "Orange Money",
                    amount = 20000.0,
                    categoryType = "LOAN_GIVEN",
                    note = "Règle 4 : Prêt accordé = créance"
                )
            )

            for (entry in demoList) {
                repository.addEntry(currentUserId, entry)
            }

            // Seed default class 9 budgets
            repository.saveBudget(
                currentUserId,
                BudgetItem(
                    id = "budget_902_621",
                    userId = currentUserId,
                    accountCode = "902",
                    targetAccountCode = "621",
                    title = "Budget Alimentation Mensuel",
                    monthlyLimit = 60000.0,
                    periodMonthYear = "ALL"
                )
            )
            repository.saveBudget(
                currentUserId,
                BudgetItem(
                    id = "budget_903_631",
                    userId = currentUserId,
                    accountCode = "903",
                    targetAccountCode = "631",
                    title = "Budget Transport Mensuel",
                    monthlyLimit = 25000.0,
                    periodMonthYear = "ALL"
                )
            )
            repository.saveBudget(
                currentUserId,
                BudgetItem(
                    id = "budget_907_551",
                    userId = currentUserId,
                    accountCode = "907",
                    targetAccountCode = "551",
                    title = "Objectif Fonds d'Urgence",
                    monthlyLimit = 100000.0,
                    periodMonthYear = "ALL"
                )
            )

            _statusMessage.value = "Exemples de données comptables SYSCOHADA initialisés !"
        }
    }

    private fun calculateFinancialSummary(
        entries: List<AccountingEntry>,
        accounts: List<Account>
    ): FinancialSummary {
        val debitTotals = mutableMapOf<String, Double>()
        val creditTotals = mutableMapOf<String, Double>()

        for (e in entries) {
            debitTotals[e.debitAccountCode] = (debitTotals[e.debitAccountCode] ?: 0.0) + e.amount
            creditTotals[e.creditAccountCode] = (creditTotals[e.creditAccountCode] ?: 0.0) + e.amount
        }

        val allCodes = (Account.DEFAULT_ACCOUNTS.map { it.code } + debitTotals.keys + creditTotals.keys).distinct()
        val accountMap = accounts.associateBy { it.code }

        val balances = mutableMapOf<String, AccountBalance>()
        for (code in allCodes) {
            val d = debitTotals[code] ?: 0.0
            val c = creditTotals[code] ?: 0.0
            val acc = accountMap[code] ?: Account.DEFAULT_ACCOUNTS.firstOrNull { it.code == code }
            val classNum = acc?.accountClass ?: (code.firstOrNull()?.digitToIntOrNull() ?: 6)
            val name = acc?.name ?: "Compte $code"

            // Règle de solde selon la nature comptable :
            // Actif (2, 3, 5) et Charges (6, 801, 803, 805) : Solde normal Débiteur (Débit - Crédit)
            // Passif (1, 4 dettes) et Produits (7, 802, 804) : Solde normal Créditeur (Crédit - Débit)
            val net = when (classNum) {
                1, 7 -> c - d // Produits & Capitaux : solde créditeur
                2, 3, 5, 6 -> d - c // Actifs & Charges : solde débiteur
                4 -> if (code.startsWith("41") || code.startsWith("42") || code.startsWith("46")) d - c else c - d
                8 -> if (code == "802" || code == "804") c - d else d - c
                else -> d - c
            }
            balances[code] = AccountBalance(
                code = code,
                name = name,
                accountClass = classNum,
                totalDebit = d,
                totalCredit = c,
                balance = net
            )
        }

        // 1. Actifs :
        // Classe 2 (Immobilisations) + Classe 3 (Stocks) + Classe 5 (Trésorerie/Placements) + Créances (411, 421, 461)
        var totalActifs = 0.0
        var totalLiquidites = 0.0
        var totalEpargne = 0.0

        for ((code, b) in balances) {
            val classNum = b.accountClass
            val bal = b.balance
            if (classNum == 2 || classNum == 3) {
                totalActifs += maxOf(0.0, bal)
            } else if (classNum == 5) {
                totalActifs += maxOf(0.0, bal)
                if (code == "511" || code == "521" || code == "531" || code == "532" || code == "541") {
                    totalLiquidites += maxOf(0.0, bal)
                } else if (code.startsWith("55")) {
                    totalEpargne += maxOf(0.0, bal)
                }
            } else if (classNum == 4 && (code.startsWith("41") || code.startsWith("42") || code.startsWith("46"))) {
                totalActifs += maxOf(0.0, bal)
            }
        }

        // 2. Dettes :
        // Classe 4 Dettes (401 Fournisseurs, 431 Dettes pers., 451 Emprunts)
        var totalDettes = 0.0
        var dettesImmediates = 0.0
        for ((code, b) in balances) {
            if (b.accountClass == 4 && (code.startsWith("40") || code.startsWith("43") || code.startsWith("45"))) {
                val d = maxOf(0.0, b.balance)
                totalDettes += d
                if (code.startsWith("40") || code.startsWith("43")) {
                    dettesImmediates += d
                }
            }
        }

        // 3. Charges (Classe 6 + 801/803/805)
        var totalCharges = 0.0
        for ((code, b) in balances) {
            if (b.accountClass == 6) {
                totalCharges += maxOf(0.0, b.balance)
            } else if (b.accountClass == 8 && (code == "801" || code == "803" || code == "805")) {
                totalCharges += maxOf(0.0, b.balance)
            }
        }

        // 4. Produits (Classe 7 + 802/804)
        var totalProduits = 0.0
        for ((code, b) in balances) {
            if (b.accountClass == 7) {
                totalProduits += maxOf(0.0, b.balance)
            } else if (b.accountClass == 8 && (code == "802" || code == "804")) {
                totalProduits += maxOf(0.0, b.balance)
            }
        }

        // 5. Indicateurs clés demandés :
        // PN = Actifs - Dettes
        val patrimoineNet = totalActifs - totalDettes

        // CE = Revenus - Charges
        val capaciteEpargne = totalProduits - totalCharges

        // TE = (Épargne / Revenus) * 100
        val tauxEpargne = if (totalProduits > 0) (totalEpargne / totalProduits) * 100.0 else 0.0

        // TD = Liquidités - Dettes immédiates
        val tresorerieDisponible = totalLiquidites - dettesImmediates

        return FinancialSummary(
            patrimoineNet = patrimoineNet,
            capaciteEpargne = capaciteEpargne,
            tauxEpargne = tauxEpargne,
            tresorerieDisponible = tresorerieDisponible,
            totalActifs = totalActifs,
            totalDettes = totalDettes,
            totalCharges = totalCharges,
            totalProduits = totalProduits,
            totalLiquidites = totalLiquidites,
            totalEpargne = totalEpargne,
            accountBalances = balances,
            currency = "FCFA"
        )
    }

    private companion object {
        const val TAG = "AccountingVM"
    }
}
