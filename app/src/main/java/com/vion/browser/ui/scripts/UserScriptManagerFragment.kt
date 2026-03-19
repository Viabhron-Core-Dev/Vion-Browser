/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.scripts

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vion.browser.R
import com.vion.browser.databinding.FragmentUserScriptManagerBinding
import com.vion.browser.extension.UserScript

/**
 * Phase 13 — UserScript Manager.
 *
 * Full-screen fragment (launched from Settings or floating menu) that lists
 * all installed user scripts and allows add / edit / toggle / delete.
 *
 * Hosted inside a BottomSheetDialog (full-height) or a standard Activity.
 */
class UserScriptManagerFragment : Fragment() {

    private var _b: FragmentUserScriptManagerBinding? = null
    private val b get() = _b!!

    private val vm: UserScriptViewModel by activityViewModels()
    private lateinit var adapter: UserScriptAdapter

    override fun onCreateView(inf: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentUserScriptManagerBinding.inflate(inf, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Adapter ───────────────────────────────────────────────────────
        adapter = UserScriptAdapter(
            onToggle = { vm.toggle(it) },
            onEdit   = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )

        b.scriptRecycler.layoutManager = LinearLayoutManager(requireContext())
        b.scriptRecycler.adapter = adapter

        // ── Observe ───────────────────────────────────────────────────────
        vm.scripts.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.scriptEmptyHint.visibility =
                if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // ── FAB — add new script ──────────────────────────────────────────
        b.fabAddScript.setOnClickListener { showEditDialog(null) }

        // ── Toolbar back ──────────────────────────────────────────────────
        b.btnScriptBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun showEditDialog(script: UserScript?) {
        EditUserScriptDialog.newInstance(script?.id)
            .show(parentFragmentManager, "edit_script")
    }

    private fun confirmDelete(script: UserScript) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.delete_script_confirm, script.name))
            .setPositiveButton(R.string.delete) { _, _ -> vm.delete(script) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }

    companion object {
        fun newInstance() = UserScriptManagerFragment()
    }
}
