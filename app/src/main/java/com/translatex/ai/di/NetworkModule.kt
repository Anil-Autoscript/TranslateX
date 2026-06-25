package com.translatex.ai.di

import com.translatex.ai.BuildConfig
import com.translatex.ai.data.remote.api.GitHubApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GITHUB_BASE_URL = "https://api.github.com/"

    /** Attaches PAT + required GitHub API headers to every request. */
    private fun authInterceptor() = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${BuildConfig.GITHUB_TOKEN}")
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .build()
        chain.proceed(req)
    }

    /**
     * Single OkHttpClient shared by both Retrofit and the manual
     * artifact-download call in [TranslationRepository].
     *
     * followRedirects = true  ← GitHub artifact URLs return a 302 to S3;
     * OkHttp follows it automatically but strips the Authorization header
     * on cross-host redirects.  We keep it true and handle auth in the
     * repository's manual request builder.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BASIC
            else
                HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor())
            .addInterceptor(logging)
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)   // artifact download can be slow
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(GITHUB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGitHubApiService(retrofit: Retrofit): GitHubApiService =
        retrofit.create(GitHubApiService::class.java)
}
