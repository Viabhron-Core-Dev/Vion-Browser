/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.vion.browser.databinding.FragmentHomepageBinding

/**
 * Custom homepage shown when the active tab URL is the Vion internal home page
 * ("about:home" sentinel) or when no URL has been loaded yet.
 *
 * Layout:
 *   - Vion logo + tagline
 *   - Search bar (queries respect the same search-engine pref as the main URL bar)
 *   - 3×3 shortcut grid (pinned bookmarks / user-defined)
 */
class HomepageFragment : Fragment() {

    private var _binding: FragmentHomepageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var shortcutAdapter: ShortcutGridAdapter

    /** Called back by the host activity when the user submits a search query. */
    var onSearch: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Shortcut grid
        shortcutAdapter = ShortcutGridAdapter(
            onItemClick = { shortcut -> onSearch?.invoke(shortcut.url) },
            onItemLongClick = { shortcut -> viewModel.deleteShortcut(shortcut) }
        )

        binding.shortcutGrid.apply {
            adapter = shortcutAdapter
            layoutManager = GridLayoutManager(requireContext(), 4)
        }

        // Search bar action
        binding.homeSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_GO
            ) {
                val query = binding.homeSearchBar.text?.toString()?.trim() ?: ""
                if (query.isNotEmpty()) onSearch?.invoke(query)
                true
            } else false
        }

        binding.homeSearchButton.setOnClickListener {
            val query = binding.homeSearchBar.text?.toString()?.trim() ?: ""
            if (query.isNotEmpty()) onSearch?.invoke(query)
        }

        // Add shortcut button
        binding.btnAddShortcut.setOnClickListener {
            AddShortcutDialog().show(parentFragmentManager, "add_shortcut")
        }

        // Observe shortcuts
        viewModel.shortcuts.observe(viewLifecycleOwner) { list ->
            shortcutAdapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val HOME_URL = "about:home"
        fun newInstance() = HomepageFragment()
    }
}
