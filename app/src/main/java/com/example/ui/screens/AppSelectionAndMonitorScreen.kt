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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DisciplineState
import com.example.model.SelectableAppItem
import com.example.service.DownloadResult
import com.example.service.StudyResourceDownloadManager
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.GlassSectionHeader
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.StrictEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UgtGold
import com.example.ui.theme.UgtPurple
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun AppSelectionAndMonitorScreen(
    state: DisciplineState,
    selectableApps: List<SelectableAppItem>,
    pythonEndpoint: String,
    pythonLastVerdict: String,
    pythonFramesInspected: Int,
    onUpdatePythonEndpoint: (String) -> Unit,
    onToggleAppSelection: (SelectableAppItem, Boolean) -> Unit,
    onInspectScreenFrameWithPython: (frameSource: String, ocrText: String) -> Unit,
    onNavigateToApkShield: () -> Unit,
    onResourceDownloaded: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPythonScript by remember { mutableStateOf(false) }
    var customFrameInput by remember { mutableStateOf("instagram.com/reels/trending_short_clip") }
    var activeDownloadingId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    val downloadedResults = remember { mutableStateMapOf<String, DownloadResult>() }

    val allowedBrowsersCount = selectableApps.count { it.category == "browser" && it.isAllowed }
    val allowedGamesCount = selectableApps.count { it.category == "game" && it.isAllowed }
    val allowedSocialsCount = selectableApps.count { it.category == "social" && it.isAllowed }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Select Application Whitelist Header Card
        item {
            Spacer(modifier = Modifier.height(6.dp))
            GlassCard(accentColor = ShieldCyan) {
                GlassSectionHeader(
                    title = "Select Allowed Applications",
                    subtitle = "Strict Quota: Max 2 Browsers • Max 1 Game (20m) • Max 2 Social (30m)",
                    icon = Icons.Filled.Apps,
                    accentColor = ShieldCyan,
                    trailingBadge = "Cloud Synced"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassPill(
                        text = "Browsers: $allowedBrowsersCount/2",
                        color = if (allowedBrowsersCount <= 2) ShieldCyan else AlertRed
                    )
                    GlassPill(
                        text = "Games: $allowedGamesCount/1",
                        color = if (allowedGamesCount <= 1) UgtGold else AlertRed
                    )
                    GlassPill(
                        text = "Social: $allowedSocialsCount/2",
                        color = if (allowedSocialsCount <= 2) StrictEmerald else AlertRed
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onNavigateToApkShield,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_apk_scanner_button")
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = ShieldCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Installed Package Audit & APK Guard", color = ShieldCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 2. Application Selection List
        items(selectableApps, key = { it.id }) { appItem ->
            val categoryColor = when (appItem.category) {
                "browser" -> ShieldCyan
                "game" -> UgtGold
                "social" -> UgtPurple
                else -> StrictEmerald
            }
            GlassCard(
                accentColor = if (appItem.isAllowed) categoryColor else TextMuted,
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = appItem.appName,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            GlassPill(
                                text = appItem.category.uppercase(),
                                color = categoryColor
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${appItem.packageName} • Daily Quota: ${appItem.dailyLimitMinutes} min",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = JetBrainsMono
                        )
                    }
                    Switch(
                        checked = appItem.isAllowed,
                        onCheckedChange = { checked ->
                            onToggleAppSelection(appItem, checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBg,
                            checkedTrackColor = categoryColor
                        ),
                        modifier = Modifier.testTag("select_app_${appItem.id}")
                    )
                }
            }
        }

        // 3. In-App Download Center
        item {
            GlassCard(accentColor = UgtGold) {
                GlassSectionHeader(
                    title = "In-App Resource & Guard Downloads",
                    subtitle = "Download study notes, strict policies & companion guard directly",
                    icon = Icons.Filled.CloudDownload,
                    accentColor = UgtGold,
                    trailingBadge = "Direct Save"
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
                            imageVector = Icons.Filled.DownloadDone,
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
                                    downloadProgress = 0.15f
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

        // 4. Screen Monitor Service Configuration
        item {
            GlassCard(accentColor = UgtPurple) {
                GlassSectionHeader(
                    title = "Screen Monitor & Content Filter",
                    subtitle = "Real-time inspection for Reels, Shorts & unverified installers",
                    icon = Icons.Filled.DesktopWindows,
                    accentColor = UgtPurple,
                    trailingBadge = "$pythonFramesInspected Checked"
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = pythonEndpoint,
                    onValueChange = onUpdatePythonEndpoint,
                    label = { Text("Screen Monitor Service Endpoint", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UgtPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = UgtPurple,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("python_endpoint_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PythonStatMiniCard(
                        label = "Short-Form",
                        value = "${state.reelsBlockedToday} Blocked",
                        color = ShieldCyan,
                        modifier = Modifier.weight(1f)
                    )
                    PythonStatMiniCard(
                        label = "Restricted Web",
                        value = "${state.adultSitesBlockedToday} Blocked",
                        color = AlertRed,
                        modifier = Modifier.weight(1f)
                    )
                    PythonStatMiniCard(
                        label = "APK Installers",
                        value = "${state.apksPurgedCount} Removed",
                        color = StrictEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customFrameInput,
                    onValueChange = { customFrameInput = it },
                    label = { Text("Verify URL or Active Window Title", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.18f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("python_ocr_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onInspectScreenFrameWithPython("ScreenMonitor", customFrameInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UgtPurple, contentColor = TextPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("python_inspect_frame_button")
                    ) {
                        Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check Policy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { showPythonScript = !showPythonScript },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Code, contentDescription = null, tint = ShieldCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showPythonScript) "Hide Script" else "Companion Script",
                            color = ShieldCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetFrameChip(
                        label = "Check Reels URL",
                        color = WarningAmber,
                        onClick = {
                            customFrameInput = "instagram.com/reels/scroll_feed"
                            onInspectScreenFrameWithPython("Instagram", customFrameInput)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PresetFrameChip(
                        label = "Check Shorts URL",
                        color = WarningAmber,
                        onClick = {
                            customFrameInput = "youtube.com/shorts/viral_loop"
                            onInspectScreenFrameWithPython("YouTube", customFrameInput)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PresetFrameChip(
                        label = "Check UGT Lecture",
                        color = StrictEmerald,
                        onClick = {
                            customFrameInput = "UGT Tuition: Rotational Mechanics Lecture 04"
                            onInspectScreenFrameWithPython("UGT_Tuition", customFrameInput)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (pythonLastVerdict.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val isBlocked = pythonLastVerdict.contains("BLOCKED") || pythonLastVerdict.contains("LOCKOUT")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background((if (isBlocked) AlertRed else StrictEmerald).copy(alpha = 0.14f))
                            .border(
                                1.dp,
                                (if (isBlocked) AlertRed else StrictEmerald).copy(alpha = 0.45f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = pythonLastVerdict,
                            color = if (isBlocked) AlertRed else StrictEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                AnimatedVisibility(visible = showPythonScript) {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF050811))
                            .border(1.dp, ShieldCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ugt_screen_guard_service.py (FastAPI + OCR Screen Monitor)",
                            color = ShieldCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = PYTHON_MONITOR_SCRIPT_SNIPPET,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = JetBrainsMono,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun PythonStatMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun PresetFrameChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private const val PYTHON_MONITOR_SCRIPT_SNIPPET = """from fastapi import FastAPI
from pydantic import BaseModel
import re

app = FastAPI(title="UGT StudyGuard Screen Monitor")

BANNED_PATTERNS = [
    r"(reels|shorts|tiktok|spotlight)",
    r"(porn|xxx|nsfw|casino|betting|gambling)",
    r"(\.apk|mod_menu|unlimited_diamonds)"
]

class ScreenFramePayload(BaseModel):
    package_name: str
    ocr_text: str

@app.post("/api/v1/inspect-screen")
def inspect_screen(payload: ScreenFramePayload):
    text = f"{payload.package_name} {payload.ocr_text}".lower()
    for pattern in BANNED_PATTERNS:
        if re.search(pattern, text):
            return {
                "allowed": False,
                "action": "LOCKOUT_30_MIN",
                "matched_rule": pattern
            }
    return {"allowed": True, "action": "PERMIT"}"""
