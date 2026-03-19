/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.panel

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vion.browser.R
import com.vion.browser.data.db.DownloadEntity
import com.vion.browser.databinding.FragmentPanelListBinding

class DownloadsTabFragment : Fragment() {

    private var _binding: FragmentPanelListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PanelViewModel by activityViewModels()

    override fun onCreateView(inf: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPanelListBinding.inflate(inf, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PanelListAdapter<DownloadEntity>(
            titleOf = { it.title },
            subtitleOf = { if (it.isComplete) it.filePath else "Downloading…" },
            onClick = { /* open file manager / intent — Phase 9 */ },
            onLongClick = { item, _ -> viewModel.deleteDownload(item) }
        )
        binding.panelRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.panelRecycler.adapter = adapter
        binding.emptyText.text = getString(R.string.no_downloads)

        // Clear completed button
        binding.fabClear.visibility = View.VISIBLE
        binding.fabClear.setOnClickListener { viewModel.clearCompletedDownloads() }

        viewModel.downloads.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object { fun newInstance() = DownloadsTabFragment() }
}
