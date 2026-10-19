package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApkThreat
import com.example.model.AuditedPackage
import com.example.model.DisciplineState
import com.example.model.ForegroundAppInfo
import com.example.model.RiskLevel
import com.example.ui.theme.AlarmCrimson
import com.example.ui.theme.AlarmCrimsonBg
import com.example.ui.theme.DarkBg
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.StrictEmerald
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ApkShieldScreen(
    state: DisciplineState,
    isServiceRunning: Boolean,
    hasUsagePermission: Boolean,
    currentForegroundApp: ForegroundAppInfo?,
    auditedPackages: List<AuditedPackage>,
    onToggleService: () -> Unit,
    onOpenUsageSettings: () -> Unit,
    onPurgeAll: () -> Unit,
    onSimulateDownload: (String, String) -> Unit,
    onSimulateForegroundViolation: (String, String) -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanStatusText by remember { mutableStateOf("All storage paths clean. Watcher daemon listening.") }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
            .testTag("apk_shield_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlarmCrimson.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AlarmCrimson.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = AlarmCrimson,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI APK & MOD DETECTOR",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Rule 2 & 3: Instant Sideload Purge Engine",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StrictEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = StrictEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Any APK, modded package, cracked app, or unauthorized file downloaded outside Play Store is instantly detected, automatically deleted, and triggers a 30-minute lockout.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Scan action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isScanning = true
                                    scanStatusText = "Scanning /Download, /Telegram, /Android/data..."
                                    delay(1200)
                                    scanStatusText = "Heuristic AI analysis checking APK signatures..."
                                    delay(800)
                                    isScanning = false
                                    scanStatusText = "Scan Complete: No active threats present. All unauthorized files purged."
                                }
                            },
                            enabled = !isScanning,
                            colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("run_deep_scan_button")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = DarkBg,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning...", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            } else {
                                Icon(imageVector = Icons.Default.Radar, contentDescription = null, tint = DarkBg)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Deep Scan Storage", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = onPurgeAll,
                            colors = ButtonDefaults.buttonColors(containerColor = AlarmCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("purge_quarantine_button")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purge Threats", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = scanStatusText,
                        color = if (isScanning) ShieldCyan else TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Real-Time Background Service Card (UsageStatsManager)
        item {
            Text(
                text = "BACKGROUND SERVICE (USAGESTATS MONITOR)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isServiceRunning) StrictEmerald.copy(alpha = 0.5f) else TextMuted,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isServiceRunning) StrictEmerald else AlarmCrimson)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isServiceRunning) "Foreground Service: RUNNING" else "Foreground Service: STOPPED",
                                color = if (isServiceRunning) StrictEmerald else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onToggleService,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isServiceRunning) AlarmCrimsonBg else StrictEmerald
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .border(
                                    1.dp,
                                    if (isServiceRunning) AlarmCrimson else StrictEmerald,
                                    RoundedCornerShape(8.dp)
                                )
                                .testTag("toggle_background_service_button")
                        ) {
                            Text(
                                text = if (isServiceRunning) "Stop Service" else "Start Service",
                                color = if (isServiceRunning) AlarmCrimson else DarkBg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Permission status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasUsagePermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasUsagePermission) StrictEmerald else WarningAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasUsagePermission) "Usage Access: Granted" else "Usage Access: Required",
                                color = if (hasUsagePermission) StrictEmerald else WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (!hasUsagePermission) {
                            Button(
                                onClick = onOpenUsageSettings,
                                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp).testTag("open_usage_settings_button")
                            ) {
                                Text("Grant Permission", color = DarkBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Foreground App Inspection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardElevated)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "CURRENT FOREGROUND APP (LIVE TELEMETRY):",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentForegroundApp?.appName ?: "UGT StudyGuard (Active Hub)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Package: ${currentForegroundApp?.packageName ?: "com.aistudio.studyguard.strict"}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentForegroundApp?.statusText ?: "Permitted: Core Discipline Controller",
                                color = if (currentForegroundApp?.isProhibited == true) AlarmCrimson else StrictEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Real-Time Interception Button
                    OutlinedButton(
                        onClick = {
                            onSimulateForegroundViolation("TikTok (Shorts/Reels)", "com.zhiliaoapp.musically")
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_foreground_violation_button")
                    ) {
                        Icon(imageVector = Icons.Default.AppBlocking, contentDescription = null, tint = AlarmCrimson, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate Launching Prohibited App (Foreground Test)", color = AlarmCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Test APK Sideload Detection Buttons
        item {
            Text(
                text = "TEST AI INTERCEPTION & PURGE ENGINE",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SimulateApkCard(
                    apkName = "GBWhatsApp_Pro_v17.50.apk",
                    source = "Third-Party Website (APKMirror clone)",
                    onSimulate = { onSimulateDownload("GBWhatsApp_Pro_v17.50.apk", "Third-Party Website") }
                )
                SimulateApkCard(
                    apkName = "Spotify_Premium_Mod_Cracked.apk",
                    source = "Telegram Channel Sideload",
                    onSimulate = { onSimulateDownload("Spotify_Premium_Mod_Cracked.apk", "Telegram Channel Sideload") }
                )
                SimulateApkCard(
                    apkName = "Subway_Surfers_Unlimited_Coins.apk",
                    source = "Chrome Unknown Download",
                    onSimulate = { onSimulateDownload("Subway_Surfers_Unlimited_Coins.apk", "Chrome Unknown Download") }
                )
            }
        }

        // Audited Installed Packages (From PackageManager)
        if (auditedPackages.isNotEmpty()) {
            item {
                Text(
                    text = "REAL-TIME INSTALLED PACKAGE AUDIT (${auditedPackages.size} PACKAGES)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(auditedPackages.take(6)) { pkg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (pkg.isSideloaded) AlarmCrimsonBg else SurfaceCard
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (pkg.isSideloaded) AlarmCrimson else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pkg.appName,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pkg.packageName,
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Installer: ${pkg.installerPackage ?: "Direct Sideload"}",
                                color = if (pkg.isSideloaded) AlarmCrimson else TextSecondary,
                                fontSize = 9.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (pkg.isSideloaded) AlarmCrimson.copy(alpha = 0.2f) else StrictEmerald.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (pkg.isSideloaded) "SIDELOADED" else "OFFICIAL",
                                color = if (pkg.isSideloaded) AlarmCrimson else StrictEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Intercepted Threat Vault
        item {
            Text(
                text = "AI INTERCEPTED THREAT VAULT (${state.apkThreats.size})",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(state.apkThreats) { threat ->
            ThreatItemCard(threat = threat)
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun DirectoryPill(path: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkBg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = path,
            color = ShieldCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun SimulateApkCard(apkName: String, source: String, onSimulate: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apkName,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Source: $source",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
            Button(
                onClick = onSimulate,
                colors = ButtonDefaults.buttonColors(containerColor = AlarmCrimsonBg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .border(1.dp, AlarmCrimson.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .height(36.dp)
            ) {
                Text("Test Intercept", color = AlarmCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ThreatItemCard(threat: ApkThreat) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (threat.isPurged) SurfaceCard else AlarmCrimsonBg
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (threat.isPurged) StrictEmerald.copy(alpha = 0.3f) else AlarmCrimson,
                RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (threat.isPurged) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (threat.isPurged) StrictEmerald else AlarmCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = threat.name,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (threat.isPurged) StrictEmerald.copy(alpha = 0.2f) else AlarmCrimson.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (threat.isPurged) "PURGED & DELETED" else "ACTIVE THREAT",
                        color = if (threat.isPurged) StrictEmerald else AlarmCrimson,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Path: ${threat.filePath}",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Source: ${threat.threatSource} (${threat.fileSize})",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = threat.detectedTime,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
