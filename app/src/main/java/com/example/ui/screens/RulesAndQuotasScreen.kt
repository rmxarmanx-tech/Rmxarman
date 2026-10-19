package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.model.DisciplineState
import com.example.ui.DisciplineViewModel
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

@Composable
fun RulesAndQuotasScreen(
    state: DisciplineState,
    onAttemptTamper: (String) -> Unit,
    onAddGameUsage: (Int) -> Unit,
    onAddSocialUsage: (String, Int) -> Unit,
    onEvaluateUrl: (String) -> DisciplineViewModel.UrlCheckResult
) {
    var testUrlInput by remember { mutableStateOf("https://youtube.com/shorts/cool_tricks") }
    var urlResult by remember { mutableStateOf<DisciplineViewModel.UrlCheckResult?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
            .testTag("rules_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // 8 Core Operational Constraints Header
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ShieldCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ShieldCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ShieldCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "8 CORE DISCIPLINE CONSTRAINTS",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "21-Day Lock Protocol Active",
                                color = StrictEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Every single setting below is locked for 21 consecutive days. Attempting to disable or bypass any toggle is registered as a security violation and triggers an immediate 30-minute lockout.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // The Protected Settings Toggles
        item {
            Text(
                text = "PROTECTED ENGINE TOGGLES (TAMPER-PROOF)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProtectedToggleCard(
                    title = "21-Day Ultra Strict Enforcement",
                    hindiSubtitle = "21 दिन का अनब्रेकेबल मोड",
                    enabled = state.is21DayModeActive,
                    onToggleAttempt = { onAttemptTamper("21-Day Ultra Strict Enforcement") }
                )
                ProtectedToggleCard(
                    title = "AI APK & Mod Auto-Purge",
                    hindiSubtitle = "सारे मॉड व एपीके ऑटो-डिलीट",
                    enabled = state.apkAutoDeleteEnabled,
                    onToggleAttempt = { onAttemptTamper("AI APK & Mod Auto-Purge") }
                )
                ProtectedToggleCard(
                    title = "100% Adult & Gambling Web Filter",
                    hindiSubtitle = "अडल्ट व अवांछित वेबसाइट्स पूरी तरह ब्लॉक",
                    enabled = state.adultWebBlockerEnabled,
                    onToggleAttempt = { onAttemptTamper("Adult & Gambling Web Filter") }
                )
                ProtectedToggleCard(
                    title = "Reels & Shorts Blocker Shield",
                    hindiSubtitle = "सभी रील्स और शॉर्ट्स 100% बंद",
                    enabled = state.reelsShortsBlockerEnabled,
                    onToggleAttempt = { onAttemptTamper("Reels & Shorts Blocker") }
                )
            }
        }

        // Allowed 2 Browsers & Safe Web Filter Tester
        item {
            Text(
                text = "ALLOWED 2 BROWSERS & WEB SHIELD (RULE 4)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Permitted Browsers: Google Chrome & Brave",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All other browsers are blocked. Allowed browsers enforce 100% adult & shorts shield.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "TEST DOMAIN / URL AGAINST SHIELD:",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = testUrlInput,
                        onValueChange = { testUrlInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("url_filter_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShieldCyan,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        placeholder = { Text("e.g. youtube.com/shorts or adultsite.com", color = TextMuted, fontSize = 12.sp) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                urlResult = onEvaluateUrl(testUrlInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("test_url_button")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = DarkBg)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test URL Filter", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                testUrlInput = "https://instagram.com/reels/feed"
                                urlResult = onEvaluateUrl(testUrlInput)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Try Reel URL", color = AlarmCrimson, fontSize = 11.sp)
                        }
                    }

                    if (urlResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val res = urlResult!!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (res.allowed) StrictEmerald.copy(alpha = 0.15f) else AlarmCrimsonBg)
                                .border(
                                    1.dp,
                                    if (res.allowed) StrictEmerald else AlarmCrimson,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = res.reason,
                                color = if (res.allowed) StrictEmerald else AlarmCrimson,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Allowed 1 Game Quota (Rule 5)
        item {
            Text(
                text = "ALLOWED 1 GAME QUOTA (RULE 5)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Games, contentDescription = null, tint = WarningAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = state.allowedGame.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Only 1 game allowed • Max 20 mins/day",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "${state.allowedGame.usedMinutes} / ${state.allowedGame.limitMinutes} min",
                            color = if (state.allowedGame.usedMinutes >= state.allowedGame.limitMinutes) AlarmCrimson else WarningAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val gameProgress = (state.allowedGame.usedMinutes.toFloat() / state.allowedGame.limitMinutes.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { gameProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (gameProgress >= 1f) AlarmCrimson else WarningAmber,
                        trackColor = SurfaceCardElevated
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onAddGameUsage(5) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("simulate_game_play_button")
                        ) {
                            Text("Simulate +5m Game Play", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Allowed 2 Social Media Quota (Rule 5)
        item {
            Text(
                text = "ALLOWED 2 SOCIAL MEDIA APPS (RULE 5)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.allowedSocials.forEach { social ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = ShieldCyan)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = social.name,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Strict 30 min study communication allowance",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "${social.usedMinutes} / ${social.limitMinutes} min",
                                    color = if (social.usedMinutes >= social.limitMinutes) AlarmCrimson else ShieldCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val socialProgress = (social.usedMinutes.toFloat() / social.limitMinutes.toFloat()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { socialProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (socialProgress >= 1f) AlarmCrimson else ShieldCyan,
                                trackColor = SurfaceCardElevated
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onAddSocialUsage(social.id, 5) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardElevated),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Simulate +5m ${social.name.substringBefore(" ")}", color = TextPrimary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProtectedToggleCard(
    title: String,
    hindiSubtitle: String,
    enabled: Boolean,
    onToggleAttempt: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Tamper Lock",
                    tint = StrictEmerald,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$hindiSubtitle (LOCKED)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = { onToggleAttempt() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = StrictEmerald,
                    checkedTrackColor = StrictEmerald.copy(alpha = 0.3f),
                    uncheckedThumbColor = AlarmCrimson,
                    uncheckedTrackColor = AlarmCrimsonBg
                )
            )
        }
    }
}
