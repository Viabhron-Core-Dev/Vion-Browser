/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.scripts

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.vion.browser.R
import com.vion.browser.databinding.DialogEditUserScriptBinding
import com.vion.browser.extension.UserScript

/**
 * Add / Edit a user script.
 *
 * Pass an existing [UserScript] via [ARG_SCRIPT_ID] to edit it;
 * omit the argument to create a new one.
 */
class EditUserScriptDialog : DialogFragment() {

    private val vm: UserScriptViewModel by activityViewModels()
    private var _b: DialogEditUserScriptBinding? = null
    private val b get() = _b!!

    /** The script being edited, null when creating new. */
    private var existing: UserScript? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _b = DialogEditUserScriptBinding.inflate(layoutInflater)

        // Populate runAt spinner
        val runAtOptions = arrayOf("document_end", "document_start")
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, runAtOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spinnerRunAt.adapter = adapter

        // Pre-fill if editing
        val scriptId = arguments?.getLong(ARG_SCRIPT_ID, -1L) ?: -1L
        if (scriptId >= 0) {
            vm.scripts.value?.find { it.id == scriptId }?.let { s ->
                existing = s
                b.editScriptName.setText(s.name)
                b.editScriptDescription.setText(s.description)
                b.editMatchPattern.setText(s.matchPattern)
                b.editScriptCode.setText(s.code)
                val idx = runAtOptions.indexOf(s.runAt).takeIf { it >= 0 } ?: 0
                b.spinnerRunAt.setSelection(idx)
            }
        }

        val title = if (existing != null) R.string.edit_script else R.string.add_script

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(b.root)
            .setPositiveButton(R.string.save) { _, _ -> save(runAtOptions) }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun save(runAtOptions: Array<String>) {
        val name    = b.editScriptName.text.toString().trim()
        val desc    = b.editScriptDescription.text.toString().trim()
        val pattern = b.editMatchPattern.text.toString().trim().ifBlank { "*://*/*" }
        val code    = b.editScriptCode.text.toString()
        val runAt   = runAtOptions[b.spinnerRunAt.selectedItemPosition]

        if (name.isBlank() || code.isBlank()) return

        val script = existing?.copy(
            name = name, description = desc,
            matchPattern = pattern, code = code, runAt = runAt
        ) ?: UserScript(
            name = name, description = desc,
            matchPattern = pattern, code = code, runAt = runAt
        )

        if (existing != null) vm.update(script) else vm.insert(script)
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    companion object {
        const val ARG_SCRIPT_ID = "script_id"

        fun newInstance(scriptId: Long? = null) = EditUserScriptDialog().apply {
            if (scriptId != null) arguments = Bundle().apply {
                putLong(ARG_SCRIPT_ID, scriptId)
            }
        }
    }
}
