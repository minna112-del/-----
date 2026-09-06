package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class AdFormat(val label: String, val height: Dp) {
    HEADER_728x90("Adsterra 728x90 Header Banner", 70.dp),
    SIDEBAR_300x250("Adsterra 300x250 Sidebar Banner", 180.dp),
    IN_ARTICLE_468x60("Adsterra 468x60 In-Article Banner", 65.dp),
    FOOTER_728x90("Adsterra 728x90 Footer Banner", 70.dp),
    MOBILE_320x50("Adsterra 320x50 Mobile Sticky Banner", 52.dp)
}

@Composable
fun AdsterraBannerSlot(
    format: AdFormat,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle Ad Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(AdBadgeBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "বিজ্ঞাপন • Adsterra Verified Network",
                color = AdBadgeText,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Ad Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(format.height)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFFFFBEB))
                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = format.label,
                    color = Color(0xFF92400E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "High eCPM Responsive Ad Placement Ready",
                    color = Color(0xFFB45309),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
