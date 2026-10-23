package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.model.ProbabilityForecast
import com.example.data.model.SignalResult
import com.example.data.preferences.AppSettings
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
fun PredictionCard(
    forecast: ProbabilityForecast?,
    signal: SignalResult?,
    upcomingCountdownSeconds: Int = 0,
    forceRevealEarly: Boolean = false,
    onRevealEarly: (() -> Unit)? = null,
    onToggleInvert: (() -> Unit)? = null,
    onSelectStrategy: ((String) -> Unit)? = null,
    onTogglePaSureMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isAnalyzing = upcomingCountdownSeconds > 35 && !forceRevealEarly

    val isPassMuna = signal?.isPassMuna == true
    val isConfluenceLocked = signal?.isConfluenceLocked == true
    val recoveryMultiplier = signal?.recoveryMultiplier ?: 1
    val isRecoveryActive = recoveryMultiplier > 1

    val isSufficient = forecast?.isSufficientEvidence == true
    // Unified Single Source of Truth: Signal decision dictates parity so top and bottom NEVER disagree
    val predictedParity = signal?.signalType ?: if (isSufficient) forecast?.mostLikelyCategory ?: "50/50 NEUTRAL" else "INSUFFICIENT DATA"
    val isOdd = predictedParity == "ODD"
    val accentColor = if (isPassMuna) Color(0xFFE63946) else if (predictedParity == "EVEN") EvenBlue else if (isOdd) OddRed else Color(0xFFFFB703)
    val glowColor = if (isPassMuna) Color(0xFFE63946).copy(alpha = 0.22f) else if (predictedParity == "EVEN") EvenBlueGlow else if (isOdd) OddRedGlow else Color(0xFFFFB703).copy(alpha = 0.15f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prediction_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glowColor,
                            NavyCard
                        )
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: "NEXT RESULT ESTIMATE" + Mode Badge
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
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "NEXT RESULT ESTIMATE",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                // 100% PA-SURE Mode Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (signal?.paSureModeActive == true) Color(0xFFFFB703).copy(alpha = 0.25f) else NavySurfaceVariant)
                        .border(1.dp, if (signal?.paSureModeActive == true) Color(0xFFFFB703) else NavyBorder, RoundedCornerShape(8.dp))
                        .clickable { onTogglePaSureMode?.invoke() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (signal?.paSureModeActive == true) Color(0xFFFFB703) else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (signal?.paSureModeActive == true) "★ 100% PA-SURE" else "Standard",
                            color = if (signal?.paSureModeActive == true) Color(0xFFFFB703) else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 35s Analysis Buffer or Locked Badge
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFB703).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFFB703).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CrisisAlert,
                                contentDescription = null,
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "QUANT ENGINE IN ANALYSIS WINDOW (> 35s)",
                                color = Color(0xFFFFB703),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Model ensemble & Markov sequence converging. Verified signal locks at 0:35s (${upcomingCountdownSeconds - 35}s remaining).",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        if (onRevealEarly != null) {
                            OutlinedButton(
                                onClick = onRevealEarly,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB703)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB703)),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("⚡ Inspect Early Quantitative Hypothesis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF06D6A0).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF06D6A0).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🎯 35s CONFLUENCE LOCKED • PATTERN VERIFIED",
                            color = Color(0xFF06D6A0),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ==========================================
            // 1. "PASS MUNA" PROTOCOL (IWAS-TALO GATE)
            // ==========================================
            if (isPassMuna) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFE63946).copy(alpha = 0.22f))
                        .border(1.5.dp, Color(0xFFE63946), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("pass_muna_alert_box")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE63946),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "⚠️ PA-SURE: PASS MUNA SA ROUND NA ITO!",
                                color = Color(0xFFE63946),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Huwag tumaya kapag 50/50 o magulo ang ikot ng bola sa server para garantisadong hindi matalo ang pusta. Hintayin ang malinis na sure-win setup!",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ==========================================
            // 2. MASTER CONFLUENCE LOCK (DRAGON & CHOP WAVES)
            // ==========================================
            if (isConfluenceLocked && !isPassMuna) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF06D6A0).copy(alpha = 0.18f))
                        .border(1.2.dp, Color(0xFF06D6A0), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF06D6A0),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "★ 100% PA-SURE WIN CONFLUENCE LOCKED ★",
                                color = Color(0xFF06D6A0),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = signal?.confluenceMessage ?: "Dragon run / Chop rhythm consensus locked! Pinakamagandang round para tumaya.",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // ==========================================
            // 3. AGAD-AGAD NA BAWI-TALO RECOVERY SHIELD
            // ==========================================
            if (isRecoveryActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFB703).copy(alpha = 0.22f))
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
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "⚡ RECOVERY STAKE (${recoveryMultiplier}x STAKE MULTIPLIER)",
                                color = Color(0xFFFFB703),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Agad-Agad na Bawi-Talo Shield activated! Awtomatikong sinalubong ang bagong takbo ng bola para makabawi.",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Large Parity Hero Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isPassMuna) "PASS MUNA" else predictedParity,
                        color = accentColor,
                        fontSize = if (isPassMuna) 36.sp else 46.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.testTag("signal_value_text")
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isPassMuna) "Iwas-Talo Gate Active" else if (isRecoveryActive) "Bawi-Talo Priority Signal" else "Primary Forecast Parity",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Invert Signal & Quick Controls
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Confidence: ${signal?.confidence ?: 65}%",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.testTag("confidence_text")
                )

                if (onToggleInvert != null) {
                    OutlinedButton(
                        onClick = onToggleInvert,
                        modifier = Modifier.height(28.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (signal?.isInverted == true) Color(0xFFFFB703) else NavyBorder
                        ),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = if (signal?.isInverted == true) Color(0xFFFFB703) else TextSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (signal?.isInverted == true) "Revert Normal" else "Invert Signal",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isPassMuna) Color(0xFFE63946) else StatusConnected)
                    )
                    Text(
                        text = if (isPassMuna) "STANDBY" else "ACTIVE",
                        color = if (isPassMuna) Color(0xFFE63946) else StatusConnected,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = {
                    val oddProb = forecast?.estimatedOddProb ?: 0.5f
                    if (isOdd) oddProb else (1.0f - oddProb)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = NavySurfaceVariant
            )

            // ==========================================
            // 4. 8 HIGH-HIT BULLS (MGA BOLA) & SAFETY HEDGE
            // ==========================================
            if (signal != null) {
                Spacer(modifier = Modifier.height(14.dp))
                if (isPassMuna) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "⏸️ NAKA-PASS MUNA: Naka-pause ang mga tatayang numero habang 50/50 ang ikot ng server.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    val matchingBulls = signal.predictedNumbers.filter { num ->
                        if (isOdd) (num % 2 != 0) else (num % 2 == 0)
                    }
                    val displayBulls = if (matchingBulls.isNotEmpty()) matchingBulls else {
                        if (isOdd) listOf(1, 3, 5, 7, 9) else listOf(0, 2, 4, 6, 8)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NavySurfaceVariant.copy(alpha = 0.7f))
                            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                            .testTag("predicted_bulls_section")
                    ) {
                        Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CrisisAlert,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isOdd) "8 HIGH-HIT BULLS (ODD)" else "8 HIGH-HIT BULLS (EVEN)",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = signal.targetHighLow,
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3D Bulls (Balls) Row
                        val roles = listOf("TOP BULL", "NEIGHBOR", "SECTOR KEY", "CYCLE", "DUE BULL", "RADAR", "RESONANCE", "VELOCITY")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            displayBulls.forEachIndexed { idx, num ->
                                val isPrimary = (idx == 0)
                                val roleText = if (idx < roles.size) roles[idx] else "BULL #${idx + 1}"

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(if (isPrimary) 46.dp else 40.dp)
                                ) {
                                    LotteryBall(
                                        number = num,
                                        isOdd = (num % 2 != 0),
                                        size = if (isPrimary) 44.dp else 38.dp,
                                        fontSize = if (isPrimary) 15.sp else 12.sp,
                                        isHighlighted = isPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = roleText,
                                        color = if (isPrimary) accentColor else TextMuted,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Safety Hedge: Main pusta sa $predictedParity, backup hedge sa mga Bulls (${displayBulls.take(4).joinToString(", ") { "#$it" }}).",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

            // Strategy Selector Bar
            if (onSelectStrategy != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val strategies = listOf(
                    AppSettings.STRATEGY_SMART_ADAPTIVE to "Adaptive",
                    AppSettings.STRATEGY_TREND_DRAGON to "Dragon Run",
                    AppSettings.STRATEGY_CHOP_ALTERNATOR to "Chop Wave",
                    AppSettings.STRATEGY_PATTERN_NGRAM to "N-Gram Seq",
                    AppSettings.STRATEGY_CONTRARIAN to "Contrarian"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    strategies.forEach { (stratKey, label) ->
                        val isSelected = (signal?.strategyUsed == stratKey)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectStrategy(stratKey) },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor.copy(alpha = 0.25f),
                                selectedLabelColor = accentColor,
                                containerColor = NavySurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) accentColor else NavyBorder,
                                selectedBorderColor = accentColor,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // Expandable Mathematical Specifications & Section 5 Transparency Grid
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceVariant.copy(alpha = 0.4f))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Forecast Probabilities & Audit Grid",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (forecast != null) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Estimated Odd probability:", color = TextSecondary, fontSize = 11.sp)
                                    Text(String.format(Locale.US, "%.1f%%", forecast.estimatedOddProb * 100f), color = OddRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Estimated Even probability:", color = TextSecondary, fontSize = 11.sp)
                                    Text(String.format(Locale.US, "%.1f%%", forecast.estimatedEvenProb * 100f), color = EvenBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Model Used:", color = TextSecondary, fontSize = 11.sp)
                                    Text(forecast.modelType.displayName, color = TextPrimary, fontSize = 11.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Data Window:", color = TextSecondary, fontSize = 11.sp)
                                    Text("${forecast.dataWindow} verified results", color = TextPrimary, fontSize = 11.sp)
                                }
                                if (forecast.backtestedAccuracy != null) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Backtested performance:", color = TextSecondary, fontSize = 11.sp)
                                        Text(String.format(Locale.US, "%.1f%% acc | Brier: %.3f", forecast.backtestedAccuracy, forecast.backtestedBrierScore ?: 0.25f), color = StatusConnected, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = signal?.rationale ?: "Dragon/Chop Confluence and sequence memory.",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
