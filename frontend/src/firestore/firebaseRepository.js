import firebase from "./firestore";

const db = firebase.firestore();

const firestoreRepo = {}

firestoreRepo.addUser = (userData) => {
        const userRef = db.collection("users").add({
            username: userData.username,
            firstname: userData.firstname,
            lastname: userData.lastname,
            email: userData.email
        });  
}

export default firestoreRepo;

    
