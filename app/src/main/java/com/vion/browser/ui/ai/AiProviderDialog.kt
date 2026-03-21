/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.ai

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.vion.browser.R

class AiProviderDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = requireContext()
        val providers = AiProvider.values()
        val labels = providers.map { it.displayName }.toTypedArray()
        val current = AiPrefs.provider(ctx)
        val checkedItem = providers.indexOfFirst { it == current }.coerceAtLeast(0)

        return AlertDialog.Builder(ctx)
            .setTitle(R.string.ai_provider)
            .setSingleChoiceItems(labels, checkedItem) { dialog, which ->
                val chosen = providers[which]
                AiPrefs.setProvider(ctx, chosen)
                // Prompt for API key if empty
                val key = AiPrefs.apiKey(ctx, chosen)
                if (key.isBlank() && chosen != AiProvider.OLLAMA) {
                    dialog.dismiss()
                    showApiKeyDialog(chosen)
                } else {
                    dialog.dismiss()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun showApiKeyDialog(provider: AiProvider) {
        val ctx = requireContext()
        val input = EditText(ctx).apply {
            hint = "Paste your ${provider.displayName} API key"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        AlertDialog.Builder(ctx)
            .setTitle(provider.displayName)
            .setView(input)
            .setPositiveButton(R.string.create) { _, _ ->
                val key = input.text.toString().trim()
                if (key.isNotEmpty()) AiPrefs.setApiKey(ctx, provider, key)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
