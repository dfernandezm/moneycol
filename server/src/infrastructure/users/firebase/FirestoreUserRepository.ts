import { firebaseInstance } from "../../authentication/firebase/FirebaseConfiguration";
import { UserRepository, User } from "../UserService";
import firebase from 'firebase';


export default class FirestoreUserRepository implements UserRepository {
    //TODO: this should not find and change status, only update
    async updateUser(email: string): Promise<any> {
        let db: FirebaseFirestore.Firestore = firebaseInstance.getFirestore();
        let usersRef = db.collection("users");
        let usersSnapshot = await usersRef.where("email", "==", email.toLowerCase()).get();
        if (usersSnapshot.empty) {
            console.log('No user found with email ' + email);
            throw new Error('No user found with email ' + email);
        }

        let foundUser = usersSnapshot.docs[0].data();
        foundUser.status = "ACTIVE";

        const updateResult = usersRef.doc(foundUser.userId).set(foundUser);
        console.log("Updated user", updateResult);
    }



    //TODO: byEmail(email)
    //TODO: updateUser(user)

    async persistUser(user: User): Promise<object> {
        console.log("Saving user in firestore");
        let db = firebaseInstance.getFirestore();
        user.email = user.email.toLowerCase();
        let docRef = db.collection('users').doc(user.userId);
        return docRef.set(user);
    }
}

