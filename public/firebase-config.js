// গোলাপি নিউজ - Firebase কনফিগারেশন (পাবলিক সাইট ও অ্যাডমিন প্যানেল উভয়ে ব্যবহৃত)
import { initializeApp } from "https://www.gstatic.com/firebasejs/10.13.1/firebase-app.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.13.1/firebase-firestore.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.13.1/firebase-auth.js";
import { getStorage } from "https://www.gstatic.com/firebasejs/10.13.1/firebase-storage.js";

const firebaseConfig = {
  apiKey: "AIzaSyBkQWnZlhRxMZhOfkjYuJSeqk-gNia5_ro",
  authDomain: "golapinews.firebaseapp.com",
  projectId: "golapinews",
  storageBucket: "golapinews.firebasestorage.app",
  messagingSenderId: "236072481044",
  appId: "1:236072481044:web:7f80deb9e371a451489201",
  measurementId: "G-D8JWJ0LMEL"
};

export const firebaseApp = initializeApp(firebaseConfig);
export const db = getFirestore(firebaseApp);
export const auth = getAuth(firebaseApp);
export const storage = getStorage(firebaseApp);
