package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FocusSessionDoc
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSectionHeader
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.StrictEmerald
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UgtGold
import com.example.ui.theme.UgtPurple
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

enum class StudioTimerTab(val label: String, val modeKey: String, val accent: Color) {
    FOCUS_TIMER("Focus Timer", "FOCUS", ShieldCyan),
    POMODORO("Pomodoro", "POMODORO", StrictEmerald),
    STOPWATCH("Stopwatch", "STOPWATCH", UgtGold),
    ULTRA_FOCUS("Ultra-Focus", "ULTRA_FOCUS", UgtPurple)
}

enum class PomodoroPhase(val title: String, val minutes: Int, val color: Color) {
    WORK("Focus Sprint (25m)", 25, StrictEmerald),
    SHORT_BREAK("Short Rest (5m)", 5, ShieldCyan),
    LONG_BREAK("Deep Recovery (15m)", 15, UgtPurple)
}

@Composable
fun FocusTimersScreen(
    cloudSessions: List<FocusSessionDoc>,
    isUltraFocusLocked: Boolean,
    onToggleUltraFocusLock: (Boolean) -> Unit,
    onLogFocusSession: (mode: String, durationMinutes: Int, subject: String) -> Unit,
    onEarlyUltraFocusAbortPenalty: () -> Unit
) {
    var activeTab by remember { mutableStateOf(StudioTimerTab.FOCUS_TIMER) }
    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "Reasoning")
    var selectedSubject by remember { mutableStateOf(subjects[0]) }

    // 1. Focus Timer state
    val focusPresets = listOf(15, 30, 45, 60, 90)
    var focusDurationMins by remember { mutableIntStateOf(45) }
    var focusRemainingSec by remember { mutableLongStateOf(45 * 60L) }
    var isFocusRunning by remember { mutableStateOf(false) }

    // 2. Pomodoro state
    var pomoPhase by remember { mutableStateOf(PomodoroPhase.WORK) }
    var pomoCycle by remember { mutableIntStateOf(1) }
    var pomoRemainingSec by remember { mutableLongStateOf(25 * 60L) }
    var isPomoRunning by remember { mutableStateOf(false) }

    // 3. Stopwatch state
    var stopwatchSec by remember { mutableLongStateOf(0L) }
    var isStopwatchRunning by remember { mutableStateOf(false) }
    val stopwatchLaps = remember { mutableStateListOf<String>() }

    // 4. Ultra-Focus state
    var ultraDurationMins by remember { mutableIntStateOf(60) }
    var ultraRemainingSec by remember { mutableLongStateOf(60 * 60L) }
    var isUltraRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isFocusRunning) {
        while (isFocusRunning && focusRemainingSec > 0) {
            delay(1000L)
            focusRemainingSec--
            if (focusRemainingSec == 0L) {
                isFocusRunning = false
                onLogFocusSession("FOCUS", focusDurationMins, selectedSubject)
            }
        }
    }

    LaunchedEffect(isPomoRunning) {
        while (isPomoRunning && pomoRemainingSec > 0) {
            delay(1000L)
            pomoRemainingSec--
            if (pomoRemainingSec == 0L) {
                isPomoRunning = false
                if (pomoPhase == PomodoroPhase.WORK) {
                    onLogFocusSession("POMODORO", pomoPhase.minutes, selectedSubject)
                }
            }
        }
    }

    LaunchedEffect(isStopwatchRunning) {
        while (isStopwatchRunning) {
            delay(1000L)
            stopwatchSec++
        }
    }

    LaunchedEffect(isUltraRunning) {
        while (isUltraRunning && ultraRemainingSec > 0) {
            delay(1000L)
            ultraRemainingSec--
            if (ultraRemainingSec == 0L) {
                isUltraRunning = false
                onToggleUltraFocusLock(false)
                onLogFocusSession("ULTRA_FOCUS", ultraDurationMins, selectedSubject)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            GlassCard(accentColor = activeTab.accent) {
                GlassSectionHeader(
                    title = "Focus & Pomodoro Studio",
                    subtitle = "Cloud-Synced Study Timers, Stopwatch & Ultra-Focus Lock",
                    icon = Icons.Filled.Timer,
                    accentColor = activeTab.accent,
                    trailingBadge = "${cloudSessions.sumOf { it.durationMinutes }}m Logged"
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Mode Switcher Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioTimerTab.entries.forEach { tab ->
                        val selected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) tab.accent.copy(alpha = 0.22f)
                                    else Color.White.copy(alpha = 0.04f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) tab.accent else Color.White.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { activeTab = tab }
                                .padding(vertical = 10.dp)
                                .testTag("timer_mode_${tab.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                color = if (selected) tab.accent else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { subj ->
                        val isSubjSelected = selectedSubject == subj
                        FilterChip(
                            selected = isSubjSelected,
                            onClick = { selectedSubject = subj },
                            label = {
                                Text(
                                    text = subj,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSubjSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeTab.accent.copy(alpha = 0.2f),
                                selectedLabelColor = activeTab.accent,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Active Timer Display
        item {
            when (activeTab) {
                StudioTimerTab.FOCUS_TIMER -> {
                    GlassCard(accentColor = ShieldCyan) {
                        Text(
                            text = "Custom Countdown Focus Timer",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            focusPresets.forEach { mins ->
                                val isSelected = focusDurationMins == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) ShieldCyan.copy(alpha = 0.2f)
                                            else Color.White.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) ShieldCyan else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable(enabled = !isFocusRunning) {
                                            focusDurationMins = mins
                                            focusRemainingSec = mins * 60L
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        color = if (isSelected) ShieldCyan else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val progress = (focusRemainingSec.toFloat() / (focusDurationMins * 60f)).coerceIn(0f, 1f)
                        CircularTimerRing(
                            formattedTime = formatTimerSeconds(focusRemainingSec),
                            subtitle = "$selectedSubject • Deep Focus",
                            progress = progress,
                            accentColor = ShieldCyan
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isFocusRunning = !isFocusRunning },
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan, contentColor = DarkBg),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("focus_start_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isFocusRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFocusRunning) "Pause" else "Start Focus",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    isFocusRunning = false
                                    focusRemainingSec = focusDurationMins * 60L
                                },
                                modifier = Modifier.weight(0.6f)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = TextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset", color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val elapsedMins = ((focusDurationMins * 60L - focusRemainingSec) / 60L).toInt().coerceAtLeast(focusDurationMins)
                                onLogFocusSession("FOCUS", elapsedMins, selectedSubject)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_focus_session_button")
                        ) {
                            Icon(Icons.Filled.CloudDone, contentDescription = null, tint = StrictEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync Completed Focus Session to Cloud", color = StrictEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                StudioTimerTab.POMODORO -> {
                    GlassCard(accentColor = StrictEmerald) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Pomodoro Interval Engine",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Cycle $pomoCycle of 4 • ${pomoPhase.title}",
                                    color = pomoPhase.color,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            GlassPill(text = "CYCLE $pomoCycle/4", color = pomoPhase.color)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PomodoroPhase.entries.forEach { phase ->
                                val selected = pomoPhase == phase
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selected) phase.color.copy(alpha = 0.2f)
                                            else Color.White.copy(alpha = 0.04f)
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) phase.color else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            isPomoRunning = false
                                            pomoPhase = phase
                                            pomoRemainingSec = phase.minutes * 60L
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${phase.minutes}m",
                                        color = if (selected) phase.color else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        val progress = (pomoRemainingSec.toFloat() / (pomoPhase.minutes * 60f)).coerceIn(0f, 1f)
                        CircularTimerRing(
                            formattedTime = formatTimerSeconds(pomoRemainingSec),
                            subtitle = "${pomoPhase.title} • $selectedSubject",
                            progress = progress,
                            accentColor = pomoPhase.color
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isPomoRunning = !isPomoRunning },
                                colors = ButtonDefaults.buttonColors(containerColor = pomoPhase.color, contentColor = DarkBg),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pomodoro_start_button")
                            ) {
                                Icon(
                                    imageVector = if (isPomoRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPomoRunning) "Pause Sprint" else "Start Pomodoro",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    isPomoRunning = false
                                    if (pomoPhase == PomodoroPhase.WORK) {
                                        onLogFocusSession("POMODORO", 25, selectedSubject)
                                        pomoPhase = if (pomoCycle % 4 == 0) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
                                    } else {
                                        pomoCycle = (pomoCycle % 4) + 1
                                        pomoPhase = PomodoroPhase.WORK
                                    }
                                    pomoRemainingSec = pomoPhase.minutes * 60L
                                },
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Icon(Icons.Filled.SkipNext, contentDescription = null, tint = StrictEmerald)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Next Phase", color = StrictEmerald)
                            }
                        }
                    }
                }

                StudioTimerTab.STOPWATCH -> {
                    GlassCard(accentColor = UgtGold) {
                        Text(
                            text = "Endurance Study Stopwatch",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Track open-ended problem solving & mock paper speed",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        CircularTimerRing(
                            formattedTime = formatTimerSeconds(stopwatchSec),
                            subtitle = "$selectedSubject • Lap Stopwatch",
                            progress = ((stopwatchSec % 60L) / 60f).coerceIn(0.05f, 1f),
                            accentColor = UgtGold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { isStopwatchRunning = !isStopwatchRunning },
                                colors = ButtonDefaults.buttonColors(containerColor = UgtGold, contentColor = DarkBg),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("stopwatch_start_button")
                            ) {
                                Icon(
                                    imageVector = if (isStopwatchRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isStopwatchRunning) "Pause" else "Start",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    stopwatchLaps.add(
                                        0,
                                        "Lap ${stopwatchLaps.size + 1} ($selectedSubject) — ${formatTimerSeconds(stopwatchSec)}"
                                    )
                                },
                                modifier = Modifier.weight(0.7f)
                            ) {
                                Icon(Icons.Filled.Flag, contentDescription = null, tint = UgtGold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lap", color = UgtGold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val mins = (stopwatchSec / 60L).toInt().coerceAtLeast(1)
                                    onLogFocusSession("STOPWATCH", mins, selectedSubject)
                                    isStopwatchRunning = false
                                    stopwatchSec = 0L
                                    stopwatchLaps.clear()
                                },
                                modifier = Modifier.weight(0.8f)
                            ) {
                                Text("Save & Reset", color = StrictEmerald, fontSize = 11.sp)
                            }
                        }

                        AnimatedVisibility(visible = stopwatchLaps.isNotEmpty()) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                stopwatchLaps.take(5).forEach { lap ->
                                    Text(
                                        text = "• $lap",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                StudioTimerTab.ULTRA_FOCUS -> {
                    GlassCard(accentColor = UgtPurple) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ultra-Focus Lockdown Mode",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Zero-tolerance deep work. Aborting early triggers an immediate 30-minute phone lockout!",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isUltraFocusLocked,
                                onCheckedChange = { enabled ->
                                    if (!enabled && isUltraRunning) {
                                        isUltraRunning = false
                                        onToggleUltraFocusLock(false)
                                        onEarlyUltraFocusAbortPenalty()
                                    } else {
                                        onToggleUltraFocusLock(enabled)
                                        isUltraRunning = enabled
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DarkBg,
                                    checkedTrackColor = UgtPurple
                                ),
                                modifier = Modifier.testTag("ultra_focus_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val progress = (ultraRemainingSec.toFloat() / (ultraDurationMins * 60f)).coerceIn(0f, 1f)
                        CircularTimerRing(
                            formattedTime = formatTimerSeconds(ultraRemainingSec),
                            subtitle = if (isUltraFocusLocked) "ULTRA-FOCUS SHIELD ENGAGED • $selectedSubject" else "Ready to Lock In • $selectedSubject",
                            progress = progress,
                            accentColor = UgtPurple
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isUltraRunning = true
                                    onToggleUltraFocusLock(true)
                                },
                                enabled = !isUltraRunning,
                                colors = ButtonDefaults.buttonColors(containerColor = UgtPurple, contentColor = TextPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("engage_ultra_focus_button")
                            ) {
                                Icon(Icons.Filled.Lock, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Engage Ultra-Focus", fontWeight = FontWeight.Bold)
                            }
                            if (isUltraRunning) {
                                Button(
                                    onClick = {
                                        isUltraRunning = false
                                        onToggleUltraFocusLock(false)
                                        onEarlyUltraFocusAbortPenalty()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed, contentColor = TextPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Bolt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Abort (30m Penalty)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cloud-Synced Focus History
        item {
            GlassCard(accentColor = StrictEmerald) {
                GlassSectionHeader(
                    title = "Cloud Focus Session Log",
                    subtitle = "Real-time Firestore history across Focus, Pomodoro & Ultra-Focus",
                    icon = Icons.Filled.CloudDone,
                    accentColor = StrictEmerald,
                    trailingBadge = "${cloudSessions.size} Sessions"
                )
            }
        }

        if (cloudSessions.isEmpty()) {
            item {
                GlassCard(accentColor = ShieldCyan) {
                    Text(
                        text = "No cloud focus sessions recorded yet. Start a Focus Timer, Pomodoro sprint, or Ultra-Focus session above to sync your progress.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(cloudSessions.take(10), key = { it.id.ifBlank { it.hashCode().toString() } }) { session ->
                val dateFmt = remember { SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()) }
                val dateStr = session.createdAt?.toDate()?.let { dateFmt.format(it) } ?: "Just now"
                GlassCard(accentColor = ShieldCyan, contentPadding = 12.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${session.subject} • ${session.mode}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = dateStr,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        GlassPill(
                            text = "${session.durationMinutes} min",
                            color = StrictEmerald
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun CircularTimerRing(
    formattedTime: String,
    subtitle: String,
    progress: Float,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(175.dp)) {
            val strokeWidth = 12.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                color = TextPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTimerSeconds(totalSec: Long): String {
    val hrs = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return if (hrs > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", mins, secs)
    }
}
