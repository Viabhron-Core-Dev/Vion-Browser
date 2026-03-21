/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.ai

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vion.browser.R
import com.vion.browser.databinding.FragmentAiPanelBinding

/**
 * OPEN state of the AI assistant window.
 * Shows the conversation history + input field.
 * Hosted inside a BottomSheetDialog (full height) from MainWebViewActivity.
 */
class AiPanelFragment : Fragment() {

    private var _binding: FragmentAiPanelBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AiViewModel by activityViewModels()
    private lateinit var chatAdapter: AiChatAdapter

    override fun onCreateView(inf: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAiPanelBinding.inflate(inf, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Chat recycler ─────────────────────────────────────────────────
        chatAdapter = AiChatAdapter()
        binding.aiChatRecycler.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.aiChatRecycler.adapter = chatAdapter

        // ── Observe messages ──────────────────────────────────────────────
        viewModel.messages.observe(viewLifecycleOwner) { msgs ->
            chatAdapter.submitList(msgs) {
                if (msgs.isNotEmpty())
                    binding.aiChatRecycler.smoothScrollToPosition(msgs.size - 1)
            }
            binding.aiEmptyHint.visibility = if (msgs.isEmpty()) View.VISIBLE else View.GONE
        }

        // ── Loading indicator ─────────────────────────────────────────────
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.aiProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !loading
        }

        // ── Send ──────────────────────────────────────────────────────────
        binding.btnSend.setOnClickListener { sendMessage() }
        binding.aiInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); true } else false
        }

        // ── Clear ─────────────────────────────────────────────────────────
        binding.btnAiClear.setOnClickListener { viewModel.clearConversation() }

        // ── Summarise shortcut ────────────────────────────────────────────
        binding.btnAiSummarize.setOnClickListener {
            val prompt = buildString {
                append(getString(R.string.ai_summarize))
                if (viewModel.pageTitle.isNotBlank()) append(": ${viewModel.pageTitle}")
                if (viewModel.pageUrl.isNotBlank()) append(" (${viewModel.pageUrl})")
            }
            binding.aiInput.setText(prompt)
        }

        // ── Provider settings ─────────────────────────────────────────────
        binding.btnAiSettings.setOnClickListener {
            AiProviderDialog().show(parentFragmentManager, "ai_provider")
        }

        // ── Pre-fill from ARG_PREFILL ─────────────────────────────────────
        arguments?.getString(ARG_PREFILL)?.let { prefill ->
            binding.aiInput.setText(prefill)
        }
    }

    private fun sendMessage() {
        val text = binding.aiInput.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return
        binding.aiInput.text?.clear()
        viewModel.sendMessage(text, requireContext())
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    companion object {
        const val ARG_PREFILL = "prefill"
        fun newInstance(prefill: String? = null) = AiPanelFragment().apply {
            if (prefill != null) arguments = Bundle().apply { putString(ARG_PREFILL, prefill) }
        }
    }
}
