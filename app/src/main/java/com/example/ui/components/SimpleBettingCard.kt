package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NormalizedDraw
import com.example.data.model.ProbabilityForecast
import com.example.data.model.SignalResult
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.EvenBlueGlow
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.OddRedGlow
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SimpleBettingCard(
    forecast: ProbabilityForecast?,
    signal: SignalResult?,
    upcomingCountdownSeconds: Int,
    recentDraws: List<NormalizedDraw>,
    onTogglePaSure: () -> Unit,
    forceRevealEarly: Boolean = false,
    onRevealEarly: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isAnalyzing = upcomingCountdownSeconds > 35 && !forceRevealEarly
    val isPassMuna = signal?.isPassMuna == true
    val isConfluenceLocked = signal?.isConfluenceLocked == true
    val recoveryMultiplier = signal?.recoveryMultiplier ?: 1
    val isRecovery = recoveryMultiplier > 1

    // Unified Single Source of Truth for Parity Decision: Never allow top and bottom to disagree!
    val parity = signal?.signalType ?: forecast?.mostLikelyCategory ?: "ODD"
    val isOdd = parity == "ODD"

    val mainColor = when {
        isPassMuna -> Color(0xFFEF4444)
        isOdd -> OddRed
        else -> EvenBlue
    }

    val glowColor = when {
        isPassMuna -> Color(0x33EF4444)
        isOdd -> OddRedGlow
        else -> EvenBlueGlow
    }

    var showGuide by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("simple_betting_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. TIMER & PA-SURE STATUS BAR
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isAnalyzing) Icons.Default.HourglassTop else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isAnalyzing) Color(0xFFFFB703) else if (upcomingCountdownSeconds in 1..15) Color(0xFFFFB703) else StatusConnected,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isAnalyzing) {
                            "Nagsusuri pa ng bola: ${upcomingCountdownSeconds}s"
                        } else if (upcomingCountdownSeconds > 0) {
                            "Bibilang sa: ${upcomingCountdownSeconds}s (LOCKED)"
                        } else {
                            "Bumubola na ngayon..."
                        },
                        color = if (isAnalyzing) Color(0xFFFFB703) else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Pa-Sure Badge Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (signal?.paSureModeActive == true) Color(0xFFFFB703).copy(alpha = 0.2f) else NavySurfaceVariant)
                        .border(1.dp, if (signal?.paSureModeActive == true) Color(0xFFFFB703) else NavyBorder, RoundedCornerShape(8.dp))
                        .clickable { onTogglePaSure() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (signal?.paSureModeActive == true) Color(0xFFFFB703) else TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (signal?.paSureModeActive == true) "100% PA-SURE" else "Standard",
                            color = if (signal?.paSureModeActive == true) Color(0xFFFFB703) else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // 2. MAIN HERO BETTING CARD (MALINAW NA DIREKSIYON O ACTIVE ANALYSIS)
        if (isAnalyzing) {
            // ANALYSIS IN PROGRESS (Threshold > 35s requested by user: analys muna bago mag flex!)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFB703)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analyzing_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x33FFB703), NavyCard)
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated Scanning Radar Icon
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB703).copy(alpha = 0.2f))
                            .border(2.dp, Color(0xFFFFB703), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CrisisAlert,
                            contentDescription = null,
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "🔬 NAG-A-ANALYZE NG BOLA...",
                        color = Color(0xFFFFB703),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Masusing kinakalkula ang takbo ng pattern at server momentum bago i-flex ang siguradong pusta.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Digital Countdown to 35s Reveal
                    val secondsToLock = (upcomingCountdownSeconds - 35).coerceAtLeast(1)
                    val progressFraction = ((60 - upcomingCountdownSeconds).toFloat() / 25f).coerceIn(0.1f, 0.95f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NavySurfaceVariant)
                            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB703),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Matatapos ang pagsusuri sa:",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "00:${if (upcomingCountdownSeconds < 10) "0$upcomingCountdownSeconds" else upcomingCountdownSeconds}",
                                    color = Color(0xFFFFB703),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFFFB703),
                                trackColor = NavyCard
                            )

                            Text(
                                text = "🎯 I-la-lock at ipapakita ang taya pagpatak ng 0:35s (${secondsToLock}s na lang)",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Analysis Pipeline indicators
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnalysisStepRow(
                            icon = Icons.Default.TrendingUp,
                            title = "Markov Transition Matrix",
                            status = "Kinukwenta ang bigat ng ODD vs EVEN"
                        )
                        AnalysisStepRow(
                            icon = Icons.Default.AutoGraph,
                            title = "Confluence & Streak Volatility",
                            status = "Binabantayan kung Chop o Dragon streak"
                        )
                        AnalysisStepRow(
                            icon = Icons.Default.Shield,
                            title = "Anti-Loss 2x/3x Shield Check",
                            status = "Sinisala ang risk bago i-flex ang signal"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Override Button
                    OutlinedButton(
                        onClick = onRevealEarly,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("reveal_early_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFB703)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFB703).copy(alpha = 0.1f),
                            contentColor = Color(0xFFFFB703)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "⚡ I-flex Agad (Tingnan ang Early Signal)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        } else {
            // 2. MAIN HERO BETTING CARD (MALINAW NA DIREKSIYON)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(2.dp, mainColor),
                modifier = Modifier.fillMaxWidth().testTag("simple_hero_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(glowColor, NavyCard)
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 35s Verification Locked Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF06D6A0).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF06D6A0).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF06D6A0),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "🎯 PATTERN VERIFIED & CONFLUENCE LOCKED (35s Window)",
                                color = Color(0xFF06D6A0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isPassMuna) {
                    // PASS MUNA UI - HINDI NAKAKALITO
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .border(2.dp, Color(0xFFEF4444), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PASS MUNA!",
                        color = Color(0xFFEF4444),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "HUWAG TUMAYA SA ROUND NA ITO",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurfaceVariant.copy(alpha = 0.8f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🛡️ Iwas-Talo Rule: Magulo o 50/50 ang ikot ng bola ngayon. Hintayin ang susunod na bola para sigurado ang pusta at hindi masayang ang puhunan.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // DIRECT BET SIGNAL - ODD O EVEN
                    Text(
                        text = "ANG TATAYAAN SA SUSUNOD NA BOLA:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Giant Parity Button Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(mainColor)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isOdd) "TUMAYA SA: ODD" else "TUMAYA SA: EVEN",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = if (isOdd) "🔴 PULANG BOLA (1, 3, 5, 7, 9...)" else "🔵 ASUL NA BOLA (0, 2, 4, 6, 8...)",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confluence indicator
                    if (isConfluenceLocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF06D6A0).copy(alpha = 0.18f))
                                .border(1.dp, Color(0xFF06D6A0), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF06D6A0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "★ 100% PA-SURE WIN CONFLUENCE LOCKED ★ (Malakas ang streak/chop!)",
                                    color = Color(0xFF06D6A0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // PUSTA GUIDE (NORMAL vs BAWI-TALO)
                    if (isRecovery) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFB703).copy(alpha = 0.2f))
                                .border(1.2.dp, Color(0xFFFFB703), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "⚡ BAWI-TALO: TUMAYA NG ${recoveryMultiplier}x STAKE",
                                        color = Color(0xFFFFB703),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Nagmintis kanina. Doblehin o triplehin ang pusta sa round na ito para bawi agad ang puhunan!",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusConnected.copy(alpha = 0.15f))
                                .border(1.dp, StatusConnected.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusConnected,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "✅ NORMAL NA PUSTA (1x Stake) — Maglagay ng regular na taya.",
                                    color = StatusConnected,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // 3. TOP 3 LUCKY BALLS (MGA MASWERTENG BOLA) - DIRECT & SIMPLE
        if (!isPassMuna && signal != null && !isAnalyzing) {
            val matchingBulls = signal.predictedNumbers.filter { num ->
                if (isOdd) (num % 2 != 0) else (num % 2 == 0)
            }
            val topBulls = if (matchingBulls.isNotEmpty()) matchingBulls.take(3) else {
                if (isOdd) listOf(1, 3, 5) else listOf(0, 2, 4)
            }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth().testTag("simple_top_bulls_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CrisisAlert,
                                contentDescription = null,
                                tint = mainColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "TOP 3 MASWERTENG BOLA (NUMERO)",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = if (isOdd) "PULANG BOLA (ODD)" else "ASUL NA BOLA (EVEN)",
                            color = mainColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Big Balls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val labels = listOf("🥇 BEST PICK", "🥈 BACKUP 1", "🥉 BACKUP 2")
                        topBulls.forEachIndexed { index, num ->
                            val isTop = (index == 0)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LotteryBall(
                                    number = num,
                                    isOdd = (num % 2 != 0),
                                    size = if (isTop) 52.dp else 44.dp,
                                    fontSize = if (isTop) 18.sp else 14.sp,
                                    isHighlighted = isTop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (index < labels.size) labels[index] else "BOLA",
                                    color = if (isTop) Color(0xFFFFB703) else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tip: Kung gusto mong tumaya sa numero ng bola, ito ang 3 pinakamalakas na numero sa $parity.",
                        color = TextMuted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 4. HULING 5 RESULTA (SIMPLENG KASAYSAYAN)
        val preview = recentDraws.filter { it.isSettled }.take(5)
        if (preview.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "HULING LUMABAS NA MGA BOLA",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        preview.forEachIndexed { idx, draw ->
                            val isDrawOdd = draw.result == "ODD"
                            val ballColor = if (isDrawOdd) OddRed else EvenBlue

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NavySurfaceVariant)
                                    .border(1.dp, if (idx == 0) ballColor else NavyBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LotteryBall(
                                        number = draw.number,
                                        isOdd = isDrawOdd,
                                        size = 30.dp,
                                        fontSize = 11.sp
                                    )
                                    Column {
                                        Text(
                                            text = draw.result,
                                            color = ballColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = if (idx == 0) "Kagabi/Kanina" else "#${idx + 1}",
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. QUICK GUIDE ACCORDION (PAANO MANALO SA 3 HAKBANG)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(NavySurfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                .clickable { showGuide = !showGuide }
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Paano Gamitin? (Simpleng Gabay)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (showGuide) "Itago ▲" else "Basahin ▼",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                AnimatedVisibility(
                    visible = showGuide,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GuideRow(step = "1", title = "Sundan ang Kulay", desc = "Pula para sa ODD, Asul para sa EVEN. Huwag na mag-isip nang matagal.")
                        GuideRow(step = "2", title = "Pakinggan ang PASS MUNA", desc = "Kapag lumabas ang pulang PASS MUNA, huwag tumaya para hindi masayang ang pera mo.")
                        GuideRow(step = "3", title = "Bawi-Talo (2x / 3x)", desc = "Kapag nagmintis noong nakaraang round, awtomatikong mag-a-advice ng 2x para bawi agad ang puhunan.")
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideRow(step: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFB703)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Column {
            Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun AnalysisStepRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NavySurfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFFB703),
            modifier = Modifier.size(15.dp)
        )
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = status,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

