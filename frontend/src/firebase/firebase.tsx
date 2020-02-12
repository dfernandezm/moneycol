import firebase from "firebase/app";
import "firebase/auth";
import "firebase/firestore";

//TODO: Firebase should be in server side under an interface as Auth provider
// This works in production/deployed as there is an ingress rule for /api that points to moneycolserver
const FIREBASE_CONFIG_URL = process.env.NODE_ENV === 'production' ? "api/firebaseConfig" : 
                                    "http://localhost:4000/api/firebaseConfig"

const findFirebaseConfig = async () => {
  const response = await fetch(FIREBASE_CONFIG_URL);
  return response.json();
}

let firebaseConfig = "";
let firebaseApp: any = {};

export const myFirebase = async () => {
  if (!firebaseConfig) {
    firebaseConfig = await findFirebaseConfig()
  }
 
  if (!firebaseApp.auth) {
    firebaseApp = firebase.initializeApp(firebaseConfig);
    const baseDb = firebaseApp.firestore();
  }
  
  return firebaseApp;
}
