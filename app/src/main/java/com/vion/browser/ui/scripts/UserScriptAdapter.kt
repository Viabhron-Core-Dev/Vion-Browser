/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.scripts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vion.browser.databinding.ItemUserScriptBinding
import com.vion.browser.extension.UserScript

class UserScriptAdapter(
    private val onToggle: (UserScript) -> Unit,
    private val onEdit:   (UserScript) -> Unit,
    private val onDelete: (UserScript) -> Unit,
) : ListAdapter<UserScript, UserScriptAdapter.VH>(DIFF) {

    inner class VH(val b: ItemUserScriptBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemUserScriptBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val script = getItem(position)
        with(holder.b) {
            scriptName.text        = script.name
            scriptDescription.text = script.description.ifBlank { script.matchPattern }
            scriptRunAt.text       = script.runAt
            scriptToggle.isChecked = script.enabled

            scriptToggle.setOnCheckedChangeListener(null)
            scriptToggle.setOnCheckedChangeListener { _, _ -> onToggle(script) }

            btnEditScript.setOnClickListener  { onEdit(script) }
            btnDeleteScript.setOnClickListener { onDelete(script) }

            // Dim the card when disabled
            root.alpha = if (script.enabled) 1f else 0.5f
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<UserScript>() {
            override fun areItemsTheSame(a: UserScript, b: UserScript) = a.id == b.id
            override fun areContentsTheSame(a: UserScript, b: UserScript) = a == b
        }
    }
}
