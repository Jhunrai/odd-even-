package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NormalizedDraw
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoundedBallGrid(
    drawsAscending: List<NormalizedDraw>,
    selectedCount: Int,
    onSelectCount: (Int) -> Unit,
    colorMappingVerified: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedDrawForDetail by remember { mutableStateOf<NormalizedDraw?>(null) }

    val recentDraws = drawsAscending.takeLast(selectedCount).reversed() // Newest first

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rounded_ball_grid_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header and Count Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RECENT DRAW OUTCOMES",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (colorMappingVerified) "Red: Odd / Blue: Even" else "Parity Beads (Color unverified)",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                // Window Filter Selector (10, 20, 50, 100, 720)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(10, 20, 50, 100, 720).forEach { count ->
                        val isSelected = (selectedCount == count)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCount(count) },
                            label = { Text("$count", fontSize = 10.sp) },
                            modifier = Modifier.height(26.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OddRed.copy(alpha = 0.25f),
                                selectedLabelColor = OddRed,
                                containerColor = NavySurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) OddRed else NavyBorder,
                                selectedBorderColor = OddRed,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ball Display FlowRow
            if (recentDraws.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No draw records available.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentDraws.forEach { draw ->
                        val isPending = !draw.isSettled || draw.result == "PENDING"
                        val isOdd = draw.result == "ODD"
                        val ballColor = if (isPending) Color(0xFFFFB703) else if (isOdd) OddRed else EvenBlue

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ballColor.copy(alpha = 0.2f))
                                .border(
                                    width = if (isPending) 1.5.dp else 1.2.dp,
                                    color = ballColor,
                                    shape = CircleShape
                                )
                                .clickable { selectedDrawForDetail = draw },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPending) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = "Pending draw",
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (isOdd) "O" else "E",
                                        color = ballColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (draw.number > 0) {
                                        Text(
                                            text = "${draw.number % 100}",
                                            color = ballColor.copy(alpha = 0.85f),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog on Ball Click
    selectedDrawForDetail?.let { draw ->
        AlertDialog(
            onDismissRequest = { selectedDrawForDetail = null },
            confirmButton = {
                TextButton(onClick = { selectedDrawForDetail = null }) {
                    Text("Close", color = TextPrimary)
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Draw #${draw.serial}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (!draw.isSettled) Color(0xFFFFB703).copy(alpha = 0.2f)
                                else if (draw.result == "ODD") OddRed.copy(alpha = 0.2f)
                                else EvenBlue.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = draw.result,
                            color = if (!draw.isSettled) Color(0xFFFFB703) else if (draw.result == "ODD") OddRed else EvenBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailRow("Record ID", draw.id)
                    DetailRow("Timestamp", draw.timestamp)
                    DetailRow("Scheduled Open", draw.openTime.ifEmpty { "N/A" })
                    DetailRow("Lottery Num", draw.lotteryNum.ifEmpty { "N/A" })
                    DetailRow("User Guess", draw.userGuess.ifEmpty { "None" })
                    DetailRow("Game State", draw.gameState)
                    DetailRow("Settlement Status", if (draw.isSettled) "VERIFIED COMPLETED" else "PENDING DRAW")
                    DetailRow("Computed Number", "${draw.number}")
                }
            },
            containerColor = NavyCard
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", color = TextSecondary, fontSize = 11.sp)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
