package com.example.model

enum class RiskLevel {
    CRITICAL, HIGH, MEDIUM
}

data class ApkThreat(
    val id: String,
    val name: String,
    val filePath: String,
    val threatSource: String,
    val detectedTime: String,
    val riskLevel: RiskLevel,
    val fileSize: String,
    val isPurged: Boolean = false,
    val packageName: String = "unknown.mod.installer"
)

data class AllowedApp(
    val id: String,
    val name: String,
    val type: String, // "browser", "game", "social"
    val packageName: String,
    val limitMinutes: Int,
    val usedMinutes: Int
)

data class SelectableAppItem(
    val id: String,
    val appName: String,
    val packageName: String,
    val category: String, // "browser", "game", "social", "study"
    val isAllowed: Boolean,
    val dailyLimitMinutes: Int
)

data class Question(
    val id: Int,
    val subject: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class UgtModule(
    val id: String,
    val title: String,
    val duration: String,
    val summary: String,
    val keyFormulas: List<String>,
    val solvedExample: String
)

data class UgtSubject(
    val id: String,
    val title: String,
    val instructor: String,
    val chapterCount: Int,
    val progressPercent: Int,
    val modules: List<UgtModule>
)

data class ViolationLog(
    val id: String,
    val timestampFormatted: String,
    val title: String,
    val details: String,
    val penaltyMinutes: Int = 30
)

data class DisciplineState(
    val is21DayModeActive: Boolean = true,
    val streakDay: Int = 7,
    val totalDays: Int = 21,
    val startDateMillis: Long = System.currentTimeMillis() - (6L * 24 * 60 * 60 * 1000),
    val isUninstallUnlocked: Boolean = false,
    val downloadedResourceIds: Set<String> = emptySet(),
    val lastDownloadedFileMessage: String = "",
    val isLockoutActive: Boolean = false,
    val lockoutRemainingSeconds: Long = 0L,
    val lockoutReason: String = "",
    val isNextDayLockedToUgt: Boolean = false,
    val nextDayLockReason: String = "",
    val isUltraFocusLocked: Boolean = false,
    val pythonEndpoint: String = "http://10.0.2.2:8000/api/v1/inspect-screen",
    val pythonLastVerdict: String = "Screen Monitor Active • Protecting against short-form feeds & unauthorized installers",
    val pythonFramesInspected: Int = 148,
    val allowedBrowsers: List<AllowedApp> = listOf(
        AllowedApp("br1", "Google Chrome (Study Safe)", "browser", "com.android.chrome", 120, 42),
        AllowedApp("br2", "Brave Browser (Shielded)", "browser", "com.brave.browser", 90, 25)
    ),
    val allowedGame: AllowedApp = AllowedApp(
        id = "gm1",
        name = "Chess: Tactical Grandmaster",
        type = "game",
        packageName = "com.chess.tactics",
        limitMinutes = 20,
        usedMinutes = 14
    ),
    val allowedSocials: List<AllowedApp> = listOf(
        AllowedApp("sc1", "WhatsApp (Study & Batch Group)", "social", "com.whatsapp", 30, 18),
        AllowedApp("sc2", "Telegram (UGT Class Materials)", "social", "org.telegram.messenger", 30, 11)
    ),
    val selectableApps: List<SelectableAppItem> = listOf(
        SelectableAppItem("br1", "Google Chrome", "com.android.chrome", "browser", true, 120),
        SelectableAppItem("br2", "Brave Browser", "com.brave.browser", "browser", true, 90),
        SelectableAppItem("br3", "Mozilla Firefox", "org.mozilla.firefox", "browser", false, 0),
        SelectableAppItem("gm1", "Chess: Tactical Grandmaster", "com.chess.tactics", "game", true, 20),
        SelectableAppItem("gm2", "FreeFire Max", "com.dts.freefiremax", "game", false, 0),
        SelectableAppItem("gm3", "Battlegrounds Mobile", "com.pubg.imobile", "game", false, 0),
        SelectableAppItem("sc1", "WhatsApp (Batch Group)", "com.whatsapp", "social", true, 30),
        SelectableAppItem("sc2", "Telegram (UGT Notes)", "org.telegram.messenger", "social", true, 30),
        SelectableAppItem("sc3", "Instagram", "com.instagram.android", "social", false, 0),
        SelectableAppItem("sc4", "Snapchat", "com.snapchat.android", "social", false, 0),
        SelectableAppItem("st1", "UGT Tuition Official", "com.ugt.tuition.official", "study", true, 480)
    ),
    val adultWebBlockerEnabled: Boolean = true,
    val reelsShortsBlockerEnabled: Boolean = true,
    val apkAutoDeleteEnabled: Boolean = true,
    val playStoreDistractionGuardEnabled: Boolean = true,
    val tamperAttemptCount: Int = 0,
    val blockedCountTotal: Int = 14,
    val reelsBlockedToday: Int = 9,
    val adultSitesBlockedToday: Int = 3,
    val apksPurgedCount: Int = 2,
    val isAssessmentPassedToday: Boolean = false,
    val assessmentScore: Int = 0,
    val apkThreats: List<ApkThreat> = listOf(
        ApkThreat(
            id = "th_1",
            name = "Instagram_Mod_v290_AntiBan.apk",
            filePath = "/storage/emulated/0/Download/Instagram_Mod_v290_AntiBan.apk",
            threatSource = "External Browser Sideload",
            detectedTime = "Today, 11:24 AM",
            riskLevel = RiskLevel.CRITICAL,
            fileSize = "64.2 MB",
            isPurged = true
        ),
        ApkThreat(
            id = "th_2",
            name = "FreeFire_Unlimited_Diamonds_Patch.apk",
            filePath = "/storage/emulated/0/Download/Telegram/FreeFire_Unlimited_Diamonds_Patch.apk",
            threatSource = "Unverified Sideload APK",
            detectedTime = "Yesterday, 04:15 PM",
            riskLevel = RiskLevel.CRITICAL,
            fileSize = "112.8 MB",
            isPurged = true
        )
    ),
    val violationLogs: List<ViolationLog> = listOf(
        ViolationLog(
            id = "v_1",
            timestampFormatted = "Today, 02:10 PM",
            title = "Short-Form Video Feed Blocked",
            details = "Content filter prevented access to instagram.com/reels.",
            penaltyMinutes = 0
        ),
        ViolationLog(
            id = "v_2",
            timestampFormatted = "Yesterday, 04:15 PM",
            title = "Unverified APK Removed",
            details = "Package guard removed FreeFire_Unlimited_Diamonds_Patch.apk and enforced 30-min lock.",
            penaltyMinutes = 30
        )
    )
)
