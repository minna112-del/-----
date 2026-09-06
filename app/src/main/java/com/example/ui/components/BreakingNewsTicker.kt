package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BreakingNewsItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BreakingNewsTicker(
    items: List<BreakingNewsItem>,
    onItemClick: (BreakingNewsItem) -> Unit
) {
    if (items.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(items.size) {
        if (items.size > 1) {
            while (true) {
                delay(4500)
                currentIndex = (currentIndex + 1) % items.size
            }
        }
    }

    val currentItem = items.getOrNull(currentIndex) ?: items.first()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RoseLight)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable { onItemClick(currentItem) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red / Rose badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(RosePrimary)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "ব্রেকিং",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Animated text cycle
        AnimatedContent(
            targetState = currentItem,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn())
                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
            },
            label = "BreakingTicker"
        ) { target ->
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = target.headline,
                    color = RoseDeep,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
