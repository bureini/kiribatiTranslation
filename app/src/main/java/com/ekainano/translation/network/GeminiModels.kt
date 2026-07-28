package com.ekainano.translation.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PropertySchema(
    val type: String, 
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    val type: String, 
    val properties: Map<String, PropertySchema>, 
    val required: List<String>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String, 
    val temperature: Float, 
    val responseSchema: ResponseSchema
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class TextResponse(
    val text: String
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class ParsedTranslationResponse(
    val translation: String,
    val breakdown: String?,
    val cultural: String?
)
