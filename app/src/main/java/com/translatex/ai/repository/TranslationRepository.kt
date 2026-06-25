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
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TranslationRepository"

/**
 * Orchestrates the full GitHub-Actions translation flow:
 *  1. Dispatch workflow_dispatch event with translation inputs.
 *  2. Poll workflow runs until our run appears and completes.
 *  3. Download the result artifact (a ZIP containing result.json).
 *  4. Parse and return [TranslationResult].
 *  5. Persist to Room.
 */
@Singleton
class TranslationRepository @Inject constructor(
    private val api: GitHubApiService,
    private val dao: TranslationDao
) {

    private val owner    = BuildConfig.GITHUB_OWNER
    private val repo     = BuildConfig.GITHUB_REPO
    private val workflow = BuildConfig.WORKFLOW_FILE

    // ── Public API ───────────────────────────────────────────────────────

    suspend fun translate(
        text: String,
        sourceCode: String,
        targetCode: String
    ): TranslationResult {
        if (text.isBlank()) return TranslationResult.Error("Input text is empty.")

        val requestId = UUID.randomUUID().toString().take(8)

        return try {
            // 1. Trigger the workflow
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
                return TranslationResult.Error(
                    "Failed to start translation workflow (${dispatchResponse.code()})."
                )
            }

            // 2. Wait briefly for GitHub to register the run
            delay(3_000)

            // 3. Poll until completed (max ~2 minutes)
            val runId = pollForRunId(requestId) ?: return TranslationResult.Error(
                "Translation timed out. Please try again."
            )

            // 4. Download and unzip the artifact
            val json = downloadResultJson(runId) ?: return TranslationResult.Error(
                "Could not read translation result."
            )

            // 5. Parse
            val dto = Gson().fromJson(json, TranslationResultDto::class.java)
            if (!dto.success || dto.translatedText == null) {
                return TranslationResult.Error(dto.error ?: "Translation failed.")
            }

            // 6. Persist to Room
            dao.insert(
                TranslationEntity(
                    sourceText          = text,
                    translatedText      = dto.translatedText,
                    sourceLanguageCode  = sourceCode,
                    targetLanguageCode  = targetCode
                )
            )

            TranslationResult.Success(dto.translatedText)

        } catch (e: Exception) {
            Log.e(TAG, "translate() threw", e)
            TranslationResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    fun getHistory(): Flow<List<TranslationEntity>> = dao.getAllHistory()

    fun getFavorites(): Flow<List<TranslationEntity>> = dao.getFavorites()

    fun searchHistory(query: String): Flow<List<TranslationEntity>> =
        dao.searchHistory(query)

    suspend fun toggleFavorite(entity: TranslationEntity) =
        dao.setFavorite(entity.id, !entity.isFavorite)

    suspend fun deleteTranslation(entity: TranslationEntity) =
        dao.delete(entity)

    suspend fun clearHistory() = dao.clearNonFavorites()

    suspend fun clearAll() = dao.clearAll()

    // ── Private helpers ──────────────────────────────────────────────────

    /**
     * Poll GitHub every 5 s for up to 24 attempts (≈ 2 min) looking for a
     * completed run whose name contains [requestId].
     */
    private suspend fun pollForRunId(requestId: String): Long? {
        repeat(24) { attempt ->
            delay(if (attempt == 0) 0L else 5_000L)

            runCatching {
                val runs = api.getWorkflowRuns(owner, repo, workflow, perPage = 10)
                val match = runs.workflowRuns.find { run ->
                    (run.name.contains(requestId) || run.status != "queued") &&
                    run.conclusion != null
                }
                if (match != null) {
                    Log.d(TAG, "Found run ${match.id} for $requestId (attempt $attempt)")
                    return match.id
                }
            }.onFailure { Log.w(TAG, "Poll attempt $attempt failed", it) }
        }
        return null
    }

    /**
     * Fetches the first artifact from [runId], downloads its ZIP, and
     * extracts the first JSON file found inside it.
     */
    private suspend fun downloadResultJson(runId: Long): String? {
        val artifacts = api.getRunArtifacts(owner, repo, runId)
        val artifact  = artifacts.artifacts.firstOrNull() ?: return null

        val response = api.downloadArtifact(artifact.archiveDownloadUrl)
        if (!response.isSuccessful) return null

        val bytes = response.body()?.bytes() ?: return null
        return ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.endsWith(".json")) {
                    return@use zip.bufferedReader().use(BufferedReader::readText)
                }
                entry = zip.nextEntry
            }
            null
        }
    }
}
