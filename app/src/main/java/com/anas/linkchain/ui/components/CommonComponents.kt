package com.anas.linkchain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anas.linkchain.ui.theme.DarkAccent
import com.anas.linkchain.ui.theme.DarkAccentSoft

@Composable
fun MadeByAnasBadge(modifier: Modifier = Modifier) {
    val goldGradient = Brush.horizontalGradient(
        colors = listOf(DarkAccent, DarkAccentSoft, DarkAccent)
    )
    Text(
        text = "Made by Anas",
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = modifier
            .background(goldGradient, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}