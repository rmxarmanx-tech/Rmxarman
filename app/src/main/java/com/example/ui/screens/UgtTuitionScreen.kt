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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurriculumData
import com.example.model.UgtModule
import com.example.model.UgtSubject
import com.example.ui.theme.DarkBg
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.StrictEmerald
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UgtBrandGold
import com.example.ui.theme.UgtBrandPurple
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@Composable
fun UgtTuitionScreen() {
    val subjects = CurriculumData.ugtSubjects
    var selectedSubjectIndex by remember { mutableIntStateOf(0) }
    val currentSubject = subjects[selectedSubjectIndex]
    var selectedModule by remember { mutableStateOf(currentSubject.modules.first()) }

    var isVideoPlaying by remember { mutableStateOf(false) }
    var videoProgress by remember { mutableFloatStateOf(0.35f) }
    var playbackSpeed by remember { mutableStateOf("1.0x") }
    var scratchpadText by remember {
        mutableStateOf("Notes from today's Gauss Law lecture:\n1. Total flux through closed surface = Q / ε0.\n2. Keep practicing 3D vectors before tonight's 5-minute assessment.")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
            .testTag("ugt_tuition_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Portal Branding Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(UgtBrandGold, StrictEmerald)),
                        RoundedCornerShape(16.dp)
                    )
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
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(listOf(UgtBrandGold, UgtBrandPurple))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = DarkBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "UGT TUITION APPLICATION",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Designated Learning Sanctuary (Always Whitelisted)",
                                    color = StrictEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StrictEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "STUDY PERMITTED",
                                color = StrictEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Even during the 30-minute lockout or full next-day restriction, all UGT video lectures, formula sheets, and study modules remain fully accessible for uninterrupted academic focus.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Subject Tabs
        item {
            TabRow(
                selectedTabIndex = selectedSubjectIndex,
                containerColor = SurfaceCard,
                contentColor = ShieldCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSubjectIndex]),
                        color = StrictEmerald
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                subjects.forEachIndexed { index, sub ->
                    Tab(
                        selected = selectedSubjectIndex == index,
                        onClick = {
                            selectedSubjectIndex = index
                            selectedModule = sub.modules.first()
                        },
                        text = {
                            Text(
                                text = sub.title.substringBefore(" &"),
                                color = if (selectedSubjectIndex == index) TextPrimary else TextMuted,
                                fontWeight = if (selectedSubjectIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
        }

        // Active Video Lecture Player Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ShieldCyan.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Column {
                    // Video Viewport Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF0F172A), DarkBg))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = ShieldCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedModule.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Faculty: ${currentSubject.instructor}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(StrictEmerald)
                                        .clickable { isVideoPlaying = !isVideoPlaying },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = DarkBg,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        // Playback Speed badge at top right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkBg.copy(alpha = 0.8f))
                                .clickable {
                                    playbackSpeed = when (playbackSpeed) {
                                        "1.0x" -> "1.25x"
                                        "1.25x" -> "1.5x"
                                        "1.5x" -> "2.0x"
                                        else -> "1.0x"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = playbackSpeed,
                                color = ShieldCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Scrubber Bar
                    Column(modifier = Modifier.padding(14.dp)) {
                        Slider(
                            value = videoProgress,
                            onValueChange = { videoProgress = it },
                            colors = SliderDefaults.colors(
                                thumbColor = StrictEmerald,
                                activeTrackColor = StrictEmerald,
                                inactiveTrackColor = SurfaceCardElevated
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val curMins = (videoProgress * 45).toInt()
                            Text(
                                text = "$curMins:15 / 45:00",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isVideoPlaying) "STREAMING ACTIVE" else "PAUSED",
                                color = if (isVideoPlaying) StrictEmerald else WarningAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Summary & Key Formulas Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCardElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = UgtBrandGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CORE FORMULA CHEAT-SHEET",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedModule.summary,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    selectedModule.keyFormulas.forEach { formula ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBg)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = formula,
                                color = ShieldCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = androidx.compose.runtime.rememberCoroutineScope()
                    var downloadedPath by remember(selectedModule.id) { mutableStateOf<String?>(null) }
                    Button(
                        onClick = {
                            scope.launch {
                                val res = com.example.service.StudyResourceDownloadManager.downloadResource(
                                    context = context,
                                    resource = com.example.service.StudyResourceDownloadManager.catalog.first(),
                                    onProgress = {}
                                )
                                downloadedPath = res.savedPath
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StrictEmerald,
                            contentColor = DarkBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("ugt_download_formula_button")
                    ) {
                        Text(
                            text = if (downloadedPath != null) "Downloaded to Device ($downloadedPath)" else "Download Chapter Formula & Notes Kit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Solved Problem of the Day
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = StrictEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SOLVED FACULTY DRILL",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedModule.solvedExample,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Student Scratchpad & Study Log
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "STUDY NOTES & SCRATCHPAD",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = scratchpadText,
                        onValueChange = { scratchpadText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StrictEmerald,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        placeholder = { Text("Write formulas, notes, questions for faculty...", color = TextMuted, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Available Modules in this Subject
        item {
            Text(
                text = "CHAPTER MODULES IN ${currentSubject.title.uppercase()}",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(currentSubject.modules) { mod ->
            val isCurrent = mod.id == selectedModule.id
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isCurrent) SurfaceCardElevated else SurfaceCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedModule = mod }
                    .border(
                        1.dp,
                        if (isCurrent) StrictEmerald else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
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
                            text = mod.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Duration: ${mod.duration}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isCurrent) StrictEmerald else TextMuted
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
