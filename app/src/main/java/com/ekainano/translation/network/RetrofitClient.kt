package com.ekainano.translation.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class PropertySchema(val type: String, val description: String? = null)

@JsonClass(generateAdapter = true)
data class ResponseSchema(val type: String, val properties: Map<String, PropertySchema>, val required: List<String>)

@JsonClass(generateAdapter = true)
data class GenerationConfig(val responseMimeType: String, val temperature: Float, val responseSchema: ResponseSchema)

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class TextResponse(val text: String)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<Candidate>?)

@JsonClass(generateAdapter = true)
data class ParsedTranslationResponse(
    val translation: String,
    val breakdown: String?,
    val cultural: String?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val translationAdapter = moshi.adapter(ParsedTranslationResponse::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterMoshiFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}

// Inline extension function helper for adding moshi factory syntax easily
private fun Retrofit.Builder.addConverterMoshiFactory(moshi: Moshi): Retrofit.Builder {
    return this.addConverterFactory(MoshiConverterFactory.create(moshi))
}