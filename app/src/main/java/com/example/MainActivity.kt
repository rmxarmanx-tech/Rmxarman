package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.ui.DisciplineViewModel
import com.example.ui.components.LockoutOverlay
import com.example.ui.components.NextDayRestrictionBanner
import com.example.ui.components.StudyGuardTopBar
import com.example.ui.screens.ApkShieldScreen
import com.example.ui.screens.AppSelectionAndMonitorScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusTimersScreen
import com.example.ui.screens.NightlyExamScreen
import com.example.ui.screens.RulesAndQuotasScreen
import com.example.ui.screens.UgtTuitionScreen
import com.example.ui.screens.signOut
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.StrictEmerald
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    STATUS("Command", Icons.Filled.Shield, Icons.Outlined.Shield, "tab_status"),
    FOCUS_STUDIO("Focus", Icons.Filled.Timer, Icons.Outlined.Timer, "tab_focus_studio"),
    APPS_MONITOR("Apps & Guard", Icons.Filled.Apps, Icons.Outlined.Apps, "tab_apps_monitor"),
    EXAM("Assessment", Icons.Filled.Bolt, Icons.Outlined.Bolt, "tab_exam"),
    UGT_TUITION("UGT Tuition", Icons.Filled.School, Icons.Outlined.School, "tab_ugt_tuition")
}

class MainActivity : ComponentActivity() {

    private val viewModel: DisciplineViewModel by viewModels()
    private val requestedTabState = kotlinx.coroutines.flow.MutableStateFlow<AppTab?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getStringExtra("OPEN_TAB") == "UGT_TUITION") {
            requestedTabState.value = AppTab.UGT_TUITION
        }

        setContent {
            MyApplicationTheme {
                val requestedTab by requestedTabState.collectAsState()
                StudyGuardRootGate(
                    viewModel = viewModel,
                    requestedTab = requestedTab,
                    onResetRequestedTab = { requestedTabState.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getStringExtra("OPEN_TAB") == "UGT_TUITION") {
            requestedTabState.value = AppTab.UGT_TUITION
        }
    }
}

@Composable
fun StudyGuardRootGate(
    viewModel: DisciplineViewModel,
    requestedTab: AppTab? = null,
    onResetRequestedTab: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var currentUser by remember {
        mutableStateOf(
            try {
                Firebase.auth.currentUser
            } catch (e: Exception) {
                null
            }
        )
    }

    DisposableEffect(Unit) {
        val authInstance = try {
            Firebase.auth
        } catch (e: Exception) {
            null
        }
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        authInstance?.addAuthStateListener(listener)
        onDispose {
            authInstance?.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        LaunchedEffect(Unit) {
            viewModel.clearAuthenticatedUser()
        }
        AuthScreen(
            onAuthSuccess = {
                currentUser = try {
                    Firebase.auth.currentUser
                } catch (e: Exception) {
                    null
                }
            }
        )
    } else {
        LaunchedEffect(user.uid) {
            viewModel.bindAuthenticatedUser(user.uid)
        }
        StudyGuardMainApp(
            viewModel = viewModel,
            userEmail = user.email ?: user.displayName ?: "Authenticated Student",
            requestedTab = requestedTab,
            onResetRequestedTab = onResetRequestedTab,
            onSignOut = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        viewModel.clearAuthenticatedUser()
                        currentUser = null
                    },
                    scope = scope
                )
            }
        )
    }
}

@Composable
fun StudyGuardMainApp(
    viewModel: DisciplineViewModel,
    userEmail: String? = null,
    requestedTab: AppTab? = null,
    onResetRequestedTab: () -> Unit = {},
    onSignOut: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val cloudSessions by viewModel.cloudFocusSessions.collectAsState()
    val isExamInProgress by viewModel.isExamInProgress.collectAsState()
    val examRemainingSeconds by viewModel.examRemainingSeconds.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val examResult by viewModel.examFinishedResult.collectAsState()

    val isServiceRunning by com.example.service.AppMonitorHub.isServiceRunning.collectAsState()
    val currentForegroundApp by com.example.service.AppMonitorHub.currentForegroundApp.collectAsState()
    val auditedPackages by com.example.service.AppMonitorHub.auditedPackages.collectAsState()
    val hasUsagePermission = viewModel.hasUsageStatsPermission()
    val canDrawOverlays = viewModel.canDrawOverlays()

    var currentTab by remember { mutableStateOf(requestedTab ?: AppTab.STATUS) }
    var subScreen by remember { mutableStateOf<String?>(null) } // "APK_SHIELD" or "RULES_QUOTAS"
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(requestedTab) {
        if (requestedTab != null) {
            subScreen = null
            currentTab = requestedTab
            onResetRequestedTab()
        }
    }

    LaunchedEffect(hasUsagePermission) {
        if (hasUsagePermission && !isServiceRunning) {
            viewModel.startUsageMonitorService()
        }
    }

    BackHandler(enabled = subScreen != null || currentTab != AppTab.STATUS) {
        if (subScreen != null) {
            subScreen = null
        } else {
            currentTab = AppTab.STATUS
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DarkBg,
        topBar = {
            if (!state.isLockoutActive) {
                Column {
                    StudyGuardTopBar(
                        streakDay = state.streakDay,
                        totalDays = state.totalDays,
                        userEmail = userEmail,
                        onInfoClick = { showInfoDialog = true },
                        onSignOutClick = onSignOut
                    )
                    NextDayRestrictionBanner(
                        isRestricted = state.isNextDayLockedToUgt,
                        reason = state.nextDayLockReason
                    )
                }
            }
        },
        bottomBar = {
            if (!state.isLockoutActive) {
                NavigationBar(
                    containerColor = SurfaceDark.copy(alpha = 0.94f),
                    contentColor = ShieldCyan,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    AppTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab && subScreen == null
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                subScreen = null
                                currentTab = tab
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) StrictEmerald else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) StrictEmerald else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SurfaceCard
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLockoutActive) {
                LockoutOverlay(
                    remainingSeconds = state.lockoutRemainingSeconds,
                    lockoutReason = state.lockoutReason,
                    onOpenUgtTuition = {
                        viewModel.clearLockoutOverrideForTesting()
                        subScreen = null
                        currentTab = AppTab.UGT_TUITION
                    },
                    onAdminOverride = {
                        viewModel.clearLockoutOverrideForTesting()
                    }
                )
            } else if (subScreen == "APK_SHIELD") {
                ApkShieldScreen(
                    state = state,
                    isServiceRunning = isServiceRunning,
                    hasUsagePermission = hasUsagePermission,
                    currentForegroundApp = currentForegroundApp,
                    auditedPackages = auditedPackages,
                    onToggleService = {
                        if (isServiceRunning) {
                            viewModel.stopUsageMonitorService()
                        } else {
                            viewModel.startUsageMonitorService()
                        }
                    },
                    onOpenUsageSettings = { viewModel.openUsageSettings() },
                    onPurgeAll = { viewModel.scanAndPurgeApks() },
                    onSimulateDownload = { apk, source ->
                        viewModel.simulateApkDownloadAttempt(apk, source)
                    },
                    onSimulateForegroundViolation = { appName, pkg ->
                        com.example.service.AppMonitorHub.notifyProhibitedAppDetected(
                            com.example.model.ProhibitedAppEvent(
                                packageName = pkg,
                                appName = appName,
                                reason = "Real-time UsageStats Monitor caught prohibited distracting app '$appName' in foreground."
                            )
                        )
                    }
                )
            } else if (subScreen == "RULES_QUOTAS") {
                RulesAndQuotasScreen(
                    state = state,
                    onAttemptTamper = { setting -> viewModel.attemptToggleSetting(setting) },
                    onAddGameUsage = { mins -> viewModel.addGameUsage(mins) },
                    onAddSocialUsage = { id, mins -> viewModel.addSocialUsage(id, mins) },
                    onEvaluateUrl = { url -> viewModel.evaluateUrlNavigation(url) }
                )
            } else {
                when (currentTab) {
                    AppTab.STATUS -> DashboardScreen(
                        state = state,
                        canDrawOverlays = canDrawOverlays,
                        onOpenOverlaySettings = { viewModel.openOverlaySettings() },
                        onNavigateToUgt = { currentTab = AppTab.UGT_TUITION },
                        onNavigateToExam = { currentTab = AppTab.EXAM },
                        onSimulateApkDownload = {
                            viewModel.simulateApkDownloadAttempt("Telegram_Modded_Audio_Hack.apk", "External Sideload")
                        },
                        onSimulateTamper = {
                            viewModel.attemptToggleSetting("21-Day Strict Controller Bypass")
                        },
                        onTriggerManual30mLock = {
                            viewModel.triggerLockout(
                                reason = "Daily social media limit exceeded (30 mins quota exhausted). Device locked for 30 minutes under Rule 8.",
                                durationMinutes = 30
                            )
                        },
                        onCompleteDailyCheckIn = {
                            viewModel.completeDailyStudyCheckIn()
                        },
                        onRequestUninstallProgression = {
                            viewModel.requestUninstallOrDeactivateStrictMode()
                        },
                        onResourceDownloaded = { resId, savedPath ->
                            viewModel.markResourceDownloaded(resId, savedPath)
                        }
                    )

                    AppTab.FOCUS_STUDIO -> FocusTimersScreen(
                        cloudSessions = cloudSessions,
                        isUltraFocusLocked = state.isUltraFocusLocked,
                        onToggleUltraFocusLock = { viewModel.setUltraFocusLocked(it) },
                        onLogFocusSession = { mode, mins, subject ->
                            viewModel.logFocusSession(mode, mins, subject)
                        },
                        onEarlyUltraFocusAbortPenalty = {
                            viewModel.triggerLockout(
                                reason = "Ultra-Focus Abort Penalty: Broke Ultra-Focus session before timer completion. Phone locked for 30 minutes!",
                                durationMinutes = 30
                            )
                        }
                    )

                    AppTab.APPS_MONITOR -> AppSelectionAndMonitorScreen(
                        state = state,
                        selectableApps = state.selectableApps,
                        pythonEndpoint = state.pythonEndpoint,
                        pythonLastVerdict = state.pythonLastVerdict,
                        pythonFramesInspected = state.pythonFramesInspected,
                        onUpdatePythonEndpoint = { viewModel.updatePythonEndpoint(it) },
                        onToggleAppSelection = { item, allowed ->
                            viewModel.toggleAppSelection(item, allowed)
                        },
                        onInspectScreenFrameWithPython = { src, ocr ->
                            viewModel.inspectScreenFrameWithPython(src, ocr)
                        },
                        onNavigateToApkShield = { subScreen = "APK_SHIELD" },
                        onResourceDownloaded = { resId, savedPath ->
                            viewModel.markResourceDownloaded(resId, savedPath)
                        }
                    )

                    AppTab.EXAM -> NightlyExamScreen(
                        isExamInProgress = isExamInProgress,
                        remainingSeconds = examRemainingSeconds,
                        userAnswers = userAnswers,
                        examResult = examResult,
                        onStartExam = { viewModel.startNightlyExam() },
                        onSelectAnswer = { qId, optIndex -> viewModel.selectAnswer(qId, optIndex) },
                        onSubmitExam = { viewModel.submitExam() },
                        onAbandonExam = { viewModel.abandonExam() },
                        onOpenUgtPortal = { currentTab = AppTab.UGT_TUITION }
                    )

                    AppTab.UGT_TUITION -> UgtTuitionScreen()
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "UGT StudyGuard Protocol",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Core Discipline Rules (21 Days):",
                        color = StrictEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. 21-Day Unbreakable strict mode for all settings.\n" +
                                "2. Instant AI auto-delete for all external APKs & mods.\n" +
                                "3. Distracting Play Store app download prevention.\n" +
                                "4. 100% Adult & restricted websites blocked (Only 2 browsers allowed).\n" +
                                "5. Maximum 1 game (20m) & 2 social media apps (30m).\n" +
                                "6. All Reels, Shorts, and short-form videos blocked.\n" +
                                "7. Mandatory 5-minute study exam nightly (failure restricts tomorrow to UGT app only).\n" +
                                "8. Any violation immediately locks phone for 30 minutes (only UGT app accessible).",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = StrictEmerald)
                ) {
                    Text("Understood", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showInfoDialog = false
                        subScreen = "RULES_QUOTAS"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
                ) {
                    Text("Open Full Rules & URL Tester", color = ShieldCyan, fontSize = 11.sp)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
