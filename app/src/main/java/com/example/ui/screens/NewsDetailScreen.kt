package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AdsConfig
import com.example.data.model.NewsItem
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsItem: NewsItem,
    relatedNews: List<NewsItem>,
    adsConfig: AdsConfig,
    onBack: () -> Unit,
    onOpenArticle: (NewsItem) -> Unit
) {
    val context = LocalContext.current
    var fontSizeMultiplier by remember { mutableFloatStateOf(1.0f) }
    var showFbPreview by remember { mutableStateOf(false) }

    val articleUrl = "https://golapinews.netlify.app/article.html?id=${newsItem.id}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = newsItem.category,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Slate900
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, newsItem.title)
                            putExtra(Intent.EXTRA_TEXT, "${newsItem.title}\n\n$articleUrl")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "সংবাদ শেয়ার করুন"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "শেয়ার",
                            tint = RosePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PureWhite)
                        .padding(16.dp)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(RoseContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = newsItem.category,
                            color = RoseDeep,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = newsItem.title,
                        fontSize = (22 * fontSizeMultiplier).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        lineHeight = (30 * fontSizeMultiplier).sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Author & Date Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate50)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(RoseLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✍️",
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = newsItem.author,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Text(
                                    text = "প্রকাশিত: ${newsItem.publishDate}",
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            }
                        }

                        // Text Resizer Controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Slate200,
                                modifier = Modifier.clickable {
                                    if (fontSizeMultiplier > 0.85f) fontSizeMultiplier -= 0.15f
                                }
                            ) {
                                Text(
                                    text = "A-",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RosePrimary,
                                modifier = Modifier.clickable {
                                    if (fontSizeMultiplier < 1.45f) fontSizeMultiplier += 0.15f
                                }
                            ) {
                                Text(
                                    text = "A+",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Social Share Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Facebook button
                        Button(
                            onClick = {
                                val fbUrl = "https://www.facebook.com/sharer/sharer.php?u=${Uri.encode(articleUrl)}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fbUrl)))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // WhatsApp button
                        Button(
                            onClick = {
                                val waUrl = "https://api.whatsapp.com/send?text=${Uri.encode(newsItem.title + "\n" + articleUrl)}"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Copy Link button
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("News URL", articleUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "সংবাদের লিংক কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Copy Link", fontSize = 11.sp, color = Slate800)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Facebook Share Preview Toggle button
                    OutlinedButton(
                        onClick = { showFbPreview = !showFbPreview },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RosePrimary)
                    ) {
                        Icon(
                            imageVector = if (showFbPreview) Icons.Default.VisibilityOff else Icons.Default.Preview,
                            contentDescription = "Facebook Preview",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showFbPreview) "ফেসবুক শেয়ার প্রিভিউ লুকান" else "ফেসবুক ওপেন গ্রাফ (OG) শেয়ার প্রিভিউ দেখুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Interactive Facebook Share Preview Card
                    if (showFbPreview) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                ) {
                                    AsyncImage(
                                        model = newsItem.imageUrl,
                                        contentDescription = "FB Share Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "GOLAPINEWS.NETLIFY.APP",
                                        color = Color(0xFF65676B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = newsItem.seoTitle.ifBlank { newsItem.title },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF050505),
                                        maxLines = 2
                                    )
                                    Text(
                                        text = newsItem.seoDescription.ifBlank { newsItem.shortDescription },
                                        color = Color(0xFF65676B),
                                        fontSize = 11.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Featured Image with Caption
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = newsItem.imageUrl,
                            contentDescription = newsItem.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (newsItem.imageCaption.isNotBlank()) {
                        Text(
                            text = "ছবি: ${newsItem.imageCaption}",
                            fontSize = 11.sp,
                            color = Slate500,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Highlight Quote
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RoseLight),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "“${newsItem.shortDescription}”",
                            color = RoseDeep,
                            fontSize = (14 * fontSizeMultiplier).sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = (22 * fontSizeMultiplier).sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // In-article Adsterra Banner
                    if (adsConfig.isEnabled && adsConfig.inArticleBannerActive) {
                        AdsterraBannerSlot(
                            format = AdFormat.IN_ARTICLE_468x60,
                            isActive = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Full Article Content
                    Text(
                        text = newsItem.fullContent,
                        fontSize = (16 * fontSizeMultiplier).sp,
                        color = Slate900,
                        lineHeight = (26 * fontSizeMultiplier).sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Related News Section
            item {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 18.dp)
                                .background(RosePrimary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সম্পর্কিত সংবাদ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                }
            }

            items(relatedNews) { rel ->
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    HorizontalNewsCard(
                        item = rel,
                        onClick = { onOpenArticle(rel) },
                        onShare = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${rel.title}\nhttps://golapinews.netlify.app/article.html?id=${rel.id}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "সংবাদ শেয়ার করুন"))
                        }
                    )
                }
            }
        }
    }
}
