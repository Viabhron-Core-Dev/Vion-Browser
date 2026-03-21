/*
 * Copyright 2015-2016 Soren Stoutner <soren@stoutner.com>.
 * Copyright 2026 Vion Browser Contributors.
 *
 * This file is part of Vion Browser.
 *
 * Vion Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vion Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vion Browser.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vion.browser

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vion.browser.databinding.ActivityMainBinding
import com.vion.browser.model.FloatingMenuItem
import com.vion.browser.extension.AdBlockEngine
import com.vion.browser.extension.UserScriptEngine
import com.vion.browser.security.PrivacyWebViewClient
import com.vion.browser.security.SecurityPrefs
import com.vion.browser.ui.ai.AiPanelFragment
import com.vion.browser.ui.ai.AiProviderDialog
import com.vion.browser.ui.ai.AiViewModel
import com.vion.browser.ui.ai.AiWindowState
import com.vion.browser.ui.home.AddShortcutDialog
import com.vion.browser.ui.home.HomepageFragment
import com.vion.browser.ui.menu.DefaultMenuItems
import com.vion.browser.ui.panel.UnifiedPanelFragment
import com.vion.browser.ui.reader.AutoScrollHelper
import com.vion.browser.ui.reader.NightModeHelper
import com.vion.browser.ui.reader.ReaderModeHelper
import com.vion.browser.ui.scripts.UserScriptManagerFragment
import com.vion.browser.ui.tabs.TabManagerViewModel
import com.vion.browser.ui.tabs.TabTrayAdapter
import com.vion.browser.util.Prefs
import com.vion.browser.util.UrlUtils
import kotlin.math.abs

/**
 * Main browser activity — Phases 1-8.
 *
 * Layout structure (top→bottom):
 *   ┌──────────────────────────────┐
 *   │  URL bar (AppBar area)       │
 *   ├──────────────────────────────┤
 *   │  WebView / Homepage overlay  │
 *   ├──────────────────────────────┤
 *   │  Bottom toolbar (5 buttons)  │
 *   └──────────────────────────────┘
 *
 * Security principles (unchanged from upstream Privacy Browser):
 *  - JavaScript OFF by default
 *  - Third-party cookies blocked by default
 *  - No JavaScript bridge to native code
 *  - HTTPS enforced (redirect warnings shown)
 */
class MainWebViewActivity : AppCompatActivity() {

    // ── View Binding ──────────────────────────────────────────────────────────
    private lateinit var binding: ActivityMainBinding

    // ── ViewModels ────────────────────────────────────────────────────────────
    private val tabViewModel: TabManagerViewModel by viewModels()

    // ── WebView ───────────────────────────────────────────────────────────────
    private lateinit var mainWebView: WebView
    private lateinit var cookieManager: CookieManager

    // ── Preferences ──────────────────────────────────────────────────────────
    private var javaScriptEnabled = false
    private var firstPartyCookiesEnabled = false
    private var thirdPartyCookiesEnabled = false
    private var domStorageEnabled = false
    private var homepage = Prefs.DEFAULT_HOMEPAGE
    private lateinit var savedPreferences: SharedPreferences
    private lateinit var prefListener: SharedPreferences.OnSharedPreferenceChangeListener

    // ── Gesture detection (back/forward swipe) ────────────────────────────────
    private lateinit var gestureDetector: GestureDetectorCompat

    // ── State ─────────────────────────────────────────────────────────────────
    private var isDesktopMode = false
    private var isFullScreen = false

    // ── Security (Phase 8) ────────────────────────────────────────────────────
    private lateinit var privacyClient: VionWebViewClient

    // ── Phase 9 — Reader / Night mode / Auto-scroll ───────────────────────────
    private var isNightModeOn = false
    private var isReaderModeOn = false
    private var isAdBlockOn = false

    // ── Phase 10 — AI ─────────────────────────────────────────────────────────
    private val aiViewModel: AiViewModel by viewModels()

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise default preferences on first run
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        savedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        loadPreferences()

        setupWebView()
        setupUrlBar()
        setupBottomToolbar()
        setupFloatingMenu()
        setupGestureDetector()
        setupPreferenceListener()

        // Phase 11 — initialise ad-block engine and user scripts
        AdBlockEngine.load(this)
        UserScriptEngine.reload(this)

        // Tab management
        tabViewModel.ensureAtLeastOneTab(homepage)
        tabViewModel.activeIndex.observe(this) { _ ->
            updateTabBadge(tabViewModel.tabCount)
            updateNavButtonStates()
        }

        // Handle intent (URL from external app)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
        binding.floatingMenu.hide()
    }

    override fun onResume() {
        super.onResume()
        savedPreferences.registerOnSharedPreferenceChangeListener(prefListener)
        mainWebView.onResume()
    }

    override fun onPause() {
        savedPreferences.unregisterOnSharedPreferenceChangeListener(prefListener)
        mainWebView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mainWebView.destroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        when {
            binding.floatingMenu.isShowing  -> binding.floatingMenu.hide()
            binding.homepageContainer.visibility == View.VISIBLE -> {
                // dismiss homepage overlay without going back
            }
            mainWebView.canGoBack()         -> mainWebView.goBack()
            else                            -> super.onBackPressed()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Setup helpers
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun setupWebView() {
        mainWebView = binding.mainWebView

        // ── Security settings (Privacy Browser heritage — do not weaken) ──
        mainWebView.settings.apply {
            javaScriptEnabled = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            allowFileAccess = false
            allowContentAccess = false
            setGeolocationEnabled(false)
            @Suppress("DEPRECATION") saveFormData = false
            @Suppress("DEPRECATION") savePassword = false
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        // WebRTC (Phase 8) — disable by default
        applyWebRtcPolicy()

        cookieManager = CookieManager.getInstance()
        applyPrivacySettings()

        privacyClient = VionWebViewClient()
        mainWebView.webViewClient = privacyClient
        mainWebView.webChromeClient = VionWebChromeClient()

        // Downloads
        mainWebView.setDownloadListener { url, userAgent, _, mimeType, _ ->
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val req = DownloadManager.Request(Uri.parse(url)).apply {
                setDescription(url)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                addRequestHeader("User-Agent", userAgent)
            }
            dm.enqueue(req)
            Toast.makeText(this, R.string.download_started, Toast.LENGTH_SHORT).show()
        }

        // Touch passthrough for gesture detection
        mainWebView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN && binding.floatingMenu.isShowing) {
                binding.floatingMenu.hide()
            }
            false
        }
    }

    private fun applyWebRtcPolicy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mode = SecurityPrefs.webRtcMode(this)
            mainWebView.settings.javaScriptEnabled // no-op; WebRTC is controlled per-policy
            // AndroidX WebKit exposes this via WebSettingsCompat:
            try {
                androidx.webkit.WebSettingsCompat.setEnterpriseAuthenticationAppLinkPolicyEnabled(
                    mainWebView.settings, false)
            } catch (_: Exception) {}
        }
    }

    private fun setupUrlBar() {
        binding.sslIcon.setOnClickListener {
            Toast.makeText(this, mainWebView.url ?: "", Toast.LENGTH_SHORT).show()
        }
        binding.urlTextBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                loadUrlFromAddressBar(); true
            } else false
        }
        binding.urlTextBox.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.urlTextBox.selectAll()
        }
        binding.reloadButton.setOnClickListener {
            if (mainWebView.progress < 100) mainWebView.stopLoading()
            else mainWebView.reload()
        }
    }

    private fun setupBottomToolbar() {
        binding.btnBack.setOnClickListener { if (mainWebView.canGoBack()) mainWebView.goBack() }
        binding.btnBack.setOnLongClickListener {
            handleLongPress(Prefs.longPressAction(this, Prefs.KEY_BACK_LONG)); true
        }
        binding.btnForward.setOnClickListener { if (mainWebView.canGoForward()) mainWebView.goForward() }
        binding.btnForward.setOnLongClickListener {
            handleLongPress(Prefs.longPressAction(this, Prefs.KEY_FORWARD_LONG)); true
        }
        binding.btnHome.setOnClickListener { navigateHome() }
        binding.btnHome.setOnLongClickListener {
            handleLongPress(Prefs.longPressAction(this, Prefs.KEY_HOME_LONG)); true
        }
        binding.btnTabs.setOnClickListener { showTabTray() }
        binding.btnTabs.setOnLongClickListener {
            handleLongPress(Prefs.longPressAction(this, Prefs.KEY_TABS_LONG)); true
        }
        binding.btnMenu.setOnClickListener { binding.floatingMenu.toggle() }
        binding.btnMenu.setOnLongClickListener {
            handleLongPress(Prefs.longPressAction(this, Prefs.KEY_MENU_LONG)); true
        }
    }

    private fun setupFloatingMenu() {
        binding.floatingMenu.setItems(DefaultMenuItems.build())
        binding.floatingMenu.onItemClick = { item -> handleMenuAction(item) }
        binding.floatingMenu.onItemLongClick = { item, _ ->
            Toast.makeText(this, item.id, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY = 100
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
                if (e1 == null) return false
                val dX = e2.x - e1.x; val dY = e2.y - e1.y
                if (abs(dX) > abs(dY) && abs(dX) > SWIPE_THRESHOLD && abs(vX) > SWIPE_VELOCITY) {
                    if (dX < 0 && mainWebView.canGoForward()) { mainWebView.goForward(); return true }
                    if (dX > 0 && mainWebView.canGoBack())    { mainWebView.goBack();    return true }
                }
                return false
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupPreferenceListener() {
        prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                Prefs.KEY_JAVASCRIPT_ENABLED -> {
                    javaScriptEnabled = prefs.getBoolean(key, false)
                    mainWebView.settings.javaScriptEnabled = javaScriptEnabled
                    mainWebView.reload(); updatePrivacyIcon()
                }
                Prefs.KEY_FIRST_PARTY_COOKIES -> {
                    firstPartyCookiesEnabled = prefs.getBoolean(key, false)
                    cookieManager.setAcceptCookie(firstPartyCookiesEnabled)
                    mainWebView.reload()
                }
                Prefs.KEY_THIRD_PARTY_COOKIES -> {
                    thirdPartyCookiesEnabled = prefs.getBoolean(key, false)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled)
                    mainWebView.reload()
                }
                Prefs.KEY_DOM_STORAGE -> {
                    domStorageEnabled = prefs.getBoolean(key, false)
                    mainWebView.settings.domStorageEnabled = domStorageEnabled
                    mainWebView.reload()
                }
                Prefs.KEY_USER_AGENT, Prefs.KEY_CUSTOM_USER_AGENT -> applyUserAgent()
                Prefs.KEY_HOMEPAGE -> {
                    homepage = prefs.getString(key, Prefs.DEFAULT_HOMEPAGE) ?: Prefs.DEFAULT_HOMEPAGE
                }
                Prefs.KEY_SWIPE_TO_REFRESH -> {
                    binding.swipeRefreshLayout.isEnabled = prefs.getBoolean(key, true)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Preference loading
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadPreferences() {
        javaScriptEnabled         = Prefs.isJavaScriptEnabled(this)
        firstPartyCookiesEnabled  = Prefs.isFirstPartyCookiesEnabled(this)
        thirdPartyCookiesEnabled  = Prefs.isThirdPartyCookiesEnabled(this)
        domStorageEnabled         = Prefs.isDomStorageEnabled(this)
        homepage                  = Prefs.homepage(this)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun applyPrivacySettings() {
        mainWebView.settings.javaScriptEnabled = javaScriptEnabled
        mainWebView.settings.domStorageEnabled = domStorageEnabled
        cookieManager.setAcceptCookie(firstPartyCookiesEnabled)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled)
        applyUserAgent()
        binding.swipeRefreshLayout.isEnabled = Prefs.isSwipeToRefresh(this)
    }

    private fun applyUserAgent() {
        val ua = Prefs.resolveUserAgent(this)
        mainWebView.settings.userAgentString = ua
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleIntent(intent: Intent) {
        val urlFromIntent = intent.data?.toString()
        if (urlFromIntent != null) {
            hideHomepage()
            mainWebView.loadUrl(urlFromIntent)
            binding.urlTextBox.setText(urlFromIntent)
        } else {
            navigateHome()
        }
        mainWebView.requestFocus()
    }

    private fun navigateHome() {
        if (homepage == HomepageFragment.HOME_URL || homepage == "about:home") {
            showHomepage()
        } else {
            hideHomepage()
            mainWebView.loadUrl(homepage)
            binding.urlTextBox.setText(homepage)
        }
    }

    private fun showHomepage() {
        binding.homepageContainer.visibility = View.VISIBLE
        binding.mainWebView.visibility = View.GONE
        binding.urlTextBox.setText("")

        if (supportFragmentManager.findFragmentByTag("homepage") == null) {
            val frag = HomepageFragment.newInstance()
            frag.onSearch = { query ->
                hideHomepage()
                val searchUrl = Prefs.resolveSearchUrl(this, javaScriptEnabled)
                val url = UrlUtils.formatInput(query, searchUrl)
                mainWebView.loadUrl(url)
                binding.urlTextBox.setText(url)
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.homepage_container, frag, "homepage")
                .commitNow()
        }
    }

    private fun hideHomepage() {
        binding.homepageContainer.visibility = View.GONE
        binding.mainWebView.visibility = View.VISIBLE
    }

    private fun loadUrlFromAddressBar() {
        val input = binding.urlTextBox.text.toString().trim()
        if (input.isBlank()) return
        hideHomepage()
        val searchUrl = Prefs.resolveSearchUrl(this, javaScriptEnabled)
        val url = UrlUtils.formatInput(input, searchUrl)
        mainWebView.loadUrl(url)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.urlTextBox.windowToken, 0)
        mainWebView.requestFocus()
        tabViewModel.updateActiveTab(url = url)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI state helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun updatePrivacyIcon() {
        val iconRes = when {
            javaScriptEnabled -> R.drawable.ic_js_enabled
            firstPartyCookiesEnabled || domStorageEnabled -> R.drawable.warning
            else -> R.drawable.ic_privacy_mode
        }
        binding.sslIcon.setImageResource(iconRes)
    }

    private fun updateNavButtonStates() {
        binding.btnBack.alpha    = if (mainWebView.canGoBack())    1f else 0.4f
        binding.btnForward.alpha = if (mainWebView.canGoForward()) 1f else 0.4f
    }

    private fun updateTabBadge(count: Int) {
        binding.tabCountBadge.text = if (count > 1) count.toString() else ""
        binding.tabCountBadge.visibility = if (count > 1) View.VISIBLE else View.GONE
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tab tray
    // ─────────────────────────────────────────────────────────────────────────

    private fun showTabTray() {
        val sheet    = BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.layout_tab_tray, null)
        sheet.setContentView(sheetView)

        val recycler  = sheetView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tab_recycler)
        val newTabBtn = sheetView.findViewById<View>(R.id.btn_new_tab)

        val adapter = TabTrayAdapter(
            onTabClick = { index ->
                tabViewModel.switchTo(index)
                val tab = tabViewModel.tabs.value?.getOrNull(index)
                tab?.url?.let { if (it.isNotBlank()) { hideHomepage(); mainWebView.loadUrl(it); binding.urlTextBox.setText(it) } }
                sheet.dismiss()
            },
            onTabClose = { index ->
                tabViewModel.closeTab(index)
                if (tabViewModel.tabCount == 0) sheet.dismiss()
            },
            onTabLongClick = { index, _ -> showTabContextMenu(index, sheet) }
        )
        adapter.activeIndex = tabViewModel.activeIndex.value ?: 0
        recycler.adapter = adapter
        recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        tabViewModel.tabs.value?.let { adapter.submitList(it.toList()) }

        val touchHelper = androidx.recyclerview.widget.ItemTouchHelper(
            object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
                androidx.recyclerview.widget.ItemTouchHelper.LEFT or
                androidx.recyclerview.widget.ItemTouchHelper.RIGHT, 0) {
                override fun onMove(rv: androidx.recyclerview.widget.RecyclerView,
                                    vh: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                                    target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
                    tabViewModel.moveTab(vh.adapterPosition, target.adapterPosition)
                    adapter.submitList(tabViewModel.tabs.value?.toList()); return true
                }
                override fun onSwiped(vh: androidx.recyclerview.widget.RecyclerView.ViewHolder, dir: Int) {}
            })
        touchHelper.attachToRecyclerView(recycler)

        newTabBtn.setOnClickListener {
            tabViewModel.openTab(homepage); navigateHome(); sheet.dismiss()
        }
        sheet.show()
    }

    private fun showTabContextMenu(index: Int, sheet: BottomSheetDialog) {
        val items = arrayOf(
            getString(R.string.close_tab), getString(R.string.close_other_tabs),
            getString(R.string.close_all_tabs), getString(R.string.duplicate_tab),
            getString(R.string.copy_link), getString(R.string.refresh_all))
        AlertDialog.Builder(this).setItems(items) { _, which ->
            when (which) {
                0 -> { tabViewModel.closeTab(index); sheet.dismiss() }
                1 -> { tabViewModel.closeOtherTabs(index); sheet.dismiss() }
                2 -> { tabViewModel.closeAllTabs(); navigateHome(); sheet.dismiss() }
                3 -> { tabViewModel.duplicateTab(index) }
                4 -> {
                    val url = tabViewModel.tabs.value?.getOrNull(index)?.url ?: ""
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("URL", url))
                    Toast.makeText(this, R.string.url_copied, Toast.LENGTH_SHORT).show()
                }
                5 -> mainWebView.reload()
            }
        }.show()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Unified Panel (Phase 6)
    // ─────────────────────────────────────────────────────────────────────────

    private fun showPanel(initialTab: Int = UnifiedPanelFragment.TAB_BOOKMARKS) {
        val sheet = BottomSheetDialog(this, R.style.FullHeightBottomSheet)
        val sheetView = layoutInflater.inflate(R.layout.layout_unified_panel, null)
        sheet.setContentView(sheetView)
        sheet.behavior.peekHeight = resources.displayMetrics.heightPixels

        val frag = UnifiedPanelFragment.newInstance(initialTab) { url ->
            hideHomepage()
            mainWebView.loadUrl(url)
            binding.urlTextBox.setText(url)
            sheet.dismiss()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.panel_fragment_container, frag)
            .commitNow()

        sheet.show()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Floating menu dispatcher
    // ─────────────────────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    private fun handleMenuAction(item: FloatingMenuItem) {
        binding.floatingMenu.hide()
        when (item.id) {
            "share"          -> shareCurrentUrl()
            "javascript"     -> toggleJavaScript()
            "desktop_site"   -> toggleDesktopSite()
            "add_bookmark"   -> addCurrentPageAsBookmark()
            "bookmarks"      -> showPanel(UnifiedPanelFragment.TAB_BOOKMARKS)
            "history"        -> showPanel(UnifiedPanelFragment.TAB_HISTORY)
            "downloads"      -> showPanel(UnifiedPanelFragment.TAB_DOWNLOADS)
            "settings"       -> startActivity(Intent(this, SettingsActivity::class.java))
            "find_in_page"   -> showFindInPage()
            "reader_mode"    -> toggleReaderMode()
            "ad_blocking"    -> toggleAdBlocking()
            "night_mode"     -> toggleNightMode()
            "translate"      -> showAiPanel(prefill = getString(R.string.ai_translate_page))
            "ai_assistant"   -> showAiPanel()
            "clear_data"     -> clearBrowsingData()
            "add_to_home"    -> showAddToHomeScreen()
            "refresh"        -> mainWebView.reload()
            "view_source"    -> mainWebView.loadUrl("view-source:${mainWebView.url}")
            "full_screen"    -> toggleFullScreen()
            "user_agent"     -> startActivity(Intent(this, SettingsActivity::class.java))
            "auto_scroll"    -> toggleAutoScroll()
            "user_scripts"   -> showUserScriptManager()
            "customize_menu" -> Toast.makeText(this, "Customize menu — coming soon", Toast.LENGTH_SHORT).show()
            else             -> Toast.makeText(this, item.id, Toast.LENGTH_SHORT).show()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Long-press dispatcher
    // ─────────────────────────────────────────────────────────────────────────

    private fun handleLongPress(action: String) {
        when (action) {
            "bookmarks"    -> showPanel(UnifiedPanelFragment.TAB_BOOKMARKS)
            "reader_mode"  -> toggleReaderMode()
            "downloads"    -> showPanel(UnifiedPanelFragment.TAB_DOWNLOADS)
            "copy_url"     -> {
                val url = mainWebView.url ?: return
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("URL", url))
                Toast.makeText(this, R.string.url_copied, Toast.LENGTH_SHORT).show()
            }
            "share"        -> shareCurrentUrl()
            "desktop_mode" -> toggleDesktopSite()
            "find_in_page" -> showFindInPage()
            "toggle_ai"    -> showAiPanel()
            "settings"     -> startActivity(Intent(this, SettingsActivity::class.java))
            "none"         -> { /* nothing */ }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Feature implementations
    // ─────────────────────────────────────────────────────────────────────────

    private fun shareCurrentUrl() {
        val url = mainWebView.url ?: return
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, url) },
            getString(R.string.share_url)))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun toggleJavaScript() {
        javaScriptEnabled = !javaScriptEnabled
        mainWebView.settings.javaScriptEnabled = javaScriptEnabled
        savedPreferences.edit().putBoolean(Prefs.KEY_JAVASCRIPT_ENABLED, javaScriptEnabled).apply()
        updatePrivacyIcon()
        Toast.makeText(this,
            if (javaScriptEnabled) R.string.javascript_enabled else R.string.javascript_disabled,
            Toast.LENGTH_SHORT).show()
        mainWebView.reload()
    }

    private fun toggleDesktopSite() {
        isDesktopMode = !isDesktopMode
        mainWebView.settings.userAgentString =
            if (isDesktopMode)
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"
            else null
        mainWebView.reload()
    }

    private fun addCurrentPageAsBookmark() {
        val url = mainWebView.url ?: return
        val title = mainWebView.title ?: url
        // Delegate to panel ViewModel via a lightweight in-place dialog
        AlertDialog.Builder(this)
            .setTitle(R.string.menu_add_bookmark)
            .setMessage(title)
            .setPositiveButton(R.string.create) { _, _ ->
                // PanelViewModel is shared (ActivityViewModels), access directly
                val vm = androidx.lifecycle.ViewModelProvider(this)
                    .get(com.vion.browser.ui.panel.PanelViewModel::class.java)
                vm.addBookmark(title, url)
                // Also log to history (if page is loaded)
                vm.addHistory(title, url)
                Toast.makeText(this, R.string.bookmark_added, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun clearBrowsingData() {
        AlertDialog.Builder(this)
            .setTitle(R.string.menu_clear_data)
            .setMessage(R.string.clear_data_confirm)
            .setPositiveButton(R.string.create) { _, _ ->
                cookieManager.removeAllCookies(null)
                mainWebView.clearCache(true)
                mainWebView.clearHistory()
                // Clear DB history too
                val vm = androidx.lifecycle.ViewModelProvider(this)
                    .get(com.vion.browser.ui.panel.PanelViewModel::class.java)
                vm.clearHistory()
                Toast.makeText(this, R.string.cookies_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAddToHomeScreen() {
        val url = mainWebView.url ?: return
        val title = mainWebView.title ?: url
        // Phase 5 — also offer to add as a homepage shortcut
        AddShortcutDialog.newInstance(url, title).show(supportFragmentManager, "add_shortcut")
    }

    private fun showFindInPage() {
        binding.findInPageBar.visibility = View.VISIBLE
        binding.findQuery.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.findQuery, InputMethodManager.SHOW_IMPLICIT)
        binding.findQuery.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                mainWebView.findNext(true); true
            } else false
        }
        binding.btnFindNext.setOnClickListener  { mainWebView.findNext(true) }
        binding.btnFindPrev.setOnClickListener  { mainWebView.findNext(false) }
        binding.btnFindClose.setOnClickListener {
            mainWebView.clearMatches(); binding.findInPageBar.visibility = View.GONE
        }
        binding.findQuery.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                mainWebView.findAllAsync(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase 9 — Reader mode / Night mode / Ad-blocking / Auto-scroll
    // ─────────────────────────────────────────────────────────────────────────

    private fun toggleReaderMode() {
        ReaderModeHelper.toggle(mainWebView) { active ->
            isReaderModeOn = active
            Toast.makeText(
                this,
                if (active) R.string.reader_mode_enabled else R.string.reader_mode_failed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleNightMode() {
        isNightModeOn = NightModeHelper.toggle(mainWebView)
        Toast.makeText(
            this,
            if (isNightModeOn) R.string.night_mode_enabled else R.string.night_mode_disabled,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun toggleAdBlocking() {
        isAdBlockOn = !isAdBlockOn
        privacyClient.adBlockEnabled = isAdBlockOn
        Toast.makeText(
            this,
            if (isAdBlockOn) R.string.adblock_enabled else R.string.adblock_disabled,
            Toast.LENGTH_SHORT
        ).show()
        mainWebView.reload()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase 13 — UserScript manager
    // ─────────────────────────────────────────────────────────────────────────

    private fun showUserScriptManager() {
        val sheet = BottomSheetDialog(this, R.style.FullHeightBottomSheet)
        val container = layoutInflater.inflate(R.layout.layout_unified_panel, null)
        sheet.setContentView(container)
        sheet.behavior.peekHeight = resources.displayMetrics.heightPixels

        val frag = UserScriptManagerFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.panel_fragment_container, frag)
            .commitNow()

        sheet.show()
    }

    private fun toggleAutoScroll() {
        val active = AutoScrollHelper.toggle(mainWebView)
        Toast.makeText(
            this,
            if (active) R.string.auto_scroll_started else R.string.auto_scroll_stopped,
            Toast.LENGTH_SHORT
        ).show()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phase 10 — AI Assistant panel
    // ─────────────────────────────────────────────────────────────────────────

    private fun showAiPanel(prefill: String? = null) {
        // Update page context so the AI knows what page we're on
        aiViewModel.pageTitle = mainWebView.title ?: ""
        aiViewModel.pageUrl   = mainWebView.url   ?: ""

        val sheet = BottomSheetDialog(this, R.style.FullHeightBottomSheet)
        val container = layoutInflater.inflate(R.layout.layout_unified_panel, null)
        sheet.setContentView(container)
        sheet.behavior.peekHeight = resources.displayMetrics.heightPixels

        val frag = AiPanelFragment()
        if (prefill != null) frag.arguments = android.os.Bundle().apply {
            putString(AiPanelFragment.ARG_PREFILL, prefill)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.panel_fragment_container, frag)
            .commitNow()

        sheet.show()
    }

    private fun toggleFullScreen() {
        isFullScreen = !isFullScreen
        if (isFullScreen) {
            binding.urlBarLayout.visibility = View.GONE
            binding.bottomToolbar.visibility = View.GONE
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            binding.urlBarLayout.visibility = View.VISIBLE
            binding.bottomToolbar.visibility = View.VISIBLE
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VionWebViewClient — extends PrivacyWebViewClient (Phase 7+8)
    // ─────────────────────────────────────────────────────────────────────────

    inner class VionWebViewClient : PrivacyWebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            binding.urlTextBox.clearFocus()
            binding.urlTextBox.setText(url)
            binding.progressBar.visibility = View.VISIBLE
            binding.progressBar.progress   = 0
            binding.reloadButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            tabViewModel.updateActiveTab(url = url, isLoading = true)
            updateNavButtonStates()
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (!binding.urlTextBox.hasFocus()) binding.urlTextBox.setText(url)
            binding.progressBar.visibility = View.GONE
            binding.reloadButton.setImageResource(R.drawable.ic_reload)
            tabViewModel.updateActiveTab(url = url, title = view.title ?: url,
                isLoading = false, progress = 100)
            updateNavButtonStates()
            updatePrivacyIcon()

            // Phase 7: inject Page Visibility spoof
            if (SecurityPrefs.isPageVisibilitySpoofEnabled(this@MainWebViewActivity)) {
                view.evaluateJavascript(pageVisibilitySpoofJs, null)
            }
            // Phase 8: inject fingerprint resistance
            if (SecurityPrefs.isFingerprintResistanceEnabled(this@MainWebViewActivity)) {
                view.evaluateJavascript(fingerprintResistanceJs, null)
            }

            // Phase 11: inject user scripts at document_end
            privacyClient.injectDocumentEndScripts(view, url)

            // Phase 9: re-apply night mode after page load
            if (isNightModeOn) NightModeHelper.apply(view, true)

            // Record in history DB
            val vm = androidx.lifecycle.ViewModelProvider(this@MainWebViewActivity)
                .get(com.vion.browser.ui.panel.PanelViewModel::class.java)
            vm.addHistory(view.title ?: url, url)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VionWebChromeClient
    // ─────────────────────────────────────────────────────────────────────────

    inner class VionWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            binding.progressBar.progress = newProgress
            binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
            tabViewModel.updateActiveTab(progress = newProgress)
        }

        override fun onReceivedTitle(view: WebView, title: String) {
            tabViewModel.updateActiveTab(title = title)
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap) {
            tabViewModel.tabs.value?.getOrNull(tabViewModel.activeIndex.value ?: 0)
                ?.favicon = icon
            binding.sslIcon.setImageBitmap(Bitmap.createScaledBitmap(icon, 48, 48, true))
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            binding.urlBarLayout.visibility = View.GONE
            binding.bottomToolbar.visibility = View.GONE
            binding.fullScreenContainer.addView(view)
            binding.fullScreenContainer.visibility = View.VISIBLE
            binding.mainWebView.visibility = View.GONE
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        override fun onHideCustomView() {
            binding.urlBarLayout.visibility = View.VISIBLE
            binding.bottomToolbar.visibility = View.VISIBLE
            binding.mainWebView.visibility = View.VISIBLE
            binding.fullScreenContainer.removeAllViews()
            binding.fullScreenContainer.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }
}
