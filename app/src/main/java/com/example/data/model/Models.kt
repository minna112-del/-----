package com.example.data.model

data class NewsItem(
    val id: String,
    val title: String,
    val slug: String,
    val category: String,
    val author: String = "নিজস্ব প্রতিবেদক",
    val authorAvatar: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop&q=80",
    val publishDate: String,
    val updateDate: String = "",
    val imageUrl: String,
    val imageCaption: String = "",
    val shortDescription: String,
    val fullContent: String,
    val isFeatured: Boolean = false,
    val isBreaking: Boolean = false,
    val isPublished: Boolean = true,
    val viewsCount: Int = 120,
    val videoUrl: String? = null,
    val seoTitle: String = "",
    val seoDescription: String = "",
    val seoKeywords: String = "",
    val ogImageUrl: String = ""
)

data class CategoryItem(
    val id: String,
    val name: String,
    val slug: String,
    val order: Int = 0,
    val isVisible: Boolean = true
)

data class BreakingNewsItem(
    val id: String,
    val headline: String,
    val newsId: String? = null,
    val order: Int = 0,
    val isActive: Boolean = true,
    val timestamp: String = "এইমাত্র"
)

data class AdsConfig(
    val isEnabled: Boolean = true,
    val popunderCode: String = "<!-- Adsterra Popunder Code -->\n<script type='text/javascript' src='//pl25000001.highratecpm.com/a1/b2/c3/a1b2c3d4e5f6.js'></script>",
    val popunderActive: Boolean = true,
    val socialBarCode: String = "<!-- Adsterra Social Bar Code -->\n<script type='text/javascript' src='//pl25000002.highratecpm.com/12/34/56/1234567890.js'></script>",
    val socialBarActive: Boolean = true,
    val headerBannerCode: String = "<!-- Adsterra 728x90 Header Banner -->\n<div class='adsterra-banner-728'>\n  <script type='text/javascript'>\n    atOptions = { 'key' : 'h728x90key', 'format' : 'iframe', 'height' : 90, 'width' : 728 };\n  </script>\n</div>",
    val headerBannerActive: Boolean = true,
    val sidebarBannerCode: String = "<!-- Adsterra 300x250 Sidebar Banner -->\n<div class='adsterra-banner-300'>\n  <script type='text/javascript'>\n    atOptions = { 'key' : 's300x250key', 'format' : 'iframe', 'height' : 250, 'width' : 300 };\n  </script>\n</div>",
    val sidebarBannerActive: Boolean = true,
    val inArticleBannerCode: String = "<!-- Adsterra 468x60 In-Article Banner -->\n<div class='adsterra-banner-in-article'>\n  <script type='text/javascript'>\n    atOptions = { 'key' : 'ia468x60key', 'format' : 'iframe', 'height' : 60, 'width' : 468 };\n  </script>\n</div>",
    val inArticleBannerActive: Boolean = true,
    val footerBannerCode: String = "<!-- Adsterra 728x90 Footer Banner -->\n<div class='adsterra-banner-footer'>\n  <script type='text/javascript'>\n    atOptions = { 'key' : 'f728x90key', 'format' : 'iframe', 'height' : 90, 'width' : 728 };\n  </script>\n</div>",
    val footerBannerActive: Boolean = true,
    val mobileBannerCode: String = "<!-- Adsterra 320x50 Mobile Banner -->\n<div class='adsterra-banner-mobile'>\n  <script type='text/javascript'>\n    atOptions = { 'key' : 'm320x50key', 'format' : 'iframe', 'height' : 50, 'width' : 320 };\n  </script>\n</div>",
    val mobileBannerActive: Boolean = true
)

data class SiteSettings(
    val siteName: String = "গোলাপি নিউজ",
    val englishName: String = "Golapi News",
    val tagline: String = "সত্যের নির্ভীক সারথি — আধুনিক ডিজিটাল বাংলা সংবাদপত্র",
    val logoUrl: String = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=120&auto=format&fit=crop&q=80",
    val faviconUrl: String = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=32&auto=format&fit=crop&q=80",
    val contactEmail: String = "editor@golapinews.com",
    val phone: String = "+880 1711-889900",
    val address: String = "কাওরান বাজার বাণিজ্যিক এলাকা, ঢাকা-১২১৫, বাংলাদেশ",
    val facebookUrl: String = "https://facebook.com/golapinews",
    val youtubeUrl: String = "https://youtube.com/@golapinews",
    val twitterUrl: String = "https://twitter.com/golapinews",
    val whatsappNumber: String = "+8801711889900",
    val aboutUsText: String = "‘গোলাপি নিউজ’ বাংলাদেশের একটি অগ্রণী আধুনিক ডিজিটাল বাংলা নিউজ পোর্টাল। নির্ভীক সাংবাদিকতা, বস্তুনিষ্ঠ সংবাদ ও তাৎক্ষণিক তথ্য প্রবাহ নিশ্চিত করাই আমাদের মূল অঙ্গীকার।",
    val privacyPolicyText: String = "গোলাপি নিউজ পাঠকদের তথ্যের গোপনীয়তা রক্ষা করতে প্রতিশ্রুতিবদ্ধ। এই সাইটটি ব্রাউজিংয়ের সময় আপনার ব্যক্তিগত তথ্য সুরক্ষায় সর্বোচ্চ সতর্কতা অবলম্বন করে। কুকিজ এবং বিজ্ঞাপনী নেটওয়ার্ক (যেমন Adsterra) সম্পর্কিত তথ্যাদি আন্তর্জাতিক ডিজিটাল নিয়ম অনুযায়ী ব্যবহৃত হয়।",
    val termsText: String = "গোলাপি নিউজ সাইট ব্যবহারের সময় সকল পাঠককে শালীনতা ও দেশের প্রচলিত সাইবার আইন মেনে চলার অনুরোধ করা হচ্ছে। আমাদের প্রকাশিত সকল সংবাদ, ছবি ও ভিডিওর সর্বস্বত্ব গোলাপি নিউজ কর্তৃপক্ষের সংরক্ষিত।"
)
