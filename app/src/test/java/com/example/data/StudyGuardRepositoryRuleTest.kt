package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class StudyGuardRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun extractFirestoreException(t: Throwable): FirebaseFirestoreException? {
        var curr: Throwable? = t
        while (curr != null) {
            if (curr is FirebaseFirestoreException) return curr
            curr = curr.cause
        }
        return null
    }

    @Test
    fun createFocusSession_authenticatedOwner_succeedsAndEmitsInFlow() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = StudyGuardRepository(firestore, auth)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createFocusSession(
                mode = "POMODORO",
                durationMinutes = 25,
                subject = "Physics Mechanics",
                completed = true
            )
        }
        assertTrue(createResult.isSuccess)
        val sessionId = createResult.getOrThrow()
        assertTrue(sessionId.isNotEmpty())

        val sessions = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeFocusSessions().first { list -> list.any { it.id == sessionId } }
        }
        assertTrue(sessions.any { it.id == sessionId })
    }

    @Test
    fun getFocusSessionById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = StudyGuardRepository(firestore, auth)
        val sessionId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createFocusSession("FOCUS", 30, "Organic Chemistry", true).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = StudyGuardRepository(firestore, auth)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.getFocusSessionById(sessionId).getOrThrow()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED for cross-user access")
        } catch (t: Throwable) {
            val fsEx = extractFirestoreException(t)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    @Test
    fun observeFocusSessions_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = StudyGuardRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeFocusSessions().first()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED for unauthenticated user")
        } catch (t: Throwable) {
            val fsEx = extractFirestoreException(t)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    @Test
    fun saveAppRuleConfig_andRecordAssessment_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = StudyGuardRepository(firestore, auth)
        val dynamicConfigId = "br_${UUID.randomUUID().toString().replace("-", "").take(10)}"

        val configResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveAppRuleConfig(
                configId = dynamicConfigId,
                packageName = "com.android.chrome",
                appName = "Google Chrome",
                category = "browser",
                isAllowed = true,
                dailyLimitMinutes = 120
            )
        }
        assertTrue(configResult.isSuccess)

        val examResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordAssessment(
                scorePercent = 80,
                correctCount = 4,
                totalQuestions = 5,
                passed = true
            )
        }
        assertTrue(examResult.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_study@test.com"
        const val BOB_EMAIL = "bob_study@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
