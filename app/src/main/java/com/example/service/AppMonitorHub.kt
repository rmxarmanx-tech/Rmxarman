package com.example.service

import com.example.model.AuditedPackage
import com.example.model.ForegroundAppInfo
import com.example.model.ProhibitedAppEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object AppMonitorHub {

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _currentForegroundApp = MutableStateFlow<ForegroundAppInfo?>(null)
    val currentForegroundApp: StateFlow<ForegroundAppInfo?> = _currentForegroundApp.asStateFlow()

    private val _auditedPackages = MutableStateFlow<List<AuditedPackage>>(emptyList())
    val auditedPackages: StateFlow<List<AuditedPackage>> = _auditedPackages.asStateFlow()

    private val _prohibitedEvents = MutableSharedFlow<ProhibitedAppEvent>(extraBufferCapacity = 10)
    val prohibitedEvents: SharedFlow<ProhibitedAppEvent> = _prohibitedEvents.asSharedFlow()

    // Officially permitted whitelist packages
    val whitelistedPackages = setOf(
        "com.aistudio.studyguard.strict",
        "com.android.chrome",
        "com.brave.browser",
        "com.chess.tactics",
        "com.whatsapp",
        "org.telegram.messenger",
        "com.google.android.apps.nexuslauncher",
        "com.google.android.inputmethod.latin",
        "com.android.systemui",
        "com.android.settings"
    )

    // Common prohibited / distraction package signatures
    val knownDistractionPackages = setOf(
        "com.zhiliaoapp.musically", // TikTok
        "com.ss.android.ugc.trill", // TikTok
        "com.instagram.android",    // Instagram (Reels)
        "com.snapchat.android",     // Snapchat
        "com.netflix.mediaclient",  // Netflix
        "com.dts.freefireth",       // Free Fire
        "com.dts.freefiremax",      // Free Fire Max
        "com.tencent.ig",           // PUBG
        "com.pubg.imobile",         // BGMI
        "com.facebook.katana",      // Facebook
        "com.tinder"                // Dating
    )

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun updateForegroundApp(info: ForegroundAppInfo) {
        _currentForegroundApp.value = info
    }

    fun updateAuditedPackages(packages: List<AuditedPackage>) {
        _auditedPackages.value = packages
    }

    fun notifyProhibitedAppDetected(event: ProhibitedAppEvent) {
        _prohibitedEvents.tryEmit(event)
    }
}
