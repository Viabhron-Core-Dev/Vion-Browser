/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.panel

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vion.browser.R
import com.vion.browser.data.db.SavedPageEntity
import com.vion.browser.databinding.FragmentPanelListBinding

class SavedPagesTabFragment : Fragment() {

    private var _binding: FragmentPanelListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PanelViewModel by activityViewModels()

    override fun onCreateView(inf: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPanelListBinding.inflate(inf, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PanelListAdapter<SavedPageEntity>(
            titleOf = { it.title },
            subtitleOf = { "${it.format} • ${it.url}" },
            onClick = { /* open saved file — Phase 9 */ },
            onLongClick = { item, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_saved_page)
                    .setMessage(item.title)
                    .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteSavedPage(item) }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        binding.panelRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.panelRecycler.adapter = adapter
        binding.emptyText.text = getString(R.string.no_saved_pages)

        viewModel.savedPages.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object { fun newInstance() = SavedPagesTabFragment() }
}
