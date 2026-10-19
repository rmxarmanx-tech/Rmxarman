package com.example.model

data class AuditedPackage(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val installerPackage: String?,
    val isSideloaded: Boolean,
    val isProhibited: Boolean,
    val category: String, // "Whitelisted Browser", "Whitelisted Game", "Whitelisted Social", "Sideloaded APK", "Prohibited Distraction"
    val detectedAt: Long = System.currentTimeMillis()
)

data class ForegroundAppInfo(
    val packageName: String,
    val appName: String,
    val lastResumedTimestamp: Long,
    val isProhibited: Boolean,
    val isWhitelisted: Boolean,
    val statusText: String
)

data class ProhibitedAppEvent(
    val packageName: String,
    val appName: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val triggeredPenaltyMinutes: Int = 30
)
