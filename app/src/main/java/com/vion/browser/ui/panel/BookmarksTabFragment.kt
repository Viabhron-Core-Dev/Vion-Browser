/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.panel

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vion.browser.R
import com.vion.browser.data.db.BookmarkEntity
import com.vion.browser.databinding.FragmentPanelListBinding

class BookmarksTabFragment : Fragment() {

    private var _binding: FragmentPanelListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PanelViewModel by activityViewModels()
    private var onNavigate: ((String) -> Unit)? = null

    override fun onCreateView(inf: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPanelListBinding.inflate(inf, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PanelListAdapter<BookmarkEntity>(
            titleOf = { it.title },
            subtitleOf = { it.url },
            onClick = { onNavigate?.invoke(it.url) },
            onLongClick = { item, anchor -> showBookmarkContextMenu(item, anchor) }
        )
        binding.panelRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.panelRecycler.adapter = adapter
        binding.emptyText.text = getString(R.string.no_bookmarks)

        viewModel.rootBookmarks.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showBookmarkContextMenu(item: BookmarkEntity, anchor: View) {
        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.menu_bookmark_item)
            setOnMenuItemClickListener { mi ->
                when (mi.itemId) {
                    R.id.action_delete_bookmark -> { viewModel.deleteBookmark(item); true }
                    R.id.action_pin_bookmark    -> { viewModel.toggleBookmarkPin(item); true }
                    else -> false
                }
            }
            show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object {
        fun newInstance(onNavigate: ((String) -> Unit)?) =
            BookmarksTabFragment().apply { this.onNavigate = onNavigate }
    }
}
