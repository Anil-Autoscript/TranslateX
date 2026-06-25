package com.translatex.ai.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Request to trigger GitHub Actions workflow ──────────────────────────────

/**
 * Wrapper required by the GitHub Actions "workflow_dispatch" API.
 */
data class WorkflowDispatchRequest(
    @SerializedName("ref")
    val ref: String = "main",

    @SerializedName("inputs")
    val inputs: TranslationInputs
)

/**
 * The inputs forwarded to the workflow as environment variables.
 */
data class TranslationInputs(
    @SerializedName("text")
    val text: String,

    @SerializedName("source")
    val source: String,

    @SerializedName("target")
    val target: String,

    /** Unique run ID so the app can poll for this specific run. */
    @SerializedName("request_id")
    val requestId: String
)

// ── GitHub API response shapes ──────────────────────────────────────────────

data class WorkflowRunsResponse(
    @SerializedName("workflow_runs")
    val workflowRuns: List<WorkflowRun>
)

data class WorkflowRun(
    @SerializedName("id")
    val id: Long,

    @SerializedName("status")
    val status: String,          // queued | in_progress | completed

    @SerializedName("conclusion")
    val conclusion: String?,     // success | failure | null

    @SerializedName("name")
    val name: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("jobs_url")
    val jobsUrl: String
)

data class JobsResponse(
    @SerializedName("jobs")
    val jobs: List<Job>
)

data class Job(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("steps")
    val steps: List<Step>
)

data class Step(
    @SerializedName("name")
    val name: String,

    @SerializedName("conclusion")
    val conclusion: String?
)

// ── Artifact / translation result ──────────────────────────────────────────

data class ArtifactsResponse(
    @SerializedName("artifacts")
    val artifacts: List<Artifact>
)

data class Artifact(
    @SerializedName("id")
    val id: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("archive_download_url")
    val archiveDownloadUrl: String,

    @SerializedName("expired")
    val expired: Boolean
)

/**
 * The JSON payload written by the Python script and uploaded as a
 * GitHub Actions artifact.
 */
data class TranslationResultDto(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("translatedText")
    val translatedText: String?,

    @SerializedName("error")
    val error: String?
)
