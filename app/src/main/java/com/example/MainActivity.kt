package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.model.NewsItem
import com.example.data.repository.NewsRepository
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.admin.AdminLoginDialog
import com.example.ui.admin.AdminNewsEditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewsDetailScreen
import com.example.ui.screens.StaticPageScreen
import com.example.ui.screens.WebPreviewScreen
import com.example.ui.theme.GolapiNewsTheme
import com.example.ui.theme.Slate50

sealed class AppScreen {
    data object Home : AppScreen()
    data class NewsDetail(val newsId: String) : AppScreen()
    data object AdminDashboard : AppScreen()
    data class AdminNewsEditor(val newsId: String?) : AppScreen()
    data object WebPreview : AppScreen()
    data class StaticPage(val pageKey: String) : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = NewsRepository(this)

        setContent {
            GolapiNewsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate50
                ) {
                    val newsList by repository.newsList.collectAsState()
                    val categories by repository.categories.collectAsState()
                    val breakingNews by repository.breakingNews.collectAsState()
                    val adsConfig by repository.adsConfig.collectAsState()
                    val siteSettings by repository.siteSettings.collectAsState()

                    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
                    var showAdminLogin by remember { mutableStateOf(false) }
                    var isAdminAuthenticated by remember { mutableStateOf(false) }
                    var selectedCategory by remember { mutableStateOf("all") }
                    var searchQuery by remember { mutableStateOf("") }

                    // Handle Back Press
                    BackHandler(enabled = currentScreen != AppScreen.Home) {
                        when (currentScreen) {
                            is AppScreen.AdminNewsEditor -> currentScreen = AppScreen.AdminDashboard
                            else -> currentScreen = AppScreen.Home
                        }
                    }

                    when (val screen = currentScreen) {
                        is AppScreen.Home -> {
                            HomeScreen(
                                newsList = newsList,
                                categories = categories,
                                breakingNews = breakingNews,
                                adsConfig = adsConfig,
                                siteSettings = siteSettings,
                                selectedCategory = selectedCategory,
                                onSelectCategory = { selectedCategory = it },
                                searchQuery = searchQuery,
                                onSearchChange = { searchQuery = it },
                                onOpenArticle = { item ->
                                    currentScreen = AppScreen.NewsDetail(item.id)
                                },
                                onOpenAdmin = {
                                    if (isAdminAuthenticated) {
                                        currentScreen = AppScreen.AdminDashboard
                                    } else {
                                        showAdminLogin = true
                                    }
                                },
                                onOpenWebPreview = {
                                    currentScreen = AppScreen.WebPreview
                                },
                                onOpenStaticPage = { key ->
                                    currentScreen = AppScreen.StaticPage(key)
                                }
                            )
                        }

                        is AppScreen.NewsDetail -> {
                            val article = newsList.firstOrNull { it.id == screen.newsId } ?: newsList.firstOrNull()
                            if (article != null) {
                                val related = newsList.filter { it.id != article.id && it.category == article.category }
                                    .ifEmpty { newsList.filter { it.id != article.id } }
                                    .take(3)

                                NewsDetailScreen(
                                    newsItem = article,
                                    relatedNews = related,
                                    adsConfig = adsConfig,
                                    onBack = { currentScreen = AppScreen.Home },
                                    onOpenArticle = { currentScreen = AppScreen.NewsDetail(it.id) }
                                )
                            } else {
                                currentScreen = AppScreen.Home
                            }
                        }

                        is AppScreen.AdminDashboard -> {
                            AdminDashboardScreen(
                                newsList = newsList,
                                categories = categories,
                                breakingNews = breakingNews,
                                adsConfig = adsConfig,
                                siteSettings = siteSettings,
                                onSaveNews = { repository.saveNews(it) },
                                onDeleteNews = { repository.deleteNews(it) },
                                onAddCategory = { repository.addCategoryByName(it) },
                                onDeleteCategory = { repository.deleteCategory(it) },
                                onToggleCategoryVisibility = { id, visible -> repository.toggleCategoryVisibility(id, visible) },
                                onAddBreakingNews = { repository.addBreakingNews(it) },
                                onDeleteBreakingNews = { repository.deleteBreakingNews(it) },
                                onToggleBreakingActive = { id, active -> repository.toggleBreakingNewsActive(id, active) },
                                onSaveAdsConfig = { repository.saveAdsConfig(it) },
                                onSaveSiteSettings = { repository.saveSiteSettings(it) },
                                onOpenCreateNews = { currentScreen = AppScreen.AdminNewsEditor(null) },
                                onOpenEditNews = { currentScreen = AppScreen.AdminNewsEditor(it.id) },
                                onOpenWebPreview = { currentScreen = AppScreen.WebPreview },
                                onBack = { currentScreen = AppScreen.Home }
                            )
                        }

                        is AppScreen.AdminNewsEditor -> {
                            val editingItem = if (screen.newsId != null) {
                                newsList.firstOrNull { it.id == screen.newsId }
                            } else null

                            AdminNewsEditorScreen(
                                editingItem = editingItem,
                                categories = categories,
                                onSave = { saved ->
                                    repository.saveNews(saved)
                                    currentScreen = AppScreen.AdminDashboard
                                },
                                onBack = { currentScreen = AppScreen.AdminDashboard }
                            )
                        }

                        is AppScreen.WebPreview -> {
                            WebPreviewScreen(
                                newsList = newsList,
                                categories = categories,
                                breakingNews = breakingNews,
                                adsConfig = adsConfig,
                                siteSettings = siteSettings,
                                onBack = { currentScreen = AppScreen.Home }
                            )
                        }

                        is AppScreen.StaticPage -> {
                            StaticPageScreen(
                                pageKey = screen.pageKey,
                                siteSettings = siteSettings,
                                onBack = { currentScreen = AppScreen.Home }
                            )
                        }
                    }

                    // Admin Login Modal Dialog
                    if (showAdminLogin) {
                        AdminLoginDialog(
                            onDismiss = { showAdminLogin = false },
                            onLoginSuccess = {
                                showAdminLogin = false
                                isAdminAuthenticated = true
                                currentScreen = AppScreen.AdminDashboard
                            }
                        )
                    }
                }
            }
        }
    }
}
