package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AppSettings
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusOffline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentSettings: AppSettings,
    onSaveApiSettings: (String, Int, Int, Int) -> Unit,
    onUpdateAutoRefresh: (Boolean, Int) -> Unit,
    onUpdateHaptic: (Boolean) -> Unit,
    onResetDefaults: () -> Unit,
    onClearCache: () -> Unit,
    onUpdatePatternStrategy: (String) -> Unit = {},
    onUpdateInvertSignal: (Boolean) -> Unit = {},
    onUpdateBullsCount: (Int) -> Unit = {},
    onUpdateAutoAntiLoss: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var urlText by remember(currentSettings.apiUrl) { mutableStateOf(currentSettings.apiUrl) }
    var pageText by remember(currentSettings.page) { mutableStateOf(currentSettings.page.toString()) }
    var limitText by remember(currentSettings.limit) { mutableStateOf(currentSettings.limit.toString()) }
    var typeText by remember(currentSettings.type) { mutableStateOf(currentSettings.type.toString()) }

    var showClearConfirm by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Title Row
        item {
            Column {
                Text(
                    text = "SETTINGS & TELEMETRY",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "API Endpoint, Pattern Analysis Engine & Anti-Loss Controls",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // 1. API Configuration Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_config_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = OddRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "API SERVICE CONFIGURATION",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        TextButton(
                            onClick = {
                                urlText = AppSettings.DEFAULT_API_URL
                                pageText = AppSettings.DEFAULT_PAGE.toString()
                                limitText = AppSettings.DEFAULT_LIMIT.toString()
                                typeText = AppSettings.DEFAULT_TYPE.toString()
                                onResetDefaults()
                                saveMessage = "Reset to default endpoint"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = EvenBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", color = EvenBlue, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("API Base URL:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_url_input"),
                        singleLine = false,
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OddRed,
                            unfocusedBorderColor = NavyBorder,
                            focusedContainerColor = NavySurfaceVariant,
                            unfocusedContainerColor = NavySurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Parameters Row: page, limit, type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Page
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Page", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = pageText,
                                onValueChange = { pageText = it },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OddRed,
                                    unfocusedBorderColor = NavyBorder,
                                    focusedContainerColor = NavySurfaceVariant,
                                    unfocusedContainerColor = NavySurfaceVariant,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.testTag("api_page_input")
                            )
                        }

                        // Limit
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Limit", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = limitText,
                                onValueChange = { limitText = it },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OddRed,
                                    unfocusedBorderColor = NavyBorder,
                                    focusedContainerColor = NavySurfaceVariant,
                                    unfocusedContainerColor = NavySurfaceVariant,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.testTag("api_limit_input")
                            )
                        }

                        // Type
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Type", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = typeText,
                                onValueChange = { typeText = it },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OddRed,
                                    unfocusedBorderColor = NavyBorder,
                                    focusedContainerColor = NavySurfaceVariant,
                                    unfocusedContainerColor = NavySurfaceVariant,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.testTag("api_type_input")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val p = pageText.toIntOrNull() ?: 1
                            val l = limitText.toIntOrNull() ?: 50
                            val t = typeText.toIntOrNull() ?: 24
                            onSaveApiSettings(urlText, p, l, t)
                            saveMessage = "API settings applied and syncing..."
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_api_settings_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OddRed, contentColor = TextPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply & Sync API", fontWeight = FontWeight.Bold)
                    }

                    if (saveMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = saveMessage!!,
                            color = StatusConnected,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 2. Pattern Analysis Strategy & Anti-Loss Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pattern_strategy_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = OddRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "PATTERN STRATEGY & ANTI-LOSS",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Calibrate algorithm momentum and numbering sequence",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Active Analysis Strategy:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val strategies = listOf(
                        Triple(
                            AppSettings.STRATEGY_SMART_ADAPTIVE,
                            "Smart Adaptive (Auto)",
                            "Auto-detects Dragon runs, Chop ping-pong, and N-gram sequences"
                        ),
                        Triple(
                            AppSettings.STRATEGY_TREND_DRAGON,
                            "Dragon Momentum (Runs)",
                            "Never bets against streaks; follows runs to prevent consecutive losses"
                        ),
                        Triple(
                            AppSettings.STRATEGY_CHOP_ALTERNATOR,
                            "Chop Alternator",
                            "Strict alternating sequence follower (ping-pong rhythm)"
                        ),
                        Triple(
                            AppSettings.STRATEGY_PATTERN_NGRAM,
                            "N-Gram Sequence",
                            "Deep historical sequence memory pattern matching"
                        ),
                        Triple(
                            AppSettings.STRATEGY_CONTRARIAN,
                            "Contrarian",
                            "Mean reversion mode to anticipate streak terminations"
                        )
                    )

                    strategies.forEach { (stratKey, title, desc) ->
                        val isSelected = currentSettings.patternStrategy == stratKey
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) OddRed.copy(alpha = 0.15f) else NavySurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) OddRed else NavyBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onUpdatePatternStrategy(stratKey) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = title,
                                        color = if (isSelected) OddRed else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = OddRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = desc,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto Anti-Loss Shield Mode Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (currentSettings.autoAntiLossProtection) StatusConnected else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Auto Anti-Loss Shield",
                                    color = if (currentSettings.autoAntiLossProtection) StatusConnected else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Automatically reverses signal if 2+ consecutive losses occur to stop counter-drift",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = currentSettings.autoAntiLossProtection,
                            onCheckedChange = { onUpdateAutoAntiLoss(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StatusConnected,
                                checkedTrackColor = NavyCard,
                                uncheckedTrackColor = NavyCard
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Manual Anti-Loss Invert Signal Mode Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = if (currentSettings.invertSignal) Color(0xFFFFB703) else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Manual Invert Signal Mode",
                                    color = if (currentSettings.invertSignal) Color(0xFFFFB703) else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Manually mirror signal output (ODD ↔ EVEN)",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = currentSettings.invertSignal,
                            onCheckedChange = { onUpdateInvertSignal(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFB703),
                                checkedTrackColor = NavyCard,
                                uncheckedTrackColor = NavyCard
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bulls Count Selector
                    Text("Predicted Bulls Count (Candidate Balls):", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(6, 8, 10, 14).forEach { count ->
                            val isSelected = currentSettings.bullsCount == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OddRed else NavySurfaceVariant)
                                    .clickable { onUpdateBullsCount(count) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count Bulls",
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Auto-Refresh Preferences Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_refresh_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = StatusConnected,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "AUTO-REFRESH INTERVAL",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (currentSettings.autoRefreshEnabled) "Polling every ${currentSettings.refreshIntervalSeconds}s" else "Disabled",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = currentSettings.autoRefreshEnabled,
                            onCheckedChange = { checked ->
                                onUpdateAutoRefresh(checked, currentSettings.refreshIntervalSeconds)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OddRed,
                                checkedTrackColor = NavySurfaceVariant,
                                uncheckedTrackColor = NavySurfaceVariant
                            ),
                            modifier = Modifier.testTag("auto_refresh_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Refresh Frequency:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30, 60).forEach { seconds ->
                            val isSelected = currentSettings.refreshIntervalSeconds == seconds
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OddRed else NavySurfaceVariant)
                                    .clickable {
                                        onUpdateAutoRefresh(currentSettings.autoRefreshEnabled, seconds)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${seconds}s",
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Hardware Haptic Feedback
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = EvenBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "HAPTIC FEEDBACK",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Vibrate briefly upon receiving new draw result",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = currentSettings.hapticFeedbackEnabled,
                        onCheckedChange = { checked ->
                            onUpdateHaptic(checked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EvenBlue,
                            checkedTrackColor = NavySurfaceVariant,
                            uncheckedTrackColor = NavySurfaceVariant
                        )
                    )
                }
            }
        }

        // 4. Cache & Data Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "DATA MANAGEMENT",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Clear offline Room database cache and reload fresh data from the server.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOffline),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusOffline.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_cache_button")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Local Cache & Re-sync", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Mathematical & Responsible Use Notice
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavySurfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = OddRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "STATISTICAL NOTICE",
                            color = OddRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "OddEven Live Analyzer processes raw live telemetry from the game API and provides mathematical indicators based on Markov transition frequencies and mean reversion. Independent random draw events have no guaranteed outcomes.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Local Database?") },
            text = { Text("This will purge all cached historical draws and signals from local storage and trigger a fresh sync from the API.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearCache()
                        saveMessage = "Local cache purged and resync initiated"
                    }
                ) {
                    Text("Clear Cache", color = StatusOffline, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = NavyCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}
