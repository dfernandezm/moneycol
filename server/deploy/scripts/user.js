const firestoreRepo = require('./firestore/firebaseRepository');

const registerUser = (user) => {
    return firestoreRepo.addUser(user);
}

module.exports = {registerUser};