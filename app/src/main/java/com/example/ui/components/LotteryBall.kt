package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EvenBlue
import com.example.ui.theme.EvenBlueDark
import com.example.ui.theme.EvenBlueLight
import com.example.ui.theme.OddRed
import com.example.ui.theme.OddRedDark
import com.example.ui.theme.OddRedLight

@Composable
fun LotteryBall(
    number: Int,
    isOdd: Boolean = (number % 2 != 0),
    size: Dp = 38.dp,
    fontSize: TextUnit = 14.sp,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 3D Sphere styling - RED for ODD, BLUE for EVEN
    val gradientColors = if (isOdd) {
        listOf(
            OddRedLight,
            OddRed,
            OddRedDark
        )
    } else {
        listOf(
            EvenBlueLight,
            EvenBlue,
            EvenBlueDark
        )
    }

    val glowBorderColor = if (isOdd) Color(0xFFFF8595) else Color(0xFF93C5FD)

    Box(
        modifier = modifier
            .size(size)
            .shadow(if (isHighlighted) 8.dp else 4.dp, CircleShape, spotColor = if (isOdd) OddRed else EvenBlue)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = gradientColors,
                    radius = 80f
                )
            )
            .border(
                if (isHighlighted) 2.dp else 1.dp,
                if (isHighlighted) glowBorderColor else Color.White.copy(alpha = 0.35f),
                CircleShape
            )
            .testTag("lottery_ball_$number"),
        contentAlignment = Alignment.Center
    ) {
        // Inner semi-translucent sphere highlight for 3D depth
        Box(
            modifier = Modifier
                .size(size * 0.76f)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
