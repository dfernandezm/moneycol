import { firebaseInstance } from "../../authentication/firebase/FirebaseConfiguration";
import { UserRepository, User } from "../UserService";
import UserNotFoundError from "../UserNotFoundError";
import InvalidValueError from "../InvalidValueError";


export default class FirestoreUserRepository implements UserRepository {

    async byEmail(email: string): Promise<User> {
        let db: FirebaseFirestore.Firestore = firebaseInstance.getFirestore();
        let usersRef = db.collection("users");
        let usersSnapshot = await usersRef.where("email", "==", email.toLowerCase()).get();
        
        if (usersSnapshot.empty) {
            console.log('No user found with email ' + email);
            throw new UserNotFoundError('No user found with email ' + email);
        }

        const foundUser = usersSnapshot.docs[0].data() as User;
        return foundUser;
    }

    async updateUserData(userData: User): Promise<object> {
        let db: FirebaseFirestore.Firestore = firebaseInstance.getFirestore();
        let usersRef = db.collection("users");
        let userId = userData.userId;

        if (!userId) {
            throw new InvalidValueError("userId must be present to update user", userData)
        }

        let result = await usersRef.doc(userId).set(userData);
        console.log("Updated user", result);
        return result;
    }

    async persistUser(user: User): Promise<object> {
        console.log("Saving user in firestore");
        let db = firebaseInstance.getFirestore();
        user.email = user.email.toLowerCase();
        let docRef = db.collection('users').doc(user.userId);
        return docRef.set(user);
    }
}

