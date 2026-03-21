/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiMessage(val role: String, val content: String)   // role: "user" | "assistant"

class AiViewModel(application: Application) : AndroidViewModel(application) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _messages = MutableLiveData<List<AiMessage>>(emptyList())
    val messages: LiveData<List<AiMessage>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _windowState = MutableLiveData(AiWindowState.DOT)
    val windowState: LiveData<AiWindowState> = _windowState

    /** Current page context injected automatically */
    var pageTitle: String = ""
    var pageUrl: String = ""

    fun setWindowState(state: AiWindowState) { _windowState.value = state }

    fun clearConversation() { _messages.value = emptyList() }

    fun sendMessage(userText: String, context: android.content.Context) {
        val current = _messages.value?.toMutableList() ?: mutableListOf()
        current.add(AiMessage("user", userText))
        _messages.value = current
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = runCatching {
                val provider = AiPrefs.provider(context)
                val apiKey   = AiPrefs.apiKey(context, provider)
                val system   = AiPrefs.systemPrompt(context)
                val pageCtx  = if (pageUrl.isNotBlank())
                    "\n\nCurrent page: $pageTitle ($pageUrl)" else ""

                when (provider) {
                    AiProvider.GEMINI    -> callGemini(apiKey, system + pageCtx, current)
                    AiProvider.OPENAI,
                    AiProvider.MISTRAL,
                    AiProvider.OLLAMA    -> callOpenAiCompat(
                        provider.apiBaseUrl, apiKey, system + pageCtx, current,
                        modelFor(provider))
                    AiProvider.ANTHROPIC -> callAnthropic(apiKey, system + pageCtx, current)
                }
            }

            withContext(Dispatchers.Main) {
                _isLoading.value = false
                result.onSuccess { reply ->
                    val updated = _messages.value?.toMutableList() ?: mutableListOf()
                    updated.add(AiMessage("assistant", reply))
                    _messages.value = updated
                }
                result.onFailure { e ->
                    _error.value = e.message ?: "Unknown error"
                }
            }
        }
    }

    // ── Provider implementations ─────────────────────────────────────────

    private suspend fun callGemini(
        apiKey: String,
        system: String,
        history: List<AiMessage>
    ): String = withContext(Dispatchers.IO) {
        val model = "gemini-2.0-flash"
        val url   = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val contents = JSONArray().apply {
            // Gemini uses "user" / "model" roles
            history.forEach { msg ->
                put(JSONObject().apply {
                    put("role", if (msg.role == "assistant") "model" else "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.content) })
                    })
                })
            }
        }

        val body = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", system) })
                })
            })
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1024)
            })
        }.toString()

        val req = Request.Builder().url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        val resp = http.newCall(req).execute()
        val json = JSONObject(resp.body?.string() ?: throw Exception("Empty response"))
        if (!resp.isSuccessful) {
            val err = json.optJSONObject("error")?.optString("message") ?: "HTTP ${resp.code}"
            throw Exception(err)
        }
        json.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

    private suspend fun callOpenAiCompat(
        baseUrl: String,
        apiKey: String,
        system: String,
        history: List<AiMessage>,
        model: String
    ): String = withContext(Dispatchers.IO) {
        val messages = JSONArray().apply {
            put(JSONObject().apply { put("role", "system"); put("content", system) })
            history.forEach { msg ->
                put(JSONObject().apply { put("role", msg.role); put("content", msg.content) })
            }
        }
        val body = JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("max_tokens", 1024)
            put("temperature", 0.7)
        }.toString()

        val req = Request.Builder().url("$baseUrl/chat/completions")
            .post(body.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        val resp = http.newCall(req).execute()
        val json = JSONObject(resp.body?.string() ?: throw Exception("Empty response"))
        if (!resp.isSuccessful) {
            val err = json.optJSONObject("error")?.optString("message") ?: "HTTP ${resp.code}"
            throw Exception(err)
        }
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private suspend fun callAnthropic(
        apiKey: String,
        system: String,
        history: List<AiMessage>
    ): String = withContext(Dispatchers.IO) {
        val messages = JSONArray().apply {
            history.forEach { msg ->
                put(JSONObject().apply { put("role", msg.role); put("content", msg.content) })
            }
        }
        val body = JSONObject().apply {
            put("model", "claude-3-5-haiku-20241022")
            put("max_tokens", 1024)
            put("system", system)
            put("messages", messages)
        }.toString()

        val req = Request.Builder().url("https://api.anthropic.com/v1/messages")
            .post(body.toRequestBody("application/json".toMediaType()))
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .build()
        val resp = http.newCall(req).execute()
        val json = JSONObject(resp.body?.string() ?: throw Exception("Empty response"))
        if (!resp.isSuccessful) {
            val err = json.optJSONObject("error")?.optString("message") ?: "HTTP ${resp.code}"
            throw Exception(err)
        }
        json.getJSONArray("content").getJSONObject(0).getString("text")
    }

    private fun modelFor(provider: AiProvider) = when (provider) {
        AiProvider.OPENAI    -> "gpt-4o-mini"
        AiProvider.MISTRAL   -> "mistral-small-latest"
        AiProvider.OLLAMA    -> "llama3"
        else                 -> "gpt-4o-mini"
    }
}
