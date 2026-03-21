/*
 * Copyright 2015-2016 Soren Stoutner <soren@stoutner.com>.
 * Copyright 2026 Vion Browser Contributors.
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.vion.browser.ui.about.AboutPagerAdapter

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar = findViewById<Toolbar>(R.id.about_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.about_vion)

        val viewPager = findViewById<ViewPager2>(R.id.about_view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.about_tab_layout)

        viewPager.adapter = AboutPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = AboutPagerAdapter.TAB_TITLES[position]
        }.attach()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
