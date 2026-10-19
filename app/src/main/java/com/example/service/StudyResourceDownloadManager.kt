package com.example.service

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.CurriculumData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

data class DownloadableResource(
    val id: String,
    val title: String,
    val subtitle: String,
    val fileName: String,
    val category: String,
    val sizeLabel: String,
    val mimeType: String
)

data class DownloadResult(
    val success: Boolean,
    val savedPath: String,
    val fileName: String,
    val contentPreview: String
)

object StudyResourceDownloadManager {

    val catalog: List<DownloadableResource> = listOf(
        DownloadableResource(
            id = "dl_formula_book",
            title = "UGT Complete Formula & Concept Handbook",
            subtitle = "Mathematics, Physics, Chemistry & Biology quick-revision notes",
            fileName = "UGT_Complete_Formula_Handbook_2026.html",
            category = "Study Material",
            sizeLabel = "1.4 MB",
            mimeType = "text/html"
        ),
        DownloadableResource(
            id = "dl_question_bank",
            title = "Nightly Assessment Solved Question Bank",
            subtitle = "All mandatory 5-minute assessment questions with full explanations",
            fileName = "UGT_Solved_Assessment_Bank.txt",
            category = "Exam Prep",
            sizeLabel = "640 KB",
            mimeType = "text/plain"
        ),
        DownloadableResource(
            id = "dl_strict_profile",
            title = "21-Day Strict Mode Policy & App Whitelist",
            subtitle = "Exportable strict mode rules, browser locks & daily quota backup",
            fileName = "UGT_21Day_Strict_Policy.json",
            category = "Strict Profile",
            sizeLabel = "128 KB",
            mimeType = "application/json"
        ),
        DownloadableResource(
            id = "dl_python_guard",
            title = "Desktop & Mobile Screen Guard Companion",
            subtitle = "Standalone Python FastAPI + OCR screen monitor service script",
            fileName = "ugt_screen_guard_service.py",
            category = "System Guard",
            sizeLabel = "210 KB",
            mimeType = "text/x-python"
        ),
        DownloadableResource(
            id = "dl_timetable_csv",
            title = "21-Day Ultra-Strict Study Planner Sheet",
            subtitle = "Daily study blocks, Pomodoro targets & nightly assessment tracker",
            fileName = "UGT_21Day_Study_Planner.csv",
            category = "Planner",
            sizeLabel = "95 KB",
            mimeType = "text/csv"
        )
    )

    suspend fun downloadResource(
        context: Context,
        resource: DownloadableResource,
        onProgress: (Float) -> Unit
    ): DownloadResult = withContext(Dispatchers.IO) {
        onProgress(0.2f)
        delay(180)
        onProgress(0.55f)
        val content = buildContentForResource(resource.id)
        delay(200)
        onProgress(0.85f)

        val savedLocation = saveToDeviceDownloads(
            context = context,
            fileName = resource.fileName,
            mimeType = resource.mimeType,
            content = content
        )
        delay(120)
        onProgress(1.0f)

        DownloadResult(
            success = true,
            savedPath = savedLocation,
            fileName = resource.fileName,
            contentPreview = content
        )
    }

    fun shareDownloadedContent(context: Context, title: String, fileName: String, content: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$title ($fileName)")
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(sendIntent, "Save or Share $fileName").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(chooser) }
    }

    private fun saveToDeviceDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String
    ): String {
        // 1. Always write a copy to app-specific external Downloads folder (zero permissions needed)
        val appDownloadsDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir,
            "UGT_StudyGuard"
        ).apply { mkdirs() }
        val localFile = File(appDownloadsDir, fileName)
        runCatching { localFile.writeText(content) }

        // 2. On Android 10+ (API 29+), also write directly to public System Downloads via MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/UGT_StudyGuard")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        out.write(content.toByteArray(Charsets.UTF_8))
                    }
                    return "Downloads/UGT_StudyGuard/$fileName"
                }
            }
        }
        return "Downloads/UGT_StudyGuard/$fileName"
    }

    private fun buildContentForResource(resourceId: String): String {
        return when (resourceId) {
            "dl_formula_book" -> buildString {
                appendLine("<!DOCTYPE html><html><head><meta charset='utf-8'><title>UGT Complete Formula Handbook</title></head><body>")
                appendLine("<h1>UGT Tuition — Complete Formula & Concept Handbook (2026)</h1>")
                CurriculumData.ugtSubjects.forEach { subject ->
                    appendLine("<h2>${subject.title} (${subject.instructor})</h2>")
                    subject.modules.forEach { mod ->
                        appendLine("<h3>${mod.title} (${mod.duration})</h3>")
                        appendLine("<p>${mod.summary}</p><ul>")
                        mod.keyFormulas.forEach { f -> appendLine("<li><code>$f</code></li>") }
                        appendLine("</ul><p><b>Solved Example:</b> ${mod.solvedExample}</p><hr/>")
                    }
                }
                appendLine("</body></html>")
            }

            "dl_question_bank" -> buildString {
                appendLine("====================================================")
                appendLine("UGT STUDYGUARD — NIGHTLY ASSESSMENT SOLVED BANK")
                appendLine("====================================================")
                CurriculumData.assessmentQuestions.forEachIndexed { idx, q ->
                    appendLine("\nQ${idx + 1} [${q.subject}]: ${q.text}")
                    q.options.forEachIndexed { optIdx, opt ->
                        val marker = if (optIdx == q.correctIndex) "[CORRECT]" else "         "
                        appendLine("   ${('A' + optIdx)}. $opt $marker")
                    }
                    appendLine("   Explanation: ${q.explanation}")
                }
            }

            "dl_strict_profile" -> """
                {
                  "profileName": "UGT StudyGuard 21-Day Strict Commitment",
                  "commitmentDurationDays": 21,
                  "uninstallProtection": {
                    "enabled": true,
                    "unlockCondition": "COMPLETE_21_DAYS_AND_PASS_NIGHTLY_ASSESSMENTS",
                    "violationPenaltyMinutes": 30,
                    "allowedAppDuringLockout": "com.ugt.tuition.official"
                  },
                  "browserPolicy": {
                    "maxAllowedBrowsers": 2,
                    "allowedPackages": ["com.android.chrome", "com.brave.browser"],
                    "blockAdultContent": true,
                    "blockShortFormVideo": true
                  },
                  "appQuotas": {
                    "maxGames": 1,
                    "gameDailyLimitMinutes": 20,
                    "maxSocialApps": 2,
                    "socialDailyLimitMinutes": 30,
                    "autoDeleteSideloadedApks": true
                  }
                }
            """.trimIndent()

            "dl_python_guard" -> """
                #!/usr/bin/env python3
                \"\"\"
                UGT StudyGuard — Desktop & Mobile Screen Monitor Service
                Run with: uvicorn ugt_screen_guard_service:app --host 0.0.0.0 --port 8000
                \"\"\"
                from fastapi import FastAPI
                from pydantic import BaseModel
                import re

                app = FastAPI(title="UGT StudyGuard Screen Monitor", version="2.4.0")

                SHORT_FORM_PATTERNS = re.compile(r"(reels|shorts|tiktok|spotlight)", re.IGNORECASE)
                RESTRICTED_PATTERNS = re.compile(r"(porn|xxx|nsfw|casino|betting|gambling|\.apk|mod_menu)", re.IGNORECASE)

                class ScreenFrameEvent(BaseModel):
                    package_name: str
                    window_title: str
                    ocr_text: str

                @app.post("/api/v1/inspect-screen")
                def inspect_screen(event: ScreenFrameEvent):
                    combined = f"{event.package_name} {event.window_title} {event.ocr_text}"
                    if SHORT_FORM_PATTERNS.search(combined):
                        return {"allowed": False, "action": "LOCKOUT_30_MIN", "reason": "Short-form video feed blocked"}
                    if RESTRICTED_PATTERNS.search(combined):
                        return {"allowed": False, "action": "LOCKOUT_30_MIN", "reason": "Restricted content or sideload APK blocked"}
                    return {"allowed": True, "action": "PERMIT", "reason": "Verified study window"}
            """.trimIndent()

            else -> buildString {
                appendLine("Day,Target Focus Hours,Pomodoro Cycles,Nightly Assessment Status,Uninstall Progression")
                for (day in 1..21) {
                    val unlockStatus = if (day < 21) "Locked (${21 - day}d remaining)" else "Unlocked (21-Day Goal Met)"
                    appendLine("Day $day,4.0 Hours,8 Cycles,Mandatory 5-Min Quiz,$unlockStatus")
                }
            }
        }
    }
}
