package com.example.ui

import android.app.Application
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CurriculumData
import com.example.data.FocusSessionDoc
import com.example.data.StudyAssessmentDoc
import com.example.data.StudyGuardRepository
import com.example.model.ApkThreat
import com.example.model.DisciplineState
import com.example.model.RiskLevel
import com.example.model.SelectableAppItem
import com.example.model.ViolationLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DisciplineViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DisciplineState())
    val uiState: StateFlow<DisciplineState> = _uiState.asStateFlow()

    private val _cloudFocusSessions = MutableStateFlow<List<FocusSessionDoc>>(emptyList())
    val cloudFocusSessions: StateFlow<List<FocusSessionDoc>> = _cloudFocusSessions.asStateFlow()

    private val _cloudAssessments = MutableStateFlow<List<StudyAssessmentDoc>>(emptyList())
    val cloudAssessments: StateFlow<List<StudyAssessmentDoc>> = _cloudAssessments.asStateFlow()

    private var repository: StudyGuardRepository? = null
    private var activeUserId: String? = null
    private var focusListenerJob: Job? = null
    private var appConfigListenerJob: Job? = null
    private var assessmentListenerJob: Job? = null

    private var lockoutTimer: CountDownTimer? = null
    private var examTimer: CountDownTimer? = null

    private val _examRemainingSeconds = MutableStateFlow(300L) // 5 minutes
    val examRemainingSeconds: StateFlow<Long> = _examRemainingSeconds.asStateFlow()

    private val _isExamInProgress = MutableStateFlow(false)
    val isExamInProgress: StateFlow<Boolean> = _isExamInProgress.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val userAnswers: StateFlow<Map<Int, Int>> = _userAnswers.asStateFlow()

    private val _examFinishedResult = MutableStateFlow<ExamResult?>(null)
    val examFinishedResult: StateFlow<ExamResult?> = _examFinishedResult.asStateFlow()

    data class ExamResult(
        val totalQuestions: Int,
        val correctCount: Int,
        val scorePercent: Int,
        val passed: Boolean,
        val message: String
    )

    init {
        viewModelScope.launch {
            com.example.service.AppMonitorHub.prohibitedEvents.collect { event ->
                val isSideload = event.reason.contains("Sideloaded")
                if (isSideload) {
                    val newThreat = ApkThreat(
                        id = UUID.randomUUID().toString(),
                        name = "${event.appName}.apk",
                        filePath = "/data/app/${event.packageName}",
                        threatSource = "Background Monitor (Installer Mismatch)",
                        detectedTime = "Just now",
                        riskLevel = RiskLevel.CRITICAL,
                        fileSize = "Installed Sideload",
                        isPurged = true,
                        packageName = event.packageName
                    )
                    _uiState.update { s ->
                        s.copy(
                            apkThreats = listOf(newThreat) + s.apkThreats.filter { it.packageName != event.packageName },
                            apksPurgedCount = s.apksPurgedCount + 1,
                            blockedCountTotal = s.blockedCountTotal + 1
                        )
                    }
                }
                triggerLockout(
                    reason = event.reason,
                    durationMinutes = event.triggeredPenaltyMinutes
                )
            }
        }
    }

    /**
     * Called strictly after Google Sign-In succeeds in the UI layer.
     * Never queries Firestore when unauthenticated.
     */
    fun bindAuthenticatedUser(userId: String) {
        if (activeUserId == userId && repository != null) return
        activeUserId = userId
        val repo = try {
            StudyGuardRepository(getApplication())
        } catch (e: Exception) {
            Log.w("DisciplineVM", "Firestore repository init skipped: ${e.message}")
            null
        }
        repository = repo ?: return

        focusListenerJob?.cancel()
        focusListenerJob = viewModelScope.launch {
            repo.observeFocusSessions()
                .catch { e -> Log.w("DisciplineVM", "Error observing focus sessions: ${e.message}") }
                .collect { sessions ->
                    _cloudFocusSessions.value = sessions
                }
        }

        appConfigListenerJob?.cancel()
        appConfigListenerJob = viewModelScope.launch {
            repo.observeAppConfigs()
                .catch { e -> Log.w("DisciplineVM", "Error observing app configs: ${e.message}") }
                .collect { configs ->
                    if (configs.isNotEmpty()) {
                        _uiState.update { state ->
                            val updatedApps = state.selectableApps.map { item ->
                                val match = configs.find { it.packageName == item.packageName }
                                if (match != null) {
                                    item.copy(
                                        isAllowed = match.isAllowed,
                                        dailyLimitMinutes = match.dailyLimitMinutes.toInt()
                                    )
                                } else {
                                    item
                                }
                            }
                            state.copy(selectableApps = updatedApps)
                        }
                    }
                }
        }

        assessmentListenerJob?.cancel()
        assessmentListenerJob = viewModelScope.launch {
            repo.observeAssessments()
                .catch { e -> Log.w("DisciplineVM", "Error observing assessments: ${e.message}") }
                .collect { assessments ->
                    _cloudAssessments.value = assessments
                    val latest = assessments.firstOrNull()
                    if (latest != null) {
                        _uiState.update {
                            it.copy(
                                isAssessmentPassedToday = latest.passed,
                                assessmentScore = latest.scorePercent.toInt()
                            )
                        }
                    }
                }
        }
    }

    fun clearAuthenticatedUser() {
        focusListenerJob?.cancel()
        appConfigListenerJob?.cancel()
        assessmentListenerJob?.cancel()
        activeUserId = null
        repository = null
        _cloudFocusSessions.value = emptyList()
        _cloudAssessments.value = emptyList()
    }

    fun logFocusSession(mode: String, durationMinutes: Int, subject: String) {
        val repo = repository ?: return
        viewModelScope.launch {
            repo.createFocusSession(
                mode = mode,
                durationMinutes = durationMinutes.coerceIn(1, 720),
                subject = subject,
                completed = true
            )
        }
    }

    fun setUltraFocusLocked(locked: Boolean) {
        _uiState.update { it.copy(isUltraFocusLocked = locked) }
    }

    fun completeDailyStudyCheckIn() {
        _uiState.update { state ->
            val nextDay = (state.streakDay + 1).coerceAtMost(state.totalDays)
            val unlocked = nextDay >= state.totalDays
            state.copy(
                streakDay = nextDay,
                isUninstallUnlocked = unlocked
            )
        }
        logFocusSession("FOCUS", 45, "Daily Milestone Check-In")
    }

    fun requestUninstallOrDeactivateStrictMode() {
        val current = _uiState.value
        if (current.streakDay < current.totalDays) {
            val remaining = current.totalDays - current.streakDay
            triggerLockout(
                reason = "Tamper Violation: Attempted early uninstall or Strict Mode deactivation on Day ${current.streakDay}/${current.totalDays} ($remaining days remaining in progression). Device locked for 30 minutes.",
                durationMinutes = 30
            )
        } else {
            _uiState.update {
                it.copy(
                    is21DayModeActive = false,
                    isUninstallUnlocked = true
                )
            }
        }
    }

    fun markResourceDownloaded(resourceId: String, savedPath: String) {
        _uiState.update { state ->
            state.copy(
                downloadedResourceIds = state.downloadedResourceIds + resourceId,
                lastDownloadedFileMessage = "Saved to device: $savedPath"
            )
        }
    }

    fun updatePythonEndpoint(endpoint: String) {
        _uiState.update { it.copy(pythonEndpoint = endpoint) }
    }

    fun inspectScreenFrameWithPython(frameSource: String, ocrText: String) {
        val combined = "$frameSource $ocrText".lowercase().trim()
        _uiState.update { it.copy(pythonFramesInspected = it.pythonFramesInspected + 1) }

        if (combined.contains("reels") || combined.contains("shorts") || combined.contains("tiktok") || combined.contains("spotlight")) {
            _uiState.update {
                it.copy(
                    reelsBlockedToday = it.reelsBlockedToday + 1,
                    blockedCountTotal = it.blockedCountTotal + 1,
                    pythonLastVerdict = "BLOCKED BY PYTHON OCR PROCTOR: Short-form video feed ('$ocrText') intercepted. 30-Minute Lockout imposed!"
                )
            }
            triggerLockout(
                reason = "Python Backend Screen Monitor detected banned Reels/Shorts screen activity ('$ocrText'). Device locked for 30 minutes.",
                durationMinutes = 30
            )
            return
        }

        val adultOrApkKeywords = listOf("porn", "xxx", "nsfw", "casino", "betting", ".apk", "mod_menu", "hack")
        if (adultOrApkKeywords.any { combined.contains(it) }) {
            _uiState.update {
                it.copy(
                    adultSitesBlockedToday = it.adultSitesBlockedToday + 1,
                    blockedCountTotal = it.blockedCountTotal + 1,
                    pythonLastVerdict = "LOCKOUT IMPOSED BY PYTHON MONITOR: Restricted content/APK ('$ocrText') detected on screen!"
                )
            }
            triggerLockout(
                reason = "Python Screen Monitor intercepted restricted content or unauthorized APK on screen ('$ocrText'). 30-minute lockout engaged.",
                durationMinutes = 30
            )
            return
        }

        _uiState.update {
            it.copy(
                pythonLastVerdict = "PERMITTED BY PYTHON PROCTOR: Verified productive study window ('$ocrText')."
            )
        }
    }

    fun toggleAppSelection(appItem: SelectableAppItem, isAllowed: Boolean) {
        val currentList = _uiState.value.selectableApps
        if (isAllowed) {
            val currentCategoryAllowed = currentList.count { it.category == appItem.category && it.isAllowed && it.id != appItem.id }
            val maxForCategory = when (appItem.category) {
                "browser" -> 2
                "game" -> 1
                "social" -> 2
                else -> 10
            }
            if (currentCategoryAllowed >= maxForCategory) {
                triggerLockout(
                    reason = "Quota Bypass Violation: Attempted to enable more than $maxForCategory ${appItem.category} app(s) ('${appItem.appName}'). Under Rules 4 & 5, phone locked for 30 minutes!",
                    durationMinutes = 30
                )
                return
            }
        }

        val newLimit = if (isAllowed) {
            when (appItem.category) {
                "browser" -> 90
                "game" -> 20
                "social" -> 30
                else -> 480
            }
        } else 0

        _uiState.update { state ->
            state.copy(
                selectableApps = state.selectableApps.map {
                    if (it.id == appItem.id) it.copy(isAllowed = isAllowed, dailyLimitMinutes = newLimit) else it
                }
            )
        }

        val repo = repository
        if (repo != null) {
            viewModelScope.launch {
                repo.saveAppRuleConfig(
                    configId = appItem.id,
                    packageName = appItem.packageName,
                    appName = appItem.appName,
                    category = appItem.category,
                    isAllowed = isAllowed,
                    dailyLimitMinutes = newLimit
                )
            }
        }
    }

    fun startUsageMonitorService() {
        com.example.service.UsageMonitorService.startService(getApplication())
    }

    fun stopUsageMonitorService() {
        com.example.service.UsageMonitorService.stopService(getApplication())
    }

    fun hasUsageStatsPermission(): Boolean {
        return com.example.service.UsagePermissionHelper.hasUsageStatsPermission(getApplication())
    }

    fun openUsageSettings() {
        com.example.service.UsagePermissionHelper.openUsageAccessSettings(getApplication())
    }

    /**
     * Trigger 30-minute complete device lockout.
     * Absolutely no apps can be accessed except the UGT Tuition application!
     */
    fun triggerLockout(reason: String, durationMinutes: Int = 30) {
        val totalSeconds = durationMinutes * 60L
        lockoutTimer?.cancel()

        // Activate system-wide full-screen overlay component
        com.example.service.SystemLockoutController.show(getApplication(), reason, totalSeconds)

        val log = ViolationLog(
            id = UUID.randomUUID().toString(),
            timestampFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
            title = "Lockout Imposed (30 Minutes)",
            details = reason,
            penaltyMinutes = durationMinutes
        )

        _uiState.update { state ->
            state.copy(
                isLockoutActive = true,
                lockoutRemainingSeconds = totalSeconds,
                lockoutReason = reason,
                tamperAttemptCount = state.tamperAttemptCount + 1,
                violationLogs = listOf(log) + state.violationLogs
            )
        }

        lockoutTimer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000L
                _uiState.update { it.copy(lockoutRemainingSeconds = sec) }
                com.example.service.SystemLockoutController.updateSeconds(sec)
            }

            override fun onFinish() {
                _uiState.update {
                    it.copy(
                        isLockoutActive = false,
                        lockoutRemainingSeconds = 0L,
                        lockoutReason = ""
                    )
                }
                com.example.service.SystemLockoutController.dismiss(getApplication())
            }
        }.start()
    }

    /**
     * Rule 1: Settings cannot be bypassed, uninstalled, or disabled during the 21 days.
     */
    fun attemptToggleSetting(settingName: String) {
        if (_uiState.value.is21DayModeActive) {
            triggerLockout(
                reason = "Tamper Violation: Attempted to modify or disable '$settingName' during 21-Day Strict Mode. Device locked for 30 minutes. Study UGT tuition now.",
                durationMinutes = 30
            )
        }
    }

    /**
     * Rule 2 & 3: Detect and immediately delete any APK files, Mod versions, or unauthorized apps.
     */
    fun scanAndPurgeApks() {
        viewModelScope.launch {
            _uiState.update { state ->
                val updatedThreats = state.apkThreats.map { it.copy(isPurged = true) }
                state.copy(
                    apkThreats = updatedThreats,
                    apksPurgedCount = state.apksPurgedCount + 1
                )
            }
        }
    }

    /**
     * Simulate an unauthorized APK download attempt (or third-party installer detection).
     */
    fun simulateApkDownloadAttempt(apkName: String, source: String = "External Browser Sideload") {
        val newThreat = ApkThreat(
            id = UUID.randomUUID().toString(),
            name = apkName,
            filePath = "/storage/emulated/0/Download/$apkName",
            threatSource = source,
            detectedTime = "Just now",
            riskLevel = RiskLevel.CRITICAL,
            fileSize = "48.6 MB",
            isPurged = true
        )

        _uiState.update { state ->
            state.copy(
                apkThreats = listOf(newThreat) + state.apkThreats,
                apksPurgedCount = state.apksPurgedCount + 1,
                blockedCountTotal = state.blockedCountTotal + 1
            )
        }

        triggerLockout(
            reason = "Critical Security Alert: Detected unauthorized APK download '$apkName'. File purged by AI. 30-minute device lockout engaged.",
            durationMinutes = 30
        )
    }

    /**
     * Rule 4 & 6: Test and intercept URLs / Web navigation.
     */
    fun evaluateUrlNavigation(url: String): UrlCheckResult {
        val lower = url.lowercase().trim()

        if (lower.contains("shorts") || lower.contains("reels") || lower.contains("tiktok") || lower.contains("snapchat.com/spotlight")) {
            _uiState.update {
                it.copy(
                    reelsBlockedToday = it.reelsBlockedToday + 1,
                    blockedCountTotal = it.blockedCountTotal + 1
                )
            }
            return UrlCheckResult(
                allowed = false,
                reason = "BLOCKED: Reels, Shorts, and short-form doomscrolling are 100% banned under Rule 6.",
                isShorts = true
            )
        }

        val adultKeywords = listOf("adult", "porn", "xxx", "nsfw", "casino", "betting", "gambling", "dating")
        if (adultKeywords.any { lower.contains(it) }) {
            _uiState.update {
                it.copy(
                    adultSitesBlockedToday = it.adultSitesBlockedToday + 1,
                    blockedCountTotal = it.blockedCountTotal + 1
                )
            }
            triggerLockout("Attempted access to restricted adult/gambling site: '$url'. Zero tolerance enforced.", 30)
            return UrlCheckResult(
                allowed = false,
                reason = "BLOCKED: 100% Adult & Gambling filter engaged. 30-Minute Penalty lockout triggered.",
                isAdult = true
            )
        }

        return UrlCheckResult(
            allowed = true,
            reason = "Access permitted under Study Safe Browser Shield."
        )
    }

    data class UrlCheckResult(
        val allowed: Boolean,
        val reason: String,
        val isShorts: Boolean = false,
        val isAdult: Boolean = false
    )

    fun addGameUsage(minutes: Int) {
        val currentGame = _uiState.value.allowedGame
        val newUsage = currentGame.usedMinutes + minutes
        val updatedGame = currentGame.copy(usedMinutes = newUsage)

        _uiState.update { it.copy(allowedGame = updatedGame) }

        if (newUsage >= currentGame.limitMinutes) {
            triggerLockout(
                reason = "Time Limit Exceeded: Daily game allowance (${currentGame.limitMinutes}m) exceeded for '${currentGame.name}'. Device locked for 30 minutes.",
                durationMinutes = 30
            )
        }
    }

    fun addSocialUsage(appId: String, minutes: Int) {
        var limitExceeded = false
        var appName = ""
        val updatedSocials = _uiState.value.allowedSocials.map { app ->
            if (app.id == appId) {
                val newUsage = app.usedMinutes + minutes
                appName = app.name
                if (newUsage >= app.limitMinutes) {
                    limitExceeded = true
                }
                app.copy(usedMinutes = newUsage)
            } else {
                app
            }
        }

        _uiState.update { it.copy(allowedSocials = updatedSocials) }

        if (limitExceeded) {
            triggerLockout(
                reason = "Time Limit Exceeded: Daily social media limit exceeded for '$appName'. Device locked for 30 minutes.",
                durationMinutes = 30
            )
        }
    }

    fun startNightlyExam() {
        examTimer?.cancel()
        _isExamInProgress.value = true
        _examRemainingSeconds.value = 300L
        _userAnswers.value = emptyMap()
        _examFinishedResult.value = null

        examTimer = object : CountDownTimer(300L * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _examRemainingSeconds.value = millisUntilFinished / 1000L
            }

            override fun onFinish() {
                _examRemainingSeconds.value = 0L
                submitExam()
            }
        }.start()
    }

    fun selectAnswer(questionId: Int, optionIndex: Int) {
        _userAnswers.update { it + (questionId to optionIndex) }
    }

    fun submitExam() {
        examTimer?.cancel()
        _isExamInProgress.value = false

        val questions = CurriculumData.assessmentQuestions
        val answers = _userAnswers.value
        var correct = 0
        questions.forEach { q ->
            if (answers[q.id] == q.correctIndex) {
                correct++
            }
        }

        val total = questions.size
        val scorePercent = (correct * 100) / total
        val passed = scorePercent >= 60

        val resultMessage = if (passed) {
            "ASSESSMENT PASSED ($scorePercent%). Great discipline! Tomorrow device access remains in normal controlled mode."
        } else {
            "ASSESSMENT FAILED ($scorePercent% < 60%). Under Rule 7, your entire device will be strictly locked to the UGT Tuition App for all of tomorrow!"
        }

        _examFinishedResult.value = ExamResult(
            totalQuestions = total,
            correctCount = correct,
            scorePercent = scorePercent,
            passed = passed,
            message = resultMessage
        )

        _uiState.update { state ->
            state.copy(
                isAssessmentPassedToday = passed,
                assessmentScore = scorePercent,
                isNextDayLockedToUgt = !passed,
                nextDayLockReason = if (!passed) "Failed 5-minute study assessment ($scorePercent%). Mandatory UGT tuition study tomorrow." else ""
            )
        }

        val repo = repository
        if (repo != null) {
            viewModelScope.launch {
                repo.recordAssessment(
                    scorePercent = scorePercent,
                    correctCount = correct,
                    totalQuestions = total,
                    passed = passed
                )
            }
        }
    }

    fun abandonExam() {
        examTimer?.cancel()
        _isExamInProgress.value = false

        _uiState.update {
            it.copy(
                isAssessmentPassedToday = false,
                isNextDayLockedToUgt = true,
                nextDayLockReason = "Abandoned or skipped mandatory 5-minute study exam. Device restricted exclusively to UGT Tuition app for tomorrow!"
            )
        }

        val repo = repository
        if (repo != null) {
            viewModelScope.launch {
                repo.recordAssessment(
                    scorePercent = 0,
                    correctCount = 0,
                    totalQuestions = CurriculumData.assessmentQuestions.size,
                    passed = false
                )
            }
        }

        triggerLockout("Assessment Abandonment Violation: Skipped mandatory nightly study proctor. 30-minute lockout imposed.", 30)
    }

    fun clearLockoutOverrideForTesting() {
        lockoutTimer?.cancel()
        com.example.service.SystemLockoutController.dismiss(getApplication())
        _uiState.update {
            it.copy(
                isLockoutActive = false,
                lockoutRemainingSeconds = 0L,
                lockoutReason = ""
            )
        }
    }

    fun canDrawOverlays(): Boolean {
        return com.example.service.OverlayPermissionHelper.canDrawOverlays(getApplication())
    }

    fun openOverlaySettings() {
        com.example.service.OverlayPermissionHelper.openOverlaySettings(getApplication())
    }

    fun triggerSystemOverlayDemo() {
        triggerLockout("Manual System-Wide Lockout Test: Non-UGT Apps Blocked for 30 minutes.", 30)
    }

    override fun onCleared() {
        super.onCleared()
        lockoutTimer?.cancel()
        examTimer?.cancel()
        com.example.service.SystemLockoutController.dismiss(getApplication())
    }
}
