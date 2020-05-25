import { FirebaseUserService } from './firebase/FirebaseUserService'
import { UserService, userRepository } from './UserService';
import { firebaseInstance } from '../authentication/firebase/FirebaseConfiguration';

const userService: UserService = new FirebaseUserService(firebaseInstance, userRepository);

export { userService };