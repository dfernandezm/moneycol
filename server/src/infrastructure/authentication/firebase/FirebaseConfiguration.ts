import "firebase/auth";
import "firebase/firestore";
import * as admin from "firebase-admin";
import firebase from "firebase/app";

const API_KEY = process.env.FIREBASE_API_KEY;

class FirebaseConfig {

    private FIREBASE_CONFIG = {
        apiKey: API_KEY,
        authDomain: "moneycol.firebaseapp.com",
        databaseURL: "https://moneycol.firebaseio.com",
        projectId: "moneycol",
        storageBucket: "moneycol.appspot.com",
        messagingSenderId: "461081581931",
        appId: "1:461081581931:web:3ca5344ae0e1df6dfa542e"
    };

    private firebaseApp: any = {};
    private adminApp: any = {};

    get() {
        if (!this.firebaseApp.auth) {
            //TODO: This should be cached more globally, otherwise we can't use firebase sessions that survive across servers
            this.firebaseApp = firebase.initializeApp(this.FIREBASE_CONFIG);
        }
        return this.firebaseApp;
    }

    getAdmin() {
        if (!this.adminApp.auth) {
            //TODO: This should be cached more globally, otherwise we can't use firebase sessions that survive across servers
            this.adminApp = admin.initializeApp(this.FIREBASE_CONFIG);
        }
        return this.adminApp;
    }
}

export const firebaseInstance = new FirebaseConfig();