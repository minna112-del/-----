// গোলাপি নিউজ (Golapi News) - Client Application JS
let newsData = null;
let currentFontSize = 18;
let currentCategory = 'all';
let searchQuery = '';

// Load news data live from Firestore (admin panel দিয়ে যা পোস্ট হয় তা সাথে সাথে এখানে দেখাবে)
// ব্যর্থ হলে স্ট্যাটিক news-data.json, এরপরও ব্যর্থ হলে embedded fallback ব্যবহার হবে
async function loadData() {
  try {
    const { db } = await import('./firebase-config.js');
    const {
      collection, getDocs, doc, getDoc, query, orderBy
    } = await import('https://www.gstatic.com/firebasejs/10.13.1/firebase-firestore.js');

    const newsSnap = await getDocs(query(collection(db, 'news'), orderBy('createdAt', 'desc')));
    const news = newsSnap.docs
      .map(d => ({ id: d.id, ...d.data() }))
      .filter(n => n.isPublished !== false);

    if (news.length === 0) throw new Error('Firestore এ এখনো কোনো নিউজ নেই');

    const catSnap = await getDocs(collection(db, 'categories'));
    const categories = catSnap.docs
      .map(d => ({ id: d.id, ...d.data() }))
      .sort((a, b) => (a.order || 0) - (b.order || 0));

    const brkSnap = await getDocs(collection(db, 'breakingNews'));
    const breakingNews = brkSnap.docs
      .map(d => ({ id: d.id, ...d.data() }))
      .filter(b => b.isActive !== false);

    const settingsSnap = await getDoc(doc(db, 'siteSettings', 'main'));
    const siteSettings = settingsSnap.exists() ? settingsSnap.data() : {};

    newsData = { siteSettings, breakingNews, news, categories };
    initApp();
  } catch (err) {
    console.warn('Firestore থেকে লোড ব্যর্থ, স্ট্যাটিক news-data.json ট্রাই করা হচ্ছে:', err);
    try {
      const res = await fetch('news-data.json?v=' + Date.now());
      if (!res.ok) throw new Error('HTTP ' + res.status);
      newsData = await res.json();
      initApp();
    } catch (err2) {
      console.warn('news-data.json ও ব্যর্থ, embedded fallback ব্যবহার হচ্ছে:', err2);
      initFallbackData();
    }
  }
}

function initFallbackData() {
  newsData = {
    siteSettings: {
      siteName: "গোলাপি নিউজ",
      englishName: "Golapi News",
      tagline: "সত্যের নির্ভীক সারথি — আধুনিক ডিজিটাল বাংলা সংবাদপত্র",
      contactEmail: "golapishoponline.bd@gmail.com",
      phone: "01612-057371",
      hotline: "01612-057371",
      address: "চৌরাস্তা ,বেগমগণ্জ, নোয়াখালী , বাংলাদেশ",
      facebookUrl: "https://facebook.com/golapinews",
      youtubeUrl: "https://youtube.com/@golapinews",
      aboutUsText: "‘গোলাপি নিউজ’ বাংলাদেশের একটি অগ্রণী আধুনিক ডিজিটাল বাংলা নিউজ পোর্টাল। নির্ভীক সাংবাদিকতা, বস্তুনিষ্ঠ সংবাদ ও তাৎক্ষণিক তথ্য প্রবাহ নিশ্চিত করাই আমাদের মূল অঙ্গীকার।",
      privacyPolicyText: "গোলাপি নিউজ পাঠকদের তথ্যের গোপনীয়তা রক্ষা করতে প্রতিশ্রুতিবদ্ধ। এই সাইটটি ব্রাউজিংয়ের সময় আপনার ব্যক্তিগত তথ্য সুরক্ষায় সর্বোচ্চ সতর্কতা অবলম্বন করে। কুকিজ এবং বিজ্ঞাপনী নেটওয়ার্ক (যেমন Adsterra) সম্পর্কিত তথ্যাদি আন্তর্জাতিক ডিজিটাল নিয়ম অনুযায়ী ব্যবহৃত হয়।",
      termsText: "গোলাপি নিউজ সাইট ব্যবহারের সময় সকল পাঠককে শালীনতা ও দেশের প্রচলিত সাইবার আইন মেনে চলার অনুরোধ করা হচ্ছে। আমাদের প্রকাশিত সকল সংবাদ, ছবি ও ভিডিওর সর্বস্বত্ব গোলাপি নিউজ কর্তৃপক্ষের সংরক্ষিত।"
    },
    breakingNews: [
      { id: "brk_1", headline: "আন্তর্জাতিক বাজারে জ্বালানি তেলের দাম আরও কমেছে, দেশে সমন্বয়ের ইঙ্গিত", timestamp: "৫ মিনিট আগে" },
      { id: "brk_2", headline: "মেট্রোরেলে নতুন ১০টি স্টেশন চালু, যাত্রীদের অভূতপূর্ব স্বস্তি ও ব্যাপক ভিড়", timestamp: "১৫ মিনিট আগে" },
      { id: "brk_3", headline: "টি-টোয়েন্টি সিরিজে ঐতিহাসিক জয় পেল বাংলাদেশ ক্রিকেট দল", timestamp: "৩০ মিনিট আগে" },
      { id: "brk_4", headline: "বঙ্গোপসাগরে সৃষ্ট লঘুচাপের কারণে ৩ নম্বর সতর্কতা সংকেত জারি", timestamp: "১ ঘণ্টা আগে" }
    ],
    news: [
      {
        id: "news_lead",
        title: "বাংলাদেশের ডিজিটাল অর্থনীতিতে নতুন বিপ্লব: রপ্তানি ছাড়াল রেকর্ড ১০ বিলিয়ন ডলার",
        category: "জাতীয়",
        author: "তাহমিদ হাসান",
        publishDate: "০৫ সেপ্টেম্বর ২০২৬, সকাল ১০:১৫",
        imageUrl: "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=800&auto=format&fit=crop&q=80",
        imageCaption: "রাজধানীতে সফটওয়্যার পার্ক ও হাইটেক প্রকল্পের নতুন কেন্দ্রবিন্দু",
        shortDescription: "তথ্যপ্রযুক্তি খাতের অভূতপূর্ব বিকাশে বৈশ্বিক বাজারে বাংলাদেশের সফটওয়্যার ও আইটি সেবা খাতের রপ্তানি প্রথমবারের মতো ১০ বিলিয়ন ডলারের মাইলফলক স্পর্শ করেছে।",
        fullContent: "তথ্যপ্রযুক্তি খাতে ঐতিহাসিক এক সাফল্যের সাক্ষী হলো বাংলাদেশ। চলতি অর্থবছরে দেশের আইটি ও ফ্রিল্যান্সিং খাতের মোট বৈশ্বিক আয় ১০ বিলিয়ন ডলার অতিক্রম করেছে।\n\nসংশ্লিষ্ট বিশেষজ্ঞরা জানিয়েছেন, তরুণ উদ্যোক্তাদের জন্য করমুক্ত সুযোগ, আন্তর্জাতিক পেমেন্ট গেটওয়ের সহজলভ্যতা এবং দেশজুড়ে দ্রুতগতির ব্রডব্যান্ড নেটওয়ার্ক বিস্তারের কারণেই এ অভূতপূর্ব প্রবৃদ্ধি সম্ভব হয়েছে।\n\nপরিকল্পনা কমিশনের এক বিশেষ প্রতিবেদনে উল্লেখ করা হয়, ফ্রিল্যান্সিং ও সফটওয়্যার ডেভেলপমেন্টে এখন প্রত্যক্ষ ও পরোক্ষভাবে যুক্ত রয়েছেন প্রায় ১৫ লাখ তরুণ-তরুণী।",
        isFeatured: true,
        viewsCount: 12450
      },
      {
        id: "news_metro",
        title: "মেট্রোরেলে নতুন ১০টি স্টেশন পুরোদমে চালু: যানজটমুক্ত রাজধানীর নতুন রূপ",
        category: "জাতীয়",
        author: "ফারহানা করিম",
        publishDate: "০৫ সেপ্টেম্বর ২০২৬, সকাল ০৯:০০",
        imageUrl: "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800&auto=format&fit=crop&q=80",
        imageCaption: "সকালে মেট্রোরেলে অফিসগামী যাত্রীদের সুশৃঙ্খল ভিড়",
        shortDescription: "উত্তরা থেকে মতিঝিল ছাড়িয়ে কমলাপুর পর্যন্ত নতুন স্টেশনগুলোর কার্যক্রম চালু হওয়ায় ঢাকাবাসীর যাতায়াত ব্যবস্থায় বিপুল স্বস্তি ফিরে এসেছে।",
        fullContent: "ঢাকার গণপরিবহনে নতুন ইতিহাস সৃষ্টি করে মেট্রোরেলের বর্ধিত রুটের ১০টি নতুন স্টেশন আজ থেকে সর্বসাধারণের জন্য খুলে দেওয়া হলো। ভোর ৬টা থেকে রাত ১১টা পর্যন্ত ৫ মিনিট পরপর ট্রেন চলাচল করায় লাখ লাখ কর্মজীবী মানুষ নির্বিঘ্নে তাদের কর্মস্থলে পৌঁছাতে পারছেন।",
        isFeatured: false,
        viewsCount: 8920
      },
      {
        id: "news_politics_1",
        title: "সুষ্ঠু ও নিরপেক্ষ নির্বাচন আয়োজনে নির্বাচন কমিশনের কঠোর নির্বাচনী রোডম্যাপ ঘোষণা",
        category: "রাজনীতি",
        author: "মো. শফিকুর রহমান",
        publishDate: "০৫ সেপ্টেম্বর ২০২৬, দুপুর ০১:৪৫",
        imageUrl: "https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?w=800&auto=format&fit=crop&q=80",
        imageCaption: "প্রধান নির্বাচন কমিশনারের ব্রিফিং কক্ষ",
        shortDescription: "ভোটাধিকার সুরক্ষিত করতে পাঁচ দফা বিশেষ নিরাপত্তা নির্দেশনা জারি করেছে কমিশন। প্রতিটি ভোটকেন্দ্রে সিসিটিভি ক্যামেরার লাইভ মনিটরিং থাকবে।",
        fullContent: "জাতীয় নির্বাচনের তফসিল ঘোষণার পূর্বে রাজনৈতিক দলগুলোর সঙ্গে ধারাবাহিক সংলাপের পর নির্বাচন কমিশন চূড়ান্ত রোডম্যাপ প্রকাশ করেছে।",
        isFeatured: false,
        viewsCount: 6730
      },
      {
        id: "news_cricket",
        title: "অসাধারণ অলরাউন্ড নৈপুণ্যে টি-টোয়েন্টি সিরিজ জয় বাংলাদেশের",
        category: "খেলাধুলা",
        author: "রাকিবুল হাসান",
        publishDate: "০৫ সেপ্টেম্বর ২০২৬, রাত ১০:২০",
        imageUrl: "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=800&auto=format&fit=crop&q=80",
        imageCaption: "মিরপুর শেরেবাংলা স্টেডিয়ামে ট্রফি হাতে টাইগারদের উল্লাস",
        shortDescription: "শেষ ওভারের নাটকীয়তায় ২ উইকেটে রুদ্ধশ্বাস জয় নিশ্চিত করে ট্রফি উঁচিয়ে ধরল টাইগাররা। ম্যাচ সেরা হয়েছেন তরুণ পেসার।",
        fullContent: "মিরপুরের হোম অব ক্রিকেটে চরম নাটকীয় এক ম্যাচে শক্তিশালী প্রতিপক্ষকে হারিয়ে ৩ ম্যাচের টি-টোয়েন্টি সিরিজ ২-১ ব্যবধানে জিতে নিল বাংলাদেশ। শেষ ওভারে দরকার ছিল ১১ রান, এক বল বাকি থাকতেই অবিস্মরণীয় জয় ছিনিয়ে নেয় টাইগাররা।",
        isFeatured: false,
        viewsCount: 15890
      },
      {
        id: "news_tech",
        title: "কৃত্রিম বুদ্ধিমত্তায় বাংলা ভাষা প্রক্রিয়াকরণে যুগান্তকারী সাফল্য তৈরি করল বাংলাদেশি গবেষকরা",
        category: "প্রযুক্তি",
        author: "অনিন্দ্য রায়",
        publishDate: "০৪ সেপ্টেম্বর ২০২৬, বিকাল ০৪:১৫",
        imageUrl: "https://images.unsplash.com/photo-1677442136019-21780efad99a?w=800&auto=format&fit=crop&q=80",
        imageCaption: "কৃত্রিম বুদ্ধিমত্তা ল্যাবে বাংলা ন্যাচারাল ল্যাঙ্গুয়েজ প্রসেসিং গবেষণা",
        shortDescription: "বাংলা ব্যাকরণ, আঞ্চলিক উপভাষা ও উচ্চারণের নিখুঁত অনুবাদে সক্ষম প্রথম ওপেন-সোর্স লার্জ ল্যাঙ্গুয়েজ মডেল উন্মোচন করেছে বুয়েট গবেষক দল।",
        fullContent: "বাংলা ভাষার সমৃদ্ধ সাহিত্য ও কোটি মানুষের দৈনন্দিন ভাব আদান-প্রদানকে ডিজিটাল জগতে আরও গতিশীল করতে তৈরি হলো সম্পূর্ণ দেশীয় প্রযুক্তির এআই মডেল।",
        isFeatured: false,
        viewsCount: 9810
      },
      {
        id: "news_international",
        title: "জলবায়ু সম্মেলনে ঐতিহাসিক চুক্তি: উন্নয়নশীল দেশগুলোর জন্য ১০০ বিলিয়ন ডলারের সবুজ তহবিল",
        category: "আন্তর্জাতিক",
        author: "আন্তর্জাতিক ডেস্ক",
        publishDate: "০৫ সেপ্টেম্বর ২০২৬, ভোর ০৬:৩০",
        imageUrl: "https://images.unsplash.com/photo-1618042164219-62c820f10723?w=800&auto=format&fit=crop&q=80",
        imageCaption: "জাতিসংঘের পরিবেশ সম্মেলন কেন্দ্রে বিভিন্ন দেশের প্রতিনিধিদের স্বাক্ষর",
        shortDescription: "গ্লোবাল ওয়ার্মিং ও বন্যা মোকাবিলায় ক্ষতিগ্রস্ত উপকূলীয় দেশগুলোকে আর্থিক ক্ষতিপূরণ প্রদানে একমত হয়েছে বিশ্বনেতারা।",
        fullContent: "বৈশ্বিক জলবায়ু সংকটের বিরুদ্ধে লড়াইয়ে এক যুগান্তকারী পদক্ষেপে বিশ্বনেতারা ১০০ বিলিয়ন ডলারের আন্তর্জাতিক ক্ষতিপূরণ তহবিল গঠনে সর্বসম্মত চুক্তি স্বাক্ষর করেছেন।",
        isFeatured: false,
        viewsCount: 5410
      },
      {
        id: "news_lifestyle",
        title: "দৈনন্দিন জীবনে মানসিক চাপ কমাতে বিশেষজ্ঞদের ৫টি কার্যকর পরামর্শ",
        category: "লাইফস্টাইল",
        author: "ডা. সুমাইয়া ইসলাম",
        publishDate: "০৪ সেপ্টেম্বর ২০২৬, দুপুর ০১:০০",
        imageUrl: "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800&auto=format&fit=crop&q=80",
        imageCaption: "সকালের প্রাকৃতিক পরিবেশে ধ্যান ও স্বাস্থ্যচর্চা",
        shortDescription: "কর্মব্যস্ত জীবনে ক্লান্তি ও উদ্বেগ দূর করতে নিয়মিত সকালের হাঁটা, ডিজিটাল ডিটক্স ও সঠিক খাদ্যাভ্যাস গড়ে তোলার তাগিদ দিয়েছেন পুষ্টিবিদরা।",
        fullContent: "আধুনিক শহুরে জীবনে মানসিক চাপ এক নীরব ঘাতকে রূপ নিয়েছে। চিকিৎসকদের মতে, সামান্য কিছু জীবনযাত্রার পরিবর্তন এনে মন ও শরীর উভয়কেই সতেজ রাখা সম্ভব।",
        isFeatured: false,
        viewsCount: 7420
      },
      {
        id: "news_islam",
        title: "পবিত্র মাহে রমজানের প্রস্তুতি: আত্মশুদ্ধি ও সহমর্মিতার গুরুত্ব",
        category: "ইসলাম",
        author: "মাওলানা আব্দুল্লাহ আল ক্বাফী",
        publishDate: "০৩ সেপ্টেম্বর ২০২৬, সকাল ১০:০০",
        imageUrl: "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=80",
        imageCaption: "বায়তুল মোকাররম জাতীয় মসজিদে মুসল্লিদের ইবাদত",
        shortDescription: "ইসলামে রমজান মাসের মূল শিক্ষা হলো আত্মসংযম, গরিব-অসহায় মানুষের পাশে দাঁড়ানো এবং আত্মিক পবিত্রতা অর্জন করা।",
        fullContent: "পবিত্র কোরআনে আল্লাহ তাআলা রোজাকে মানবজাতির জন্য তাকওয়া অর্জনের প্রধান মাধ্যম হিসেবে নির্ধারণ করেছেন।",
        isFeatured: false,
        viewsCount: 11200
      },
      {
        id: "news_entertainment",
        title: "আন্তর্জাতিক চলচ্চিত্র উৎসবে দেশের সিনেমার লাল গালিচায় জয়জয়কার",
        category: "বিনোদন",
        author: "বিনোদন প্রতিবেদক",
        publishDate: "০৩ সেপ্টেম্বর ২০২৬, রাত ০৯:০০",
        imageUrl: "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
        imageCaption: "কান চলচ্চিত্র উৎসবে তরুণ বাংলাদেশি নির্মাতার পুরস্কার গ্রহণ",
        shortDescription: "মৌলিক গল্প ও নান্দনিক চিত্রনাট্যের গুণে বিশ্বমঞ্চে সেরা নির্মাতার পুরস্কার জিতে নিলেন বাংলাদেশের উদীয়মান চলচ্চিত্রকার।",
        fullContent: "বিশ্ব চলচ্চিত্রের অন্যতম মর্যাদাপূর্ণ উৎসবে বাংলাদেশের চলচ্চিত্র ইতিহাসের নতুন এক পালক যুক্ত হলো। লাল গালিচায় বাংলাদেশের ঐতিহ্যবাহী পোশাকে উপস্থিত হয়ে বিশ্ব চলচ্চিত্রের বোদ্ধাদের প্রশংসা কুড়িয়েছেন তরুণ টিম।",
        isFeatured: false,
        viewsCount: 14300
      }
    ]
  };
  initApp();
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

// -------------------------------------------------------------
// HOMEPAGE LOGIC (Dynamic news rendering, category tabs, search)
// -------------------------------------------------------------
function initHomePage() {
  if (!newsData) return;

  // 1. Sync Footer Details from siteSettings
  syncFooterInfo();

  // 2. Sync Breaking News Ticker
  renderBreakingTicker();

  // 3. Render Popular news sidebar
  renderPopularSidebar();

  // 4. Check URL hash for category filtering (e.g. #category-খেলাধুলা or #sports)
  const hash = decodeURIComponent(window.location.hash || '');
  if (hash.startsWith('#category-')) {
    const catName = hash.replace('#category-', '');
    setCategory(catName);
  } else if (hash.startsWith('#') && hash.length > 1 && !['video', 'top'].includes(hash.substring(1))) {
    const slugMap = {
      national: 'জাতীয়',
      politics: 'রাজনীতি',
      international: 'আন্তর্জাতিক',
      sports: 'খেলাধুলা',
      entertainment: 'বিনোদন',
      tech: 'প্রযুক্তি',
      lifestyle: 'লাইফস্টাইল',
      islam: 'ইসলাম'
    };
    const mapped = slugMap[hash.substring(1)];
    if (mapped) {
      setCategory(mapped);
    } else {
      renderNewsFeed();
    }
  } else {
    renderNewsFeed();
  }

  // 5. Attach category click handlers to nav links if present
  setupCategoryTabs();
}

function syncFooterInfo() {
  if (!newsData || !newsData.siteSettings) return;
  const s = newsData.siteSettings;
  const fEmail = document.getElementById('footerEmail');
  const fPhone = document.getElementById('footerPhone');
  const fAddress = document.getElementById('footerAddress');
  const fHotline = document.getElementById('footerHotline');
  const fName = document.getElementById('footerSiteName');

  if (fEmail) fEmail.innerText = s.contactEmail || 'golapishoponline.bd@gmail.com';
  if (fPhone) fPhone.innerText = s.phone || '01612-057371';
  if (fAddress) fAddress.innerText = s.address || 'চৌরাস্তা ,বেগমগণ্জ, নোয়াখালী , বাংলাদেশ';
  if (fHotline) fHotline.innerText = s.hotline || s.phone || '01612-057371';
  if (fName) fName.innerText = s.siteName || 'গোলাপি নিউজ';
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

function renderResponsiveImg(url, alt, sizes, extraClass = '', id = '') {
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
      } else if (href && href.startsWith('#') && href !== '#video') {
        e.preventDefault();
        const catSlug = href.substring(1);
        const catName = this.innerText.trim();
        setCategory(catName);
      }
    });
  });
}

function setCategory(catName) {
  currentCategory = catName;
  searchQuery = ''; // Clear search when switching category
  const searchInput = document.getElementById('searchInput');
  if (searchInput) searchInput.value = '';

  // Update active tab styling
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

  // Scroll to news content gently on mobile
  if (window.innerWidth < 768) {
    const target = document.getElementById('mainNewsArea');
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}

// Global search handler called on input
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

  // 1. If searching, filter by title, shortDescription, fullContent, author, category
  if (searchQuery) {
    const q = searchQuery.toLowerCase();
    filtered = filtered.filter(item =>
      (item.title && item.title.toLowerCase().includes(q)) ||
      (item.shortDescription && item.shortDescription.toLowerCase().includes(q)) ||
      (item.fullContent && item.fullContent.toLowerCase().includes(q)) ||
      (item.category && item.category.toLowerCase().includes(q)) ||
      (item.author && item.author.toLowerCase().includes(q))
    );

    // Hide lead card during search
    if (leadContainer) leadContainer.style.display = 'none';

    // Show search status banner
    if (searchStatus) {
      searchStatus.style.display = 'block';
      searchStatus.innerHTML = `
        <div class="search-status-bar">
          <span>🔍 ‘<strong>${escapeHtml(searchQuery)}</strong>’ অনুসন্ধানে <strong>${filtered.length}</strong>টি সংবাদ পাওয়া গেছে</span>
          <button class="search-clear-btn" onclick="clearSearch()" title="অনুসন্ধান বাতিল">✕ বাতিল</button>
        </div>
      `;
    }

    if (sectionHeading) {
      sectionHeading.innerHTML = `অনুসন্ধানের ফলাফল`;
    }

  } else if (currentCategory && currentCategory !== 'all' && currentCategory !== 'সর্বশেষ') {
    // 2. Filter by Category
    filtered = filtered.filter(item => 
      item.category && item.category.trim().toLowerCase() === currentCategory.trim().toLowerCase()
    );

    // Hide separate lead card; show category headline & items
    if (leadContainer) leadContainer.style.display = 'none';

    if (searchStatus) {
      searchStatus.style.display = 'block';
      searchStatus.innerHTML = `
        <div class="category-filter-bar">
          <span class="active-cat-label">📂 ক্যাটাগরি: <strong>${escapeHtml(currentCategory)}</strong> (${filtered.length}টি সংবাদ)</span>
          <button class="filter-reset-btn" onclick="clearCategoryFilter()">← সব সংবাদ দেখুন</button>
        </div>
      `;
    }

    if (sectionHeading) {
      sectionHeading.innerHTML = `${escapeHtml(currentCategory)} সংবাদ`;
    }

  } else {
    // 3. Default Homepage View (All news)
    if (searchStatus) {
      searchStatus.style.display = 'none';
      searchStatus.innerHTML = '';
    }

    if (sectionHeading) {
      sectionHeading.innerHTML = `শীর্ষ সংবাদ`;
    }

    // Render Lead News Card
    const leadItem = newsData.news.find(n => n.isFeatured) || newsData.news[0];
    if (leadContainer && leadItem) {
      leadContainer.style.display = 'block';
      leadContainer.innerHTML = `
        <section class="lead-news-card" onclick="openArticle('${leadItem.id}')" role="button" tabindex="0">
          <div class="lead-image-wrap">
            ${renderResponsiveImg(leadItem.imageUrl, leadItem.title, '(max-width: 640px) 100vw, (max-width: 1024px) 800px, 1000px')}
            <span class="category-pill">${escapeHtml(leadItem.category)} • শীর্ষ সংবাদ</span>
          </div>
          <div class="lead-body">
            <h1 class="lead-title">${escapeHtml(leadItem.title)}</h1>
            <p class="lead-excerpt">${escapeHtml(leadItem.shortDescription)}</p>
            <div class="meta-row">
              <span class="author">✍️ ${escapeHtml(leadItem.author || 'নিজস্ব প্রতিবেদক')}</span>
              <span class="time">🕒 ${escapeHtml(leadItem.publishDate)}</span>
            </div>
          </div>
        </section>
      `;
    }

    // Leave the lead item out of the bottom grid if in default all-news view
    if (leadItem) {
      filtered = filtered.filter(n => n.id !== leadItem.id);
    }
  }

  // Render Grid
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
    <article class="story-card" onclick="openArticle('${item.id}')" role="button" tabindex="0">
      <div class="card-img-wrap">
        ${renderResponsiveImg(item.imageUrl, item.title, '(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 380px')}
        <span class="card-badge">${escapeHtml(item.category)}</span>
      </div>
      <div class="card-body">
        <h3 class="card-title">${escapeHtml(item.title)}</h3>
        <p class="card-snippet">${escapeHtml(item.shortDescription || '')}</p>
        <div class="card-meta">
          <span class="card-time">🕒 ${escapeHtml(item.publishDate)}</span>
          ${item.viewsCount ? `<span class="card-views">👁️ ${item.viewsCount} বার পঠিত</span>` : ''}
        </div>
      </div>
    </article>
  `).join('');
}

// -------------------------------------------------------------
// ARTICLE VIEW LOGIC
// -------------------------------------------------------------
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

  // Update Dynamic SEO Tags
  setMetaTag('metaTitle', article.title);
  setMetaTag('metaDesc', article.shortDescription);
  setMetaTag('ogTitle', article.title);
  setMetaTag('ogDesc', article.shortDescription);
  setMetaTag('ogImage', article.imageUrl);
  setMetaTag('ogUrl', window.location.href);

  renderRelated(article.category, article.id);
  renderPopularSidebar();
  syncFooterInfo();
}

function setMetaTag(id, value) {
  const el = document.getElementById(id);
  if (el) el.setAttribute('content', value);
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
  if (artImage) {
    const figure = artImage.closest('figure');
    if (figure) figure.style.display = 'none';
    else artImage.style.display = 'none';
  }
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
    <div class="story-card" onclick="openArticle('${item.id}')" role="button" tabindex="0">
      <div class="card-img-wrap">
        ${renderResponsiveImg(item.imageUrl, item.title, '(max-width: 640px) 100vw, 360px')}
        <span class="card-badge">${escapeHtml(item.category)}</span>
      </div>
      <div class="card-body">
        <h4 class="card-title">${escapeHtml(item.title)}</h4>
        <span class="card-time">🕒 ${escapeHtml(item.publishDate)}</span>
      </div>
    </div>
  `).join('');
}

function renderPopularSidebar() {
  const container = document.getElementById('popularSidebar');
  if (!container || !newsData || !newsData.news) return;
  const list = [...newsData.news].sort((a, b) => (b.viewsCount || 0) - (a.viewsCount || 0)).slice(0, 5);
  container.innerHTML = list.map((item, idx) => `
    <li onclick="openArticle('${item.id}')" role="button" tabindex="0">
      <span class="rank-num">${getBengaliNumber(idx + 1)}</span>
      <div class="pop-details">
        <h4>${escapeHtml(item.title)}</h4>
        <span class="pop-time">${escapeHtml(item.publishDate)}</span>
      </div>
    </li>
  `).join('');
}

// Social Share
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
  if (currentFontSize < 15) currentFontSize = 15;
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

function getBengaliNumber(num) {
  const bn = ['০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯'];
  return num.toString().split('').map(d => bn[parseInt(d)] || d).join('');
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
