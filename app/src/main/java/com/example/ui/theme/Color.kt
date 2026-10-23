package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary theme palette - High-tech Dark Obsidian & Electric Accents
val NavyDark = Color(0xFF090D16)
val NavySurface = Color(0xFF101726)
val NavySurfaceVariant = Color(0xFF192237)
val NavyCard = Color(0xFF151D2F)
val NavyBorder = Color(0xFF22304A)

// Odd = RED & Even = BLUE (as requested: ODD RED, BLUE EVEN)
val OddRed = Color(0xFFFF334B)
val OddRedGlow = Color(0x33FF334B)
val OddRedDark = Color(0xFF991B1B)
val OddRedLight = Color(0xFFFF6B7D)

val EvenBlue = Color(0xFF2563EB)
val EvenBlueGlow = Color(0x332563EB)
val EvenBlueDark = Color(0xFF1E40AF)
val EvenBlueLight = Color(0xFF60A5FA)

// Keep backwards-compat aliases for any components referencing prior names if any
val OddCyan = OddRed
val OddCyanGlow = OddRedGlow
val OddCyanDark = OddRedDark
val EvenAmber = EvenBlue
val EvenAmberGlow = EvenBlueGlow
val EvenAmberDark = EvenBlueDark

// Status & Indicators
val StatusConnected = Color(0xFF10B981)
val StatusOffline = Color(0xFFEF4444)
val StatusConnecting = Color(0xFFF59E0B)

val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)

val AccentGold = Color(0xFFFFD166)
val AccentPurple = Color(0xFF8B5CF6)
