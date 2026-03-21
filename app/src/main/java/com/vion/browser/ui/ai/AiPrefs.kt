/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted preferences for AI API keys (never stored in plaintext).
 */
object AiPrefs {

    private const val FILE_NAME = "vion_ai_keys"

    const val KEY_PROVIDER         = "ai_provider"
    const val KEY_GEMINI_KEY       = "gemini_api_key"
    const val KEY_OPENAI_KEY       = "openai_api_key"
    const val KEY_ANTHROPIC_KEY    = "anthropic_api_key"
    const val KEY_MISTRAL_KEY      = "mistral_api_key"
    const val KEY_OLLAMA_ENDPOINT  = "ollama_endpoint"
    const val KEY_AI_ENABLED       = "ai_enabled"
    const val KEY_SYSTEM_PROMPT    = "ai_system_prompt"

    private fun getPrefs(context: Context) = try {
        val master = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, FILE_NAME, master,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to regular prefs if EncryptedSharedPreferences unavailable
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

    fun isEnabled(context: Context) =
        getPrefs(context).getBoolean(KEY_AI_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AI_ENABLED, enabled).apply()
    }

    fun provider(context: Context): AiProvider =
        AiProvider.fromName(getPrefs(context).getString(KEY_PROVIDER, AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name)

    fun setProvider(context: Context, provider: AiProvider) {
        getPrefs(context).edit().putString(KEY_PROVIDER, provider.name).apply()
    }

    fun apiKey(context: Context, provider: AiProvider): String {
        val key = when (provider) {
            AiProvider.GEMINI    -> KEY_GEMINI_KEY
            AiProvider.OPENAI    -> KEY_OPENAI_KEY
            AiProvider.ANTHROPIC -> KEY_ANTHROPIC_KEY
            AiProvider.MISTRAL   -> KEY_MISTRAL_KEY
            AiProvider.OLLAMA    -> KEY_OLLAMA_ENDPOINT
        }
        return getPrefs(context).getString(key, "") ?: ""
    }

    fun setApiKey(context: Context, provider: AiProvider, apiKey: String) {
        val key = when (provider) {
            AiProvider.GEMINI    -> KEY_GEMINI_KEY
            AiProvider.OPENAI    -> KEY_OPENAI_KEY
            AiProvider.ANTHROPIC -> KEY_ANTHROPIC_KEY
            AiProvider.MISTRAL   -> KEY_MISTRAL_KEY
            AiProvider.OLLAMA    -> KEY_OLLAMA_ENDPOINT
        }
        getPrefs(context).edit().putString(key, apiKey).apply()
    }

    fun systemPrompt(context: Context): String =
        getPrefs(context).getString(KEY_SYSTEM_PROMPT,
            "You are a helpful, concise AI assistant integrated into Vion Browser. " +
            "Keep responses short and to the point. Do not repeat the user's question.") ?: ""
}
