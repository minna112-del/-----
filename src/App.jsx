import React, { useEffect, useState } from "react";
import {
  collection,
  addDoc,
  deleteDoc,
  doc,
  updateDoc,
  onSnapshot,
  query,
  orderBy,
  where,
} from "firebase/firestore";
import { db } from "./firebase";

import hisabImage from './hisab.jpeg';
import mahsinImg from './MAHSIN.jpeg';
import rubelImg from './RUBEL.jpeg';
import mojammelImg from './MOJAMMEL.jpeg';
import sojibImg from './SOJIB.jpeg';
import arifImg from './ARIF.jpeg';
import splashVideo from './splash_video.mp4';

// ================= CONFIG =================
const ENTRY_PIN = "1919";
const EDIT_PIN = "8019";

// ================= MEMBERS =================
const MEMBERS = [
  { id: "m1", name: "মহসিন", img: mahsinImg },
  { id: "m2", name: "রুবেল", img: rubelImg },
  { id: "m3", name: "মোজাম্মেল", img: mojammelImg },
  { id: "m4", name: "সজিব", img: sojibImg },
  { id: "m5", name: "আরিফ", img: arifImg },
];
const memberNamesOnly = MEMBERS.map((m) => m.name);
const memberImgMap = {};
MEMBERS.forEach(m => memberImgMap[m.name] = m.img);

// ================= পরিষ্কারের সিডিউল =================
const CLEANING_SCHEDULE = [
  { day: 1, name: "রুবেল" },
  { day: 6, name: "সজিব" },
  { day: 12, name: "মোজাম্মেল" },
  { day: 17, name: "মহসিন" },
  { day: 24, name: "আরিফ" },
];

const getCleaningPersonForDay = (dayOfMonth) => {
  let current = CLEANING_SCHEDULE[CLEANING_SCHEDULE.length - 1];
  for (const s of CLEANING_SCHEDULE) {
    if (s.day <= dayOfMonth) current = s;
  }
  return current.name;
};

const getNextCleaning = () => {
  const now = new Date();
  const today = now.getDate();
  const y = now.getFullYear();
  const m = now.getMonth();
  let next = CLEANING_SCHEDULE.find(s => s.day >= today);
  if (next) return { ...next, date: new Date(y, m, next.day) };
  const first = CLEANING_SCHEDULE[0];
  return { ...first, date: new Date(y, m + 1, first.day) };
};

// ================= HELPERS =================
const pad2 = (n) => String(n).padStart(2, "0");
const currentMonthKey = () => `${new Date().getFullYear()}-${pad2(new Date().getMonth() + 1)}`;
const formatDateForInput = (ts) => {
  const d = new Date(ts);
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;
};

// ================= AVATAR COMPONENT =================
const MemberAvatar = ({ name }) => {
  const src = memberImgMap[name] || hisabImage;
  return (
    <img
      src={src}
      alt={name}
      className="w-16 h-16 rounded-full object-cover object-top border-4 border-blue-500 shadow-lg scale-110"
      onError={(e) => e.target.src = `https://via.placeholder.com/64?text=${name[0]}`}
    />
  );
};

// ================= নোটিফিকেশন হেল্পার =================
const notifiedKey = () => `notified_${new Date().toDateString()}`;

function useCleaningNotification() {
  const [permission, setPermission] = useState(
    typeof Notification !== "undefined" ? Notification.permission : "default"
  );

  const enableNotifications = () => {
    if (typeof Notification === "undefined") return;
    Notification.requestPermission().then(setPermission);
  };

  useEffect(() => {
    if (typeof Notification === "undefined") return;
    const interval = setInterval(() => {
      const now = new Date();
      const today = now.getDate();
      const isCleaningDay = CLEANING_SCHEDULE.some(s => s.day === today);
      const already = localStorage.getItem(notifiedKey());

      if (isCleaningDay && now.getHours() === 11 && !already && Notification.permission === "granted") {
        const person = getCleaningPersonForDay(today);
        new Notification("🧹 বাসা পরিষ্কারের রিমাইন্ডার", {
          body: `আজ ${person}-এর বাসা পরিষ্কার করার দিন!`,
          icon: hisabImage,
        });
        localStorage.setItem(notifiedKey(), "1");
      }
    }, 30000);
    return () => clearInterval(interval);
  }, []);

  return { permission, enableNotifications };
}

// ================= APP =================
export default function App() {
  const [isUnlocked, setIsUnlocked] = useState(false);
  const [showSplash, setShowSplash] = useState(false);
  const [pinInput, setPinInput] = useState("");
  const [pinError, setPinError] = useState("");

  const [monthKey, setMonthKey] = useState(currentMonthKey());
  const [marketItems, setMarketItems] = useState([]);
  const [shoppingList, setShoppingList] = useState([]);
  const [loading, setLoading] = useState(true);

  const [newItemText, setNewItemText] = useState("");
  const [newAmount, setNewAmount] = useState("");
  const [newCategory, setNewCategory] = useState("বাজার");
  const [selectedBuyer, setSelectedBuyer] = useState(memberNamesOnly[0]);
  const [selectedDate, setSelectedDate] = useState(formatDateForInput(Date.now()));
  const [newShoppingItem, setNewShoppingItem] = useState("");

  const [actionModal, setActionModal] = useState({ isOpen: false, type: "", item: null, error: "" });
  const [actionPinInput, setActionPinInput] = useState("");
  const [editModal, setEditModal] = useState({ isOpen: false, id: "", text: "", amount: "", buyer: "", date: "", category: "বাজার" });

  const { permission: notifPermission, enableNotifications } = useCleaningNotification();
  const todayCleaner = getCleaningPersonForDay(new Date().getDate());
  const nextCleaning = getNextCleaning();

  useEffect(() => {
    setLoading(true);
    const [yy, mm] = monthKey.split("-").map(Number);
    const start = new Date(yy, mm - 1, 1).getTime();
    const end = new Date(yy, mm, 1).getTime();

    const qExpenses = query(
      collection(db, "expenses"),
      where("timestamp", ">=", start),
      where("timestamp", "<", end),
      orderBy("timestamp", "desc")
    );
    const unsubExpenses = onSnapshot(qExpenses, (snap) => {
      setMarketItems(snap.docs.map(d => ({ id: d.id, ...d.data() })));
      setLoading(false);
    });

    const qShopping = query(collection(db, "shopping_list"), orderBy("timestamp", "asc"));
    const unsubShopping = onSnapshot(qShopping, (snap) => setShoppingList(snap.docs.map(d => ({ id: d.id, ...d.data() }))));

    return () => { unsubExpenses(); unsubShopping(); };
  }, [monthKey]);

  const unlock = () => {
    if (pinInput === ENTRY_PIN) {
      setPinError("");
      setPinInput("");
      setShowSplash(true);
      setTimeout(() => {
        setShowSplash(false);
        setIsUnlocked(true);
      }, 10000);
    } else { setPinError("ভুল পিন! আবার চেষ্টা করুন"); }
  };

  const handleAddExpense = async (e) => {
    e.preventDefault();
    if (!newItemText || !newAmount) return;
    try {
      await addDoc(collection(db, "expenses"), {
        text: newItemText,
        amount: Number(newAmount),
        buyer: selectedBuyer,
        category: newCategory,
        timestamp: new Date(selectedDate).getTime(),
      });
      setNewItemText(""); setNewAmount("");
    } catch (error) {
      console.error(error);
      alert("ডাটা সেভ করতে সমস্যা হয়েছে!");
    }
  };

  const handleAddShoppingItem = async (e) => {
    e.preventDefault();
    if (!newShoppingItem) return;
    try {
      await addDoc(collection(db, "shopping_list"), { text: newShoppingItem, timestamp: Date.now() });
      setNewShoppingItem("");
    } catch (error) { console.error(error); }
  };

  const deleteShoppingItem = async (id) => {
    try { await deleteDoc(doc(db, "shopping_list", id)); } catch (error) { console.error(error); }
  };

  const verifyActionPin = async () => {
    if (actionPinInput !== EDIT_PIN) {
      setActionModal(prev => ({ ...prev, error: "ভুল পিন!" }));
      return;
    }
    const { type, item } = actionModal;
    if (type === 'delete') {
      try {
        await deleteDoc(doc(db, "expenses", item.id));
        setActionModal({ isOpen: false, type: "", item: null, error: "" });
        setActionPinInput("");
      } catch (error) { setActionModal(prev => ({ ...prev, error: "ডিলেট করতে সমস্যা হয়েছে!" })); }
    } else if (type === 'edit') {
      setEditModal({ isOpen: true, id: item.id, text: item.text, amount: item.amount, buyer: item.buyer, date: formatDateForInput(item.timestamp), category: item.category || "বাজার" });
      setActionModal({ isOpen: false, type: "", item: null, error: "" });
      setActionPinInput("");
    }
  };

  const handleUpdateExpense = async (e) => {
    e.preventDefault();
    try {
      await updateDoc(doc(db, "expenses", editModal.id), {
        text: editModal.text,
        amount: Number(editModal.amount),
        buyer: editModal.buyer,
        category: editModal.category,
        timestamp: new Date(editModal.date).getTime(),
      });
      setEditModal({ isOpen: false, id: "", text: "", amount: "", buyer: "", date: "", category: "বাজার" });
    } catch (error) { alert("আপডেট ব্যর্থ হয়েছে!"); }
  };

  const totalMarketExpense = marketItems.reduce((sum, i) => sum + Number(i.amount || 0), 0);
  const memberSpending = {};
  memberNamesOnly.forEach(n => memberSpending[n] = 0);
  marketItems.forEach(i => { if (memberSpending[i.buyer] !== undefined) memberSpending[i.buyer] += Number(i.amount || 0); });
  const perPersonMarket = totalMarketExpense / memberNamesOnly.length;
  const balances = {};
  memberNamesOnly.forEach(name => { balances[name] = memberSpending[name] - perPersonMarket; });

  if (showSplash) {
    return (
      <div className="fixed inset-0 z-50 bg-black flex items-center justify-center overflow-hidden">
        <video src={splashVideo} autoPlay playsInline className="w-full h-full object-cover" />
      </div>
    );
  }

  if (!isUnlocked) {
    return (
      <div className="min-h-screen bg-blue-950 flex items-center justify-center p-4 text-center">
        <div className="bg-white rounded-3xl w-full max-w-xs p-8 shadow-2xl">
          <div className="flex flex-col items-center mb-6">
            <img src={hisabImage} alt="Hishab Logo" className="w-20 h-20 rounded-2xl object-cover mb-2" />
            <h1 className="text-4xl font-black mb-2">হিসাব</h1>
            <p className="text-gray-600">মহসিন , রুবেল , মোজাম্মেল সজিব ও আরিফ</p>
          </div>
          <input type="password" maxLength={4} value={pinInput} onChange={(e) => setPinInput(e.target.value)} placeholder="••••" className="w-full text-3xl text-center py-5 border-2 rounded-2xl outline-none tracking-widest font-mono focus:border-blue-500 transition-colors" />
          {pinError && <p className="text-red-500 mt-4 font-bold">{pinError}</p>}
          <button onClick={unlock} className="mt-6 w-full bg-blue-600 text-white font-bold py-4 rounded-2xl text-lg transition hover:bg-blue-700 shadow-lg">প্রবেশ করুন</button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen relative p-5 pb-20">
      <style>
        {`@import url('https://fonts.googleapis.com/css2?family=Dancing+Script:wght@700&display=swap');`}
      </style>

      <div className="fixed inset-0 z-0">
        <div className="absolute inset-0 bg-white/85 backdrop-blur-[4px] z-10"></div>
        <img src={hisabImage} className="w-full h-full object-cover z-0" alt="background" />
      </div>

      <div className="max-w-md mx-auto space-y-6 relative z-20">

        <div className="flex flex-col items-center mb-6 pt-4">
          <img src={hisabImage} alt="Hishab Logo" className="w-20 h-20 rounded-2xl object-cover mb-2 shadow-lg" />
          <h1 className="text-4xl font-black text-gray-800 tracking-tight">বাসা পরিষ্কারের সিডিউল</h1>
          <p style={{ fontFamily: "'Dancing Script', cursive" }} className="text-blue-600 text-xl font-bold mt-1 tracking-wider drop-shadow-md">
            Powered by Mahsin
          </p>
        </div>

        {/* পরিষ্কারের সিডিউল কার্ড */}
        <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-3xl p-6 text-white shadow-lg">
          <p className="text-xs uppercase font-bold tracking-widest opacity-80">আজকের দায়িত্ব</p>
          <p className="text-2xl font-black mt-1">🧹 {todayCleaner}</p>
          <div className="mt-3 pt-3 border-t border-white/30 flex justify-between items-center">
            <div>
              <p className="text-xs opacity-80">পরবর্তী পরিষ্কার</p>
              <p className="font-bold">{nextCleaning.name} • {nextCleaning.date.toLocaleDateString('bn-BD', { day: 'numeric', month: 'long' })}</p>
            </div>
            {notifPermission !== "granted" && (
              <button onClick={enableNotifications} className="bg-white/20 hover:bg-white/30 px-3 py-2 rounded-xl text-xs font-bold transition-colors">
                🔔 নোটিফিকেশন চালু করুন
              </button>
            )}
          </div>
          <div className="mt-4 grid grid-cols-5 gap-2 text-center">
            {CLEANING_SCHEDULE.map(s => (
              <div key={s.day} className={`rounded-xl p-2 ${s.name === todayCleaner ? 'bg-white text-green-700 font-black' : 'bg-white/15'}`}>
                <p className="text-[10px]">{s.day} তারিখ</p>
                <p className="text-xs font-bold">{s.name}</p>
              </div>
            ))}
          </div>
        </div>

        <div className="flex justify-center mb-6">
          <div className="bg-white/90 backdrop-blur-sm px-4 py-2 rounded-2xl shadow-sm border border-blue-100 flex items-center gap-3">
            <span className="text-xl">📅</span>
            <input
              type="month"
              value={monthKey}
              onChange={(e) => setMonthKey(e.target.value || currentMonthKey())}
              className="bg-transparent font-bold text-gray-800 outline-none cursor-pointer"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-blue-600 rounded-3xl p-5 text-white shadow-lg flex flex-col items-center justify-center text-center">
            <p className="text-[10px] uppercase font-bold tracking-widest opacity-80">মোট খরচ</p>
            <p className="text-2xl font-black mt-1">৳{totalMarketExpense.toFixed(0)}</p>
          </div>

          {memberNamesOnly.map((name) => (
            <div key={name} className="relative bg-white rounded-3xl p-5 text-gray-800 shadow shadow-blue-100 flex flex-col items-center justify-center text-center border border-blue-50 overflow-hidden">
              <div className="absolute inset-0 opacity-[0.15] bg-cover bg-top" style={{ backgroundImage: `url(${memberImgMap[name]})` }}></div>
              <div className="relative z-10">
                <p className="text-[10px] uppercase font-bold tracking-widest text-blue-600 drop-shadow-md">{name} করেছে</p>
                <p className="text-2xl font-black mt-1 text-gray-900 drop-shadow-md">৳{memberSpending[name].toFixed(0)}</p>
              </div>
            </div>
          ))}

          <div className="bg-yellow-400 rounded-3xl p-5 text-yellow-900 shadow-lg flex flex-col items-center justify-center text-center">
            <p className="text-[10px] uppercase font-bold tracking-widest opacity-80">বাজার করতে হবে</p>
            <p className="text-2xl font-black mt-1">{shoppingList.length}টি আইটেম</p>
          </div>
        </div>

        <div className="bg-yellow-50/90 backdrop-blur-sm rounded-3xl p-6 shadow-md border-t-8 border-yellow-400">
          <h3 className="font-bold text-xl mb-5 text-gray-800 flex items-center gap-2">🛒 কি কি আনতে হবে?</h3>
          <form onSubmit={handleAddShoppingItem} className="flex gap-2 mb-6">
            <input type="text" placeholder="জিনিসের নাম..." value={newShoppingItem} onChange={(e) => setNewShoppingItem(e.target.value)} className="flex-1 p-3 border-2 border-yellow-200 rounded-2xl outline-none focus:border-yellow-400 bg-white" required />
            <button type="submit" className="bg-yellow-400 hover:bg-yellow-500 text-yellow-900 px-6 rounded-2xl font-black text-xl shadow-sm transition-colors">➕</button>
          </form>
          <div className="space-y-2">
            {shoppingList.length === 0 ? (
              <p className="text-gray-500 text-sm italic text-center py-2">কোনো আইটেম নেই।</p>
            ) : (
              shoppingList.map(item => (
                <div key={item.id} className="flex justify-between items-center bg-white/80 p-3 rounded-xl border border-yellow-200 shadow-sm">
                  <span className="text-gray-800 font-semibold">{item.text}</span>
                  <button onClick={() => deleteShoppingItem(item.id)} className="text-red-400 font-bold px-2 hover:scale-125 transition-transform">✕</button>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="space-y-4">
          {memberNamesOnly.map((name) => (
            <div key={name} className="bg-white/95 backdrop-blur-md rounded-3xl p-6 shadow-md flex items-center gap-6 border border-gray-100">
              <MemberAvatar name={name} />
              <div className="flex-1">
                <p className="text-xl font-bold text-gray-800">{name}</p>
                <p className={`text-3xl font-black mt-1 ${balances[name] >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                  {balances[name] >= 0 ? '+' : '-'}৳{Math.abs(balances[name]).toFixed(2)}
                </p>
              </div>
            </div>
          ))}
        </div>

        <div className="bg-white/95 backdrop-blur-md rounded-3xl p-6 shadow-lg border border-gray-100">
          <h3 className="font-bold text-lg mb-4 text-gray-800">নতুন খরচ যোগ করুন</h3>
          <form onSubmit={handleAddExpense} className="space-y-4">
            <div className="flex gap-2">
              {["বাজার", "পরিষ্কার সামগ্রী"].map(cat => (
                <button key={cat} type="button" onClick={() => setNewCategory(cat)}
                  className={`flex-1 py-2 rounded-xl font-bold text-sm transition-colors ${newCategory === cat ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600'}`}>
                  {cat}
                </button>
              ))}
            </div>
            <input type="text" placeholder="পণ্যের নাম..." value={newItemText} onChange={(e) => setNewItemText(e.target.value)} className="w-full p-4 border-2 border-gray-200 rounded-2xl outline-none focus:border-blue-500 transition-colors" required />
            <div className="flex gap-3">
              <input type="number" step="0.01" placeholder="টাকা" value={newAmount} onChange={(e) => setNewAmount(e.target.value)} className="flex-1 p-4 border-2 border-gray-200 rounded-2xl outline-none focus:border-blue-500 transition-colors" required />
              <select value={selectedBuyer} onChange={(e) => setSelectedBuyer(e.target.value)} className="p-4 border-2 border-gray-200 rounded-2xl bg-white outline-none focus:border-blue-500 font-bold transition-colors cursor-pointer">
                {memberNamesOnly.map(n => <option key={n} value={n}>{n}</option>)}
              </select>
            </div>
            <input type="date" value={selectedDate} onChange={(e) => setSelectedDate(e.target.value)} className="w-full p-4 border-2 border-gray-200 rounded-2xl outline-none focus:border-blue-500 text-gray-600 transition-colors" />
            <button type="submit" className="w-full bg-blue-600 text-white py-4 rounded-2xl font-black text-xl shadow-lg hover:bg-blue-700 transition-colors">সেভ করুন</button>
          </form>
        </div>

        <div className="bg-white/95 backdrop-blur-md rounded-3xl p-6 shadow border border-gray-100 mb-8">
          <h3 className="font-bold text-lg mb-4 text-gray-800">খরচের তালিকা</h3>
          <div className="space-y-3">
            {marketItems.length === 0 && !loading && (
              <p className="text-gray-500 text-center py-4 italic">এই মাসে কোনো খরচ নেই</p>
            )}
            {marketItems.map((item) => (
              <div key={item.id} className="flex justify-between items-center p-4 bg-gray-50 hover:bg-gray-100 transition-colors rounded-2xl border border-gray-100 shadow-sm">
                <div className="flex-1">
                  <p className="font-bold text-gray-900 text-lg">{item.text}</p>
                  <p className="text-sm text-gray-500">
                    {item.category === "পরিষ্কার সামগ্রী" ? "🧹" : "🛒"} {item.buyer} • {new Date(item.timestamp).toLocaleDateString('bn-BD')}
                  </p>
                </div>
                <div className="text-right">
                  <p className="font-black text-blue-600 text-xl">৳{item.amount.toFixed(2)}</p>
                  <div className="flex gap-2 mt-2 justify-end">
                    <button onClick={() => setActionModal({ isOpen: true, type: 'edit', item, error: "" })} className="bg-blue-100 hover:bg-blue-200 text-blue-800 p-2 rounded-lg transition-colors text-sm">✏️</button>
                    <button onClick={() => setActionModal({ isOpen: true, type: 'delete', item, error: "" })} className="bg-red-100 hover:bg-red-200 text-red-800 p-2 rounded-lg transition-colors text-sm">🗑️</button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {actionModal.isOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-3xl p-6 w-full max-w-xs text-center shadow-2xl animate-in zoom-in duration-200">
            <h3 className="font-bold text-xl mb-4">সিকিউরিটি পিন দিন</h3>
            <input type="password" maxLength={4} value={actionPinInput} onChange={(e) => setActionPinInput(e.target.value)} placeholder="••••" className="w-full text-3xl text-center py-4 border-2 border-gray-200 rounded-2xl outline-none focus:border-blue-600 tracking-widest mb-2 font-mono" />
            {actionModal.error && <p className="text-red-500 font-bold mb-4">{actionModal.error}</p>}
            <div className="flex gap-3 mt-4">
              <button onClick={() => { setActionModal({ isOpen: false, type: "", item: null, error: "" }); setActionPinInput(""); }} className="flex-1 bg-gray-100 hover:bg-gray-200 py-3 rounded-2xl font-bold transition-colors text-gray-700">বাতিল</button>
              <button onClick={verifyActionPin} className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-2xl font-bold transition-colors">নিশ্চিত</button>
            </div>
          </div>
        </div>
      )}

      {editModal.isOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-2xl animate-in slide-in-from-bottom-4 duration-200">
            <h3 className="font-bold text-xl mb-4 text-center">সংশোধন করুন</h3>
            <form onSubmit={handleUpdateExpense} className="space-y-4">
              <div className="flex gap-2">
                {["বাজার", "পরিষ্কার সামগ্রী"].map(cat => (
                  <button key={cat} type="button" onClick={() => setEditModal({ ...editModal, category: cat })}
                    className={`flex-1 py-2 rounded-xl font-bold text-sm transition-colors ${editModal.category === cat ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600'}`}>
                    {cat}
                  </button>
                ))}
              </div>
              <input type="text" value={editModal.text} onChange={(e) => setEditModal({ ...editModal, text: e.target.value })} className="w-full p-4 border-2 border-gray-200 rounded-2xl outline-none focus:border-blue-500" required />
              <div className="flex gap-3">
                <input type="number" step="0.01" value={editModal.amount} onChange={(e) => setEditModal({ ...editModal, amount: e.target.value })} className="flex-1 p-4 border-2 border-gray-200 rounded-2xl outline-none focus:border-blue-500" required />
                <select value={editModal.buyer} onChange={(e) => setEditModal({ ...editModal, buyer: e.target.value })} className="p-4 border-2 border-gray-200 rounded-2xl font-bold outline-none focus:border-blue-500 bg-white">
                  {memberNamesOnly.map(n => <option key={n} value={n}>{n}</option>)}
                </select>
              </div>
              <input type="date" value={editModal.date} onChange={(e) => setEditModal({ ...editModal, date: e.target.value })} className="w-full p-4 border-2 border-gray-200 rounded-2xl outline-none focus:border-blue-500 text-gray-700" />
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setEditModal({ isOpen: false, id: "", text: "", amount: "", buyer: "", date: "", category: "বাজার" })} className="flex-1 bg-gray-100 hover:bg-gray-200 text-gray-800 py-4 rounded-2xl font-bold transition-colors">বাতিল</button>
                <button type="submit" className="flex-1 bg-green-600 hover:bg-green-700 text-white py-4 rounded-2xl font-black transition-colors">আপডেট</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}