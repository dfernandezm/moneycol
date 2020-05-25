import { firebaseInstance } from "../../authentication/firebase/FirebaseConfiguration";
import { UserRepository, User } from "../UserService";

export default class FirestoreUserRepository implements UserRepository {
    async persistUser(user: User): Promise<object> {
        console.log("Saving user in firestore");
        let db = firebaseInstance.getFirestore();
        let docRef = db.collection('users').doc(user.userId);
        return docRef.set(user);
    }
}

