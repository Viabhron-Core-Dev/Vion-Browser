/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.home

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.vion.browser.R

class AddShortcutDialog : DialogFragment() {

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.dialog_add_shortcut, null)
        val labelEdit = view.findViewById<EditText>(R.id.shortcut_label_input)
        val urlEdit = view.findViewById<EditText>(R.id.shortcut_url_input)

        // Pre-fill URL if passed as argument
        arguments?.getString(ARG_URL)?.let { urlEdit.setText(it) }
        arguments?.getString(ARG_LABEL)?.let { labelEdit.setText(it) }

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.add_shortcut)
            .setView(view)
            .setPositiveButton(R.string.create) { _, _ ->
                val label = labelEdit.text.toString().trim()
                val url = urlEdit.text.toString().trim()
                if (label.isNotEmpty() && url.isNotEmpty()) {
                    viewModel.addShortcut(label, url)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    companion object {
        const val ARG_URL = "url"
        const val ARG_LABEL = "label"

        fun newInstance(url: String, label: String) = AddShortcutDialog().apply {
            arguments = Bundle().also {
                it.putString(ARG_URL, url)
                it.putString(ARG_LABEL, label)
            }
        }
    }
}
