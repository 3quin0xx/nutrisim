package com.nutrisim.india.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisim.india.ui.theme.*

@Composable
fun MacroCard(
    label: String,
    value: Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.07f))))
            .padding(horizontal = 8.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${value.toInt()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(unit, fontSize = 10.sp, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(label, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
fun CostSummaryCard(
    totalCost: Float,
    costPerGramProtein: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(AccentGold.copy(alpha = 0.22f), AccentGold.copy(alpha = 0.04f))))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Total Cost", fontSize = 12.sp, color = TextSecondary)
                Text(
                    "₹${String.format("%.1f", totalCost)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Cost / g Protein", fontSize = 12.sp, color = TextSecondary)
                Text(
                    "₹${String.format("%.2f", costPerGramProtein)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Saffron
                )
            }
        }
    }
}
