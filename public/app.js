// গোলাপি নিউজ (Golapi News) - Client Application JS
let newsData = null;
let currentFontSize = 18;

async function loadData() {
  try {
    const res = await fetch('news-data.json');
    newsData = await res.json();
    initApp();
  } catch (err) {
    console.warn('Using embedded fallback data:', err);
    initFallbackData();
  }
}

function initFallbackData() {
  newsData = {
    siteSettings: {
      siteName: "গোলাপি নিউজ",
      tagline: "সত্যের নির্ভীক সারথি — আধুনিক ডিজিটাল বাংলা সংবাদপত্র",
      aboutUsText: "গোলাপি নিউজ বাংলাদেশের একটি অগ্রণী ডিজিটাল বাংলা নিউজ পোর্টাল।",
      privacyPolicyText: "পাঠকদের তথ্য সুরক্ষায় আমরা প্রতিশ্রুতিবদ্ধ।",
      termsText: "সকল কপিরাইট সংরক্ষিত।"
    },
    news: [
      {
        id: "news_lead",
        title: "বাংলাদেশের ডিজিটাল অর্থনীতিতে নতুন বিপ্লব: রপ্তানি ছাড়াল রেকর্ড ১০ বিলিয়ন ডলার",
        category: "জাতীয়",
        author: "তাহমিদ হাসান",
        publishDate: "০৫ সেপ্টেম্বর ২০২৬, সকাল ১০:১৫",
        imageUrl: "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=800&auto=format&fit=crop&q=80",
        imageCaption: "রাজধানীতে সফটওয়্যার পার্ক ও হাইটেক প্রকল্পের নতুন কেন্দ্রবিন্দু",
        shortDescription: "তথ্যপ্রযুক্তি খাতের অভূতপূর্ব বিকাশে বৈশ্বিক বাজারে বাংলাদেশের সফটওয়্যার ও আইটি সেবা খাতের রপ্তানি প্রথমবারের মতো ১০ বিলিয়ন ডলারের মাইলফলক স্পর্শ করেছে।",
        fullContent: "তথ্যপ্রযুক্তি খাতে ঐতিহাসিক এক সাফল্যের সাক্ষী হলো বাংলাদেশ। চলতি অর্থবছরে দেশের আইটি ও ফ্রিল্যান্সিং খাতের মোট বৈশ্বিক আয় ১০ বিলিয়ন ডলার অতিক্রম করেছে।\n\nসংশ্লিষ্ট বিশেষজ্ঞরা জানিয়েছেন, তরুণ উদ্যোক্তাদের জন্য করমুক্ত সুযোগ, আন্তর্জাতিক পেমেন্ট গেটওয়ের সহজলভ্যতা এবং দেশজুড়ে দ্রুতগতির ব্রডব্যান্ড নেটওয়ার্ক বিস্তারের কারণেই এ অভূতপূর্ব প্রবৃদ্ধি সম্ভব হয়েছে।\n\nপরিকল্পনা কমিশনের এক বিশেষ প্রতিবেদনে উল্লেখ করা হয়, ফ্রিল্যান্সিং ও সফটওয়্যার ডেভেলপমেন্টে এখন প্রত্যক্ষ ও পরোক্ষভাবে যুক্ত রয়েছেন প্রায় ১৫ লাখ তরুণ-তরুণী।"
      },
      {
        id: "news_metro",
        title: "মেট্রোরেলে নতুন ১০টি স্টেশন পুরোদমে চালু: যানজটমুক্ত রাজধানীর নতুন রূপ",
        category: "জাতীয়",
        author: "ফারহানা করিম",
        publishDate: "০৫ সেপ্টেম্বর ২০২৬, সকাল ০৯:০০",
        imageUrl: "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800&auto=format&fit=crop&q=80",
        shortDescription: "উত্তরা থেকে মতিঝিল ছাড়িয়ে কমলাপুর পর্যন্ত নতুন স্টেশনগুলোর কার্যক্রম চালু হওয়ায় ঢাকাবাসীর স্বস্তি...",
        fullContent: "ঢাকার গণপরিবহনে নতুন ইতিহাস সৃষ্টি করে মেট্রোরেলের বর্ধিত রুটের ১০টি নতুন স্টেশন আজ থেকে সর্বসাধারণের জন্য খুলে দেওয়া হলো। ভোর ৬টা থেকে রাত ১১টা পর্যন্ত ৫ মিনিট পরপর ট্রেন চলাচল করায় লাখ লাখ কর্মজীবী মানুষ নির্বিঘ্নে তাদের কর্মস্থলে পৌঁছাতে পারছেন।"
      },
      {
        id: "news_cricket",
        title: "অসাধারণ অলরাউন্ড নৈপুণ্যে টি-টোয়েন্টি সিরিজ জয় বাংলাদেশের",
        category: "খেলাধুলা",
        author: "রাকিবুল হাসান",
        publishDate: "০৫ সেপ্টেম্বর ২০২৬, রাত ১০:২০",
        imageUrl: "https://images.unsplash.com/photo-1531415074868-036b107e775a?w=800&auto=format&fit=crop&q=80",
        shortDescription: "শেষ ওভারের নাটকীয়তায় ২ উইকেটে রুদ্ধশ্বাস জয় নিশ্চিত করে ট্রফি উঁচিয়ে ধরল টাইগাররা...",
        fullContent: "মিরপুরের হোম অব ক্রিকেটে চরম নাটকীয় এক ম্যাচে শক্তিশালী প্রতিপক্ষকে হারিয়ে ৩ ম্যাচের টি-টোয়েন্টি সিরিজ ২-১ ব্যবধানে জিতে নিল বাংলাদেশ। প্রথমে ব্যাট করতে নেমে প্রতিপক্ষ সংগ্রহ করেছিল ১৬৮ রান। জবাবে ব্যাট করতে নেমে শেষ ওভারে জয়ের জন্য প্রয়োজন ছিল ১১ রান।"
      }
    ]
  };
  initApp();
}

function initApp() {
  if (window.location.pathname.includes('article.html') || window.location.search.includes('id=')) {
    loadArticleView();
  }
}

function openArticle(id) {
  window.location.href = 'article.html?id=' + encodeURIComponent(id);
}

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

  if (pageTitle) pageTitle.innerText = article.title + ' | ' + newsData.siteSettings.siteName;
  if (artTitle) artTitle.innerText = article.title;
  if (artCategory) artCategory.innerText = article.category;
  if (artAuthor) artAuthor.innerText = article.author || 'নিজস্ব প্রতিবেদক';
  if (artDate) artDate.innerText = 'প্রকাশিত: ' + article.publishDate;
  if (artImage) artImage.src = article.imageUrl;
  if (artCaption) artCaption.innerText = article.imageCaption || article.title;
  if (artBody) artBody.innerText = article.fullContent;

  // Update Open Graph tags for Facebook crawler
  const metaTitle = document.getElementById('metaTitle');
  const metaDesc = document.getElementById('metaDesc');
  const ogTitle = document.getElementById('ogTitle');
  const ogDesc = document.getElementById('ogDesc');
  const ogImage = document.getElementById('ogImage');
  const ogUrl = document.getElementById('ogUrl');

  if (metaTitle) metaTitle.setAttribute('content', article.title);
  if (metaDesc) metaDesc.setAttribute('content', article.shortDescription);
  if (ogTitle) ogTitle.setAttribute('content', article.title);
  if (ogDesc) ogDesc.setAttribute('content', article.shortDescription);
  if (ogImage) ogImage.setAttribute('content', article.imageUrl);
  if (ogUrl) ogUrl.setAttribute('content', window.location.href);

  renderRelated(article.category, article.id);
  renderPopularSidebar();
}

function renderStaticPage(type) {
  const titles = {
    about: 'আমাদের সম্পর্কে',
    privacy: 'গোপনীয়তা নীতি',
    terms: 'ব্যবহারের শর্তাবলী'
  };
  const texts = {
    about: newsData.siteSettings.aboutUsText,
    privacy: newsData.siteSettings.privacyPolicyText,
    terms: newsData.siteSettings.termsText
  };

  const pageTitle = document.getElementById('pageTitle');
  const artCategory = document.getElementById('artCategory');
  const artTitle = document.getElementById('artTitle');
  const artDate = document.getElementById('artDate');
  const artImage = document.getElementById('artImage');
  const artBody = document.getElementById('artBody');

  if (pageTitle) pageTitle.innerText = (titles[type] || 'তথ্য') + ' | ' + newsData.siteSettings.siteName;
  if (artCategory) artCategory.innerText = 'গোলাপি নিউজ';
  if (artTitle) artTitle.innerText = titles[type] || 'তথ্য';
  if (artDate) artDate.innerText = 'আপডেট: ২০২৬';
  if (artImage) artImage.style.display = 'none';
  if (artBody) artBody.innerText = texts[type] || 'তথ্য প্রস্তুত করা হচ্ছে।';
  const shareBox = document.querySelector('.share-box');
  if (shareBox) shareBox.style.display = 'none';
}

function renderRelated(category, excludeId) {
  const container = document.getElementById('relatedList');
  if (!container || !newsData) return;
  const list = newsData.news.filter(n => n.id !== excludeId).slice(0, 2);
  container.innerHTML = list.map(item => `
    <div class="story-card" onclick="openArticle('${item.id}')">
      <div class="card-img-wrap">
        <img src="${item.imageUrl}" alt="${item.title}" />
      </div>
      <div class="card-body">
        <h4 class="card-title">${item.title}</h4>
      </div>
    </div>
  `).join('');
}

function renderPopularSidebar() {
  const container = document.getElementById('popularSidebar');
  if (!container || !newsData) return;
  const list = [...newsData.news].slice(0, 5);
  container.innerHTML = list.map((item, idx) => `
    <li onclick="openArticle('${item.id}')">
      <span class="rank-num">${idx + 1}</span>
      <div class="pop-details">
        <h4>${item.title}</h4>
        <span class="pop-time">${item.publishDate}</span>
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
    alert('সংবাদের লিংক সফলভাবে কপি করা হয়েছে!');
  });
}

function changeFontSize(delta) {
  currentFontSize += delta;
  if (currentFontSize < 14) currentFontSize = 14;
  if (currentFontSize > 28) currentFontSize = 28;
  const body = document.getElementById('artBody');
  if (body) body.style.fontSize = currentFontSize + 'px';
}

function handleSearch(query) {
  console.log('Search:', query);
}

document.addEventListener('DOMContentLoaded', loadData);
