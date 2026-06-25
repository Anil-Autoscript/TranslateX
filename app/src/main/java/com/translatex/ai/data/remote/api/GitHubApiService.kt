package com.translatex.ai.data.remote.api

import com.translatex.ai.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit interface for all GitHub REST API calls used by TranslateX.
 *
 * Base URL: https://api.github.com/
 */
interface GitHubApiService {

    /**
     * Triggers the translate.yml workflow via workflow_dispatch.
     */
    @POST("repos/{owner}/{repo}/actions/workflows/{workflow}/dispatches")
    suspend fun triggerWorkflow(
        @Path("owner")    owner: String,
        @Path("repo")     repo: String,
        @Path("workflow") workflow: String,
        @Body            body: WorkflowDispatchRequest
    ): Response<Unit>

    /**
     * Lists recent workflow runs so we can find the one we just triggered.
     */
    @GET("repos/{owner}/{repo}/actions/workflows/{workflow}/runs")
    suspend fun getWorkflowRuns(
        @Path("owner")    owner: String,
        @Path("repo")     repo: String,
        @Path("workflow") workflow: String,
        @Query("per_page") perPage: Int = 5
    ): WorkflowRunsResponse

    /**
     * Fetches artifacts produced by a specific run.
     */
    @GET("repos/{owner}/{repo}/actions/runs/{run_id}/artifacts")
    suspend fun getRunArtifacts(
        @Path("owner")  owner: String,
        @Path("repo")   repo: String,
        @Path("run_id") runId: Long
    ): ArtifactsResponse

    /**
     * Downloads an artifact ZIP.  The URL is the full archive_download_url
     * returned by [getRunArtifacts], so we use @Url here.
     */
    @Streaming
    @GET
    suspend fun downloadArtifact(
        @Url url: String
    ): Response<ResponseBody>
}
