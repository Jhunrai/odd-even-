package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ForecastModelType
import com.example.data.model.ProbabilityForecast
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.OddRed
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ForecastComparisonScreen(
    forecasts: List<ProbabilityForecast>,
    activeModel: ForecastModelType,
    onSelectModel: (ForecastModelType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("forecast_comparison_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Header Title
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = OddRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "PROBABILITY FORECAST MODELS",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Compare 5 Statistical Models with Laplace Smoothing",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Model Cards
        items(forecasts, key = { it.modelType.name }) { forecast ->
            val isActive = activeModel == forecast.modelType
            val isOdd = forecast.mostLikelyCategory == "ODD"
            val accentColor = if (isOdd) OddRed else EvenBlue

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("model_card_${forecast.modelType.name}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                border = androidx.compose.foundation.BorderStroke(
                    if (isActive) 1.5.dp else 1.dp,
                    if (isActive) OddRed else NavyBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = forecast.modelType.displayName,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = forecast.modelType.description,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OddRed.copy(alpha = 0.2f))
                                    .border(1.dp, OddRed, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = OddRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Estimated Probabilities
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavySurfaceVariant.copy(alpha = 0.6f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "ESTIMATED ODD", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale.US, "%.1f%%", forecast.estimatedOddProb * 100f),
                                color = OddRed,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "MOST LIKELY", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = forecast.mostLikelyCategory,
                                color = accentColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "ESTIMATED EVEN", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format(Locale.US, "%.1f%%", forecast.estimatedEvenProb * 100f),
                                color = EvenBlue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { forecast.estimatedOddProb },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = OddRed,
                        trackColor = EvenBlue
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Backtested Stats Line
                    if (forecast.backtestedAccuracy != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Backtest: %.1f%% acc | Brier: %.3f",
                                    forecast.backtestedAccuracy,
                                    forecast.backtestedBrierScore ?: 0.25f
                                ),
                                color = StatusConnected,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )

                            if (!isActive) {
                                Button(
                                    onClick = { onSelectModel(forecast.modelType) },
                                    modifier = Modifier.height(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text("Set as Main", fontSize = 10.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
