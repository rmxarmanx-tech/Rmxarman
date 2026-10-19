package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.DisciplineViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UGT StudyGuard", appName)
    }

    @Test
    fun `rule 1 tamper attempt triggers 30 min lockout`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisciplineViewModel(app)

        vm.attemptToggleSetting("21-Day Strict Controller Bypass")
        val state = vm.uiState.value

        assertTrue(state.isLockoutActive)
        assertEquals(1800L, state.lockoutRemainingSeconds) // 30 minutes in seconds
        assertTrue(state.lockoutReason.contains("Tamper Violation"))
    }

    @Test
    fun `rule 2 apk detection immediately purges and locks phone for 30 minutes`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisciplineViewModel(app)

        vm.simulateApkDownloadAttempt("ModGame_v2.apk", "Unknown Source")
        val state = vm.uiState.value

        assertTrue(state.isLockoutActive)
        assertTrue(state.apkThreats.any { it.name == "ModGame_v2.apk" && it.isPurged })
    }

    @Test
    fun `rule 6 shorts and reels urls are intercepted`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisciplineViewModel(app)

        val result1 = vm.evaluateUrlNavigation("https://youtube.com/shorts/12345")
        assertFalse(result1.allowed)
        assertTrue(result1.isShorts)

        val result2 = vm.evaluateUrlNavigation("https://instagram.com/reels/video")
        assertFalse(result2.allowed)
        assertTrue(result2.isShorts)
    }

    @Test
    fun `rule 7 exam failure restricts device to UGT tuition for tomorrow`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisciplineViewModel(app)

        vm.startNightlyExam()
        // Submit without answers => 0% score (<60%)
        vm.submitExam()
        val state = vm.uiState.value

        assertFalse(state.isAssessmentPassedToday)
        assertTrue(state.isNextDayLockedToUgt)
    }

    @Test
    fun `real-time prohibited app event triggers 30 min lockout via AppMonitorHub`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisciplineViewModel(app)

        com.example.service.AppMonitorHub.notifyProhibitedAppDetected(
            com.example.model.ProhibitedAppEvent(
                packageName = "com.zhiliaoapp.musically",
                appName = "TikTok",
                reason = "Prohibited distracting application 'TikTok' launched in foreground"
            )
        )

        // Wait brief tick for coroutine
        Thread.sleep(50)
        val state = vm.uiState.value
        assertTrue(state.isLockoutActive)
        assertTrue(state.lockoutReason.contains("TikTok"))
    }

    @Test
    fun `system lockout controller activates on penalty and clears on dismiss`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisciplineViewModel(app)

        vm.triggerLockout("Exceeded limit violation", 30)

        assertTrue(com.example.service.SystemLockoutController.isOverlayActive.value)
        assertEquals(1800L, com.example.service.SystemLockoutController.remainingSeconds.value)
        assertTrue(com.example.service.SystemLockoutController.lockoutReason.value.contains("Exceeded limit"))

        vm.clearLockoutOverrideForTesting()
        assertFalse(com.example.service.SystemLockoutController.isOverlayActive.value)
    }

    @Test
    fun `21 day uninstall progression blocks early uninstall and unlocks at day 21`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisciplineViewModel(app)

        // Attempting uninstall on Day 7 triggers 30m lockout
        vm.requestUninstallOrDeactivateStrictMode()
        assertTrue(vm.uiState.value.isLockoutActive)
        vm.clearLockoutOverrideForTesting()

        // Progress to Day 21
        repeat(14) {
            vm.completeDailyStudyCheckIn()
        }
        assertEquals(21, vm.uiState.value.streakDay)
        assertTrue(vm.uiState.value.isUninstallUnlocked)

        // Now uninstall progression succeeds without lockout
        vm.requestUninstallOrDeactivateStrictMode()
        assertFalse(vm.uiState.value.isLockoutActive)
        assertFalse(vm.uiState.value.is21DayModeActive)
    }

    @Test
    fun `in-app study resource download records saved path`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisciplineViewModel(app)

        vm.markResourceDownloaded("pkg_ugt_formula_handbook", "Downloads/UGT_StudyGuard/UGT_Complete_Formula_Handbook_2026.txt")
        assertTrue(vm.uiState.value.downloadedResourceIds.contains("pkg_ugt_formula_handbook"))
        assertTrue(vm.uiState.value.lastDownloadedFileMessage.contains("UGT_Complete_Formula_Handbook_2026.txt"))
    }
}


