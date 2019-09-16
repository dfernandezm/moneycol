const firebase = require("./firestore");

const db = firebase.firestore();

const addUser = (userData) => {
        const userRef = db.collection("users").add({
            username: userData.username,
            firstname: userData.firstname,
            lastname: userData.lastname,
            email: userData.email
        });  
}

module.exports  = { addUser };

    
