/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.ai

/**
 * Three display states for the AI assistant (PRD §3.3).
 *
 *  DOT   — minimised floating dot (18 dp circle, draggable)
 *  GHOST — translucent pill (shows last response preview)
 *  OPEN  — full bottom sheet / panel
 */
enum class AiWindowState { DOT, GHOST, OPEN }
