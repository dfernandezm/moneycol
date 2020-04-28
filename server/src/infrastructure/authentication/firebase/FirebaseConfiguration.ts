import "firebase/auth";
import "firebase/firestore";

import firebase from "firebase/app";

class FirebaseConfig {

    private FIREBASE_CONFIG = {
        apiKey: "",
        authDomain: "moneycol.firebaseapp.com",
        databaseURL: "https://moneycol.firebaseio.com",
        projectId: "moneycol",
        storageBucket: "moneycol.appspot.com",
        messagingSenderId: "461081581931",
        appId: "1:461081581931:web:3ca5344ae0e1df6dfa542e"
    };

    private firebaseApp: any = {};

    get() {
        if (!this.firebaseApp.auth) {
            this.firebaseApp = firebase.initializeApp(this.FIREBASE_CONFIG);
        }

        return this.firebaseApp;
    }

}

export const firebaseInstance = new FirebaseConfig();