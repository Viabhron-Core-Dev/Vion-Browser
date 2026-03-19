/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.ui.menu

import com.vion.browser.R
import com.vion.browser.model.FloatingMenuItem

/**
 * Default floating menu item catalogue.
 * 10 items per page (2 columns × 5 rows).
 * User can rearrange via drag in the full manager (future).
 */
object DefaultMenuItems {

    fun build(): List<FloatingMenuItem> = listOf(
        // Page 0
        FloatingMenuItem("share",            R.string.menu_share,            R.drawable.ic_menu_share,    page = 0, position = 0),
        FloatingMenuItem("user_agent",       R.string.menu_user_agent,       R.drawable.ic_menu_ua,       page = 0, position = 1),
        FloatingMenuItem("desktop_site",     R.string.menu_desktop_site,     R.drawable.ic_menu_desktop,  page = 0, position = 2),
        FloatingMenuItem("javascript",       R.string.menu_javascript,       R.drawable.ic_menu_js,       page = 0, position = 3),
        FloatingMenuItem("add_bookmark",     R.string.menu_add_bookmark,     R.drawable.ic_menu_bookmark, page = 0, position = 4),
        FloatingMenuItem("bookmarks",        R.string.menu_bookmarks,        R.drawable.ic_menu_bookmarks,page = 0, position = 5),
        FloatingMenuItem("history",          R.string.menu_history,          R.drawable.ic_menu_history,  page = 0, position = 6),
        FloatingMenuItem("downloads",        R.string.menu_downloads,        R.drawable.ic_menu_downloads,page = 0, position = 7),
        FloatingMenuItem("settings",         R.string.menu_settings,         R.drawable.ic_menu_settings, page = 0, position = 8),
        FloatingMenuItem("find_in_page",     R.string.menu_find_in_page,     R.drawable.ic_menu_find,     page = 0, position = 9),

        // Page 1
        FloatingMenuItem("reader_mode",      R.string.menu_reader_mode,      R.drawable.ic_menu_reader,   page = 1, position = 0),
        FloatingMenuItem("ad_blocking",      R.string.menu_ad_blocking,      R.drawable.ic_menu_adblock,  page = 1, position = 1),
        FloatingMenuItem("night_mode",       R.string.menu_night_mode,       R.drawable.ic_menu_night,    page = 1, position = 2),
        FloatingMenuItem("translate",        R.string.menu_translate,        R.drawable.ic_menu_translate,page = 1, position = 3),
        FloatingMenuItem("clear_data",       R.string.menu_clear_data,       R.drawable.ic_menu_clear,    page = 1, position = 4),
        FloatingMenuItem("add_to_home",      R.string.menu_add_to_home_screen,R.drawable.ic_menu_home,    page = 1, position = 5),
        FloatingMenuItem("refresh",          R.string.menu_refresh,          R.drawable.ic_menu_refresh,  page = 1, position = 6),
        FloatingMenuItem("view_source",      R.string.menu_view_source,      R.drawable.ic_menu_source,   page = 1, position = 7),
        FloatingMenuItem("full_screen",      R.string.menu_full_screen,      R.drawable.ic_menu_fullscreen,page = 1, position = 8),
        FloatingMenuItem("customize_menu",   R.string.menu_customize_menu,   R.drawable.ic_menu_customize,page = 1, position = 9),

        // Page 2
        FloatingMenuItem("auto_scroll",      R.string.auto_scroll,           R.drawable.ic_arrow_down,    page = 2, position = 0),
        FloatingMenuItem("user_scripts",     R.string.user_scripts,          R.drawable.ic_menu_grid,     page = 2, position = 1)
    )
}
