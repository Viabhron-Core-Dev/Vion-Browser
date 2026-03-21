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
import com.vion.browser.data.db.HistoryEntity
import com.vion.browser.databinding.FragmentPanelListBinding

class HistoryTabFragment : Fragment() {

    private var _binding: FragmentPanelListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PanelViewModel by activityViewModels()
    private var onNavigate: ((String) -> Unit)? = null

    override fun onCreateView(inf: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPanelListBinding.inflate(inf, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PanelListAdapter<HistoryEntity>(
            titleOf = { it.title.ifBlank { it.url } },
            subtitleOf = { it.url },
            onClick = { onNavigate?.invoke(it.url) },
            onLongClick = { item, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_history_entry)
                    .setMessage(item.url)
                    .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteHistory(item) }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        )
        binding.panelRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.panelRecycler.adapter = adapter
        binding.emptyText.text = getString(R.string.no_history)

        // Clear all button (shown via toolbar or floating button)
        binding.fabClear.visibility = View.VISIBLE
        binding.fabClear.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.clear_history_confirm)
                .setPositiveButton(R.string.clear) { _, _ -> viewModel.clearHistory() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        viewModel.history.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object {
        fun newInstance(onNavigate: ((String) -> Unit)?) =
            HistoryTabFragment().apply { this.onNavigate = onNavigate }
    }
}
