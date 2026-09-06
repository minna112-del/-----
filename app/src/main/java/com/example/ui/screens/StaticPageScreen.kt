package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SiteSettings
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaticPageScreen(
    pageKey: String,
    siteSettings: SiteSettings,
    onBack: () -> Unit
) {
    val title = when (pageKey) {
        "about" -> "আমাদের সম্পর্কে"
        "privacy" -> "গোপনীয়তা নীতি"
        "terms" -> "ব্যবহারের শর্তাবলী"
        else -> "তথ্য"
    }

    val content = when (pageKey) {
        "about" -> siteSettings.aboutUsText
        "privacy" -> siteSettings.privacyPolicyText
        "terms" -> siteSettings.termsText
        else -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontSize = 17.sp,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        containerColor = Slate50
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RoseContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = siteSettings.siteName,
                                color = RoseDeep,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Slate200)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = content,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "যোগাযোগের ঠিকানা:\n${siteSettings.address}\nইমেইল: ${siteSettings.contactEmail}\nফোন: ${siteSettings.phone}",
                            fontSize = 13.sp,
                            color = Slate600,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
