import { db, auth, storage } from './firebase-config.js';
import {
  collection, getDocs, getDoc, doc, setDoc, deleteDoc, query, orderBy, serverTimestamp
} from "https://www.gstatic.com/firebasejs/10.13.1/firebase-firestore.js";
import {
  signInWithEmailAndPassword, onAuthStateChanged, signOut
} from "https://www.gstatic.com/firebasejs/10.13.1/firebase-auth.js";
import {
  ref, uploadBytes, getDownloadURL
} from "https://www.gstatic.com/firebasejs/10.13.1/firebase-storage.js";

const $ = (id) => document.getElementById(id);

// বাংলাদেশ সময় (Asia/Dhaka) অনুযায়ী তারিখ + বার + সময় বাংলায় ফরম্যাট করা হয়
// এটা ব্যবহারকারীর ফোনের টাইমজোন যাই হোক না কেন, সবসময় বাংলাদেশ সময় দেখাবে
function formatDhakaDate(date) {
  const dayNames = ['রবিবার', 'সোমবার', 'মঙ্গলবার', 'বুধবার', 'বৃহস্পতিবার', 'শুক্রবার', 'শনিবার'];
  const monthNames = ['জানুয়ারি', 'ফেব্রুয়ারি', 'মার্চ', 'এপ্রিল', 'মে', 'জুন', 'জুলাই', 'আগস্ট', 'সেপ্টেম্বর', 'অক্টোবর', 'নভেম্বর', 'ডিসেম্বর'];
  const enDigits = '0123456789', bnDigits = '০১২৩৪৫৬৭৮৯';
  const toBn = (n) => String(n).split('').map(d => bnDigits[enDigits.indexOf(d)] ?? d).join('');

  const dhakaParts = new Intl.DateTimeFormat('en-US', {
    timeZone: 'Asia/Dhaka',
    weekday: 'long', year: 'numeric', month: 'numeric', day: 'numeric',
    hour: 'numeric', minute: 'numeric', hour12: true
  }).formatToParts(date);

  const get = (type) => dhakaParts.find(p => p.type === type)?.value;
  const dayIndex = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'].indexOf(get('weekday'));
  const day = toBn(get('day'));
  const month = monthNames[parseInt(get('month'), 10) - 1];
  const year = toBn(get('year'));
  const hour = toBn(get('hour'));
  const minute = toBn(get('minute').padStart(2, '0'));
  const ampm = get('dayPeriod') === 'AM' ? 'সকাল' : (parseInt(get('hour'), 10) < 5 ? 'রাত' : 'বিকাল/সন্ধ্যা');

  return `${dayNames[dayIndex]}, ${day} ${month} ${year}, ${ampm} ${hour}:${minute}`;
}

// ---------- Toast ----------
function toast(msg) {
  const t = $('toast');
  t.innerText = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 2200);
}

// ---------- Auth ----------
$('loginBtn').addEventListener('click', async () => {
  const email = $('loginEmail').value.trim();
  const password = $('loginPassword').value;
  $('loginError').innerText = '';
  try {
    await signInWithEmailAndPassword(auth, email, password);
  } catch (err) {
    $('loginError').innerText = 'লগইন ব্যর্থ: সঠিক ইমেইল ও পাসওয়ার্ড দিন';
  }
});

$('logoutBtn').addEventListener('click', () => signOut(auth));

onAuthStateChanged(auth, (user) => {
  if (user) {
    $('loginScreen').classList.add('hidden');
    $('dashboard').classList.remove('hidden');
    $('addNewsFab').classList.remove('hidden');
    loadNewsList();
    loadBreakingList();
    loadSettings();
  } else {
    $('loginScreen').classList.remove('hidden');
    $('dashboard').classList.add('hidden');
    $('addNewsFab').classList.add('hidden');
  }
});

// ---------- Tabs ----------
document.querySelectorAll('.tab').forEach(tabEl => {
  tabEl.addEventListener('click', () => {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    tabEl.classList.add('active');
    ['news', 'breaking', 'settings'].forEach(name => {
      $('tab-' + name).classList.toggle('hidden', name !== tabEl.dataset.tab);
    });
  });
});

// ---------- Toggle switches ----------
document.querySelectorAll('.toggle').forEach(t => {
  t.addEventListener('click', () => t.classList.toggle('on'));
});

// ---------- Slug helper ----------
function slugify(title) {
  return title.trim().replace(/\s+/g, '-').slice(0, 40) + '-' + Date.now().toString(36);
}

// ================= NEWS =================
let newsCache = [];

const SITE_URL = 'https://golapinews.netlify.app';

async function loadNewsList() {
  const listEl = $('newsList');
  listEl.innerHTML = '<p class="empty-note">লোড হচ্ছে...</p>';
  const snap = await getDocs(query(collection(db, 'news'), orderBy('createdAt', 'desc')));
  newsCache = snap.docs.map(d => ({ id: d.id, ...d.data() }));

  $('seedBox').classList.toggle('hidden', newsCache.length !== 0);

  const liveCount = newsCache.filter(n => n.isPublished !== false).length;
  const draftCount = newsCache.length - liveCount;
  $('statTotal').innerText = newsCache.length;
  $('statLive').innerText = liveCount;
  $('statDraft').innerText = draftCount;

  if (newsCache.length === 0) {
    listEl.innerHTML = '<p class="empty-note">এখনো কোনো নিউজ নেই। নিচের + বাটনে চাপ দিয়ে প্রথম নিউজ পোস্ট করুন।</p>';
    return;
  }

  listEl.innerHTML = newsCache.map(n => {
    const isLive = n.isPublished !== false;
    return `
    <div class="news-card">
      <img src="${escapeHtml(n.imageUrl || '')}" alt="" loading="lazy" onerror="this.style.visibility='hidden'" />
      <div class="news-card-body">
        <div class="news-card-title">${escapeHtml(n.title)}</div>
        <div class="news-card-meta">
          <span class="badge ${isLive ? 'badge-live' : 'badge-draft'}">${isLive ? 'লাইভ' : 'খসড়া'}</span>
          ${n.isFeatured ? '<span class="badge badge-featured">শীর্ষ সংবাদ</span>' : ''}
          <span class="badge badge-cat">${escapeHtml(n.category || '')}</span>
        </div>
        <div class="row-actions">
          <button class="btn-secondary btn-small" onclick="window.__editNews('${n.id}')">এডিট</button>
          ${isLive ? `<button class="btn-share btn-small" onclick="window.__shareNews('${n.id}')">📤 শেয়ার</button>` : ''}
          <button class="btn-danger btn-small" onclick="window.__deleteNews('${n.id}')">ডিলিট</button>
        </div>
      </div>
    </div>
  `;
  }).join('');
}

window.__shareNews = (id) => {
  const item = newsCache.find(n => n.id === id);
  if (!item) return;
  const url = `${SITE_URL}/article.html?id=${encodeURIComponent(id)}`;
  const text = item.title + '\n' + (item.shortDescription || '');
  if (navigator.share) {
    navigator.share({ title: item.title, text, url }).catch(() => {});
  } else {
    const fbUrl = 'https://www.facebook.com/sharer/sharer.php?u=' + encodeURIComponent(url);
    window.open(fbUrl, '_blank');
  }
};

function escapeHtml(str) {
  if (!str) return '';
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ---- Editor modal open/close ----
function setUploadPreview(url) {
  const prompt = $('uploadPrompt');
  const preview = $('uploadPreview');
  if (url) {
    preview.src = url;
    preview.classList.remove('hidden');
    prompt.classList.add('hidden');
  } else {
    preview.classList.add('hidden');
    preview.src = '';
    prompt.classList.remove('hidden');
  }
}

function openEditor(item) {
  $('editorError').innerText = '';
  if (item) {
    $('editorTitle').innerText = 'নিউজ সম্পাদনা';
    $('editId').value = item.id;
    $('fTitle').value = item.title || '';
    $('fCategory').value = item.category || 'জাতীয়';
    $('fAuthor').value = item.author || 'নিজস্ব প্রতিবেদক';
    $('fImageUrl').value = item.imageUrl || '';
    setUploadPreview(item.imageUrl || '');
    $('fImageCaption').value = item.imageCaption || '';
    $('fShortDesc').value = item.shortDescription || '';
    $('fFullContent').value = item.fullContent || '';
    $('fVideoUrl').value = item.videoUrl || '';
    $('fFeatured').classList.toggle('on', !!item.isFeatured);
    $('fIsBreaking').classList.toggle('on', !!item.isBreaking);
    $('fPublished').classList.toggle('on', item.isPublished !== false);
  } else {
    $('editorTitle').innerText = 'নতুন নিউজ তৈরি করুন';
    $('editId').value = '';
    $('fTitle').value = '';
    $('fCategory').value = 'জাতীয়';
    $('fAuthor').value = 'নিজস্ব প্রতিবেদক';
    $('fImageUrl').value = '';
    setUploadPreview('');
    $('fImageCaption').value = '';
    $('fShortDesc').value = '';
    $('fFullContent').value = '';
    $('fVideoUrl').value = '';
    $('fFeatured').classList.remove('on');
    $('fIsBreaking').classList.remove('on');
    $('fPublished').classList.add('on');
  }
  $('editorModal').classList.remove('hidden');
}

$('addNewsFab').addEventListener('click', () => openEditor(null));
$('editorCancelBtn').addEventListener('click', () => $('editorModal').classList.add('hidden'));

// ---- Image Upload (Firebase Storage) ----
$('imageUploadArea').addEventListener('click', () => {
  if ($('uploadProgress').classList.contains('hidden')) {
    $('fImageFile').click();
  }
});

$('fImageFile').addEventListener('change', async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  $('uploadPrompt').classList.add('hidden');
  $('uploadPreview').classList.add('hidden');
  $('uploadProgress').classList.remove('hidden');
  $('uploadProgress').innerText = 'আপলোড হচ্ছে...';

  try {
    const safeName = Date.now() + '-' + file.name.replace(/[^a-zA-Z0-9.\-_]/g, '');
    const storageRef = ref(storage, 'news-images/' + safeName);
    await uploadBytes(storageRef, file);
    const url = await getDownloadURL(storageRef);
    $('fImageUrl').value = url;
    $('uploadProgress').classList.add('hidden');
    setUploadPreview(url);
    toast('ছবি আপলোড হয়েছে ✓');
  } catch (err) {
    console.error('Upload error:', err);
    $('uploadProgress').classList.add('hidden');
    setUploadPreview('');
    toast('ছবি আপলোড ব্যর্থ হয়েছে, আবার চেষ্টা করুন');
  }
});

window.__editNews = (id) => {
  const item = newsCache.find(n => n.id === id);
  if (item) openEditor(item);
};

window.__deleteNews = async (id) => {
  if (!confirm('এই নিউজটি স্থায়ীভাবে ডিলিট করতে চান?')) return;
  await deleteDoc(doc(db, 'news', id));
  toast('নিউজ ডিলিট হয়েছে');
  loadNewsList();
};

$('editorSaveBtn').addEventListener('click', async () => {
  const title = $('fTitle').value.trim();
  const imageUrl = $('fImageUrl').value.trim();
  const shortDescription = $('fShortDesc').value.trim();
  const fullContent = $('fFullContent').value.trim();

  if (!title || !imageUrl || !shortDescription || !fullContent) {
    $('editorError').innerText = 'শিরোনাম, ছবি, সারসংক্ষেপ ও সম্পূর্ণ প্রতিবেদন আবশ্যক';
    return;
  }

  const isNewPost = !$('editId').value;
  const id = $('editId').value || slugify(title);

  const data = {
    title,
    slug: id,
    category: $('fCategory').value,
    author: $('fAuthor').value.trim() || 'নিজস্ব প্রতিবেদক',
    imageUrl,
    imageCaption: $('fImageCaption').value.trim(),
    shortDescription,
    fullContent,
    videoUrl: $('fVideoUrl').value.trim() || null,
    isFeatured: $('fFeatured').classList.contains('on'),
    isBreaking: $('fIsBreaking').classList.contains('on'),
    isPublished: $('fPublished').classList.contains('on'),
    seoTitle: title,
    seoDescription: shortDescription,
    ogImageUrl: imageUrl,
    viewsCount: 0
  };

  // নতুন নিউজ হলেই শুধু প্রকাশের তারিখ/সময় বসবে (বাংলাদেশ সময় অনুযায়ী, বার-সহ)
  // এডিট করলে আগের প্রকাশের তারিখ অপরিবর্তিত থাকবে — যাতে পুরনো নিউজ তালিকার উপরে উঠে না যায়
  if (isNewPost) {
    data.publishDate = formatDhakaDate(new Date());
    data.createdAt = serverTimestamp();
  }

  try {
    await setDoc(doc(db, 'news', id), data, { merge: true });
    toast('নিউজ সংরক্ষণ হয়েছে ✓');
    $('editorModal').classList.add('hidden');
    loadNewsList();
  } catch (err) {
    $('editorError').innerText = 'সংরক্ষণে সমস্যা হয়েছে, আবার চেষ্টা করুন';
  }
});

// ================= BREAKING NEWS =================
async function loadBreakingList() {
  const listEl = $('brkList');
  const snap = await getDocs(collection(db, 'breakingNews'));
  const items = snap.docs.map(d => ({ id: d.id, ...d.data() }));
  if (items.length === 0) {
    listEl.innerHTML = '<p class="empty-note">কোনো ব্রেকিং নিউজ নেই</p>';
    return;
  }
  listEl.innerHTML = items.map(b => `
    <div class="news-item">
      <div class="news-item-title" style="flex:1;">${escapeHtml(b.headline)}</div>
      <div class="row-actions">
        <button class="btn-danger btn-small" onclick="window.__deleteBrk('${b.id}')">ডিলিট</button>
      </div>
    </div>
  `).join('');
}

window.__deleteBrk = async (id) => {
  await deleteDoc(doc(db, 'breakingNews', id));
  toast('ডিলিট হয়েছে');
  loadBreakingList();
};

$('brkAddBtn').addEventListener('click', async () => {
  const headline = $('brkHeadline').value.trim();
  if (!headline) return;
  const id = 'brk_' + Date.now();
  await setDoc(doc(db, 'breakingNews', id), {
    headline, isActive: true, timestamp: 'এখনই', createdAt: serverTimestamp()
  });
  $('brkHeadline').value = '';
  toast('ব্রেকিং নিউজ যোগ হয়েছে');
  loadBreakingList();
});

// ================= SITE SETTINGS =================
async function loadSettings() {
  const snap = await getDoc(doc(db, 'siteSettings', 'main'));
  const s = snap.exists() ? snap.data() : {};
  $('setSiteName').value = s.siteName || 'গোলাপি নিউজ';
  $('setTagline').value = s.tagline || '';
  $('setEmail').value = s.contactEmail || '';
  $('setPhone').value = s.phone || '';
  $('setAddress').value = s.address || '';
  $('setFacebook').value = s.facebookUrl || '';
  $('setYoutube').value = s.youtubeUrl || '';
  $('setAbout').value = s.aboutUsText || '';
}

$('settingsSaveBtn').addEventListener('click', async () => {
  await setDoc(doc(db, 'siteSettings', 'main'), {
    siteName: $('setSiteName').value.trim(),
    tagline: $('setTagline').value.trim(),
    contactEmail: $('setEmail').value.trim(),
    phone: $('setPhone').value.trim(),
    address: $('setAddress').value.trim(),
    facebookUrl: $('setFacebook').value.trim(),
    youtubeUrl: $('setYoutube').value.trim(),
    aboutUsText: $('setAbout').value.trim()
  }, { merge: true });
  toast('সেটিংস সংরক্ষণ হয়েছে ✓');
});

// ================= ONE-TIME SEED IMPORT =================
function stripUndefinedFields(obj) {
  const clean = {};
  Object.keys(obj).forEach(key => {
    if (obj[key] !== undefined) clean[key] = obj[key];
  });
  return clean;
}

$('seedBtn').addEventListener('click', async () => {
  if (!confirm('আগের news-data.json ফাইলের সব ডেটা Firestore এ কপি হবে। চালিয়ে যেতে চান?')) return;
  $('seedBtn').disabled = true;
  $('seedBtn').innerText = 'ইম্পোর্ট হচ্ছে...';
  try {
    const res = await fetch('news-data.json?v=' + Date.now());
    if (!res.ok) throw new Error('news-data.json fetch failed: HTTP ' + res.status);
    const data = await res.json();

    for (const n of (data.news || [])) {
      const id = n.id || slugify(n.title);
      const { id: _skip, ...rest } = n;
      await setDoc(doc(db, 'news', id), stripUndefinedFields({ ...rest, createdAt: serverTimestamp() }), { merge: true });
    }
    for (const c of (data.categories || [])) {
      const id = c.id || c.slug || slugify(c.name);
      const { id: _skip, ...rest } = c;
      await setDoc(doc(db, 'categories', id), stripUndefinedFields(rest), { merge: true });
    }
    for (const b of (data.breakingNews || [])) {
      const id = b.id || ('brk_' + Date.now() + Math.random().toString(36).slice(2, 6));
      const { id: _skip, ...rest } = b;
      await setDoc(doc(db, 'breakingNews', id), stripUndefinedFields({ ...rest, isActive: true, createdAt: serverTimestamp() }), { merge: true });
    }
    if (data.siteSettings) {
      await setDoc(doc(db, 'siteSettings', 'main'), stripUndefinedFields(data.siteSettings), { merge: true });
    }

    toast('ইম্পোর্ট সম্পন্ন হয়েছে ✓');
    loadNewsList();
    loadBreakingList();
    loadSettings();
  } catch (err) {
    console.error('Seed import error:', err);
    toast('ইম্পোর্ট ব্যর্থ: ' + (err.message || 'অজানা সমস্যা'));
  } finally {
    $('seedBtn').disabled = false;
    $('seedBtn').innerText = 'প্রাথমিক ডেটা ইম্পোর্ট করুন';
  }
});
