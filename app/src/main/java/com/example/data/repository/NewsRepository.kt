package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NewsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("golapi_news_prefs", Context.MODE_PRIVATE)

    private val _newsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsList: StateFlow<List<NewsItem>> = _newsList.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _breakingNews = MutableStateFlow<List<BreakingNewsItem>>(emptyList())
    val breakingNews: StateFlow<List<BreakingNewsItem>> = _breakingNews.asStateFlow()

    private val _adsConfig = MutableStateFlow(AdsConfig())
    val adsConfig: StateFlow<AdsConfig> = _adsConfig.asStateFlow()

    private val _siteSettings = MutableStateFlow(SiteSettings())
    val siteSettings: StateFlow<SiteSettings> = _siteSettings.asStateFlow()

    private val _isBreakingActive = MutableStateFlow(true)
    val isBreakingActive: StateFlow<Boolean> = _isBreakingActive.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val newsJson = prefs.getString("news_list", null)
        if (newsJson != null) {
            _newsList.value = parseNewsList(newsJson)
        } else {
            _newsList.value = getInitialNews()
            saveNewsList()
        }

        val catJson = prefs.getString("categories", null)
        if (catJson != null) {
            _categories.value = parseCategories(catJson)
        } else {
            _categories.value = getInitialCategories()
            saveCategories()
        }

        val breakingJson = prefs.getString("breaking_news", null)
        if (breakingJson != null) {
            _breakingNews.value = parseBreaking(breakingJson)
        } else {
            _breakingNews.value = getInitialBreaking()
            saveBreaking()
        }

        _isBreakingActive.value = prefs.getBoolean("breaking_active", true)

        // Load Ads
        _adsConfig.value = AdsConfig(
            isEnabled = prefs.getBoolean("ads_enabled", true),
            popunderCode = prefs.getString("ads_popunder", AdsConfig().popunderCode) ?: AdsConfig().popunderCode,
            popunderActive = prefs.getBoolean("ads_popunder_active", true),
            socialBarCode = prefs.getString("ads_socialbar", AdsConfig().socialBarCode) ?: AdsConfig().socialBarCode,
            socialBarActive = prefs.getBoolean("ads_socialbar_active", true),
            headerBannerCode = prefs.getString("ads_header", AdsConfig().headerBannerCode) ?: AdsConfig().headerBannerCode,
            headerBannerActive = prefs.getBoolean("ads_header_active", true),
            sidebarBannerCode = prefs.getString("ads_sidebar", AdsConfig().sidebarBannerCode) ?: AdsConfig().sidebarBannerCode,
            sidebarBannerActive = prefs.getBoolean("ads_sidebar_active", true),
            inArticleBannerCode = prefs.getString("ads_inarticle", AdsConfig().inArticleBannerCode) ?: AdsConfig().inArticleBannerCode,
            inArticleBannerActive = prefs.getBoolean("ads_inarticle_active", true),
            footerBannerCode = prefs.getString("ads_footer", AdsConfig().footerBannerCode) ?: AdsConfig().footerBannerCode,
            footerBannerActive = prefs.getBoolean("ads_footer_active", true),
            mobileBannerCode = prefs.getString("ads_mobile", AdsConfig().mobileBannerCode) ?: AdsConfig().mobileBannerCode,
            mobileBannerActive = prefs.getBoolean("ads_mobile_active", true)
        )

        // Load Settings
        val rawEmail = prefs.getString("site_email", "golapishoponline.bd@gmail.com") ?: "golapishoponline.bd@gmail.com"
        val realEmail = if (rawEmail == "editor@golapinews.com" || rawEmail.isBlank()) "golapishoponline.bd@gmail.com" else rawEmail

        val rawPhone = prefs.getString("site_phone", "01612-057371") ?: "01612-057371"
        val realPhone = if (rawPhone.contains("1711") || rawPhone.contains("১৭১১") || rawPhone.isBlank()) "01612-057371" else rawPhone

        val rawAddress = prefs.getString("site_address", "চৌরাস্তা ,বেগমগণ্জ, নোয়াখালী , বাংলাদেশ") ?: "চৌরাস্তা ,বেগমগণ্জ, নোয়াখালী , বাংলাদেশ"
        val realAddress = if (rawAddress.contains("বাজার") || rawAddress.isBlank()) "চৌরাস্তা ,বেগমগণ্জ, নোয়াখালী , বাংলাদেশ" else rawAddress

        _siteSettings.value = SiteSettings(
            siteName = prefs.getString("site_name", "গোলাপি নিউজ") ?: "গোলাপি নিউজ",
            tagline = prefs.getString("site_tagline", "সত্যের নির্ভীক সারথি — আধুনিক ডিজিটাল বাংলা সংবাদপত্র") ?: "সত্যের নির্ভীক সারথি — আধুনিক ডিজিটাল বাংলা সংবাদপত্র",
            contactEmail = realEmail,
            phone = realPhone,
            address = realAddress,
            facebookUrl = prefs.getString("site_facebook", "https://facebook.com/golapinews") ?: "https://facebook.com/golapinews",
            youtubeUrl = prefs.getString("site_youtube", "https://youtube.com/@golapinews") ?: "https://youtube.com/@golapinews"
        )
        // Keep preferences synchronized with real data
        prefs.edit().apply {
            putString("site_email", realEmail)
            putString("site_phone", realPhone)
            putString("site_address", realAddress)
            apply()
        }
    }

    // CRUD NEWS
    fun addNews(news: NewsItem) {
        val current = _newsList.value.toMutableList()
        current.add(0, news)
        _newsList.value = current
        saveNewsList()
    }

    fun updateNews(news: NewsItem) {
        val current = _newsList.value.toMutableList()
        val index = current.indexOfFirst { it.id == news.id }
        if (index != -1) {
            current[index] = news
            _newsList.value = current
            saveNewsList()
        }
    }

    fun deleteNews(id: String) {
        val current = _newsList.value.toMutableList()
        current.removeAll { it.id == id }
        _newsList.value = current
        saveNewsList()
    }

    fun togglePublish(id: String) {
        val current = _newsList.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(isPublished = !item.isPublished)
            _newsList.value = current
            saveNewsList()
        }
    }

    fun incrementViews(id: String) {
        val current = _newsList.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(viewsCount = item.viewsCount + 1)
            _newsList.value = current
            saveNewsList()
        }
    }

    // CRUD CATEGORIES
    fun addCategory(cat: CategoryItem) {
        val current = _categories.value.toMutableList()
        current.add(cat)
        _categories.value = current
        saveCategories()
    }

    fun updateCategory(cat: CategoryItem) {
        val current = _categories.value.toMutableList()
        val index = current.indexOfFirst { it.id == cat.id }
        if (index != -1) {
            current[index] = cat
            _categories.value = current
            saveCategories()
        }
    }

    fun deleteCategory(id: String) {
        val current = _categories.value.toMutableList()
        current.removeAll { it.id == id }
        _categories.value = current
        saveCategories()
    }

    // BREAKING NEWS
    fun setBreakingActive(active: Boolean) {
        _isBreakingActive.value = active
        prefs.edit().putBoolean("breaking_active", active).apply()
    }

    fun addBreaking(item: BreakingNewsItem) {
        val current = _breakingNews.value.toMutableList()
        current.add(0, item)
        _breakingNews.value = current
        saveBreaking()
    }

    fun updateBreaking(item: BreakingNewsItem) {
        val current = _breakingNews.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index != -1) {
            current[index] = item
            _breakingNews.value = current
            saveBreaking()
        }
    }

    fun deleteBreaking(id: String) {
        val current = _breakingNews.value.toMutableList()
        current.removeAll { it.id == id }
        _breakingNews.value = current
        saveBreaking()
    }

    // ADS
    fun updateAdsConfig(config: AdsConfig) {
        _adsConfig.value = config
        prefs.edit().apply {
            putBoolean("ads_enabled", config.isEnabled)
            putString("ads_popunder", config.popunderCode)
            putBoolean("ads_popunder_active", config.popunderActive)
            putString("ads_socialbar", config.socialBarCode)
            putBoolean("ads_socialbar_active", config.socialBarActive)
            putString("ads_header", config.headerBannerCode)
            putBoolean("ads_header_active", config.headerBannerActive)
            putString("ads_sidebar", config.sidebarBannerCode)
            putBoolean("ads_sidebar_active", config.sidebarBannerActive)
            putString("ads_inarticle", config.inArticleBannerCode)
            putBoolean("ads_inarticle_active", config.inArticleBannerActive)
            putString("ads_footer", config.footerBannerCode)
            putBoolean("ads_footer_active", config.footerBannerActive)
            putString("ads_mobile", config.mobileBannerCode)
            putBoolean("ads_mobile_active", config.mobileBannerActive)
            apply()
        }
    }

    // SETTINGS
    fun updateSiteSettings(settings: SiteSettings) {
        _siteSettings.value = settings
        prefs.edit().apply {
            putString("site_name", settings.siteName)
            putString("site_tagline", settings.tagline)
            putString("site_email", settings.contactEmail)
            putString("site_phone", settings.phone)
            putString("site_address", settings.address)
            putString("site_facebook", settings.facebookUrl)
            putString("site_youtube", settings.youtubeUrl)
            apply()
        }
    }

    // CONVENIENCE WRAPPER METHODS
    fun saveNews(news: NewsItem) {
        val current = _newsList.value.toMutableList()
        val index = current.indexOfFirst { it.id == news.id }
        if (index != -1) {
            current[index] = news
        } else {
            current.add(0, news)
        }
        _newsList.value = current
        saveNewsList()
    }

    fun addCategoryByName(name: String) {
        val id = "cat_${System.currentTimeMillis()}"
        val slug = name.lowercase().replace(" ", "-")
        addCategory(CategoryItem(id = id, name = name, slug = slug, order = _categories.value.size + 1, isVisible = true))
    }

    fun toggleCategoryVisibility(id: String, isVisible: Boolean) {
        val current = _categories.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(isVisible = isVisible)
            _categories.value = current
            saveCategories()
        }
    }

    fun addBreakingNews(headline: String) {
        val item = BreakingNewsItem(
            id = "brk_${System.currentTimeMillis()}",
            headline = headline,
            timestamp = "এইমাত্র",
            isActive = true
        )
        addBreaking(item)
    }

    fun deleteBreakingNews(id: String) {
        deleteBreaking(id)
    }

    fun toggleBreakingNewsActive(id: String, active: Boolean) {
        val current = _breakingNews.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(isActive = active)
            _breakingNews.value = current
            saveBreaking()
        }
    }

    fun saveAdsConfig(config: AdsConfig) {
        updateAdsConfig(config)
    }

    fun saveSiteSettings(settings: SiteSettings) {
        updateSiteSettings(settings)
    }

    // Persistence helpers
    private fun saveNewsList() {
        val arr = JSONArray()
        _newsList.value.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("slug", item.slug)
                put("category", item.category)
                put("author", item.author)
                put("authorAvatar", item.authorAvatar)
                put("publishDate", item.publishDate)
                put("updateDate", item.updateDate)
                put("imageUrl", item.imageUrl)
                put("imageCaption", item.imageCaption)
                put("shortDescription", item.shortDescription)
                put("fullContent", item.fullContent)
                put("isFeatured", item.isFeatured)
                put("isBreaking", item.isBreaking)
                put("isPublished", item.isPublished)
                put("viewsCount", item.viewsCount)
                put("videoUrl", item.videoUrl ?: "")
                put("seoTitle", item.seoTitle)
                put("seoDescription", item.seoDescription)
                put("seoKeywords", item.seoKeywords)
                put("ogImageUrl", item.ogImageUrl)
            }
            arr.put(obj)
        }
        prefs.edit().putString("news_list", arr.toString()).apply()
    }

    private fun parseNewsList(json: String): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    NewsItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.getString("title"),
                        slug = obj.optString("slug", "post-$i"),
                        category = obj.getString("category"),
                        author = obj.optString("author", "নিজস্ব প্রতিবেদক"),
                        authorAvatar = obj.optString("authorAvatar", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80"),
                        publishDate = obj.optString("publishDate", "০৫ সেপ্টেম্বর ২০২৬"),
                        updateDate = obj.optString("updateDate", ""),
                        imageUrl = obj.getString("imageUrl"),
                        imageCaption = obj.optString("imageCaption", ""),
                        shortDescription = obj.getString("shortDescription"),
                        fullContent = obj.getString("fullContent"),
                        isFeatured = obj.optBoolean("isFeatured", false),
                        isBreaking = obj.optBoolean("isBreaking", false),
                        isPublished = obj.optBoolean("isPublished", true),
                        viewsCount = obj.optInt("viewsCount", 100),
                        videoUrl = obj.optString("videoUrl").takeIf { it.isNotEmpty() },
                        seoTitle = obj.optString("seoTitle", ""),
                        seoDescription = obj.optString("seoDescription", ""),
                        seoKeywords = obj.optString("seoKeywords", ""),
                        ogImageUrl = obj.optString("ogImageUrl", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return getInitialNews()
        }
        return list
    }

    private fun saveCategories() {
        val arr = JSONArray()
        _categories.value.forEach { cat ->
            val obj = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("slug", cat.slug)
                put("order", cat.order)
                put("isVisible", cat.isVisible)
            }
            arr.put(obj)
        }
        prefs.edit().putString("categories", arr.toString()).apply()
    }

    private fun parseCategories(json: String): List<CategoryItem> {
        val list = mutableListOf<CategoryItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CategoryItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        slug = obj.getString("slug"),
                        order = obj.optInt("order", i),
                        isVisible = obj.optBoolean("isVisible", true)
                    )
                )
            }
        } catch (e: Exception) {
            return getInitialCategories()
        }
        return list
    }

    private fun saveBreaking() {
        val arr = JSONArray()
        _breakingNews.value.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("headline", item.headline)
                put("newsId", item.newsId ?: "")
                put("order", item.order)
                put("isActive", item.isActive)
                put("timestamp", item.timestamp)
            }
            arr.put(obj)
        }
        prefs.edit().putString("breaking_news", arr.toString()).apply()
    }

    private fun parseBreaking(json: String): List<BreakingNewsItem> {
        val list = mutableListOf<BreakingNewsItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    BreakingNewsItem(
                        id = obj.getString("id"),
                        headline = obj.getString("headline"),
                        newsId = obj.optString("newsId").takeIf { it.isNotEmpty() },
                        order = obj.optInt("order", i),
                        isActive = obj.optBoolean("isActive", true),
                        timestamp = obj.optString("timestamp", "এইমাত্র")
                    )
                )
            }
        } catch (e: Exception) {
            return getInitialBreaking()
        }
        return list
    }

    // Default authentic dataset
    private fun getInitialCategories(): List<CategoryItem> = listOf(
        CategoryItem("cat_all", "সর্বশেষ", "latest", 0),
        CategoryItem("cat_national", "জাতীয়", "national", 1),
        CategoryItem("cat_politics", "রাজনীতি", "politics", 2),
        CategoryItem("cat_international", "আন্তর্জাতিক", "international", 3),
        CategoryItem("cat_sports", "খেলাধুলা", "sports", 4),
        CategoryItem("cat_entertainment", "বিনোদন", "entertainment", 5),
        CategoryItem("cat_tech", "প্রযুক্তি", "tech", 6),
        CategoryItem("cat_lifestyle", "লাইফস্টাইল", "lifestyle", 7),
        CategoryItem("cat_islam", "ইসলাম", "islam", 8),
        CategoryItem("cat_video", "ভিডিও", "video", 9)
    )

    private fun getInitialBreaking(): List<BreakingNewsItem> = listOf(
        BreakingNewsItem("brk_1", "আন্তর্জাতিক বাজারে জ্বালানি তেলের দাম আরও কমেছে, দেশে সমন্বয়ের ইঙ্গিত", "news_lead", 0, true, "৫ মিনিট আগে"),
        BreakingNewsItem("brk_2", "মেট্রোরেলে নতুন ১০টি স্টেশন চালু, যাত্রীদের অভূতপূর্ব সাড়া", "news_metro", 1, true, "১৫ মিনিট আগে"),
        BreakingNewsItem("brk_3", "টি-টোয়েন্টি সিরিজে ঐতিহাসিক জয় পেল বাংলাদেশ ক্রিকেট দল", "news_cricket", 2, true, "৩০ মিনিট আগে"),
        BreakingNewsItem("brk_4", "বঙ্গোপসাগরে সৃষ্ট লঘুচাপের কারণে ৩ নম্বর সতর্কতা সংকেত জারি", "news_weather", 3, true, "১ ঘণ্টা আগে")
    )

    private fun getInitialNews(): List<NewsItem> = listOf(
        NewsItem(
            id = "news_lead",
            title = "বাংলাদেশের ডিজিটাল অর্থনীতিতে নতুন বিপ্লব: রপ্তানি ছাড়াল রেকর্ড ১০ বিলিয়ন ডলার",
            slug = "bangladesh-digital-economy-record-export",
            category = "জাতীয়",
            author = "তাহমিদ হাসান",
            publishDate = "০৫ সেপ্টেম্বর ২০২৬, সকাল ১০:১৫",
            updateDate = "০৫ সেপ্টেম্বর ২০২৬, দুপুর ১২:৩০",
            imageUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=800&auto=format&fit=crop&q=80",
            imageCaption = "রাজধানীতে সফটওয়্যার পার্ক ও হাইটেক প্রকল্পের নতুন কেন্দ্রবিন্দু",
            shortDescription = "তথ্যপ্রযুক্তি খাতের অভূতপূর্ব বিকাশে বৈশ্বিক বাজারে বাংলাদেশের সফটওয়্যার ও আইটি সেবা খাতের রপ্তানি প্রথমবারের মতো ১০ বিলিয়ন ডলারের মাইলফলক স্পর্শ করেছে।",
            fullContent = """তথ্যপ্রযুক্তি খাতে ঐতিহাসিক এক সাফল্যের সাক্ষী হলো বাংলাদেশ। চলতি অর্থবছরে দেশের আইটি ও ফ্রিল্যান্সিং খাতের মোট বৈশ্বিক আয় ১০ বিলিয়ন ডলার অতিক্রম করেছে। 

সংশ্লিষ্ট বিশেষজ্ঞরা জানিয়েছেন, তরুণ উদ্যোক্তাদের জন্য করমুক্ত সুযোগ, আন্তর্জাতিক পেমেন্ট গেটওয়ের সহজলভ্যতা এবং দেশজুড়ে দ্রুতগতির ব্রডব্যান্ড নেটওয়ার্ক বিস্তারের কারণেই এ অভূতপূর্ব প্রবৃদ্ধি সম্ভব হয়েছে।

পরিকল্পনা কমিশনের এক বিশেষ প্রতিবেদনে উল্লেখ করা হয়, ফ্রিল্যান্সিং ও সফটওয়্যার ডেভেলপমেন্টে এখন প্রত্যক্ষ ও পরোক্ষভাবে যুক্ত রয়েছেন প্রায় ১৫ লাখ তরুণ-তরুণী। ঢাকার কারওয়ান বাজার, মহাখালী আইটি ভিলেজ এবং বিভাগীয় হাইটেক পার্কগুলোতে প্রতিদিন গড়ে তৈরি হচ্ছে শত শত প্রযুক্তি সমাধান।

বিদেশি বিনিয়োগকারীরাও বাংলাদেশের প্রযুক্তি খাতে বিপুল আগ্রহ দেখাচ্ছেন। বিশেষ করে সিঙ্গাপুর, জাপান ও মধ্যপ্রাচ্যের ভেঞ্চার ক্যাপিটালিস্টরা এখানকার এআই ও ফিনটেক স্টার্টআপে সরাসরি অর্থায়ন শুরু করেছে।""",
            isFeatured = true,
            isBreaking = true,
            isPublished = true,
            viewsCount = 12450,
            seoTitle = "ডিজিটাল অর্থনীতিতে বাংলাদেশের রেকর্ড ১০ বিলিয়ন ডলার রপ্তানি | গোলাপি নিউজ",
            seoDescription = "তথ্যপ্রযুক্তি খাতে রেকর্ড রপ্তানি করল বাংলাদেশ। সফটওয়্যার ও ফ্রিল্যান্সিং খাতে বৈশ্বিক আয় বৃদ্ধি।",
            seoKeywords = "আইটি রপ্তানি, বাংলাদেশ ফ্রিল্যান্সিং, গোলাপি নিউজ, জাতীয় অর্থনীতি",
            ogImageUrl = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=1200&h=630&auto=format&fit=crop&q=80"
        ),
        NewsItem(
            id = "news_metro",
            title = "মেট্রোরেলে নতুন ১০টি স্টেশন পুরোদমে চালু: যানজটমুক্ত রাজধানীর নতুন রূপ",
            slug = "metro-rail-new-stations-opened",
            category = "জাতীয়",
            author = "ফারহানা করিম",
            publishDate = "০৫ সেপ্টেম্বর ২০২৬, সকাল ০৯:০০",
            imageUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800&auto=format&fit=crop&q=80",
            imageCaption = "সকালে মেট্রোরেলে অফিসগামী যাত্রীদের সুশৃঙ্খল ভিড়",
            shortDescription = "উত্তরা থেকে মতিঝিল ছাড়িয়ে কমলাপুর পর্যন্ত নতুন স্টেশনগুলোর কার্যক্রম চালু হওয়ায় ঢাকা শহরের যাতায়াত ব্যবস্থায় বিপুল স্বস্তি ফিরে এসেছে।",
            fullContent = """ঢাকার গণপরিবহনে নতুন ইতিহাস সৃষ্টি করে মেট্রোরেলের বর্ধিত রুটের ১০টি নতুন স্টেশন আজ থেকে সর্বসাধারণের জন্য খুলে দেওয়া হলো।

ভোর ৬টা থেকে রাত ১১টা পর্যন্ত ৫ মিনিট পরপর ট্রেন চলাচল করায় লাখ লাখ কর্মজীবী মানুষ নির্বিঘ্নে তাদের কর্মস্থলে পৌঁছাতে পারছেন। আগে যেখানে মিরপুর থেকে মতিঝিল যেতে ২ ঘণ্টা সময় লাগত, এখন তা মাত্র ২৫ মিনিটে সম্পন্ন হচ্ছে।

যাত্রীরা জানিয়েছেন, টিকিট কাটার স্মার্ট কার্ড এবং মোবাইল ব্যাংকিং অ্যাপের মাধ্যমে কিউআর কোড স্ক্যান করে দ্রুত প্রবেশ করা যাচ্ছে। মেট্রোরেল সংলগ্ন এলাকাগুলোতে ফুটপাত পরিচ্ছন্ন রাখা হয়েছে এবং ফিডার বাসের ব্যবস্থা করা হয়েছে।""",
            isFeatured = false,
            isBreaking = true,
            isPublished = true,
            viewsCount = 8920,
            seoTitle = "মেট্রোরেলের নতুন স্টেশন চালু | গোলাপি নিউজ",
            seoDescription = "উত্তরা-কমলাপুর রুটে মেট্রোরেলের নতুন স্টেশন চালু। যানজট নিরসনে নতুন মাইলফলক।",
            seoKeywords = "মেট্রোরেল, ঢাকা, যানজট, জাতীয় সংবাদ",
            ogImageUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=1200&h=630&auto=format&fit=crop&q=80"
        ),
        NewsItem(
            id = "news_politics_1",
            title = "সুষ্ঠু ও নিরপেক্ষ নির্বাচন আয়োজনে নির্বাচন কমিশনের কঠোর নির্বাচনী রোডম্যাপ ঘোষণা",
            slug = "election-commission-announces-roadmap",
            category = "রাজনীতি",
            author = "মো. শফিকুর রহমান",
            publishDate = "০৫ সেপ্টেম্বর ২০২৬, দুপুর০১:৪৫",
            imageUrl = "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?w=800&auto=format&fit=crop&q=80",
            imageCaption = "প্রধান নির্বাচন কমিশনারের ব্রিফিং কক্ষ",
            shortDescription = "সকল রাজনৈতিক দলের আস্থা অর্জনে এবং ভোটাধিকার সুরক্ষিত করতে পাঁচ দফা বিশেষ নিরাপত্তা নির্দেশনা জারি করেছে নির্বাচন কমিশন।",
            fullContent = """জাতীয় নির্বাচনের তফসিল ঘোষণার পূর্বে রাজনৈতিক দলগুলোর সঙ্গে ধারাবাহিক সংলাপের পর নির্বাচন কমিশন চূড়ান্ত রোডম্যাপ প্রকাশ করেছে।

কমিশনার এক সাংবাদিক সম্মেলনে জানান, প্রতিটি ভোটকেন্দ্রে সিসিটিভি ক্যামেরার লাইভ মনিটরিং থাকবে। ম্যাজিস্ট্রেট এবং আইন প্রয়োগকারী সংস্থার যৌথ টহল ভোটগ্রহণের ৪৮ ঘণ্টা আগে থেকেই জোরদার করা হবে।

রাজনৈতিক বিশ্লেষকরা বলছেন, স্বচ্ছতা নিশ্চিত করতে প্রযুক্তি ও আইনি কঠোরতার সমন্বয় দেশের গণতান্ত্রিক ধারাবাহিকতায় নতুন মাত্রা যোগ করবে।""",
            isFeatured = false,
            isBreaking = false,
            isPublished = true,
            viewsCount = 6730,
            seoTitle = "নির্বাচন কমিশনের রোডম্যাপ ঘোষণা | রাজনীতি | গোলাপি নিউজ",
            seoDescription = "নির্বাচন কমিশনের কঠোর রোডম্যাপ। প্রতিটি কেন্দ্রে ডিজিটাল নজরদারি।",
            seoKeywords = "রাজনীতি, নির্বাচন কমিশন, ভোট, গোলাপি নিউজ",
            ogImageUrl = "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?w=1200&h=630&auto=format&fit=crop&q=80"
        ),
        NewsItem(
            id = "news_cricket",
            title = "অসাধারণ অলরাউন্ড নৈপুণ্যে টি-টোয়েন্টি সিরিজ জয় বাংলাদেশের",
            slug = "bangladesh-wins-t20-series",
            category = "খেলাধুলা",
            author = "রাকিবুল হাসান",
            publishDate = "০৫ সেপ্টেম্বর ২০২৬, রাত ১০:২০",
            imageUrl = "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=800&auto=format&fit=crop&q=80",
            imageCaption = "মিরপুর শেরেবাংলা স্টেডিয়ামে ট্রফি হাতে টাইগারদের উল্লাস",
            shortDescription = "শেষ ওভারের নাটকীয়তায় ২ উইকেটে রুদ্ধশ্বাস জয় নিশ্চিত করে ট্রফি উঁচিয়ে ধরল টাইগাররা। ম্যাচ সেরা হয়েছেন তরুণ পেসার।",
            fullContent = """মিরপুরের হোম অব ক্রিকেটে চরম নাটকীয় এক ম্যাচে শক্তিশালী প্রতিপক্ষকে হারিয়ে ৩ ম্যাচের টি-টোয়েন্টি সিরিজ ২-১ ব্যবধানে জিতে নিল বাংলাদেশ।

প্রথমে ব্যাট করতে নেমে প্রতিপক্ষ সংগ্রহ করেছিল ১৬৮ রান। জবাবে ব্যাট করতে নেমে শেষ ওভারে জয়ের জন্য প্রয়োজন ছিল ১১ রান। টাইগারদের নবীন ফিনিশার টানা দুটি বাউন্ডারি হাকিয়ে এক বল বাকি থাকতেই অবিস্মরণীয় জয় ছিনিয়ে নেন।

পুরো স্টেডিয়াম মুখরিত হয় ‘বাংলাদেশ বাংলাদেশ’ স্লোগানে। ম্যাচ শেষে অধিনায়ক বলেন, তরুণ খেলোয়াড়দের নিবেদিত মানসিকতাই আজকের এই স্মরণীয় সাফল্যের মূল ভিত্তি।""",
            isFeatured = false,
            isBreaking = true,
            isPublished = true,
            viewsCount = 15890,
            seoTitle = "টি-টোয়েন্টি সিরিজ জিতল বাংলাদেশ | গোলাপি নিউজ খেলাধুলা",
            seoDescription = "মিরপুরে রুদ্ধশ্বাস ম্যাচে জয়ী বাংলাদেশ ক্রিকেট দল। ট্রফি জয়ের মুহূর্ত।",
            seoKeywords = "বাংলাদেশ ক্রিকেট, টি-টোয়েন্টি, খেলাধুলা, মিরপুর স্টেডিয়াম",
            ogImageUrl = "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=1200&h=630&auto=format&fit=crop&q=80"
        ),
        NewsItem(
            id = "news_international",
            title = "জলবায়ু সম্মেলনে ঐতিহাসিক চুক্তি: উন্নয়নশীল দেশগুলোর জন্য ১০০ বিলিয়ন ডলারের সবুজ তহবিল",
            slug = "cop-historic-climate-fund-agreement",
            category = "আন্তর্জাতিক",
            author = "আন্তর্জাতিক ডেস্ক",
            publishDate = "০৫ সেপ্টেম্বর ২০২৬, ভোর ০৬:৩০",
            imageUrl = "https://images.unsplash.com/photo-1618042164219-62c820f10723?w=800&auto=format&fit=crop&q=80",
            imageCaption = "জাতিসংঘের পরিবেশ সম্মেলন কেন্দ্রে বিভিন্ন দেশের প্রতিনিধিদের স্বাক্ষর",
            shortDescription = "গ্লোবাল ওয়ার্মিং ও বন্যা মোকাবিলায় ক্ষতিগ্রস্ত উপকূলীয় দেশগুলোকে আর্থিক ক্ষতিপূরণ প্রদানে একমত হয়েছে বিশ্বনেতারা।",
            fullContent = """বৈশ্বিক জলবায়ু সংকটের বিরুদ্ধে লড়াইয়ে এক যুগান্তকারী পদক্ষেপে বিশ্বনেতারা ১০০ বিলিয়ন ডলারের আন্তর্জাতিক ক্ষতিপূরণ তহবিল গঠনে সর্বসম্মত চুক্তি স্বাক্ষর করেছেন।

বাংলাদেশসহ জলবায়ু ঝুঁকিতে থাকা দেশগুলোর জন্য এ তহবিল নবায়নযোগ্য জ্বালানি বিস্তার এবং বন্যা নিয়ন্ত্রণ বাঁধ নির্মাণে সরাসরি ভূমিকা রাখবে। জাতিসংঘ মহাসচিব এই চুক্তিকে মানবজাতির অস্তিত্ব রক্ষার এক বিরাট সাফল্য হিসেবে অভিহিত করেছেন।""",
            isFeatured = false,
            isBreaking = false,
            isPublished = true,
            viewsCount = 5410,
            seoTitle = "জলবায়ু তহবিলে ঐতিহাসিক আন্তর্জাতিক চুক্তি | আন্তর্জাতিক | গোলাপি নিউজ",
            seoDescription = "জলবায়ু সম্মেলনে উন্নয়নশীল দেশগুলোর জন্য ১০০ বিলিয়ন ডলার তহবিল অনুমোদন।",
            seoKeywords = "জলবায়ু সম্মেলন, জাতিসংঘ, আন্তর্জাতিক খবর",
            ogImageUrl = "https://images.unsplash.com/photo-1618042164219-62c820f10723?w=1200&h=630&auto=format&fit=crop&q=80"
        ),
        NewsItem(
            id = "news_tech",
            title = "কৃত্রিম বুদ্ধিমত্তায় বাংলা ভাষা প্রক্রিয়াকরণে যুগান্তকারী সাফল্য তৈরি করল বাংলাদেশি গবেষকরা",
            slug = "bangla-ai-llm-breakthrough",
            category = "প্রযুক্তি",
            author = "অনিন্দ্য রায়",
            publishDate = "০৪ সেপ্টেম্বর ২০২৬, বিকাল ০৪:১৫",
            imageUrl = "https://images.unsplash.com/photo-1677442136019-21780efad99a?w=800&auto=format&fit=crop&q=80",
            imageCaption = "কৃত্রিম বুদ্ধিমত্তা ল্যাবে বাংলা ন্যাচারাল ল্যাঙ্গুয়েজ প্রসেসিং গবেষণা",
            shortDescription = "বাংলা ব্যাকরণ, আঞ্চলিক উপভাষা ও উচ্চারণের নিখুঁত অনুবাদে সক্ষম প্রথম ওপেন-সোর্স লার্জ ল্যাঙ্গুয়েজ মডেল উন্মোচন করেছে বুয়েট গবেষক দল।",
            fullContent = """বাংলা ভাষার সমৃদ্ধ সাহিত্য ও কোটি মানুষের দৈনন্দিন ভাব আদান-প্রদানকে ডিজিটাল জগতে আরও গতিশীল করতে তৈরি হলো সম্পূর্ণ দেশীয় প্রযুক্তির এআই মডেল।

এটির সাহায্যে বাংলা অডিও থেকে সরাসরি নির্ভুল টেক্সট এবং টেক্সট থেকে মানুষের মতো সাবলীল কণ্ঠস্বর তৈরি করা যায়। সরকারি দপ্তর, ব্যাংক ও হাসপাতালে গ্রাহক সেবা দিতে এই প্রযুক্তি ইতিমধ্যে পরীক্ষামূলকভাবে ব্যবহার শুরু হয়েছে।""",
            isFeatured = false,
            isBreaking = false,
            isPublished = true,
            viewsCount = 9810,
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            seoTitle = "বাংলা এআই মডেলে যুগান্তকারী সাফল্য | প্রযুক্তি | গোলাপি নিউজ",
            seoDescription = "বাংলা ভাষার জন্য তৈরি হলো সম্পূর্ণ দেশীয় কৃত্রিম বুদ্ধিমত্তা মডেল।",
            seoKeywords = "এআই, প্রযুক্তি, বাংলা ভাষা, বুয়েট",
            ogImageUrl = "https://images.unsplash.com/photo-1677442136019-21780efad99a?w=1200&h=630&auto=format&fit=crop&q=80"
        ),
        NewsItem(
            id = "news_lifestyle",
            title = "দৈনন্দিন জীবনে মানসিক চাপ কমাতে বিশেষজ্ঞদের ৫টি কার্যকর পরামর্শ",
            slug = "5-tips-to-reduce-daily-stress",
            category = "লাইফস্টাইল",
            author = "ডা. সুমাইয়া ইসলাম",
            publishDate = "০৪ সেপ্টেম্বর ২০২৬, দুপুর ০১:০০",
            imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800&auto=format&fit=crop&q=80",
            imageCaption = "সকালের প্রাকৃতিক পরিবেশে ধ্যান ও স্বাস্থ্যচর্চা",
            shortDescription = "কর্মব্যস্ত জীবনে ক্লান্তি ও উদ্বেগ দূর করতে নিয়মিত সকালের হাঁটা, ডিজিটাল ডিটক্স ও সঠিক খাদ্যাভ্যাস গড়ে তোলার তাগিদ দিয়েছেন পুষ্টিবিদরা।",
            fullContent = """আধুনিক শহুরে জীবনে মানসিক চাপ এক নীরব ঘাতকে রূপ নিয়েছে। চিকিৎসকদের মতে, সামান্য কিছু জীবনযাত্রার পরিবর্তন এনে মন ও শরীর উভয়কেই সতেজ রাখা সম্ভব।

প্রথমত, প্রতিদিন নির্দিষ্ট সময়ে পর্যাপ্ত ঘুম নিশ্চিত করা। দ্বিতীয়ত, রাতে ঘুমানোর অন্তত এক ঘণ্টা আগে স্মার্টফোন ও ল্যাপটপ থেকে দূরে থাকা। তৃতীয়ত, প্রচুর পানি পান এবং মৌসুমি ফলমূল খাওয়া। চতুর্থত, প্রতিদিন অন্তত ২০ মিনিট বুক ভরে শ্বাস নেওয়ার অনুশীলন করা। এবং পঞ্চমত, নিজের ভালো লাগার সৃজনশীল কাজে সময় দেওয়া।""",
            isFeatured = false,
            isBreaking = false,
            isPublished = true,
            viewsCount = 7420,
            seoTitle = "মানসিক চাপ কমানোর সহজ উপায় | লাইফস্টাইল | গোলাপি নিউজ",
            seoDescription = "মানসিক প্রশান্তির জন্য চিকিৎসকদের পরামর্শ। দৈনন্দিন সুস্থ জীবনের উপায়।",
            seoKeywords = "লাইফস্টাইল, মানসিক স্বাস্থ্য, সুস্থ জীবন",
            ogImageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=1200&h=630&auto=format&fit=crop&q=80"
        ),
        NewsItem(
            id = "news_islam",
            title = "পবিত্র মাহে রমজানের প্রস্তুতি: আত্মশুদ্ধি ও সহমর্মিতার গুরুত্ব",
            slug = "ramadan-spiritual-preparation",
            category = "ইসলাম",
            author = "মাওলানা আব্দুল্লাহ আল ক্বাফী",
            publishDate = "০৩ সেপ্টেম্বর ২০২৬, সকাল ১০:০০",
            imageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80",
            imageCaption = "বায়তুল মোকাররম জাতীয় মসজিদে মুসল্লিদের ইবাদত",
            shortDescription = "ইসলামে রমজান মাসের মূল শিক্ষা হলো আত্মসংযম, গরিব-অসহায় মানুষের পাশে দাঁড়ানো এবং আত্মিক পবিত্রতা অর্জন করা।",
            fullContent = """পবিত্র কোরআনে আল্লাহ তাআলা রোজাকে মানবজাতির জন্য তাকওয়া অর্জনের প্রধান মাধ্যম হিসেবে নির্ধারণ করেছেন।

কেবল উপবাস পালন নয়, বরং অন্যায়, অবিচার ও পরনিন্দা থেকে নিজের জিহ্বা ও মনকে রক্ষা করাই সিয়ামের মূল দর্শন। সমাজে যারা অনাহারে দিন কাটাচ্ছেন তাদের কষ্ট উপলব্ধি করে দান-সদকার মাধ্যমে ভ্রাতৃত্বের বন্ধন সুদৃঢ় করাই প্রকৃত মুমিনের বৈশিষ্ট্য।""",
            isFeatured = false,
            isBreaking = false,
            isPublished = true,
            viewsCount = 11200,
            seoTitle = "আত্মশুদ্ধি ও সহমর্মিতার শিক্ষা | ইসলাম | গোলাপি নিউজ",
            seoDescription = "ইসলামে রমজানের গুরুত্ব ও আত্মসংযমের বার্তা। ধর্মীয় বিশ্লেষণ।",
            seoKeywords = "ইসলাম, রমজান, তাকওয়া, গোলাপি নিউজ",
            ogImageUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&h=630&auto=format&fit=crop&q=80"
        ),
        NewsItem(
            id = "news_entertainment",
            title = "আন্তর্জাতিক চলচ্চিত্র উৎসবে দেশের সিনেমার লাল গালিচায় জয়জয়কার",
            slug = "bangladeshi-cinema-wins-at-international-film-festival",
            category = "বিনোদন",
            author = "বিনোদন প্রতিবেদক",
            publishDate = "০৩ সেপ্টেম্বর ২০২৬, রাত ০৯:০০",
            imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
            imageCaption = "কান চলচ্চিত্র উৎসবে তরুণ বাংলাদেশি নির্মাতার পুরস্কার গ্রহণ",
            shortDescription = "মৌলিক গল্প ও নান্দনিক চিত্রনাট্যের গুণে বিশ্বমঞ্চে সেরা নির্মাতার পুরস্কার জিতে নিলেন বাংলাদেশের উদীয়মান চলচ্চিত্রকার।",
            fullContent = """বিশ্ব চলচ্চিত্রের অন্যতম মর্যাদাপূর্ণ উৎসবে বাংলাদেশের চলচ্চিত্র ইতিহাসের নতুন এক পালক যুক্ত হলো। লাল গালিচায় বাংলাদেশের ঐতিহ্যবাহী পোশাকে উপস্থিত হয়ে বিশ্ব চলচ্চিত্রের বোদ্ধাদের প্রশংসা কুড়িয়েছেন তরুণ টিম।

পুরস্কার প্রাপ্তির পর নির্মাতা বলেন, ‘আমাদের মাটির গল্প, নদ-নদীর কান্না ও সাধারণ মানুষের বেঁচে থাকার সংগ্রামই এই চলচ্চিত্রের প্রাণ।’ দেশের হলগুলোতে আগামী সপ্তাহে সিনেমাটি মুক্তি পাওয়ার কথা রয়েছে।""",
            isFeatured = false,
            isBreaking = false,
            isPublished = true,
            viewsCount = 14300,
            videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            seoTitle = "আন্তর্জাতিক চলচ্চিত্র উৎসবে দেশের ছবির জয় | বিনোদন | গোলাপি নিউজ",
            seoDescription = "আন্তর্জাতিক মঞ্চে বাংলাদেশের সিনেমার অনন্য সাফল্য। সেরা নির্মাতার স্বীকৃতি।",
            seoKeywords = "বিনোদন, চলচ্চিত্র, আন্তর্জাতিক উৎসব, গোলাপি নিউজ",
            ogImageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200&h=630&auto=format&fit=crop&q=80"
        )
    )
}
