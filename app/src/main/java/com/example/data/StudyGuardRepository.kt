package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

@IgnoreExtraProperties
data class FocusSessionDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mode: String = "FOCUS",
    val durationMinutes: Long = 25L,
    val subject: String = "General Study",
    val completed: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppRuleConfigDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val packageName: String = "",
    val appName: String = "",
    val category: String = "study",
    val isAllowed: Boolean = true,
    val dailyLimitMinutes: Long = 30L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudyAssessmentDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val scorePercent: Long = 0L,
    val correctCount: Long = 0L,
    val totalQuestions: Long = 5L,
    val passed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class StudyGuardRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // CRITICAL: Always initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun unwrapFirestoreException(e: Throwable): FirebaseFirestoreException? {
        var curr: Throwable? = e
        while (curr != null) {
            if (curr is FirebaseFirestoreException) return curr
            curr = curr.cause
        }
        return null
    }

    fun observeFocusSessions(): Flow<List<FocusSessionDoc>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "focus_sessions"
        try {
            emitAll(
                db.collection(path)
                    .whereEqualTo("userId", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            FocusSessionDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    }
                    .catch { error ->
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
            )
        } catch (e: CancellationException) {
            val fsEx = unwrapFirestoreException(e)
            if (fsEx != null) {
                handleFirestoreError(fsEx, OperationType.LIST, path)
                throw fsEx
            }
            throw e
        }
    }

    suspend fun createFocusSession(
        mode: String,
        durationMinutes: Int,
        subject: String,
        completed: Boolean = true
    ): Result<String> {
        val uid = requireUserId()
        val sessionId = "sess_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("focus_sessions").document(sessionId)
        val payload = mapOf(
            "userId" to uid,
            "mode" to mode,
            "durationMinutes" to durationMinutes.toLong(),
            "subject" to subject.take(100).ifBlank { "Deep Focus" },
            "completed" to completed,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(sessionId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getFocusSessionById(sessionId: String): Result<FocusSessionDoc?> {
        val docRef = db.collection("focus_sessions").document(sessionId)
        return try {
            val snap = docRef.get().await()
            Result.success(
                snap.toObject(
                    FocusSessionDoc::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeAppConfigs(): Flow<List<AppRuleConfigDoc>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "app_configs"
        try {
            emitAll(
                db.collection(path)
                    .whereEqualTo("userId", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            AppRuleConfigDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    }
                    .catch { error ->
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
            )
        } catch (e: CancellationException) {
            val fsEx = unwrapFirestoreException(e)
            if (fsEx != null) {
                handleFirestoreError(fsEx, OperationType.LIST, path)
                throw fsEx
            }
            throw e
        }
    }

    suspend fun saveAppRuleConfig(
        configId: String,
        packageName: String,
        appName: String,
        category: String,
        isAllowed: Boolean,
        dailyLimitMinutes: Int
    ): Result<String> {
        val uid = requireUserId()
        val safeId = configId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(100)
        val docRef = db.collection("app_configs").document("${uid}_$safeId")
        val payload = mapOf(
            "userId" to uid,
            "packageName" to packageName.take(128),
            "appName" to appName.take(100),
            "category" to category,
            "isAllowed" to isAllowed,
            "dailyLimitMinutes" to dailyLimitMinutes.toLong(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeAssessments(): Flow<List<StudyAssessmentDoc>> = flow {
        val uid = auth.currentUser?.uid ?: ""
        val path = "study_assessments"
        try {
            emitAll(
                db.collection(path)
                    .whereEqualTo("userId", uid)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(
                            StudyAssessmentDoc::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    }
                    .catch { error ->
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        throw error
                    }
            )
        } catch (e: CancellationException) {
            val fsEx = unwrapFirestoreException(e)
            if (fsEx != null) {
                handleFirestoreError(fsEx, OperationType.LIST, path)
                throw fsEx
            }
            throw e
        }
    }

    suspend fun recordAssessment(
        scorePercent: Int,
        correctCount: Int,
        totalQuestions: Int,
        passed: Boolean
    ): Result<String> {
        val uid = requireUserId()
        val assessmentId = "exam_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("study_assessments").document(assessmentId)
        val payload = mapOf(
            "userId" to uid,
            "scorePercent" to scorePercent.toLong(),
            "correctCount" to correctCount.toLong(),
            "totalQuestions" to totalQuestions.toLong(),
            "passed" to passed,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(assessmentId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }
}
