// This import loads the firebase namespace.
import firebase from 'firebase/app';
 
// These imports load individual services into the firebase namespace.
import 'firebase/auth';
import 'firebase/database';
import 'firebase/firestore';

import * as admin from "firebase-admin";

export const FIREBASE_API_KEY = process.env.FIREBASE_API_KEY;

class FirebaseConfig {

    private FIREBASE_CONFIG = {
        apiKey: FIREBASE_API_KEY,
        authDomain: "moneycol.firebaseapp.com",
        databaseURL: "https://moneycol.firebaseio.com",
        projectId: "moneycol",
        storageBucket: "moneycol.appspot.com",
        messagingSenderId: "461081581931",
        appId: "1:461081581931:web:3ca5344ae0e1df6dfa542e"
    };

    private SERVICE_ACCOUNT_AUTH = {
        credential: admin.credential.applicationDefault(),
        databaseURL: "https://moneycol.firebaseio.com"
    }

    private firebaseApp: any = {};
    private adminApp: any = {};
    private firestore: any = {};

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
            this.adminApp = admin.initializeApp(this.SERVICE_ACCOUNT_AUTH);
        }
        return this.adminApp;
    }

    getFirestore() {
        if (!this.firestore.collection) {
            this.firestore = this.getAdmin().firestore();
        }
        return this.firestore;
    }
}

export const firebaseInstance = new FirebaseConfig();