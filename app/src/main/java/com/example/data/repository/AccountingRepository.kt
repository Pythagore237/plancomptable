package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Account
import com.example.data.model.AccountingEntry
import com.example.data.model.BudgetItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AccountingRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // ==================== ENTRIES ====================

    fun observeEntries(userId: String): Flow<List<AccountingEntry>> {
        val path = "users/$userId/entries"
        return db.collection("users").document(userId).collection("entries")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(AccountingEntry::class.java)
                    .sortedByDescending { it.dateMillis }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val ex = firestoreError ?: (error as? Exception)
                if (ex != null) {
                    handleFirestoreError(ex, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun addEntry(userId: String, entry: AccountingEntry): Result<String> {
        val docId = if (entry.id.isNotBlank()) entry.id else "entry_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val docRef = db.collection("users").document(userId).collection("entries").document(docId)

        val payload = mapOf(
            "userId" to userId,
            "dateMillis" to entry.dateMillis,
            "reference" to entry.reference,
            "label" to entry.label,
            "debitAccountCode" to entry.debitAccountCode,
            "debitAccountName" to entry.debitAccountName,
            "creditAccountCode" to entry.creditAccountCode,
            "creditAccountName" to entry.creditAccountName,
            "amount" to entry.amount,
            "categoryType" to entry.categoryType,
            "note" to entry.note,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteEntry(userId: String, entryId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("entries").document(entryId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // ==================== CUSTOM ACCOUNTS ====================

    fun observeAccounts(userId: String): Flow<List<Account>> {
        val path = "users/$userId/accounts"
        return db.collection("users").document(userId).collection("accounts")
            .snapshots()
            .map { snapshot ->
                val customAccounts = snapshot.toObjects(Account::class.java)
                // Combine default accounts with custom/overridden accounts
                val customMap = customAccounts.associateBy { it.code }
                val merged = Account.DEFAULT_ACCOUNTS.map { defaultAcc ->
                    customMap[defaultAcc.code] ?: defaultAcc
                } + customAccounts.filter { customAcc ->
                    Account.DEFAULT_ACCOUNTS.none { it.code == customAcc.code }
                }
                merged.sortedBy { it.code }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val ex = firestoreError ?: (error as? Exception)
                if (ex != null) {
                    handleFirestoreError(ex, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun saveAccount(userId: String, account: Account): Result<Unit> {
        val docId = if (account.id.isNotBlank()) account.id else "acc_${account.code}"
        val docRef = db.collection("users").document(userId).collection("accounts").document(docId)

        val payload = mapOf(
            "userId" to userId,
            "code" to account.code,
            "name" to account.name,
            "accountClass" to account.accountClass,
            "description" to account.description,
            "isCustom" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(userId: String, accountId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("accounts").document(accountId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // ==================== BUDGETS ====================

    fun observeBudgets(userId: String): Flow<List<BudgetItem>> {
        val path = "users/$userId/budgets"
        return db.collection("users").document(userId).collection("budgets")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(BudgetItem::class.java).sortedBy { it.accountCode }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val ex = firestoreError ?: (error as? Exception)
                if (ex != null) {
                    handleFirestoreError(ex, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun saveBudget(userId: String, budget: BudgetItem): Result<Unit> {
        val docId = if (budget.id.isNotBlank()) budget.id else "budget_${budget.accountCode}"
        val docRef = db.collection("users").document(userId).collection("budgets").document(docId)

        val payload = mapOf(
            "userId" to userId,
            "accountCode" to budget.accountCode,
            "targetAccountCode" to budget.targetAccountCode,
            "title" to budget.title,
            "monthlyLimit" to budget.monthlyLimit,
            "periodMonthYear" to budget.periodMonthYear,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        return try {
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteBudget(userId: String, budgetId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("budgets").document(budgetId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
