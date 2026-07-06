import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyCwxp37-RhOVx1OsmyGs5Zvu5WESPNYfRQ",
  authDomain: "lillahi-etimkhana-781aa.firebaseapp.com",
  projectId: "lillahi-etimkhana-781aa",
  storageBucket: "lillahi-etimkhana-781aa.firebasestorage.app",
  messagingSenderId: "584997105519",
  appId: "1:584997105519:web:30215e2b4ee45aa66d829a",
  measurementId: "G-Z7BHMT9999"
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);