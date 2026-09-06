package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    newsList: List<NewsItem>,
    categories: List<CategoryItem>,
    breakingNews: List<BreakingNewsItem>,
    adsConfig: AdsConfig,
    siteSettings: SiteSettings,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenArticle: (NewsItem) -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenWebPreview: () -> Unit,
    onOpenStaticPage: (String) -> Unit
) {
    val context = LocalContext.current
    var isSearchVisible by remember { mutableStateOf(false) }

    // Filter published news
    val publishedNews = remember(newsList, selectedCategory, searchQuery) {
        newsList.filter { it.isPublished }
            .filter { item ->
                if (selectedCategory == "all" || selectedCategory == "সর্বশেষ") true
                else item.category.equals(selectedCategory, ignoreCase = true)
            }
            .filter { item ->
                if (searchQuery.isBlank()) true
                else item.title.contains(searchQuery, ignoreCase = true) ||
                     item.shortDescription.contains(searchQuery, ignoreCase = true)
            }
    }

    val leadNews = remember(publishedNews) {
        publishedNews.firstOrNull { it.isFeatured } ?: publishedNews.firstOrNull()
    }

    val otherNews = remember(publishedNews, leadNews) {
        publishedNews.filter { it.id != leadNews?.id }
    }

    val popularNews = remember(newsList) {
        newsList.sortedByDescending { it.viewsCount }.take(5)
    }

    val videoNews = remember(newsList) {
        newsList.filter { it.videoUrl != null || it.category == "বিনোদন" || it.category == "খেলাধুলা" }.take(4)
    }

    Scaffold(
        topBar = {
            GolapiTopBar(
                siteName = siteSettings.siteName,
                tagline = siteSettings.tagline,
                isAdminMode = false,
                onToggleAdmin = onOpenAdmin,
                searchQuery = searchQuery,
                onSearchChange = onSearchChange,
                isSearchVisible = isSearchVisible,
                onToggleSearch = { isSearchVisible = !isSearchVisible },
                onOpenWebPreview = onOpenWebPreview
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Breaking News Ticker
            item {
                BreakingNewsTicker(
                    items = breakingNews.filter { it.isActive },
                    onItemClick = { b ->
                        val target = newsList.firstOrNull { it.id == b.newsId }
                        if (target != null) {
                            onOpenArticle(target)
                        }
                    }
                )
            }

            // 2. Category Nav Bar
            item {
                CategoryNavBar(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelectCategory = onSelectCategory
                )
                Divider(color = Slate200, thickness = 1.dp)
            }

            // 3. Header Adsterra Banner (728x90)
            if (adsConfig.isEnabled && adsConfig.headerBannerActive) {
                item {
                    AdsterraBannerSlot(
                        format = AdFormat.HEADER_728x90,
                        isActive = true,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // 4. Lead / Featured News
            if (leadNews != null) {
                item {
                    Column(modifier = Modifier.padding(14.dp)) {
                        FeaturedLeadCard(
                            item = leadNews,
                            onClick = { onOpenArticle(leadNews) }
                        )
                    }
                }
            }

            // 5. In-Feed Adsterra Responsive Banner
            if (adsConfig.isEnabled && adsConfig.inArticleBannerActive) {
                item {
                    AdsterraBannerSlot(
                        format = AdFormat.IN_ARTICLE_468x60,
                        isActive = true,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // 6. Section Heading: সর্বশেষ সংবাদ
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = 4.dp, height = 18.dp)
                                .background(RosePrimary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedCategory == "all" || selectedCategory == "সর্বশেষ") "সর্বশেষ সংবাদ" else selectedCategory,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    Text(
                        text = "${otherNews.size} টি সংবাদ",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }

            // 7. News List Items
            items(otherNews) { item ->
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                    HorizontalNewsCard(
                        item = item,
                        onClick = { onOpenArticle(item) },
                        onShare = {
                            shareText(context, "${item.title}\n\nপড়ুন গোলাপি নিউজে: https://golapinews.netlify.app/article.html?id=${item.id}")
                        }
                    )
                }
            }

            // 8. Video & Multimedia Section
            if (videoNews.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp)
                            .background(PureWhite)
                            .padding(vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 4.dp, height = 18.dp)
                                    .background(RosePrimary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ভিডিও ও চিত্র সংবাদ",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(videoNews) { item ->
                                VideoCard(
                                    item = item,
                                    onClick = { onOpenArticle(item) }
                                )
                            }
                        }
                    }
                }
            }

            // 9. Sidebar Banner Simulation (300x250)
            if (adsConfig.isEnabled && adsConfig.sidebarBannerActive) {
                item {
                    AdsterraBannerSlot(
                        format = AdFormat.SIDEBAR_300x250,
                        isActive = true,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // 10. Most Read Section (সর্বাধিক পঠিত)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(width = 4.dp, height = 18.dp)
                                    .background(RosePrimary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সর্বাধিক পঠিত",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        popularNews.forEachIndexed { index, item ->
                            MostReadRow(
                                rank = index + 1,
                                item = item,
                                onClick = { onOpenArticle(item) }
                            )
                            if (index < popularNews.size - 1) {
                                Divider(color = Slate100, thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // 11. Footer Banner Ad (728x90)
            if (adsConfig.isEnabled && adsConfig.footerBannerActive) {
                item {
                    AdsterraBannerSlot(
                        format = AdFormat.FOOTER_728x90,
                        isActive = true,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // 12. Complete Portal Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RosePrimary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "গোলাপি",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "নিউজ",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = siteSettings.aboutUsText,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Slate800, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "বার্তা ও যোগাযোগ বিভাগ:",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ইমেইল: ${siteSettings.contactEmail}",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "ফোন: ${siteSettings.phone}",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "ঠিকানা: ${siteSettings.address}",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "আমাদের সম্পর্কে",
                            color = RoseLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onOpenStaticPage("about") }
                        )
                        Text(
                            text = "গোপনীয়তা নীতি",
                            color = RoseLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onOpenStaticPage("privacy") }
                        )
                        Text(
                            text = "শর্তাবলী",
                            color = RoseLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onOpenStaticPage("terms") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "© ২০২৬ ${siteSettings.siteName}। সকল অধিকার সংরক্ষিত।",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "সংবাদটি শেয়ার করুন"))
}
