package com.example.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.generator.NetlifyGenerator
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    newsList: List<NewsItem>,
    categories: List<CategoryItem>,
    breakingNews: List<BreakingNewsItem>,
    adsConfig: AdsConfig,
    siteSettings: SiteSettings,
    onSaveNews: (NewsItem) -> Unit,
    onDeleteNews: (String) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onToggleCategoryVisibility: (String, Boolean) -> Unit,
    onAddBreakingNews: (String) -> Unit,
    onDeleteBreakingNews: (String) -> Unit,
    onToggleBreakingActive: (String, Boolean) -> Unit,
    onSaveAdsConfig: (AdsConfig) -> Unit,
    onSaveSiteSettings: (SiteSettings) -> Unit,
    onOpenCreateNews: () -> Unit,
    onOpenEditNews: (NewsItem) -> Unit,
    onOpenWebPreview: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabTitles = listOf(
        "📊 ওভারভিউ",
        "✍️ সংবাদ",
        "🏷️ ক্যাটাগরি",
        "⚡ ব্রেকিং",
        "📢 অ্যাডসেরা",
        "⚙️ সেটিংস",
        "🚀 নেটলিফাই"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "গোলাপি নিউজ CMS অ্যাডমিন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "লাইভ ফাইল কন্ট্রোল ও কনফিগারেশন",
                            fontSize = 11.sp,
                            color = RosePrimary
                        )
                    }
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
                    IconButton(onClick = onOpenWebPreview) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "ওয়েব প্রিভিউ",
                            tint = RosePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = PureWhite,
                contentColor = RosePrimary,
                edgePadding = 12.dp,
                divider = { Divider(color = Slate200) }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) RosePrimary else Slate700
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> OverviewTab(
                    newsList = newsList,
                    categories = categories,
                    breakingNews = breakingNews,
                    adsConfig = adsConfig,
                    onOpenCreateNews = onOpenCreateNews,
                    onOpenNetlifyTab = { selectedTab = 6 },
                    onOpenWebPreview = onOpenWebPreview
                )
                1 -> NewsListTab(
                    newsList = newsList,
                    onAddNews = onOpenCreateNews,
                    onEditNews = onOpenEditNews,
                    onDeleteNews = onDeleteNews
                )
                2 -> CategoryTab(
                    categories = categories,
                    onAddCategory = onAddCategory,
                    onDeleteCategory = onDeleteCategory,
                    onToggleVisibility = onToggleCategoryVisibility
                )
                3 -> BreakingNewsTab(
                    breakingNews = breakingNews,
                    onAddBreaking = onAddBreakingNews,
                    onDeleteBreaking = onDeleteBreakingNews,
                    onToggleActive = onToggleBreakingActive
                )
                4 -> AdsConfigTab(
                    currentConfig = adsConfig,
                    onSave = {
                        onSaveAdsConfig(it)
                        Toast.makeText(context, "বিজ্ঞাপন সেটিংস সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                )
                5 -> SiteSettingsTab(
                    currentSettings = siteSettings,
                    onSave = {
                        onSaveSiteSettings(it)
                        Toast.makeText(context, "সাইট সেটিংস সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                )
                6 -> NetlifyLiveFilesTab(
                    newsList = newsList,
                    categories = categories,
                    breakingNews = breakingNews,
                    adsConfig = adsConfig,
                    siteSettings = siteSettings,
                    onOpenWebPreview = onOpenWebPreview
                )
            }
        }
    }
}

// ---------------- 1. OVERVIEW TAB ----------------
@Composable
private fun OverviewTab(
    newsList: List<NewsItem>,
    categories: List<CategoryItem>,
    breakingNews: List<BreakingNewsItem>,
    adsConfig: AdsConfig,
    onOpenCreateNews: () -> Unit,
    onOpenNetlifyTab: () -> Unit,
    onOpenWebPreview: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "মোট সংবাদ",
                    value = "${newsList.size}",
                    color = RosePrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "প্রকাশিত",
                    value = "${newsList.count { it.isPublished }}",
                    color = Color(0xFF059669),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "ক্যাটাগরি",
                    value = "${categories.size}",
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "ব্রেকিং নিউজ",
                    value = "${breakingNews.count { it.isActive }} টি চালু",
                    color = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "অ্যাডসেরা বিজ্ঞাপন",
                    value = if (adsConfig.isEnabled) "সক্রিয়" else "বন্ধ",
                    color = if (adsConfig.isEnabled) Color(0xFF059669) else Color(0xFFDC2626),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "দ্রুত অ্যাকশন (Quick Actions)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Button(
                        onClick = onOpenCreateNews,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নতুন সংবাদ পোস্ট করুন")
                    }

                    OutlinedButton(
                        onClick = onOpenNetlifyTab,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = RosePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নেটলিফাই লাইভ ফাইলস কন্ট্রোল ও এক্সপোর্ট", color = RosePrimary)
                    }

                    Button(
                        onClick = onOpenWebPreview,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অ্যাপের ভেতরে লাইভ ওয়েব প্রিভিউ দেখুন")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = Slate500)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

// ---------------- 2. NEWS LIST TAB ----------------
@Composable
private fun NewsListTab(
    newsList: List<NewsItem>,
    onAddNews: () -> Unit,
    onEditNews: (NewsItem) -> Unit,
    onDeleteNews: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 70.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সর্বমোট ${newsList.size} টি সংবাদ সংরক্ষিত",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }
            }

            items(newsList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(RoseContainer)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(item.category, color = RoseDeep, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                if (item.isFeatured) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("শীর্ষ", color = Color(0xFFB45309), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (item.isBreaking) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFFEE2E2))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("ব্রেকিং", color = Color(0xFFDC2626), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${item.author} • ${item.publishDate}",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(onClick = { onEditNews(item) }) {
                                Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = Slate700, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onDeleteNews(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "মুছে ফেলুন", tint = RosePrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // FAB to add news
        FloatingActionButton(
            onClick = onAddNews,
            containerColor = RosePrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "নতুন সংবাদ")
        }
    }
}

// ---------------- 3. CATEGORY TAB ----------------
@Composable
private fun CategoryTab(
    categories: List<CategoryItem>,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onToggleVisibility: (String, Boolean) -> Unit
) {
    var newCatName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("নতুন ক্যাটাগরি তৈরি করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCatName,
                            onValueChange = { newCatName = it },
                            placeholder = { Text("ক্যাটাগরির নাম (যেমন: অর্থনীতি)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (newCatName.isNotBlank()) {
                                    onAddCategory(newCatName.trim())
                                    newCatName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("যোগ করুন")
                        }
                    }
                }
            }
        }

        items(categories) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("slug: ${cat.slug}", fontSize = 11.sp, color = Slate500)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = cat.isVisible,
                            onCheckedChange = { onToggleVisibility(cat.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = RosePrimary, checkedTrackColor = RoseContainer)
                        )
                        if (cat.slug != "latest") {
                            IconButton(onClick = { onDeleteCategory(cat.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "মুছে ফেলুন", tint = RosePrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 4. BREAKING NEWS TAB ----------------
@Composable
private fun BreakingNewsTab(
    breakingNews: List<BreakingNewsItem>,
    onAddBreaking: (String) -> Unit,
    onDeleteBreaking: (String) -> Unit,
    onToggleActive: (String, Boolean) -> Unit
) {
    var newHeadline by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ব্রেকিং নিউজ হেডলাইন যুক্ত করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newHeadline,
                        onValueChange = { newHeadline = it },
                        placeholder = { Text("জরুরি ব্রেকিং শিরোনাম লিখুন...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newHeadline.isNotBlank()) {
                                onAddBreaking(newHeadline.trim())
                                newHeadline = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ব্রেকিং নিউজ চালু করুন")
                    }
                }
            }
        }

        items(breakingNews) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.headline, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoseDeep)
                        Text(item.timestamp, fontSize = 10.sp, color = Slate500)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = item.isActive,
                            onCheckedChange = { onToggleActive(item.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = RosePrimary, checkedTrackColor = RoseContainer)
                        )
                        IconButton(onClick = { onDeleteBreaking(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "মুছে ফেলুন", tint = RosePrimary)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 5. ADSTERRA ADS TAB ----------------
@Composable
private fun AdsConfigTab(
    currentConfig: AdsConfig,
    onSave: (AdsConfig) -> Unit
) {
    var isEnabled by remember { mutableStateOf(currentConfig.isEnabled) }
    var popunderActive by remember { mutableStateOf(currentConfig.popunderActive) }
    var popunderCode by remember { mutableStateOf(currentConfig.popunderCode) }
    var socialBarActive by remember { mutableStateOf(currentConfig.socialBarActive) }
    var socialBarCode by remember { mutableStateOf(currentConfig.socialBarCode) }
    var headerBannerActive by remember { mutableStateOf(currentConfig.headerBannerActive) }
    var headerBannerCode by remember { mutableStateOf(currentConfig.headerBannerCode) }
    var sidebarBannerActive by remember { mutableStateOf(currentConfig.sidebarBannerActive) }
    var sidebarBannerCode by remember { mutableStateOf(currentConfig.sidebarBannerCode) }
    var inArticleBannerActive by remember { mutableStateOf(currentConfig.inArticleBannerActive) }
    var inArticleBannerCode by remember { mutableStateOf(currentConfig.inArticleBannerCode) }
    var footerBannerActive by remember { mutableStateOf(currentConfig.footerBannerActive) }
    var footerBannerCode by remember { mutableStateOf(currentConfig.footerBannerCode) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("মাস্টার অ্যাড সুইচ (Master Switch)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("এক ক্লিকে সকল বিজ্ঞাপন চালু বা বন্ধ করুন", fontSize = 11.sp, color = Slate500)
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RosePrimary, checkedTrackColor = RoseContainer)
                    )
                }
            }
        }

        item {
            AdCodeSection(
                title = "১. Adsterra Popunder Code (Head Section)",
                subtitle = "উচ্চ eCPM এর জন্য সাইটের যে কোনো জায়গায় প্রথম ক্লিকে ট্রিগার হয়",
                isActive = popunderActive,
                onToggleActive = { popunderActive = it },
                code = popunderCode,
                onCodeChange = { popunderCode = it }
            )
        }

        item {
            AdCodeSection(
                title = "২. Adsterra Social Bar Code",
                subtitle = "ইন-পেজ পুশ নোটিফিকেশন বার যা ১০০% মোবাইল ফ্রেন্ডলি",
                isActive = socialBarActive,
                onToggleActive = { socialBarActive = it },
                code = socialBarCode,
                onCodeChange = { socialBarCode = it }
            )
        }

        item {
            AdCodeSection(
                title = "৩. Header Banner (728x90)",
                subtitle = "ওয়েবসাইটের লোগোর ডানপাশে প্রদর্শিত হবে",
                isActive = headerBannerActive,
                onToggleActive = { headerBannerActive = it },
                code = headerBannerCode,
                onCodeChange = { headerBannerCode = it }
            )
        }

        item {
            AdCodeSection(
                title = "৪. Sidebar Banner (300x250)",
                subtitle = "সাইডবারের সর্বাধিক পঠিত সংবাদের উপরে প্রদর্শিত হবে",
                isActive = sidebarBannerActive,
                onToggleActive = { sidebarBannerActive = it },
                code = sidebarBannerCode,
                onCodeChange = { sidebarBannerCode = it }
            )
        }

        item {
            AdCodeSection(
                title = "৫. In-Article Banner (468x60 / Responsive)",
                subtitle = "প্রতিটি সংবাদের মূল বডির মাঝখানে প্রদর্শিত হবে",
                isActive = inArticleBannerActive,
                onToggleActive = { inArticleBannerActive = it },
                code = inArticleBannerCode,
                onCodeChange = { inArticleBannerCode = it }
            )
        }

        item {
            AdCodeSection(
                title = "৬. Footer Banner (728x90)",
                subtitle = "ওয়েবসাইটের ফুটারে প্রদর্শিত হবে",
                isActive = footerBannerActive,
                onToggleActive = { footerBannerActive = it },
                code = footerBannerCode,
                onCodeChange = { footerBannerCode = it }
            )
        }

        item {
            Button(
                onClick = {
                    onSave(
                        AdsConfig(
                            isEnabled = isEnabled,
                            popunderActive = popunderActive,
                            popunderCode = popunderCode,
                            socialBarActive = socialBarActive,
                            socialBarCode = socialBarCode,
                            headerBannerActive = headerBannerActive,
                            headerBannerCode = headerBannerCode,
                            sidebarBannerActive = sidebarBannerActive,
                            sidebarBannerCode = sidebarBannerCode,
                            inArticleBannerActive = inArticleBannerActive,
                            inArticleBannerCode = inArticleBannerCode,
                            footerBannerActive = footerBannerActive,
                            footerBannerCode = footerBannerCode
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("বিজ্ঞাপন সেটিংস সংরক্ষণ করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun AdCodeSection(
    title: String,
    subtitle: String,
    isActive: Boolean,
    onToggleActive: (Boolean) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(subtitle, fontSize = 10.sp, color = Slate500)
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(checkedThumbColor = RosePrimary, checkedTrackColor = RoseContainer)
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    label = { Text("Adsterra Script বা HTML কোড পেস্ট করুন") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }
    }
}

// ---------------- 6. SITE SETTINGS TAB ----------------
@Composable
private fun SiteSettingsTab(
    currentSettings: SiteSettings,
    onSave: (SiteSettings) -> Unit
) {
    var siteName by remember { mutableStateOf(currentSettings.siteName) }
    var tagline by remember { mutableStateOf(currentSettings.tagline) }
    var contactEmail by remember { mutableStateOf(currentSettings.contactEmail) }
    var phone by remember { mutableStateOf(currentSettings.phone) }
    var address by remember { mutableStateOf(currentSettings.address) }
    var facebookUrl by remember { mutableStateOf(currentSettings.facebookUrl) }
    var youtubeUrl by remember { mutableStateOf(currentSettings.youtubeUrl) }
    var aboutUsText by remember { mutableStateOf(currentSettings.aboutUsText) }
    var privacyPolicyText by remember { mutableStateOf(currentSettings.privacyPolicyText) }
    var termsText by remember { mutableStateOf(currentSettings.termsText) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("সাধারণ সাইট তথ্য", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = siteName,
                        onValueChange = { siteName = it },
                        label = { Text("ওয়েবসাইটের নাম") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("ট্যাগলাইন / স্লোগান") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("যোগাযোগ ও সোশ্যাল তথ্য", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = contactEmail,
                        onValueChange = { contactEmail = it },
                        label = { Text("ইমেইল ঠিকানা") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("ফোন নম্বর") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("অফিস ঠিকানা") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = facebookUrl,
                        onValueChange = { facebookUrl = it },
                        label = { Text("Facebook পেজ লিংক") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = youtubeUrl,
                        onValueChange = { youtubeUrl = it },
                        label = { Text("YouTube চ্যানেল লিংক") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("আইনি ও তথ্য সংক্রান্ত পাতা", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = aboutUsText,
                        onValueChange = { aboutUsText = it },
                        label = { Text("আমাদের সম্পর্কে (About Us)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = privacyPolicyText,
                        onValueChange = { privacyPolicyText = it },
                        label = { Text("গোপনীয়তা নীতি (Privacy Policy)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = termsText,
                        onValueChange = { termsText = it },
                        label = { Text("শর্তাবলী (Terms)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSave(
                        SiteSettings(
                            siteName = siteName,
                            tagline = tagline,
                            contactEmail = contactEmail,
                            phone = phone,
                            address = address,
                            facebookUrl = facebookUrl,
                            youtubeUrl = youtubeUrl,
                            aboutUsText = aboutUsText,
                            privacyPolicyText = privacyPolicyText,
                            termsText = termsText
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("সাইট সেটিংস সংরক্ষণ করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ---------------- 7. NETLIFY LIVE FILES TAB ----------------
@Composable
private fun NetlifyLiveFilesTab(
    newsList: List<NewsItem>,
    categories: List<CategoryItem>,
    breakingNews: List<BreakingNewsItem>,
    adsConfig: AdsConfig,
    siteSettings: SiteSettings,
    onOpenWebPreview: () -> Unit
) {
    val context = LocalContext.current
    var selectedFile by remember { mutableStateOf("news-data.json") }

    val fileContent = remember(selectedFile, newsList, categories, breakingNews, adsConfig, siteSettings) {
        when (selectedFile) {
            "news-data.json" -> NetlifyGenerator.generateNewsDataJson(newsList, categories, breakingNews, adsConfig, siteSettings)
            "index.html" -> NetlifyGenerator.generateIndexHtml(newsList, categories, breakingNews, adsConfig, siteSettings)
            "article.html" -> NetlifyGenerator.generateArticleHtml(adsConfig, siteSettings)
            "style.css" -> NetlifyGenerator.generateStyleCss()
            "app.js" -> NetlifyGenerator.generateAppJs()
            "netlify.toml" -> NetlifyGenerator.generateNetlifyToml()
            "README.md" -> NetlifyGenerator.generateDeployReadme(siteSettings)
            else -> ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Netlify ডিপ্লয় ফাইল প্রস্তুত",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF166534)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "অ্যাডমিন প্যানেলে সংবাদ বা বিজ্ঞাপনের যেকোনো পরিবর্তন স্বয়ংক্রিয়ভাবে লাইভ ডিপ্লয় ফাইলগুলোতে আপডেট হয়। কোনো কোডিং জ্ঞান ছাড়া সহজেই Netlify Drop-এ আপলোড করে লাইভ সাইট আপডেট করতে পারেন।",
                        fontSize = 12.sp,
                        color = Color(0xFF15803D),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // File Selector Chips
        item {
            val files = listOf(
                "news-data.json",
                "index.html",
                "article.html",
                "style.css",
                "app.js",
                "netlify.toml",
                "README.md"
            )
            Text("ফাইল নির্বাচন করুন:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                files.take(4).forEach { fn ->
                    FilterChip(
                        selected = selectedFile == fn,
                        onClick = { selectedFile = fn },
                        label = { Text(fn, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RosePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                files.drop(4).forEach { fn ->
                    FilterChip(
                        selected = selectedFile == fn,
                        onClick = { selectedFile = fn },
                        label = { Text(fn, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RosePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(selectedFile, fileContent)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "$selectedFile এর কোড কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ফাইল কপি করুন", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, selectedFile)
                            putExtra(Intent.EXTRA_TEXT, fileContent)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "$selectedFile ফাইলটি শেয়ার / এক্সপোর্ট করুন"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("এক্সপোর্ট করুন", fontSize = 12.sp)
                }
            }
        }

        // Code Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ফাইল ভিউয়ার: $selectedFile",
                        color = RoseLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fileContent.take(1600) + if (fileContent.length > 1600) "\n\n... (আরও ${fileContent.length - 1600} অক্ষর নিচে রয়েছে)" else "",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Setup Guide Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📖 Netlify-তে লাইভ করার ৩টি সহজ ধাপ:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Text(
                        text = "১. আপনার ব্রাউজারে app.netlify.com/drop ওপেন করুন।\n" +
                               "২. 'public' ফোল্ডারের ফাইলগুলো ড্র্যাগ করে সেখানে ছেড়ে দিন।\n" +
                               "৩. মাত্র ১ মিনিটের মধ্যে আপনার ওয়েবসাইট বিশ্বব্যাপী লাইভ হয়ে যাবে এবং ফ্রি SSL ও কাস্টম ডোমেইন পাবেন।",
                        fontSize = 12.sp,
                        color = Slate700,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
