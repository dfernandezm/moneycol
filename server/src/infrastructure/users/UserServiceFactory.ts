import { FirebaseUserService } from './firebase/FirebaseUserService'
import { UserService, userRepository } from './UserService';
import { firebaseInstance } from '../authentication/firebase/FirebaseConfiguration';
import FirebaseEmailService from './firebase/FirebaseEmailService';

const emailService: FirebaseEmailService = new FirebaseEmailService(firebaseInstance);
const userService: UserService = new FirebaseUserService(firebaseInstance, userRepository, emailService);

export { userService };