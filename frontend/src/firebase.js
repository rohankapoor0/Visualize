import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
  apiKey: "AIzaSyC0vwJrcOXgAJLPVnkvbPSLFoLA1lJe3aw",
  authDomain: "visualize-4510d.firebaseapp.com",
  projectId: "visualize-4510d",
  storageBucket: "visualize-4510d.firebasestorage.app",
  messagingSenderId: "123426332747",
  appId: "1:123426332747:web:f9dbe19c612c13720a6dce",
  measurementId: "G-KK382YBQQ6"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

// Initialize Services
export const auth = getAuth(app);
export const db = getFirestore(app);

export default app;
