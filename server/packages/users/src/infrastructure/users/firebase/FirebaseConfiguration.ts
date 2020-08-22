// This import loads the firebase namespace.
import firebase, { firestore } from 'firebase/app';
 
// These imports load individual services into the firebase namespace.
import 'firebase/auth';
import 'firebase/database';
import 'firebase/firestore';

import * as admin from "firebase-admin";

const FIREBASE_API_KEY = process.env.FIREBASE_API_KEY;

export class FirebaseConfig {

    private FIREBASE_CONFIG = {
        apiKey: FIREBASE_API_KEY,
        authDomain: "moneycol.firebaseapp.com",
        databaseURL: "https://moneycol.firebaseio.com",
        projectId: "moneycol",
        storageBucket: "moneycol.appspot.com",
        messagingSenderId: "461081581931",
        appId: "1:461081581931:web:3ca5344ae0e1df6dfa542e"
    };

    // Preferred, picks GOOGLE_APPLICATION_CREDENTIALS service account json
    private SERVICE_ACCOUNT_AUTH = {
        credential: admin.credential.applicationDefault(),
        databaseURL: "https://moneycol.firebaseio.com"
    }

    private firebaseApp: any = {};
    private adminApp: any = {};
    private firestore: any = {};

    // firebase auth client
    get() {
        if (!this.firebaseApp.auth) {
            this.firebaseApp = firebase.initializeApp(this.FIREBASE_CONFIG);
        }
        return this.firebaseApp;
    }

    // firebase auth admin SDK server
    getAdmin() {
        if (!this.adminApp.auth) {
            this.adminApp = admin.initializeApp(this.SERVICE_ACCOUNT_AUTH);
        }
        return this.adminApp;
    }

    // firestore
    getFirestore() {
        if (!this.firestore.collection) {
            this.firestore = this.getAdmin().firestore();
        }
        return this.firestore;
    }

    apiKey() {
        return FIREBASE_API_KEY;
    }
}

export const firebaseInstance = new FirebaseConfig();