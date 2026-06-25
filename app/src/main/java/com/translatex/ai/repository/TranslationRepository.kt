package com.translatex.ai.repository

import android.util.Log
import com.google.gson.Gson
import com.translatex.ai.BuildConfig
import com.translatex.ai.data.local.dao.TranslationDao
import com.translatex.ai.data.local.entity.TranslationEntity
import com.translatex.ai.data.remote.api.GitHubApiService
import com.translatex.ai.data.remote.dto.TranslationInputs
import com.translatex.ai.data.remote.dto.TranslationResultDto
import com.translatex.ai.data.remote.dto.WorkflowDispatchRequest
import com.translatex.ai.model.TranslationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TranslationRepository"

/**
 * Orchestrates the full GitHub-Actions translation flow:
 *
 *  1. Record dispatch timestamp.
 *  2. Trigger workflow_dispatch with a unique requestId embedded in inputs.
 *  3. Poll runs created AFTER dispatch timestamp until one is completed.
 *  4. Locate the artifact named "translation-result-{requestId}".
 *  5. Download the ZIP artifact, extract result.json, parse the JSON.
 *  6. Persist to Room and return [TranslationResult].
 *
 * Key fix: we match the artifact by its exact name
 * ("translation-result-{requestId}") rather than by run name, which
 * is far more reliable across GitHub API pagination.
 */
@Singleton
class TranslationRepository @Inject constructor(
    private val api:        GitHubApiService,
    private val dao:        TranslationDao,
    private val okhttp:     OkHttpClient          // injected from NetworkModule
) {

    private val owner    = BuildConfig.GITHUB_OWNER
    private val repo     = BuildConfig.GITHUB_REPO
    private val workflow = BuildConfig.WORKFLOW_FILE

    // ── Public API ────────────────────────────────────────────────────────

    suspend fun translate(
        text:       String,
        sourceCode: String,
        targetCode: String
    ): TranslationResult {
        if (text.isBlank()) return TranslationResult.Error("Input text is empty.")

        // Short random ID embedded in artifact name so we can identify our run
        val requestId       = UUID.randomUUID().toString().take(8)
        val dispatchTimeMs  = System.currentTimeMillis()

        return try {
            // ── 1. Trigger workflow ──────────────────────────────────────
            val dispatchResponse = api.triggerWorkflow(
                owner    = owner,
                repo     = repo,
                workflow = workflow,
                body     = WorkflowDispatchRequest(
                    inputs = TranslationInputs(
                        text      = text,
                        source    = sourceCode,
                        target    = targetCode,
                        requestId = requestId
                    )
                )
            )

            if (!dispatchResponse.isSuccessful) {
                val code = dispatchResponse.code()
                val msg  = dispatchResponse.errorBody()?.string() ?: "no body"
                Log.e(TAG, "Dispatch failed $code: $msg")
                return TranslationResult.Error(
                    "Failed to start translation (HTTP $code). Check your GitHub token & repo settings."
                )
            }
            Log.d(TAG, "Workflow dispatched. requestId=$requestId")

            // ── 2. Wait for GitHub to register the run (~2-4 s) ─────────
            delay(5_000)

            // ── 3. Poll for a completed run created after dispatch ───────
            val runId = pollForCompletedRun(dispatchTimeMs)
                ?: return TranslationResult.Error(
                    "Translation timed out. The workflow may still be queued — try again."
                )
            Log.d(TAG, "Run $runId completed.")

            // ── 4. Find our specific artifact by name ────────────────────
            val artifactName = "translation-result-$requestId"
            val artifactUrl  = findArtifactUrl(runId, artifactName)
                ?: return TranslationResult.Error(
                    "Result artifact not found. Workflow may have failed."
                )

            // ── 5. Download ZIP and extract result.json ──────────────────
            val json = downloadAndExtractJson(artifactUrl)
                ?: return TranslationResult.Error("Could not read translation result from artifact.")

            // ── 6. Parse ─────────────────────────────────────────────────
            val dto = Gson().fromJson(json, TranslationResultDto::class.java)
            if (!dto.success || dto.translatedText == null) {
                return TranslationResult.Error(dto.error ?: "Translation script returned an error.")
            }

            // ── 7. Persist ───────────────────────────────────────────────
            dao.insert(
                TranslationEntity(
                    sourceText         = text,
                    translatedText     = dto.translatedText,
                    sourceLanguageCode = sourceCode,
                    targetLanguageCode = targetCode
                )
            )

            TranslationResult.Success(dto.translatedText)

        } catch (e: Exception) {
            Log.e(TAG, "translate() threw", e)
            TranslationResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    // ── Room helpers ──────────────────────────────────────────────────────

    fun getHistory(): Flow<List<TranslationEntity>>            = dao.getAllHistory()
    fun getFavorites(): Flow<List<TranslationEntity>>          = dao.getFavorites()
    fun searchHistory(q: String): Flow<List<TranslationEntity>> = dao.searchHistory(q)

    suspend fun toggleFavorite(entity: TranslationEntity)     = dao.setFavorite(entity.id, !entity.isFavorite)
    suspend fun deleteTranslation(entity: TranslationEntity)  = dao.delete(entity)
    suspend fun clearHistory()                                 = dao.clearNonFavorites()
    suspend fun clearAll()                                     = dao.clearAll()

    // ── Private: polling ─────────────────────────────────────────────────

    /**
     * Poll up to 30 times (every 6 s = ~3 min max) for a workflow run
     * that was created AFTER [dispatchTimeMs] and has reached a terminal
     * conclusion (success | failure | cancelled …).
     *
     * We identify our run by dispatch time — the most recent run after our
     * dispatch timestamp is ours.
     */
    private suspend fun pollForCompletedRun(dispatchTimeMs: Long): Long? {
        // ISO-8601 string we can compare lexicographically
        val dispatchIso = isoFromMillis(dispatchTimeMs - 10_000) // 10 s grace

        repeat(30) { attempt ->
            if (attempt > 0) delay(6_000)

            runCatching {
                val response = api.getWorkflowRuns(owner, repo, workflow, perPage = 10)
                // Find the newest run created at or after dispatch
                val match = response.workflowRuns
                    .filter { it.createdAt >= dispatchIso }
                    .firstOrNull { it.conclusion != null }  // terminal state

                if (match != null) {
                    Log.d(TAG, "Matched run id=${match.id} conclusion=${match.conclusion} (attempt $attempt)")
                    return match.id
                }

                Log.d(TAG, "Attempt $attempt: no completed run yet (runs seen: ${response.workflowRuns.size})")
            }.onFailure {
                Log.w(TAG, "Poll attempt $attempt threw: ${it.message}")
            }
        }
        return null
    }

    /**
     * Search the artifacts for a run and return the archive_download_url
     * of the one whose name matches [artifactName].
     */
    private suspend fun findArtifactUrl(runId: Long, artifactName: String): String? {
        runCatching {
            val response = api.getRunArtifacts(owner, repo, runId)
            Log.d(TAG, "Artifacts for run $runId: ${response.artifacts.map { it.name }}")
            return response.artifacts
                .firstOrNull { it.name == artifactName && !it.expired }
                ?.archiveDownloadUrl
        }.onFailure {
            Log.e(TAG, "findArtifactUrl threw: ${it.message}")
        }
        return null
    }

    /**
     * Download the artifact ZIP from GitHub (which requires following a
     * redirect) using OkHttp directly so we can handle the 302 manually,
     * then extract the first *.json file from the ZIP.
     *
     * Retrofit strips the Authorization header on redirects to different
     * hosts, so we bypass Retrofit here and use OkHttp directly.
     */
    private fun downloadAndExtractJson(archiveDownloadUrl: String): String? {
        return try {
            val request = Request.Builder()
                .url(archiveDownloadUrl)
                .addHeader("Authorization", "Bearer ${BuildConfig.GITHUB_TOKEN}")
                .addHeader("Accept", "application/vnd.github+json")
                .build()

            okhttp.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Artifact download failed: ${response.code}")
                    return null
                }
                val bytes = response.body?.bytes() ?: return null
                ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (entry.name.endsWith(".json")) {
                            return@use zip.bufferedReader().use(BufferedReader::readText)
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "downloadAndExtractJson threw: ${e.message}")
            null
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────

    /** Convert epoch millis to an ISO-8601 string comparable to GitHub's created_at. */
    private fun isoFromMillis(ms: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(ms))
    }
}
