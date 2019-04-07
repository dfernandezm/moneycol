const firebase = require('firebase');

// it is not a security risk to expose this as long as we control the rules in the Firebase database / collection (need to to do it)
//https://stackoverflow.com/questions/37482366/is-it-safe-to-expose-firebase-apikey-to-the-public
// Setup basic rules: https://firebase.google.com/docs/firestore/security/rules-conditions?authuser=0
var config = {
    apiKey: "AIzaSyC7VJc42VAfUrbzF-P4WSo6G5VH856ttw4",
    authDomain: "moneycol.firebaseapp.com",
    databaseURL: "https://moneycol.firebaseio.com",
    projectId: "moneycol",
    storageBucket: "moneycol.appspot.com",
    messagingSenderId: "461081581931"
};

firebase.initializeApp(config);

module.exports = firebase;