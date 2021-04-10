// This import loads the firebase namespace.
import firebase from 'firebase/app';
 
// These imports load individual services into the firebase namespace.
import 'firebase/auth';
import 'firebase/database';
import 'firebase/firestore';

import * as admin from "firebase-admin";

export class FirebaseConfig {

    // Preferred, picks GOOGLE_APPLICATION_CREDENTIALS service account json
    // Needs exporting of this environment variable
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
            this.firebaseApp = firebase.initializeApp({
                apiKey: this.apiKey(),
                authDomain: "moneycol.firebaseapp.com",
                databaseURL: "https://moneycol.firebaseio.com",
                projectId: "moneycol",
                storageBucket: "moneycol.appspot.com",
                messagingSenderId: "461081581931",
                appId: "1:461081581931:web:3ca5344ae0e1df6dfa542e"
            });
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

    getFirestore() {
        if (!this.firestore.collection) {
            this.firestore = this.getAdmin().firestore();
        }
        return this.firestore;
    }

    apiKey() {
        return process.env.FIREBASE_API_KEY;
    }
}

export const firebaseInstance = new FirebaseConfig();