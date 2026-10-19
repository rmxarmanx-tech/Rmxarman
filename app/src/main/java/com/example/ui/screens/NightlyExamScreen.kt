package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurriculumData
import com.example.model.Question
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
fun NightlyExamScreen(
    isExamInProgress: Boolean,
    remainingSeconds: Long,
    userAnswers: Map<Int, Int>,
    examResult: DisciplineViewModel.ExamResult?,
    onStartExam: () -> Unit,
    onSelectAnswer: (Int, Int) -> Unit,
    onSubmitExam: () -> Unit,
    onAbandonExam: () -> Unit,
    onOpenUgtPortal: () -> Unit
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timerFormatted = String.format("%02d:%02d", minutes, seconds)
    val questions = CurriculumData.assessmentQuestions

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
            .testTag("exam_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(WarningAmber.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "MANDATORY NIGHTLY EXAM",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Rule 7 Constraint: 5-Minute Study Assessment",
                                    color = WarningAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isExamInProgress) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AlarmCrimsonBg)
                                    .border(1.dp, AlarmCrimson, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = timerFormatted,
                                    color = AlarmCrimson,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    modifier = Modifier.testTag("exam_timer_display")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Every night, you must complete this 5-minute study assessment (Passing score: 60%). If you fail or abandon the test, device access is 100% restricted to the UGT Tuition App for the following day.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // State 1: Exam Not Started
        if (!isExamInProgress && examResult == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardElevated),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = ShieldCyan,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tonight's Assessment Ready",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "5 High-Yield Questions • 5 Minutes Duration • Physics, Math, Chemistry, Logic",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onStartExam,
                            colors = ButtonDefaults.buttonColors(containerColor = StrictEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_exam_button")
                        ) {
                            Text(
                                text = "START 5-MINUTE ASSESSMENT NOW",
                                color = DarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // State 2: Exam In Progress
        if (isExamInProgress) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUESTIONS (${userAnswers.size} / ${questions.size} Answered)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    OutlinedButton(
                        onClick = onAbandonExam,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp).testTag("abandon_exam_button")
                    ) {
                        Text("Abandon Test (Imposes Penalty)", color = AlarmCrimson, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(questions) { question ->
                QuestionCard(
                    question = question,
                    selectedOption = userAnswers[question.id],
                    onSelectOption = { onSelectAnswer(question.id, it) }
                )
            }

            item {
                Button(
                    onClick = onSubmitExam,
                    enabled = userAnswers.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_exam_button")
                ) {
                    Text(
                        text = "SUBMIT ASSESSMENT FOR EVALUATION",
                        color = DarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // State 3: Exam Results
        if (examResult != null) {
            item {
                val res = examResult
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.passed) SurfaceCard else AlarmCrimsonBg
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (res.passed) StrictEmerald else AlarmCrimson,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (res.passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (res.passed) StrictEmerald else AlarmCrimson,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (res.passed) "ASSESSMENT PASSED" else "ASSESSMENT FAILED",
                            color = if (res.passed) StrictEmerald else AlarmCrimson,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Score: ${res.scorePercent}% (${res.correctCount} of ${res.totalQuestions} Correct)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = res.message,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!res.passed) {
                            Button(
                                onClick = onOpenUgtPortal,
                                colors = ButtonDefaults.buttonColors(containerColor = StrictEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = DarkBg)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("STUDY ON UGT TUITION APP NOW", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onStartExam,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text("Retake Practice Assessment", color = ShieldCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "DETAILED SOLUTION EXPLANATIONS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(questions) { q ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Q${q.id} • ${q.subject}",
                                color = ShieldCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val userAnswer = userAnswers[q.id]
                            val isCorrect = userAnswer == q.correctIndex
                            Text(
                                text = if (isCorrect) "CORRECT" else "INCORRECT",
                                color = if (isCorrect) StrictEmerald else AlarmCrimson,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = q.text, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Correct Answer: ${q.options[q.correctIndex]}",
                            color = StrictEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Explanation: ${q.explanation}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
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
fun QuestionCard(
    question: Question,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Subject: ${question.subject}",
                color = ShieldCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = question.text,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                question.options.forEachIndexed { index, optionText ->
                    val isSelected = selectedOption == index
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceCardElevated else DarkBg)
                            .border(
                                1.dp,
                                if (isSelected) ShieldCyan else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectOption(index) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectOption(index) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ShieldCyan,
                                unselectedColor = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = optionText,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
