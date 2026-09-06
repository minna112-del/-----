package com.example.generator

import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

object NetlifyGenerator {

    private fun buildSrcSet(url: String): String {
        if (!url.contains("images.unsplash.com")) return ""
        val base = url.substringBefore("?")
        return "${base}?w=400&q=75&auto=format&fit=crop 400w, ${base}?w=800&q=80&auto=format&fit=crop 800w, ${base}?w=1200&q=85&auto=format&fit=crop 1200w"
    }

    private fun renderResponsiveImg(
        url: String,
        alt: String,
        sizes: String,
        extraClass: String = "",
        id: String? = null
    ): String {
        val srcset = buildSrcSet(url)
        val srcSetAttr = if (srcset.isNotEmpty()) """ srcset="$srcset" sizes="$sizes"""" else ""
        val classAttr = if (extraClass.isNotEmpty()) """ class="$extraClass"""" else ""
        val idAttr = if (id != null) """ id="$id"""" else ""
        return """<img${idAttr}${classAttr} src="$url"$srcSetAttr alt="$alt" loading="lazy" decoding="async" />"""
    }

    fun generateNewsDataJson(
        newsList: List<NewsItem>,
        categories: List<CategoryItem>,
        breakingNews: List<BreakingNewsItem>,
        adsConfig: AdsConfig,
        siteSettings: SiteSettings
    ): String {
        val root = JSONObject()
        root.put("generatedAt", "2026-09-05T23:45:00Z")
        root.put("siteSettings", JSONObject().apply {
            put("siteName", siteSettings.siteName)
            put("tagline", siteSettings.tagline)
            put("contactEmail", siteSettings.contactEmail)
            put("phone", siteSettings.phone)
            put("address", siteSettings.address)
            put("facebookUrl", siteSettings.facebookUrl)
            put("youtubeUrl", siteSettings.youtubeUrl)
            put("aboutUsText", siteSettings.aboutUsText)
            put("privacyPolicyText", siteSettings.privacyPolicyText)
            put("termsText", siteSettings.termsText)
        })

        root.put("adsConfig", JSONObject().apply {
            put("isEnabled", adsConfig.isEnabled)
            put("popunderActive", adsConfig.popunderActive)
            put("socialBarActive", adsConfig.socialBarActive)
            put("headerBannerActive", adsConfig.headerBannerActive)
            put("sidebarBannerActive", adsConfig.sidebarBannerActive)
            put("inArticleBannerActive", adsConfig.inArticleBannerActive)
            put("footerBannerActive", adsConfig.footerBannerActive)
            put("mobileBannerActive", adsConfig.mobileBannerActive)
        })

        val newsArray = JSONArray()
        newsList.filter { it.isPublished }.forEach { n ->
            newsArray.put(JSONObject().apply {
                put("id", n.id)
                put("title", n.title)
                put("slug", n.slug)
                put("category", n.category)
                put("author", n.author)
                put("publishDate", n.publishDate)
                put("imageUrl", n.imageUrl)
                put("imageCaption", n.imageCaption)
                put("shortDescription", n.shortDescription)
                put("fullContent", n.fullContent)
                put("isFeatured", n.isFeatured)
                put("isBreaking", n.isBreaking)
                put("viewsCount", n.viewsCount)
                put("videoUrl", n.videoUrl ?: "")
                put("seoTitle", n.seoTitle)
                put("seoDescription", n.seoDescription)
                put("ogImageUrl", n.ogImageUrl)
            })
        }
        root.put("news", newsArray)

        val catArray = JSONArray()
        categories.filter { it.isVisible }.forEach { c ->
            catArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("slug", c.slug)
                put("order", c.order)
            })
        }
        root.put("categories", catArray)

        val brkArray = JSONArray()
        breakingNews.filter { it.isActive }.forEach { b ->
            brkArray.put(JSONObject().apply {
                put("id", b.id)
                put("headline", b.headline)
                put("newsId", b.newsId ?: "")
                put("timestamp", b.timestamp)
            })
        }
        root.put("breakingNews", brkArray)

        return root.toString(2)
    }

    fun generateIndexHtml(
        newsList: List<NewsItem>,
        categories: List<CategoryItem>,
        breakingNews: List<BreakingNewsItem>,
        adsConfig: AdsConfig,
        siteSettings: SiteSettings
    ): String {
        val lead = newsList.firstOrNull { it.isFeatured } ?: newsList.firstOrNull()
        val latest = newsList.take(6)
        val breakingText = breakingNews.joinToString("  •  ") { it.headline }

        return """<!DOCTYPE html>
<html lang="bn">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${siteSettings.siteName} | ${siteSettings.tagline}</title>
  
  <!-- Favicon -->
  <link rel="icon" type="image/svg+xml" href="favicon.svg" />
  <link rel="alternate icon" href="favicon.svg" type="image/svg+xml" />

  <!-- Primary Meta Tags -->
  <meta name="title" content="${siteSettings.siteName} - সত্যের নির্ভীক সারথি" />
  <meta name="description" content="${siteSettings.tagline}. সর্বশেষ জাতীয়, রাজনীতি, আন্তর্জাতিক, খেলাধুলা ও বিনোদন সংবাদ।" />
  <meta name="keywords" content="বাংলা নিউজ, গোলাপি নিউজ, সর্বশেষ সংবাদ, বাংলাদেশ খবর, Golapi News" />
  <meta name="author" content="${siteSettings.siteName}" />
  <meta name="robots" content="index, follow" />

  <!-- Open Graph / Facebook -->
  <meta property="og:type" content="website" />
  <meta property="og:url" content="https://golapinews.netlify.app/" />
  <meta property="og:title" content="${siteSettings.siteName} - আধুনিক ডিজিটাল বাংলা সংবাদপত্র" />
  <meta property="og:description" content="${siteSettings.tagline}" />
  <meta property="og:image" content="${lead?.imageUrl ?: siteSettings.logoUrl}" />
  <meta property="og:site_name" content="${siteSettings.siteName}" />

  <!-- Twitter Card -->
  <meta name="twitter:card" content="summary_large_image" />
  <meta name="twitter:title" content="${siteSettings.siteName}" />
  <meta name="twitter:description" content="${siteSettings.tagline}" />
  <meta name="twitter:image" content="${lead?.imageUrl ?: siteSettings.logoUrl}" />

  <!-- Google Fonts Bengali -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Hind+Siliguri:wght@400;500;600;700&family=Noto+Serif+Bengali:wght@600;700;800&display=swap" rel="stylesheet">
  
  <link rel="stylesheet" href="style.css" />

  ${if (adsConfig.isEnabled && adsConfig.popunderActive) adsConfig.popunderCode else "<!-- Adsterra Popunder Disabled -->"}
  ${if (adsConfig.isEnabled && adsConfig.socialBarActive) adsConfig.socialBarCode else "<!-- Adsterra Social Bar Disabled -->"}
</head>
<body>

  <!-- Top Bar -->
  <div class="top-header-bar">
    <div class="container top-bar-flex">
      <div class="date-time">
        <span>📅 রবিবার, ০৬ সেপ্টেম্বর ২০২৬</span>
        <span class="divider">|</span>
        <span>ঢাকা, বাংলাদেশ</span>
      </div>
      <div class="top-links">
        <a href="#about">আমাদের সম্পর্কে</a>
        <a href="#contact">যোগাযোগ</a>
        <a href="#privacy">গোপনীয়তা নীতি</a>
      </div>
    </div>
  </div>

  <!-- Main Header & Branding -->
  <header class="main-header">
    <div class="container header-flex">
      <div class="brand">
        <a href="index.html" class="logo-link">
          <span class="logo-accent">গোলাপি</span>
          <span class="logo-main">নিউজ</span>
        </a>
        <p class="tagline">${siteSettings.tagline}</p>
      </div>

      <!-- Header Adsterra Banner -->
      <div class="header-ad-space">
        ${if (adsConfig.isEnabled && adsConfig.headerBannerActive) """
        <div class="adsterra-slot header-ad">
          <span class="ad-label">বিজ্ঞাপন / Adsterra 728x90</span>
          ${adsConfig.headerBannerCode}
        </div>
        """ else """
        <div class="ad-placeholder">
          <span>বিজ্ঞাপনের স্থান (728x90)</span>
        </div>
        """}
      </div>
    </div>
  </header>

  <!-- Navigation Bar -->
  <nav class="nav-bar">
    <div class="container nav-flex">
      <div class="nav-scroll-wrapper">
        <ul class="nav-links">
          <li><a href="index.html" class="active">সর্বশেষ</a></li>
          ${categories.filter { it.isVisible && it.slug != "latest" }.joinToString("\n          ") { cat ->
              """<li><a href="#category-${cat.name}">${cat.name}</a></li>"""
          }}
        </ul>
      </div>
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input type="search" id="searchInput" placeholder="সংবাদ অনুসন্ধান করুন..." oninput="handleSearch(this.value)" autocomplete="off" aria-label="সংবাদ অনুসন্ধান" />
        <button type="button" class="search-clear-inline" onclick="clearSearch()" title="অনুসন্ধান মুছুন">✕</button>
      </div>
    </div>
  </nav>

  <!-- Breaking News Ticker -->
  <div class="breaking-news-wrapper">
    <div class="container breaking-flex">
      <div class="breaking-badge">ব্রেকিং নিউজ</div>
      <div class="breaking-ticker-marquee" id="tickerText">
        $breakingText
      </div>
    </div>
  </div>

  <!-- Main Content Layout -->
  <main class="container main-layout" id="mainNewsArea">
    <div class="content-left">
      
      <!-- Dynamic Search / Category Filter Status Banner -->
      <div id="searchStatusBanner" style="display: none;"></div>

      <!-- Lead / Featured News Container -->
      <div id="leadNewsContainer">
        ${lead?.let { l -> """
        <section class="lead-news-card" onclick="openArticle('${l.id}')" role="button" tabindex="0">
          <div class="lead-image-wrap">
            ${renderResponsiveImg(l.imageUrl, l.title, "(max-width: 640px) 100vw, (max-width: 1024px) 800px, 1000px")}
            <span class="category-pill">${l.category} • শীর্ষ সংবাদ</span>
          </div>
          <div class="lead-body">
            <h1 class="lead-title">${l.title}</h1>
            <p class="lead-excerpt">${l.shortDescription}</p>
            <div class="meta-row">
              <span class="author">✍️ ${l.author}</span>
              <span class="time">🕒 ${l.publishDate}</span>
            </div>
          </div>
        </section>
        """ } ?: ""}
      </div>

      <!-- Top Story Grid Section -->
      <div class="section-title-row">
        <h2 class="section-heading" id="storiesSectionHeading">শীর্ষ সংবাদ</h2>
      </div>

      <div class="stories-grid" id="storiesGrid">
        ${latest.filter { it.id != lead?.id }.take(4).joinToString("\n") { item -> """
        <article class="story-card" onclick="openArticle('${item.id}')" role="button" tabindex="0">
          <div class="card-img-wrap">
            ${renderResponsiveImg(item.imageUrl, item.title, "(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 380px")}
            <span class="card-badge">${item.category}</span>
          </div>
          <div class="card-body">
            <h3 class="card-title">${item.title}</h3>
            <p class="card-snippet">${item.shortDescription.take(90)}...</p>
            <div class="card-meta">
              <span class="card-time">🕒 ${item.publishDate}</span>
              ${if (item.viewsCount > 0) """<span class="card-views">👁️ ${item.viewsCount} বার পঠিত</span>""" else ""}
            </div>
          </div>
        </article>
        """ }}
      </div>

      <!-- In-Feed Adsterra Banner -->
      ${if (adsConfig.isEnabled && adsConfig.inArticleBannerActive) """
      <div class="adsterra-infeed-ad">
        <span class="ad-label">বিজ্ঞাপন / Adsterra Banner</span>
        ${adsConfig.inArticleBannerCode}
      </div>
      """ else ""}

      <!-- Video & Media News Section -->
      <section class="video-news-section" id="video">
        <div class="section-title-row">
          <h2 class="section-heading">🎥 ভিডিও ও চিত্রশালা</h2>
        </div>
        <div class="video-grid" id="videoGrid">
          ${newsList.filter { it.videoUrl != null || it.category == "বিনোদন" || it.category == "খেলাধুলা" }.take(2).joinToString("\n") { item -> """
          <div class="video-card" onclick="openArticle('${item.id}')" role="button" tabindex="0">
            <div class="video-thumb-wrap">
              ${renderResponsiveImg(item.imageUrl, item.title, "(max-width: 640px) 100vw, 500px")}
              <div class="play-icon-overlay">▶</div>
            </div>
            <h4 class="video-title">${item.title}</h4>
          </div>
          """ }}
        </div>
      </section>

    </div>

    <!-- Sidebar -->
    <aside class="sidebar-right">
      <!-- Sidebar Adsterra 300x250 Banner -->
      ${if (adsConfig.isEnabled && adsConfig.sidebarBannerActive) """
      <div class="adsterra-slot sidebar-ad">
        <span class="ad-label">বিজ্ঞাপন / Adsterra 300x250</span>
        ${adsConfig.sidebarBannerCode}
      </div>
      """ else """
      <div class="ad-placeholder sidebar">
        <span>বিজ্ঞাপনের স্থান (300x250)</span>
      </div>
      """}

      <!-- Popular / Most Read News -->
      <div class="sidebar-widget popular-widget">
        <h3 class="widget-title">🔥 সর্বাধিক পঠিত</h3>
        <ol class="popular-list" id="popularSidebar">
          ${newsList.sortedByDescending { it.viewsCount }.take(5).mapIndexed { idx, itm -> """
          <li onclick="openArticle('${itm.id}')" role="button" tabindex="0">
            <span class="rank-num">${idx + 1}</span>
            <div class="pop-details">
              <h4>${itm.title}</h4>
              <span class="pop-time">${itm.publishDate}</span>
            </div>
          </li>
          """ }.joinToString("\n")}
        </ol>
      </div>

      <!-- Social Follow Widget -->
      <div class="sidebar-widget social-widget">
        <h3 class="widget-title">আমাদের সাথে থাকুন</h3>
        <div class="social-btn-group">
          <a href="${siteSettings.facebookUrl}" target="_blank" rel="noopener noreferrer" class="social-btn fb">ফেসবুক পেজ</a>
          <a href="${siteSettings.youtubeUrl}" target="_blank" rel="noopener noreferrer" class="social-btn yt">ইউটিউব চ্যানেল</a>
        </div>
      </div>
    </aside>
  </main>

  <!-- Footer Banner Ad -->
  ${if (adsConfig.isEnabled && adsConfig.footerBannerActive) """
  <div class="container footer-ad-container">
    <div class="adsterra-slot footer-ad">
      <span class="ad-label">বিজ্ঞাপন / Adsterra 728x90</span>
      ${adsConfig.footerBannerCode}
    </div>
  </div>
  """ else ""}

  <!-- Footer -->
  <footer class="site-footer">
    <div class="container footer-grid">
      <div class="footer-about" id="about">
        <h3 class="footer-logo"><span id="footerSiteName">${siteSettings.siteName}</span></h3>
        <p>${siteSettings.aboutUsText}</p>
        <p class="copyright">© ২০২৬ ${siteSettings.siteName}। সর্বস্বত্ব সংরক্ষিত।</p>
      </div>
      <div class="footer-contact" id="contact">
        <h4>যোগাযোগ ও বার্তা বিভাগ</h4>
        <p><strong>ইমেইল:</strong> <span id="footerEmail">${siteSettings.contactEmail}</span></p>
        <p><strong>মোবাইল:</strong> <span id="footerPhone">${siteSettings.phone}</span></p>
        <p><strong>ঠিকানা:</strong> <span id="footerAddress">${siteSettings.address}</span></p>
      </div>
      <div class="footer-links" id="privacy">
        <h4>গুরুত্বপূর্ণ লিংক</h4>
        <ul>
          <li><a href="article.html?page=about">আমাদের সম্পর্কে</a></li>
          <li><a href="article.html?page=privacy">গোপনীয়তা নীতি</a></li>
          <li><a href="article.html?page=terms">ব্যবহারের শর্তাবলী</a></li>
          <li><a href="index.html">বিজ্ঞাপন দিন</a></li>
        </ul>
      </div>
    </div>
  </footer>

  <script src="app.js"></script>
</body>
</html>"""
    }

    fun generateArticleHtml(adsConfig: AdsConfig, siteSettings: SiteSettings): String {
        return """<!DOCTYPE html>
<html lang="bn">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title id="pageTitle">সংবাদ বিস্তারিত | ${siteSettings.siteName}</title>
  
  <!-- Favicon -->
  <link rel="icon" type="image/svg+xml" href="favicon.svg" />
  <link rel="alternate icon" href="favicon.svg" type="image/svg+xml" />

  <!-- SEO & Social Sharing Dynamic Tags -->
  <meta name="title" id="metaTitle" content="${siteSettings.siteName}" />
  <meta name="description" id="metaDesc" content="${siteSettings.tagline}" />

  <!-- Open Graph / Facebook -->
  <meta property="og:type" content="article" />
  <meta property="og:url" id="ogUrl" content="https://golapinews.netlify.app/article.html" />
  <meta property="og:title" id="ogTitle" content="${siteSettings.siteName}" />
  <meta property="og:description" id="ogDesc" content="${siteSettings.tagline}" />
  <meta property="og:image" id="ogImage" content="${siteSettings.logoUrl}" />
  <meta property="og:site_name" content="${siteSettings.siteName}" />

  <!-- Google Fonts Bengali -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Hind+Siliguri:wght@400;500;600;700&family=Noto+Serif+Bengali:wght@600;700;800&display=swap" rel="stylesheet">
  
  <link rel="stylesheet" href="style.css" />

  ${if (adsConfig.isEnabled && adsConfig.popunderActive) adsConfig.popunderCode else ""}
  ${if (adsConfig.isEnabled && adsConfig.socialBarActive) adsConfig.socialBarCode else ""}
</head>
<body>

  <!-- Top Bar -->
  <div class="top-header-bar">
    <div class="container top-bar-flex">
      <div class="date-time">
        <span>📅 গোলাপি নিউজ | ডিজিটাল সংস্করণ</span>
      </div>
      <div class="top-links">
        <a href="index.html">← হোমপেজে ফিরুন</a>
      </div>
    </div>
  </div>

  <!-- Header -->
  <header class="main-header">
    <div class="container header-flex">
      <div class="brand">
        <a href="index.html" class="logo-link">
          <span class="logo-accent">গোলাপি</span>
          <span class="logo-main">নিউজ</span>
        </a>
      </div>
      <div class="header-ad-space">
        ${if (adsConfig.isEnabled && adsConfig.headerBannerActive) adsConfig.headerBannerCode else ""}
      </div>
    </div>
  </header>

  <main class="container article-layout">
    <article class="article-content">
      <div class="article-category-badge" id="artCategory">জাতীয়</div>
      <h1 class="article-main-title" id="artTitle">সংবাদ লোড হচ্ছে...</h1>
      
      <div class="article-meta-bar">
        <div class="author-info">
          <span class="author-avatar">👤</span>
          <div>
            <strong id="artAuthor">প্রতিবেদক</strong>
            <span class="pub-date" id="artDate">০৫ সেপ্টেম্বর ২০২৬</span>
          </div>
        </div>
        <div class="text-resizer">
          <span>ফন্ট সাইজ:</span>
          <button onclick="changeFontSize(-1)">A-</button>
          <button onclick="changeFontSize(1)">A+</button>
        </div>
      </div>

      <!-- Social Share Buttons -->
      <div class="share-box">
        <span class="share-title">শেয়ার করুন:</span>
        <button class="share-btn fb" onclick="shareFacebook()">Facebook</button>
        <button class="share-btn wa" onclick="shareWhatsApp()">WhatsApp</button>
        <button class="share-btn me" onclick="shareMessenger()">Messenger</button>
        <button class="share-btn cp" onclick="copyArticleLink()">Copy Link</button>
      </div>

      <!-- Featured Image -->
      <figure class="article-featured-figure">
        <img id="artImage" src="" alt="Featured News Image" />
        <figcaption id="artCaption"></figcaption>
      </figure>

      <!-- In-Article Adsterra Banner -->
      ${if (adsConfig.isEnabled && adsConfig.inArticleBannerActive) """
      <div class="adsterra-infeed-ad">
        <span class="ad-label">বিজ্ঞাপন / Adsterra</span>
        ${adsConfig.inArticleBannerCode}
      </div>
      """ else ""}

      <!-- Article Body -->
      <div class="article-text-body" id="artBody"></div>

      <!-- Related News -->
      <section class="related-section">
        <h3 class="section-heading">সম্পর্কিত সংবাদ</h3>
        <div class="related-grid" id="relatedList"></div>
      </section>
    </article>

    <!-- Sidebar -->
    <aside class="sidebar-right">
      ${if (adsConfig.isEnabled && adsConfig.sidebarBannerActive) """
      <div class="adsterra-slot sidebar-ad">
        <span class="ad-label">বিজ্ঞাপন / Adsterra</span>
        ${adsConfig.sidebarBannerCode}
      </div>
      """ else ""}
      <div class="sidebar-widget">
        <h3 class="widget-title">সর্বাধিক পঠিত</h3>
        <ol class="popular-list" id="popularSidebar"></ol>
      </div>
    </aside>
  </main>

  <footer class="site-footer">
    <div class="container footer-grid">
      <div class="footer-about" id="about">
        <h3 class="footer-logo"><span id="footerSiteName">${siteSettings.siteName}</span></h3>
        <p>${siteSettings.aboutUsText}</p>
        <p class="copyright">© ২০২৬ ${siteSettings.siteName}। সর্বস্বত্ব সংরক্ষিত।</p>
      </div>
      <div class="footer-contact" id="contact">
        <h4>যোগাযোগ ও বার্তা বিভাগ</h4>
        <p><strong>ইমেইল:</strong> <span id="footerEmail">${siteSettings.contactEmail}</span></p>
        <p><strong>মোবাইল:</strong> <span id="footerPhone">${siteSettings.phone}</span></p>
        <p><strong>ঠিকানা:</strong> <span id="footerAddress">${siteSettings.address}</span></p>
      </div>
      <div class="footer-links">
        <h4>গুরুত্বপূর্ণ লিংক</h4>
        <ul>
          <li><a href="article.html?page=about">আমাদের সম্পর্কে</a></li>
          <li><a href="article.html?page=privacy">গোপনীয়তা নীতি</a></li>
          <li><a href="article.html?page=terms">ব্যবহারের শর্তাবলী</a></li>
          <li><a href="index.html">বিজ্ঞাপন দিন</a></li>
        </ul>
      </div>
    </div>
  </footer>

  <script src="app.js"></script>
</body>
</html>"""
    }

    fun generateStyleCss(): String {
        return """/* গোলাপি নিউজ (Golapi News) - Modern Editorial Style */
:root {
  --rose-primary: #E11D48;
  --rose-dark: #BE185D;
  --rose-deep: #9F1239;
  --rose-light: #FFF1F2;
  --rose-container: #FFE4E6;
  --slate-900: #0F172A;
  --slate-800: #1E293B;
  --slate-700: #334155;
  --slate-600: #475569;
  --slate-500: #64748B;
  --slate-200: #E2E8F0;
  --slate-100: #F1F5F9;
  --slate-50: #F8FAFC;
  --white: #FFFFFF;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Hind Siliguri', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: var(--slate-50);
  color: var(--slate-900);
  line-height: 1.6;
  font-size: 16px;
}

a {
  color: inherit;
  text-decoration: none;
}

.container {
  width: 92%;
  max-width: 1200px;
  margin: 0 auto;
}

/* Top Bar */
.top-header-bar {
  background: var(--slate-900);
  color: #94A3B8;
  font-size: 13px;
  padding: 6px 0;
}
.top-bar-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.top-links a {
  margin-left: 14px;
  color: #CBD5E1;
  transition: color 0.2s;
}
.top-links a:hover {
  color: #FDA4AF;
}
.divider {
  margin: 0 8px;
  color: #475569;
}

/* Header */
.main-header {
  background: var(--white);
  border-bottom: 2px solid var(--rose-primary);
  padding: 16px 0;
}
.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.brand .logo-link {
  font-size: 38px;
  font-weight: 800;
  letter-spacing: -0.5px;
  display: inline-block;
  line-height: 1;
}
.logo-accent {
  color: var(--rose-primary);
  background: var(--rose-container);
  padding: 2px 10px;
  border-radius: 6px;
  margin-right: 4px;
}
.logo-main {
  color: var(--slate-900);
}
.tagline {
  font-size: 13px;
  color: var(--slate-600);
  margin-top: 4px;
}

/* Header Ad */
.header-ad-space {
  max-width: 728px;
}
.ad-placeholder {
  width: 728px;
  height: 90px;
  background: var(--slate-100);
  border: 1px dashed var(--slate-300, #CBD5E1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--slate-500);
}
.ad-placeholder.sidebar {
  width: 100%;
  height: 250px;
}
.ad-label {
  display: block;
  font-size: 10px;
  color: #B45309;
  background: #FEF3C7;
  padding: 1px 6px;
  text-align: center;
  width: fit-content;
  margin-bottom: 4px;
}

/* Nav Bar */
.nav-bar {
  background: var(--white);
  box-shadow: 0 2px 4px rgba(0,0,0,0.03);
  position: sticky;
  top: 0;
  z-index: 100;
}
.nav-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  overflow-x: auto;
}
.nav-links {
  display: flex;
  list-style: none;
  gap: 4px;
  white-space: nowrap;
}
.nav-links a {
  display: block;
  padding: 12px 14px;
  font-weight: 600;
  color: var(--slate-800);
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
}
.nav-links a:hover,
.nav-links a.active {
  color: var(--rose-primary);
  border-bottom-color: var(--rose-primary);
}
.search-box input {
  padding: 6px 12px;
  border: 1px solid var(--slate-200);
  border-radius: 20px;
  font-size: 13px;
  outline: none;
}
.search-box input:focus {
  border-color: var(--rose-primary);
}

/* Breaking Ticker */
.breaking-news-wrapper {
  background: #FFF1F2;
  border-top: 1px solid #FECDD3;
  border-bottom: 1px solid #FECDD3;
  padding: 8px 0;
  margin-bottom: 20px;
}
.breaking-flex {
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}
.breaking-badge {
  background: var(--rose-primary);
  color: var(--white);
  padding: 3px 12px;
  font-size: 13px;
  font-weight: 700;
  border-radius: 4px;
  white-space: nowrap;
  animation: pulse 2s infinite;
}
.breaking-ticker-marquee {
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
  color: var(--rose-deep);
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Layout */
.main-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 28px;
  margin-bottom: 40px;
}

/* Lead News */
.lead-news-card {
  background: var(--white);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
  margin-bottom: 28px;
  transition: transform 0.2s;
}
.lead-news-card:hover {
  transform: translateY(-2px);
}
.lead-image-wrap {
  position: relative;
  height: 380px;
  overflow: hidden;
}
.lead-image-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.category-pill {
  position: absolute;
  top: 14px;
  left: 14px;
  background: var(--rose-primary);
  color: var(--white);
  padding: 4px 12px;
  font-size: 13px;
  font-weight: 700;
  border-radius: 4px;
}
.lead-body {
  padding: 20px;
}
.lead-title {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.35;
  color: var(--slate-900);
  margin-bottom: 10px;
}
.lead-excerpt {
  font-size: 16px;
  color: var(--slate-700);
  margin-bottom: 14px;
}
.meta-row {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--slate-500);
}

/* Top Story Grid */
.section-heading {
  font-size: 22px;
  font-weight: 700;
  color: var(--slate-900);
  border-left: 4px solid var(--rose-primary);
  padding-left: 10px;
  margin-bottom: 18px;
}
.stories-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}
.story-card {
  background: var(--white);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  cursor: pointer;
  transition: transform 0.2s;
}
.story-card:hover {
  transform: translateY(-2px);
}
.card-img-wrap {
  position: relative;
  height: 180px;
}
.card-img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.card-badge {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: rgba(15, 23, 42, 0.85);
  color: var(--white);
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}
.card-body {
  padding: 14px;
}
.card-title {
  font-size: 17px;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 8px;
}
.card-snippet {
  font-size: 13px;
  color: var(--slate-600);
  margin-bottom: 10px;
}
.card-time {
  font-size: 12px;
  color: var(--slate-500);
}

/* In-Feed Ad */
.adsterra-infeed-ad {
  background: var(--white);
  padding: 14px;
  border: 1px solid var(--slate-200);
  text-align: center;
  margin-bottom: 30px;
  border-radius: 6px;
}

/* Video Section */
.video-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 30px;
}
.video-card {
  background: var(--white);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}
.video-thumb-wrap {
  position: relative;
  height: 160px;
}
.video-thumb-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.play-icon-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: white;
  background: rgba(0,0,0,0.4);
}
.video-title {
  padding: 10px;
  font-size: 15px;
  font-weight: 600;
}

/* Sidebar Widgets */
.sidebar-widget {
  background: var(--white);
  padding: 18px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  margin-bottom: 24px;
}
.widget-title {
  font-size: 18px;
  font-weight: 700;
  border-bottom: 2px solid var(--rose-primary);
  padding-bottom: 8px;
  margin-bottom: 14px;
}
.popular-list {
  list-style: none;
}
.popular-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--slate-100);
  cursor: pointer;
}
.popular-list li:last-child {
  border-bottom: none;
}
.rank-num {
  font-size: 22px;
  font-weight: 800;
  color: var(--rose-primary);
  line-height: 1;
  min-width: 24px;
}
.pop-details h4 {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  margin-bottom: 4px;
}
.pop-time {
  font-size: 11px;
  color: var(--slate-500);
}
.social-btn-group {
  display: flex;
  gap: 10px;
}
.social-btn {
  flex: 1;
  text-align: center;
  padding: 10px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 13px;
  color: white;
}
.social-btn.fb { background: #1877F2; }
.social-btn.yt { background: #FF0000; }

/* Article Details Page */
.article-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 28px;
  margin-top: 24px;
  margin-bottom: 40px;
}
.article-content {
  background: var(--white);
  padding: 28px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.article-category-badge {
  display: inline-block;
  background: var(--rose-container);
  color: var(--rose-deep);
  font-weight: 700;
  font-size: 13px;
  padding: 3px 10px;
  border-radius: 4px;
  margin-bottom: 12px;
}
.article-main-title {
  font-size: 32px;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 16px;
}
.article-meta-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid var(--slate-200);
  border-bottom: 1px solid var(--slate-200);
  margin-bottom: 18px;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.author-avatar {
  font-size: 24px;
}
.pub-date {
  display: block;
  font-size: 12px;
  color: var(--slate-500);
}
.text-resizer button {
  background: var(--slate-100);
  border: 1px solid var(--slate-200);
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}
.share-box {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.share-title {
  font-size: 13px;
  font-weight: 600;
}
.share-btn {
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
}
.share-btn.fb { background: #1877F2; }
.share-btn.wa { background: #25D366; }
.share-btn.me { background: #0084FF; }
.share-btn.cp { background: #475569; }

.article-featured-figure {
  margin-bottom: 24px;
}
.article-featured-figure img {
  width: 100%;
  border-radius: 6px;
  max-height: 480px;
  object-fit: cover;
}
.article-featured-figure figcaption {
  font-size: 13px;
  color: var(--slate-600);
  margin-top: 6px;
  text-align: center;
  font-style: italic;
}
.article-text-body {
  font-size: 18px;
  line-height: 1.8;
  color: var(--slate-800);
  white-space: pre-line;
  margin-bottom: 30px;
}

/* Footer */
.site-footer {
  background: var(--slate-900);
  color: #CBD5E1;
  padding: 36px 0;
  margin-top: 50px;
}
.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr;
  gap: 30px;
}
.footer-logo {
  color: var(--rose-primary);
  font-size: 24px;
  margin-bottom: 10px;
}
.footer-links ul {
  list-style: none;
}
.footer-links li {
  margin-bottom: 6px;
}
.footer-links a:hover {
  color: #FDA4AF;
}

/* Responsive */
@media (max-width: 900px) {
  .main-layout,
  .article-layout {
    grid-template-columns: 1fr;
  }
  .stories-grid,
  .video-grid {
    grid-template-columns: 1fr;
  }
  .header-flex {
    flex-direction: column;
    text-align: center;
  }
  .ad-placeholder {
    width: 100%;
  }
  .footer-grid {
    grid-template-columns: 1fr;
  }
}
"""
    }

    fun generateAppJs(): String {
        return """// গোলাপি নিউজ (Golapi News) - Client Application JS
let newsData = null;
let currentFontSize = 18;
let currentCategory = 'all';
let searchQuery = '';

// Load news data from JSON
async function loadData() {
  try {
    const res = await fetch('news-data.json?v=' + Date.now());
    if (!res.ok) throw new Error('HTTP ' + res.status);
    newsData = await res.json();
    initApp();
  } catch (err) {
    console.error('Error loading news-data.json:', err);
  }
}

function initApp() {
  const isArticle = window.location.pathname.includes('article.html') || window.location.search.includes('id=') || window.location.search.includes('page=');
  if (isArticle) {
    loadArticleView();
  } else {
    initHomePage();
  }
}

function openArticle(id) {
  window.location.href = 'article.html?id=' + encodeURIComponent(id);
}

// HOMEPAGE LOGIC
function initHomePage() {
  if (!newsData) return;
  syncFooterInfo();
  renderBreakingTicker();
  renderPopularSidebar();

  const hash = decodeURIComponent(window.location.hash || '');
  if (hash.startsWith('#category-')) {
    const catName = hash.replace('#category-', '');
    setCategory(catName);
  } else {
    renderNewsFeed();
  }

  setupCategoryTabs();
}

function syncFooterInfo() {
  if (!newsData || !newsData.siteSettings) return;
  const s = newsData.siteSettings;
  const fEmail = document.getElementById('footerEmail');
  const fPhone = document.getElementById('footerPhone');
  const fAddress = document.getElementById('footerAddress');
  const fName = document.getElementById('footerSiteName');

  if (fEmail && s.contactEmail) fEmail.innerText = s.contactEmail;
  if (fPhone && s.phone) fPhone.innerText = s.phone;
  if (fAddress && s.address) fAddress.innerText = s.address;
  if (fName && s.siteName) fName.innerText = s.siteName;
}

// Responsive images with srcset & sizes for fast loading and reduced data usage on mobile
function getSrcSet(url) {
  if (!url || typeof url !== 'string') return '';
  if (url.includes('images.unsplash.com')) {
    const base = url.split('?')[0];
    return base + '?w=400&q=75&auto=format&fit=crop 400w, ' +
           base + '?w=750&q=80&auto=format&fit=crop 750w, ' +
           base + '?w=1200&q=85&auto=format&fit=crop 1200w';
  }
  return '';
}

function renderResponsiveImg(url, alt, sizes, extraClass, id) {
  const srcset = getSrcSet(url);
  const srcsetAttr = srcset ? ' srcset="' + srcset + '" sizes="' + sizes + '"' : '';
  const classAttr = extraClass ? ' class="' + extraClass + '"' : '';
  const idAttr = id ? ' id="' + id + '"' : '';
  return '<img' + idAttr + classAttr + ' src="' + url + '"' + srcsetAttr + ' alt="' + escapeHtml(alt) + '" loading="lazy" decoding="async" />';
}

function renderBreakingTicker() {
  const ticker = document.getElementById('tickerText');
  if (!ticker || !newsData) return;
  if (newsData.breakingNews && newsData.breakingNews.length > 0) {
    ticker.innerText = newsData.breakingNews.map(b => b.headline).join('  •  ');
  } else if (newsData.news) {
    ticker.innerText = newsData.news.slice(0, 4).map(n => n.title).join('  •  ');
  }
}

function setupCategoryTabs() {
  const links = document.querySelectorAll('.nav-links a');
  links.forEach(link => {
    link.addEventListener('click', function(e) {
      const href = this.getAttribute('href');
      if (href && href.startsWith('#category-')) {
        e.preventDefault();
        const cat = href.replace('#category-', '');
        setCategory(cat);
      } else if (href === 'index.html' || href === '#all' || href === '#latest') {
        e.preventDefault();
        setCategory('all');
      }
    });
  });
}

function setCategory(catName) {
  currentCategory = catName;
  searchQuery = '';
  const searchInput = document.getElementById('searchInput');
  if (searchInput) searchInput.value = '';

  const links = document.querySelectorAll('.nav-links a');
  links.forEach(l => {
    const text = l.innerText.trim();
    if ((catName === 'all' && (text === 'সর্বশেষ' || l.getAttribute('href') === 'index.html')) ||
        (text.toLowerCase() === catName.toLowerCase())) {
      l.classList.add('active');
    } else {
      l.classList.remove('active');
    }
  });

  renderNewsFeed();
}

function handleSearch(query) {
  searchQuery = (query || '').trim();
  renderNewsFeed();
}

function clearSearch() {
  searchQuery = '';
  const searchInput = document.getElementById('searchInput');
  if (searchInput) {
    searchInput.value = '';
    searchInput.focus();
  }
  renderNewsFeed();
}

function clearCategoryFilter() {
  setCategory('all');
}

function renderNewsFeed() {
  if (!newsData || !newsData.news) return;

  const leadContainer = document.getElementById('leadNewsContainer');
  const sectionHeading = document.getElementById('storiesSectionHeading');
  const storiesGrid = document.getElementById('storiesGrid');
  const searchStatus = document.getElementById('searchStatusBanner');

  let filtered = [...newsData.news];

  if (searchQuery) {
    const q = searchQuery.toLowerCase();
    filtered = filtered.filter(item =>
      (item.title && item.title.toLowerCase().includes(q)) ||
      (item.shortDescription && item.shortDescription.toLowerCase().includes(q)) ||
      (item.fullContent && item.fullContent.toLowerCase().includes(q)) ||
      (item.category && item.category.toLowerCase().includes(q)) ||
      (item.author && item.author.toLowerCase().includes(q))
    );

    if (leadContainer) leadContainer.style.display = 'none';

    if (searchStatus) {
      searchStatus.style.display = 'block';
      searchStatus.innerHTML = `
        <div class="search-status-bar">
          <span>🔍 ‘<strong>` + escapeHtml(searchQuery) + `</strong>’ অনুসন্ধানে <strong>` + filtered.length + `</strong>টি সংবাদ পাওয়া গেছে</span>
          <button class="search-clear-btn" onclick="clearSearch()" title="অনুসন্ধান বাতিল">✕ বাতিল</button>
        </div>
      `;
    }

    if (sectionHeading) {
      sectionHeading.innerHTML = `অনুসন্ধানের ফলাফল`;
    }

  } else if (currentCategory && currentCategory !== 'all' && currentCategory !== 'সর্বশেষ') {
    filtered = filtered.filter(item => 
      item.category && item.category.trim().toLowerCase() === currentCategory.trim().toLowerCase()
    );

    if (leadContainer) leadContainer.style.display = 'none';

    if (searchStatus) {
      searchStatus.style.display = 'block';
      searchStatus.innerHTML = `
        <div class="category-filter-bar">
          <span class="active-cat-label">📂 ক্যাটাগরি: <strong>` + escapeHtml(currentCategory) + `</strong> (` + filtered.length + `টি সংবাদ)</span>
          <button class="filter-reset-btn" onclick="clearCategoryFilter()">← সব সংবাদ দেখুন</button>
        </div>
      `;
    }

    if (sectionHeading) {
      sectionHeading.innerHTML = escapeHtml(currentCategory) + ` সংবাদ`;
    }

  } else {
    if (searchStatus) {
      searchStatus.style.display = 'none';
      searchStatus.innerHTML = '';
    }

    if (sectionHeading) {
      sectionHeading.innerHTML = `শীর্ষ সংবাদ`;
    }

    const leadItem = newsData.news.find(n => n.isFeatured) || newsData.news[0];
    if (leadContainer && leadItem) {
      leadContainer.style.display = 'block';
      leadContainer.innerHTML = `
        <section class="lead-news-card" onclick="openArticle('` + leadItem.id + `')" role="button" tabindex="0">
          <div class="lead-image-wrap">
            ` + renderResponsiveImg(leadItem.imageUrl, leadItem.title, '(max-width: 640px) 100vw, (max-width: 1024px) 800px, 1000px') + `
            <span class="category-pill">` + escapeHtml(leadItem.category) + ` • শীর্ষ সংবাদ</span>
          </div>
          <div class="lead-body">
            <h1 class="lead-title">` + escapeHtml(leadItem.title) + `</h1>
            <p class="lead-excerpt">` + escapeHtml(leadItem.shortDescription) + `</p>
            <div class="meta-row">
              <span class="author">✍️ ` + escapeHtml(leadItem.author || 'নিজস্ব প্রতিবেদক') + `</span>
              <span class="time">🕒 ` + escapeHtml(leadItem.publishDate) + `</span>
            </div>
          </div>
        </section>
      `;
      filtered = filtered.filter(n => n.id !== leadItem.id);
    }
  }

  if (!storiesGrid) return;

  if (filtered.length === 0) {
    storiesGrid.innerHTML = `
      <div class="empty-news-state">
        <div class="empty-icon">📰</div>
        <h3>কোনো সংবাদ পাওয়া যায়নি</h3>
        <p>ভিন্ন কোনো শব্দ দিয়ে খুঁজুন অথবা অন্যান্য ক্যাটাগরি ব্রাউজ করুন।</p>
        <button class="btn-reset-explore" onclick="clearSearch(); clearCategoryFilter();">সকল সংবাদে ফিরে যান</button>
      </div>
    `;
    return;
  }

  storiesGrid.innerHTML = filtered.map(item => `
    <article class="story-card" onclick="openArticle('` + item.id + `')" role="button" tabindex="0">
      <div class="card-img-wrap">
        ` + renderResponsiveImg(item.imageUrl, item.title, '(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 380px') + `
        <span class="card-badge">` + escapeHtml(item.category) + `</span>
      </div>
      <div class="card-body">
        <h3 class="card-title">` + escapeHtml(item.title) + `</h3>
        <p class="card-snippet">` + escapeHtml(item.shortDescription || '') + `</p>
        <div class="card-meta">
          <span class="card-time">🕒 ` + escapeHtml(item.publishDate) + `</span>
          ` + (item.viewsCount ? `<span class="card-views">👁️ ` + item.viewsCount + ` বার পঠিত</span>` : '') + `
        </div>
      </div>
    </article>
  `).join('');
}

// ARTICLE VIEW LOGIC
function loadArticleView() {
  const urlParams = new URLSearchParams(window.location.search);
  const articleId = urlParams.get('id');
  const pageType = urlParams.get('page');

  if (pageType) {
    renderStaticPage(pageType);
    return;
  }

  if (!newsData || !newsData.news) return;
  const article = newsData.news.find(n => n.id === articleId) || newsData.news[0];
  if (!article) return;

  const pageTitle = document.getElementById('pageTitle');
  const artTitle = document.getElementById('artTitle');
  const artCategory = document.getElementById('artCategory');
  const artAuthor = document.getElementById('artAuthor');
  const artDate = document.getElementById('artDate');
  const artImage = document.getElementById('artImage');
  const artCaption = document.getElementById('artCaption');
  const artBody = document.getElementById('artBody');

  const siteName = (newsData.siteSettings && newsData.siteSettings.siteName) || 'গোলাপি নিউজ';

  if (pageTitle) pageTitle.innerText = article.title + ' | ' + siteName;
  if (artTitle) artTitle.innerText = article.title;
  if (artCategory) {
    artCategory.innerText = article.category;
    artCategory.onclick = () => { window.location.href = 'index.html#category-' + encodeURIComponent(article.category); };
    artCategory.style.cursor = 'pointer';
  }
  if (artAuthor) artAuthor.innerText = article.author || 'নিজস্ব প্রতিবেদক';
  if (artDate) artDate.innerText = 'প্রকাশিত: ' + article.publishDate;
  if (artImage) {
    artImage.src = article.imageUrl;
    artImage.alt = article.title;
    artImage.loading = 'eager';
    artImage.decoding = 'async';
    const srcset = getSrcSet(article.imageUrl);
    if (srcset) {
      artImage.srcset = srcset;
      artImage.sizes = "(max-width: 640px) 100vw, (max-width: 1024px) 800px, 900px";
    } else {
      artImage.removeAttribute('srcset');
      artImage.removeAttribute('sizes');
    }
  }
  if (artCaption) artCaption.innerText = article.imageCaption || article.title;
  if (artBody) artBody.innerText = article.fullContent;

  renderRelated(article.category, article.id);
  renderPopularSidebar();
  syncFooterInfo();
}

function renderStaticPage(type) {
  const titles = {
    about: 'আমাদের সম্পর্কে',
    privacy: 'গোপনীয়তা নীতি',
    terms: 'ব্যবহারের শর্তাবলী'
  };
  const s = newsData.siteSettings || {};
  const texts = {
    about: s.aboutUsText || 'গোলাপি নিউজ বাংলাদেশের একটি অগ্রণী ডিজিটাল বাংলা নিউজ পোর্টাল।',
    privacy: s.privacyPolicyText || 'পাঠকদের তথ্য সুরক্ষায় আমরা প্রতিশ্রুতিবদ্ধ।',
    terms: s.termsText || 'সকল কপিরাইট সংরক্ষিত।'
  };

  const pageTitle = document.getElementById('pageTitle');
  const artCategory = document.getElementById('artCategory');
  const artTitle = document.getElementById('artTitle');
  const artDate = document.getElementById('artDate');
  const artImage = document.getElementById('artImage');
  const artBody = document.getElementById('artBody');

  if (pageTitle) pageTitle.innerText = (titles[type] || 'তথ্য') + ' | ' + (s.siteName || 'গোলাপি নিউজ');
  if (artCategory) artCategory.innerText = s.siteName || 'গোলাপি নিউজ';
  if (artTitle) artTitle.innerText = titles[type] || 'তথ্য';
  if (artDate) artDate.innerText = 'আপডেট: ২০২৬';
  if (artImage) artImage.style.display = 'none';
  if (artBody) artBody.innerText = texts[type] || 'তথ্য প্রস্তুত করা হচ্ছে।';
  
  const shareBox = document.querySelector('.share-box');
  if (shareBox) shareBox.style.display = 'none';
  syncFooterInfo();
}

function renderRelated(category, excludeId) {
  const container = document.getElementById('relatedList');
  if (!container || !newsData || !newsData.news) return;
  const sameCat = newsData.news.filter(n => n.category === category && n.id !== excludeId);
  const other = newsData.news.filter(n => n.id !== excludeId && !sameCat.includes(n));
  const list = [...sameCat, ...other].slice(0, 3);

  container.innerHTML = list.map(item => `
    <div class="story-card" onclick="openArticle('` + item.id + `')" role="button" tabindex="0">
      <div class="card-img-wrap">
        ` + renderResponsiveImg(item.imageUrl, item.title, '(max-width: 640px) 100vw, 360px') + `
        <span class="card-badge">` + escapeHtml(item.category) + `</span>
      </div>
      <div class="card-body">
        <h4 class="card-title">` + escapeHtml(item.title) + `</h4>
        <span class="card-time">🕒 ` + escapeHtml(item.publishDate) + `</span>
      </div>
    </div>
  `).join('');
}

function renderPopularSidebar() {
  const container = document.getElementById('popularSidebar');
  if (!container || !newsData || !newsData.news) return;
  const list = [...newsData.news].sort((a, b) => (b.viewsCount || 0) - (a.viewsCount || 0)).slice(0, 5);
  container.innerHTML = list.map((item, idx) => `
    <li onclick="openArticle('` + item.id + `')" role="button" tabindex="0">
      <span class="rank-num">` + (idx + 1) + `</span>
      <div class="pop-details">
        <h4>` + escapeHtml(item.title) + `</h4>
        <span class="pop-time">` + escapeHtml(item.publishDate) + `</span>
      </div>
    </li>
  `).join('');
}

function shareFacebook() {
  const url = encodeURIComponent(window.location.href);
  window.open('https://www.facebook.com/sharer/sharer.php?u=' + url, '_blank', 'width=600,height=400');
}

function shareWhatsApp() {
  const text = encodeURIComponent(document.title + '\n' + window.location.href);
  window.open('https://api.whatsapp.com/send?text=' + text, '_blank');
}

function shareMessenger() {
  const url = encodeURIComponent(window.location.href);
  window.open('fb-messenger://share/?link=' + url, '_blank');
}

function copyArticleLink() {
  navigator.clipboard.writeText(window.location.href).then(() => {
    showToast('সংবাদের লিংক সফলভাবে কপি করা হয়েছে!');
  }).catch(() => {
    alert('লিংক কপি করা হয়েছে: ' + window.location.href);
  });
}

function changeFontSize(delta) {
  currentFontSize += delta;
  if (currentFontSize < 14) currentFontSize = 14;
  if (currentFontSize > 28) currentFontSize = 28;
  const body = document.getElementById('artBody');
  if (body) body.style.fontSize = currentFontSize + 'px';
}

function showToast(message) {
  let toast = document.getElementById('toastNotice');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'toastNotice';
    toast.className = 'toast-notice';
    document.body.appendChild(toast);
  }
  toast.innerText = message;
  toast.classList.add('show');
  setTimeout(() => {
    toast.classList.remove('show');
  }, 2500);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

document.addEventListener('DOMContentLoaded', loadData);
"""
    }

    fun generateNetlifyToml(): String {
        return """# Netlify Configuration for গোলাপি নিউজ (Golapi News)
[build]
  publish = "."

# Custom Headers for Security & Fast Caching
[[headers]]
  for = "/*"
  [headers.values]
    X-Frame-Options = "DENY"
    X-XSS-Protection = "1; mode=block"
    X-Content-Type-Options = "nosniff"
    Referrer-Policy = "strict-origin-when-cross-origin"

[[headers]]
  for = "/style.css"
  [headers.values]
    Cache-Control = "public, max-age=31536000, immutable"

[[headers]]
  for = "/news-data.json"
  [headers.values]
    Cache-Control = "no-cache, must-revalidate"
"""
    }

    fun generateDeployReadme(siteSettings: SiteSettings): String {
        return """# গোলাপি নিউজ (Golapi News) - Netlify Production Deployment Guide

এই ফোল্ডারের ফাইলগুলো দিয়ে খুব সহজেই **Netlify**-তে সম্পূর্ণ ফ্রি এবং কাস্টম ডোমেইনসহ আপনার নিউজ পোর্টাল লাইভ করতে পারবেন।

---

### ১. Netlify Drop দিয়ে সরাসরি ডিপ্লয় করার নিয়ম (১ মিনিটে লাইভ):
১. ব্রাউজারে যান: [https://app.netlify.com/drop](https://app.netlify.com/drop)
২. আপনার Netlify একাউন্টে লগইন করুন (ফ্রি একাউন্ট)।
৩. এই সমস্ত ফাইলগুলো (`index.html`, `article.html`, `style.css`, `app.js`, `news-data.json`, `netlify.toml`) একটি ফোল্ডারে রাখুন।
৪. ফোল্ডারটি ড্র্যাগ করে Netlify Drop বক্সে ছেড়ে দিন।
৫. সাথে সাথে আপনার সাইট লাইভ হয়ে যাবে এবং একটি ফ্রি URL (যেমন: `golapinews.netlify.app`) পাবেন!

---

### ২. Adsterra বিজ্ঞাপন সেটআপ করার স্থান ও নিয়ম:
১. **Popunder Code**:
   - `index.html` এবং `article.html`-এর `<head>` ট্যাগের ভেতর বসানো আছে।
   - অ্যাডমিন প্যানেলের "অ্যাডসেরা বিজ্ঞাপন" ট্যাব থেকে সরাসরি আপডেট করতে পারেন।
২. **Social Bar Code**:
   - `<head>` বা `<body>` এর শুরুতে বসানো আছে।
৩. **Banner Ads (728x90 Header, 300x250 Sidebar, 468x60 In-Article, Footer)**:
   - `index.html` এবং `article.html`-এ নির্দিষ্ট `<div class="adsterra-slot">` পাত্রে সুন্দরভাবে প্লেস করা রয়েছে।

---

### ৩. Facebook Share Preview (Open Graph) ঠিক করার নিয়ম:
- Facebook-এ সংবাদ শেয়ার করলে ছবি, টাইটেল ও বিবরণ ঠিকভাবে প্রদর্শনের জন্য Open Graph ট্যাগ যোগ করা আছে।
- যদি নতুন লিংক শেয়ার করার পর প্রিভিউ না আসে, তবে Facebook Sharing Debugger টুল ব্যবহার করুন:
  [https://developers.facebook.com/tools/debug/](https://developers.facebook.com/tools/debug/)
  সেখানে আপনার সংবাদের URL দিয়ে **"Scrape Again"** বাটনে ক্লিক করলেই ইমেজ ও টাইটেল সাথে সাথে ক্যাশ আপডেট হয়ে যাবে।

---

### ৪. ভবিষ্যৎ আপডেট ও কন্টেন্ট পরিবর্তন:
- আপনি গোলাপি নিউজ অ্যাডমিন প্যানেল থেকে যেকোনো সময় নতুন সংবাদ লিখলে, এডিট করলে বা বিজ্ঞাপন পরিবর্তন করলে:
  ১. অ্যাডমিন প্যানেলের **"নেটলিফাই লাইভ ফাইলস"** ট্যাবে যান।
  ২. **"Export news-data.json"** অথবা **"Download Complete Bundle"** বাটনে ক্লিক করুন।
  ৩. আপডেট করা ফাইলটি Netlify-তে রি-আপলোড করলেই কোনো সার্ভার রিস্টার্ট ছাড়াই সম্পূর্ণ সাইট মুহূর্তের মধ্যে আপডেট হয়ে যাবে!
"""
    }
}
