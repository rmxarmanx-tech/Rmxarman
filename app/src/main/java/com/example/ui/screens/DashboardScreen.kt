package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisciplineState
import com.example.service.DownloadResult
import com.example.service.DownloadableResource
import com.example.service.StudyResourceDownloadManager
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSectionHeader
import com.example.ui.theme.AlarmCrimson
import com.example.ui.theme.DarkBg
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.StrictEmerald
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UgtGold
import com.example.ui.theme.UgtPurple
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    state: DisciplineState,
    canDrawOverlays: Boolean,
    onOpenOverlaySettings: () -> Unit,
    onNavigateToUgt: () -> Unit,
    onNavigateToExam: () -> Unit,
    onSimulateApkDownload: () -> Unit,
    onSimulateTamper: () -> Unit,
    onTriggerManual30mLock: () -> Unit,
    onCompleteDailyCheckIn: () -> Unit = {},
    onRequestUninstallProgression: () -> Unit = {},
    onResourceDownloaded: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showUninstallDialog by remember { mutableStateOf(false) }
    var activeDownloadingId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    val downloadedResults = remember { mutableStateMapOf<String, DownloadResult>() }

    val progressFraction = (state.streakDay.toFloat() / state.totalDays.toFloat()).coerceIn(0f, 1f)
    val progressPercent = ((state.streakDay * 100) / state.totalDays).coerceIn(0, 100)
    val daysRemaining = (state.totalDays - state.streakDay).coerceAtLeast(0)
    val isUninstallReady = state.streakDay >= state.totalDays

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. System Protection Status Banner
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (canDrawOverlays) StrictEmerald.copy(alpha = 0.45f) else WarningAmber.copy(alpha = 0.6f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    (if (canDrawOverlays) StrictEmerald else WarningAmber).copy(alpha = 0.16f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = if (canDrawOverlays) StrictEmerald else WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (canDrawOverlays) "Strict Mode Protection: Active" else "Overlay Lock Permission Required",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (canDrawOverlays) "Real-time app lock & 30-min violation shield armed" else "Grant 'Display over other apps' for full lock protection",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (!canDrawOverlays) {
                        Button(
                            onClick = onOpenOverlaySettings,
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Enable", color = DarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Hero 21-Day Strict Mode & Uninstall Progression Card
        item {
            GlassCard(
                accentColor = if (isUninstallReady) StrictEmerald else ShieldCyan,
                cornerRadius = 20.dp,
                contentPadding = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(ShieldCyan, StrictEmerald))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUninstallReady) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = DarkBg,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "21-Day Strict Mode & Uninstall Lock",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isUninstallReady) {
                                    "21-Day Commitment Complete • Uninstall Unlocked"
                                } else {
                                    "Protected Commitment • $daysRemaining Days Until Unlock"
                                },
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    GlassPill(
                        text = "$progressPercent%",
                        color = if (isUninstallReady) StrictEmerald else ShieldCyan
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = StrictEmerald,
                    trackColor = SurfaceCardElevated
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Completed: Day ${state.streakDay} of ${state.totalDays}",
                        color = StrictEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isUninstallReady) "Goal Reached" else "$daysRemaining Days Remaining",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMono
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 21-Day Visual Progression Grid (3 rows x 7 days)
                Text(
                    text = "21-DAY PROGRESSION MILESTONES",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (week in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (dayOffset in 1..7) {
                                val dayNum = week * 7 + dayOffset
                                val isCompleted = dayNum <= state.streakDay
                                val isCurrent = dayNum == state.streakDay + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isCompleted -> StrictEmerald.copy(alpha = 0.22f)
                                                isCurrent -> ShieldCyan.copy(alpha = 0.22f)
                                                else -> Color.White.copy(alpha = 0.04f)
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when {
                                                isCompleted -> StrictEmerald.copy(alpha = 0.6f)
                                                isCurrent -> ShieldCyan
                                                else -> Color.White.copy(alpha = 0.08f)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "D$dayNum",
                                        color = when {
                                            isCompleted -> StrictEmerald
                                            isCurrent -> ShieldCyan
                                            else -> TextMuted
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = JetBrainsMono
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Uninstall Protection Progression Checklist
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBg.copy(alpha = 0.7f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "UNINSTALL PROTECTION PROGRESSION",
                        color = ShieldCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    UninstallStepRow(
                        stepTitle = "1. 21-Day Commitment Streak",
                        statusText = "${state.streakDay}/21 Days",
                        isComplete = isUninstallReady
                    )
                    UninstallStepRow(
                        stepTitle = "2. Daily 5-Min Study Assessment",
                        statusText = if (state.isAssessmentPassedToday) "Passed (${state.assessmentScore}%)" else "Required Tonight",
                        isComplete = state.isAssessmentPassedToday
                    )
                    UninstallStepRow(
                        stepTitle = "3. App & Sideload Guard Integrity",
                        statusText = "Verified Active",
                        isComplete = true
                    )
                    UninstallStepRow(
                        stepTitle = "4. Uninstall Lock Release",
                        statusText = if (isUninstallReady) "Unlocked" else "Locked ($daysRemaining days left)",
                        isComplete = isUninstallReady
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCompleteDailyCheckIn,
                        enabled = !isUninstallReady,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StrictEmerald,
                            contentColor = DarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("complete_daily_checkin_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUninstallReady) "21 Days Complete" else "Log Daily Check-In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showUninstallDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("uninstall_progression_button")
                    ) {
                        Icon(
                            imageVector = if (isUninstallReady) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUninstallReady) StrictEmerald else WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUninstallReady) "Uninstall Ready" else "Uninstall Status",
                            color = if (isUninstallReady) StrictEmerald else WarningAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 3. Direct In-App Downloads & Study Kits Section
        item {
            GlassCard(
                accentColor = UgtGold,
                cornerRadius = 20.dp,
                contentPadding = 18.dp
            ) {
                GlassSectionHeader(
                    title = "In-App Downloads & Study Kits",
                    subtitle = "One-tap direct download to your phone's Downloads folder",
                    icon = Icons.Default.CloudDownload,
                    accentColor = UgtGold,
                    trailingBadge = "${StudyResourceDownloadManager.catalog.size} Packages"
                )

                if (state.lastDownloadedFileMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(StrictEmerald.copy(alpha = 0.14f))
                            .border(1.dp, StrictEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = null,
                            tint = StrictEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.lastDownloadedFileMessage,
                            color = StrictEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudyResourceDownloadManager.catalog.forEach { item ->
                        val isDownloaded = state.downloadedResourceIds.contains(item.id) || downloadedResults.containsKey(item.id)
                        val isDownloading = activeDownloadingId == item.id
                        DownloadPackageRow(
                            resource = item,
                            isDownloaded = isDownloaded,
                            isDownloading = isDownloading,
                            progress = if (isDownloading) downloadProgress else 1f,
                            onDownloadClick = {
                                scope.launch {
                                    activeDownloadingId = item.id
                                    downloadProgress = 0.1f
                                    val res = StudyResourceDownloadManager.downloadResource(
                                        context = context,
                                        resource = item,
                                        onProgress = { p -> downloadProgress = p }
                                    )
                                    downloadedResults[item.id] = res
                                    activeDownloadingId = null
                                    onResourceDownloaded(item.id, res.savedPath)
                                }
                            },
                            onShareOrOpenClick = {
                                val res = downloadedResults[item.id]
                                if (res != null) {
                                    StudyResourceDownloadManager.shareDownloadedContent(
                                        context = context,
                                        title = item.title,
                                        fileName = item.fileName,
                                        content = res.contentPreview
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        // 4. Primary Study & Assessment Portals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // UGT Tuition Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, StrictEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = StrictEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "UGT Tuition Hub",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Video lectures, notes & solved examples",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToUgt,
                            colors = ButtonDefaults.buttonColors(containerColor = StrictEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("open_ugt_portal_button")
                        ) {
                            Text("Open Tuition", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Nightly Assessment Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, ShieldCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ShieldCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "5-Min Assessment",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (state.isAssessmentPassedToday) "Passed today (${state.assessmentScore}%)" else "Mandatory daily check",
                            color = if (state.isAssessmentPassedToday) StrictEmerald else WarningAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToExam,
                            colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("open_night_exam_button")
                        ) {
                            Text(
                                if (state.isAssessmentPassedToday) "Review Score" else "Start Quiz",
                                color = DarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. Active Protection Summary & Policy Verification
        item {
            GlassCard(accentColor = ShieldCyan, cornerRadius = 18.dp, contentPadding = 16.dp) {
                GlassSectionHeader(
                    title = "Active Strict Mode Policies",
                    subtitle = "Enforced continuously across browsers and installed apps",
                    icon = Icons.Default.Shield,
                    accentColor = ShieldCyan,
                    trailingBadge = "4 Rules Active"
                )
                Spacer(modifier = Modifier.height(14.dp))
                ConstraintRow(
                    icon = Icons.Default.Language,
                    title = "Approved Browsers Only (Max 2)",
                    detail = "Google Chrome & Brave Browser • 100% Adult & Gambling Filter",
                    tint = ShieldCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                ConstraintRow(
                    icon = Icons.Default.Games,
                    title = "Single Approved Game Quota",
                    detail = "${state.allowedGame.name} • ${state.allowedGame.usedMinutes}/${state.allowedGame.limitMinutes} min used today",
                    tint = UgtGold
                )
                Spacer(modifier = Modifier.height(10.dp))
                ConstraintRow(
                    icon = Icons.Default.VideocamOff,
                    title = "Short-Form Video Shield",
                    detail = "Reels, Shorts & Spotlight feeds automatically blocked (${state.reelsBlockedToday} blocked today)",
                    tint = UgtPurple
                )
                Spacer(modifier = Modifier.height(10.dp))
                ConstraintRow(
                    icon = Icons.Default.Security,
                    title = "Sideloaded APK & Mod Blocker",
                    detail = "Unverified APK installers removed immediately (${state.apksPurgedCount} removed)",
                    tint = StrictEmerald
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Subtle policy compliance verification actions (preserving testTags)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSimulateApkDownload,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_apk_download_button")
                    ) {
                        Text("Verify APK Guard", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onSimulateTamper,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_tamper_button")
                    ) {
                        Text("Verify Settings Lock", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onTriggerManual30mLock,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_lockout_test_button")
                    ) {
                        Text("30m Lock Preview", color = AlarmCrimson, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 6. Recent Activity Log
        item {
            Text(
                text = "RECENT PROTECTION ACTIVITY",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }

        items(state.violationLogs.take(4)) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (log.penaltyMinutes > 0) AlarmCrimson else ShieldCyan)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = log.timestampFormatted,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = log.details,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showUninstallDialog) {
        AlertDialog(
            onDismissRequest = { showUninstallDialog = false },
            title = {
                Text(
                    text = if (isUninstallReady) "21-Day Commitment Complete" else "Uninstall Locked — Day ${state.streakDay} of ${state.totalDays}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isUninstallReady) {
                            "Congratulations! You have completed all 21 days of Strict Mode. Uninstall protection is now unlocked."
                        } else {
                            "Standard uninstallation and Strict Mode deactivation are locked until Day 21 ($daysRemaining days remaining).\n\nAttempting to force-disable or bypass Strict Mode before completing the 21-day progression immediately triggers a 30-minute full-device lockout where only UGT Tuition can be used."
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUninstallDialog = false
                        if (isUninstallReady) {
                            onRequestUninstallProgression()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StrictEmerald)
                ) {
                    Text(
                        text = if (isUninstallReady) "Unlock & Complete" else "Keep Studying",
                        color = DarkBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                if (!isUninstallReady) {
                    OutlinedButton(
                        onClick = {
                            showUninstallDialog = false
                            onRequestUninstallProgression()
                        }
                    ) {
                        Text("Force Bypass (30m Lockout)", color = AlarmCrimson, fontSize = 11.sp)
                    }
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun UninstallStepRow(
    stepTitle: String,
    statusText: String,
    isComplete: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isComplete) StrictEmerald else WarningAmber,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stepTitle,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = statusText,
            color = if (isComplete) StrictEmerald else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DownloadPackageRow(
    resource: DownloadableResource,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    onDownloadClick: () -> Unit,
    onShareOrOpenClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(
                1.dp,
                if (isDownloaded) StrictEmerald.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.09f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = resource.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${resource.category} • ${resource.sizeLabel} • ${resource.fileName}",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = JetBrainsMono
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = resource.subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isDownloaded) {
                OutlinedButton(
                    onClick = onShareOrOpenClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("share_download_${resource.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Open or Share",
                        tint = StrictEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", color = StrictEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDownloadClick,
                    enabled = !isDownloading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UgtGold,
                        contentColor = DarkBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("download_btn_${resource.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Download",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isDownloading) "..." else "Download",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(visible = isDownloading) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = UgtGold,
                    trackColor = SurfaceCardElevated
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun ConstraintRow(
    icon: ImageVector,
    title: String,
    detail: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
