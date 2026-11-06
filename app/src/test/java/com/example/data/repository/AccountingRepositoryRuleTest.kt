package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AccountingEntry
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AccountingRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addEntry_authenticatedOwner_createsDocument() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = AccountingRepository(firestore)

        val entry = AccountingEntry(
            userId = uid,
            dateMillis = System.currentTimeMillis(),
            reference = "TEST-REF-1",
            label = "Courses Supermarché",
            debitAccountCode = "621",
            debitAccountName = "Alimentation",
            creditAccountCode = "531",
            creditAccountName = "Orange Money",
            amount = 12500.0,
            categoryType = "EXPENSE",
            note = "Courses du mois"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addEntry(uid, entry)
        }
        assertTrue(result.isSuccess)
        val docId = result.getOrThrow()
        assertTrue(docId.isNotEmpty())

        val entries = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.observeEntries(uid).first()
        }
        assertTrue(entries.any { it.id == docId })
    }

    @Test
    fun deleteEntry_authenticatedOwner_removesDocument() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = AccountingRepository(firestore)

        val entry = AccountingEntry(
            userId = uid,
            dateMillis = System.currentTimeMillis(),
            label = "Transport Yango",
            debitAccountCode = "631",
            debitAccountName = "Transport",
            creditAccountCode = "511",
            creditAccountName = "Espèces",
            amount = 2500.0,
            categoryType = "EXPENSE"
        )

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addEntry(uid, entry)
        }
        val docId = addResult.getOrThrow()

        val delResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteEntry(uid, docId)
        }
        assertTrue(delResult.isSuccess)

        val entriesAfter = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.observeEntries(uid).first()
        }
        assertTrue(entriesAfter.none { it.id == docId })
    }

    @Test
    fun observeEntries_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = AccountingRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeEntries("unauthenticated_uid").first()
            }
            fail("Expected security rules rejection for unauthenticated user")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertNotNull(firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@compta.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
