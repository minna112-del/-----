package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.*
import com.example.generator.NetlifyGenerator
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebPreviewScreen(
    newsList: List<NewsItem>,
    categories: List<CategoryItem>,
    breakingNews: List<BreakingNewsItem>,
    adsConfig: AdsConfig,
    siteSettings: SiteSettings,
    onBack: () -> Unit
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    val htmlContent = remember(newsList, categories, breakingNews, adsConfig, siteSettings) {
        val css = NetlifyGenerator.generateStyleCss()
        val indexHtml = NetlifyGenerator.generateIndexHtml(
            newsList, categories, breakingNews, adsConfig, siteSettings
        )
        // Inject css directly for offline local webview preview
        indexHtml.replace(
            """<link rel="stylesheet" href="style.css" />""",
            """<style>$css</style>"""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "লাইভ ওয়েব প্রিভিউ (Netlify Mode)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "সম্পূর্ণ অফলাইন এইচটিএমএল/সিএসএস রেন্ডারিং",
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
                    IconButton(onClick = {
                        webViewInstance?.loadDataWithBaseURL(
                            "https://golapinews.netlify.app/",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "রিফ্রেশ",
                            tint = RosePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = WebViewClient()
                        loadDataWithBaseURL(
                            "https://golapinews.netlify.app/",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(
                        "https://golapinews.netlify.app/",
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
