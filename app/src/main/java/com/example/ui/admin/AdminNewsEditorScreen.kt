package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CategoryItem
import com.example.data.model.NewsItem
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNewsEditorScreen(
    editingItem: NewsItem?,
    categories: List<CategoryItem>,
    onSave: (NewsItem) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(editingItem?.title ?: "") }
    var category by remember { mutableStateOf(editingItem?.category ?: categories.firstOrNull()?.name ?: "জাতীয়") }
    var author by remember { mutableStateOf(editingItem?.author ?: "নিজস্ব প্রতিবেদক") }
    var publishDate by remember { mutableStateOf(editingItem?.publishDate ?: "০৫ সেপ্টেম্বর ২০২৬, বিকাল ০৩:০০") }
    var imageUrl by remember { mutableStateOf(editingItem?.imageUrl ?: "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=800") }
    var imageCaption by remember { mutableStateOf(editingItem?.imageCaption ?: "") }
    var shortDescription by remember { mutableStateOf(editingItem?.shortDescription ?: "") }
    var fullContent by remember { mutableStateOf(editingItem?.fullContent ?: "") }
    var videoUrl by remember { mutableStateOf(editingItem?.videoUrl ?: "") }
    var isFeatured by remember { mutableStateOf(editingItem?.isFeatured ?: false) }
    var isBreaking by remember { mutableStateOf(editingItem?.isBreaking ?: false) }
    var isPublished by remember { mutableStateOf(editingItem?.isPublished ?: true) }
    var seoTitle by remember { mutableStateOf(editingItem?.seoTitle ?: "") }
    var seoDescription by remember { mutableStateOf(editingItem?.seoDescription ?: "") }
    var ogImageUrl by remember { mutableStateOf(editingItem?.ogImageUrl ?: "") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingItem == null) "নতুন সংবাদ তৈরি করুন" else "সংবাদ সম্পাদনা",
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
                    Button(
                        onClick = {
                            if (title.isNotBlank() && shortDescription.isNotBlank()) {
                                val item = NewsItem(
                                    id = editingItem?.id ?: "news_${System.currentTimeMillis()}",
                                    title = title.trim(),
                                    slug = title.trim().replace(" ", "-").take(40),
                                    category = category,
                                    author = author.trim(),
                                    publishDate = publishDate.trim(),
                                    imageUrl = imageUrl.trim(),
                                    imageCaption = imageCaption.trim(),
                                    shortDescription = shortDescription.trim(),
                                    fullContent = fullContent.trim().ifEmpty { shortDescription.trim() },
                                    isFeatured = isFeatured,
                                    isBreaking = isBreaking,
                                    isPublished = isPublished,
                                    videoUrl = if (videoUrl.isNotBlank()) videoUrl.trim() else null,
                                    seoTitle = seoTitle.trim().ifEmpty { title.trim() },
                                    seoDescription = seoDescription.trim().ifEmpty { shortDescription.trim() },
                                    ogImageUrl = ogImageUrl.trim().ifEmpty { imageUrl.trim() }
                                )
                                onSave(item)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "সংরক্ষণ", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সংরক্ষণ", color = Color.White, fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // General Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "১. সাধারণ তথ্য ও শিরোনাম",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("সংবাদের মূল শিরোনাম *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Category Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("ক্যাটাগরি নির্বাচন করুন") },
                                trailingIcon = {
                                    Text(
                                        text = "▼",
                                        modifier = Modifier
                                            .clickable { categoryDropdownExpanded = true }
                                            .padding(8.dp),
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoryDropdownExpanded = true },
                                shape = RoundedCornerShape(8.dp)
                            )
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.filter { it.slug != "latest" }.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            category = cat.name
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = author,
                                onValueChange = { author = it },
                                label = { Text("প্রতিবেদক / লেখক") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = publishDate,
                                onValueChange = { publishDate = it },
                                label = { Text("প্রকাশের তারিখ ও সময়") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Image & Media Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "২. ছবি ও মাল্টিমিডিয়া",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )

                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("ফিচারড ছবির URL") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Image Preview
                        if (imageUrl.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "ছবি প্রিভিউ",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        OutlinedTextField(
                            value = imageCaption,
                            onValueChange = { imageCaption = it },
                            label = { Text("ছবির ক্যাপশন (ঐচ্ছিক)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = videoUrl,
                            onValueChange = { videoUrl = it },
                            label = { Text("ভিডিও বা ইউটিউব লিংক (ঐচ্ছিক)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Content & Description Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "৩. সংবাদের বিবরণ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )

                        OutlinedTextField(
                            value = shortDescription,
                            onValueChange = { shortDescription = it },
                            label = { Text("সংক্ষিপ্ত সারসংক্ষেপ (Excerpt) *") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = fullContent,
                            onValueChange = { fullContent = it },
                            label = { Text("সম্পূর্ণ প্রতিবেদন / মূল বডি *") },
                            minLines = 6,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Status & Visibility Flags Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "৪. প্রকাশনা ও প্লেসমেন্ট ফ্ল্যাগস",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("শীর্ষ সংবাদ (Featured)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("হোমপেজের মূল লিড নিউজ সেকশনে দেখাবে", fontSize = 11.sp, color = Slate500)
                            }
                            Switch(
                                checked = isFeatured,
                                onCheckedChange = { isFeatured = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RosePrimary, checkedTrackColor = RoseContainer)
                            )
                        }

                        Divider(color = Slate100)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ব্রেকিং নিউজ টিকার", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("টপ লাল টিকারে স্বয়ংক্রিয়ভাবে স্ক্রল করবে", fontSize = 11.sp, color = Slate500)
                            }
                            Switch(
                                checked = isBreaking,
                                onCheckedChange = { isBreaking = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RosePrimary, checkedTrackColor = RoseContainer)
                            )
                        }

                        Divider(color = Slate100)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("প্রকাশিত (Published)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("বন্ধ রাখলে খসড়া (Draft) হিসেবে থাকবে", fontSize = 11.sp, color = Slate500)
                            }
                            Switch(
                                checked = isPublished,
                                onCheckedChange = { isPublished = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RosePrimary, checkedTrackColor = RoseContainer)
                            )
                        }
                    }
                }
            }

            // SEO & Social Open Graph Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "৫. SEO ও ফেসবুক ওপেন গ্রাফ (OG)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )

                        OutlinedTextField(
                            value = seoTitle,
                            onValueChange = { seoTitle = it },
                            label = { Text("SEO মেটা টাইটেল (Title Tag)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = seoDescription,
                            onValueChange = { seoDescription = it },
                            label = { Text("SEO মেটা ডেসক্রিপশন") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = ogImageUrl,
                            onValueChange = { ogImageUrl = it },
                            label = { Text("Facebook Open Graph Image URL") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}
