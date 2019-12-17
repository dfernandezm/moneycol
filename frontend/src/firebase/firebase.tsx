import firebase from "firebase/app";
import "firebase/auth";
import "firebase/firestore";

//TODO: try to remove this
const firebaseConfig = {
    apiKey: "AIzaSyDImTA3-o5ew92DQ4pg0-nVKTHR92ncq-U",
    authDomain: "moneycol.firebaseapp.com",
    databaseURL: "https://moneycol.firebaseio.com",
    projectId: "moneycol",
    storageBucket: "moneycol.appspot.com",
    messagingSenderId: "461081581931",
    appId: "1:461081581931:web:3ca5344ae0e1df6dfa542e"
  };

export const myFirebase = firebase.initializeApp(firebaseConfig);
const baseDb = myFirebase.firestore();
export const db = baseDb;