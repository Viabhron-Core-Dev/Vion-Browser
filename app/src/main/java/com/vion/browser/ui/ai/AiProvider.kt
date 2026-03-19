/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.ai

/**
 * Supported AI providers.
 * API keys are stored in EncryptedSharedPreferences (never plain prefs).
 */
enum class AiProvider(val displayName: String, val apiBaseUrl: String) {
    GEMINI(
        displayName = "Google Gemini",
        apiBaseUrl  = "https://generativelanguage.googleapis.com/v1beta"
    ),
    OPENAI(
        displayName = "OpenAI ChatGPT",
        apiBaseUrl  = "https://api.openai.com/v1"
    ),
    ANTHROPIC(
        displayName = "Anthropic Claude",
        apiBaseUrl  = "https://api.anthropic.com/v1"
    ),
    MISTRAL(
        displayName = "Mistral AI",
        apiBaseUrl  = "https://api.mistral.ai/v1"
    ),
    OLLAMA(
        displayName = "Ollama (local)",
        apiBaseUrl  = "http://localhost:11434/v1"
    );

    companion object {
        val DEFAULT = GEMINI
        fun fromName(name: String) = values().firstOrNull { it.name == name } ?: GEMINI
    }
}
