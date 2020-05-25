import { UserService, CreateUserCommand, UserCreatedResult, UserStatus, UserRepository } from "../UserService";
import { FirebaseConfig } from  '../../authentication/firebase/FirebaseConfiguration';
import InvalidValueError from "../InvalidValueError";

const ACCOUNT_DISABLED_ERROR_CODE = 'auth/user-disabled';

class FirebaseUserService implements UserService {
    
    private firebaseInstance: FirebaseConfig;
    private userRepository: UserRepository;

    constructor(firebaseInstance: FirebaseConfig, userRepository: UserRepository) {
        this.firebaseInstance = firebaseInstance;
        this.userRepository = userRepository;
    }

    async signUpWithEmail(createUserCommand: CreateUserCommand): Promise<UserCreatedResult> {

        try {

            this.validate(createUserCommand);

            const userDisplayName = this.setDisplayName(createUserCommand);
            const email = createUserCommand.email;
            const password = createUserCommand.password;
            const displayName = userDisplayName;

            // This could be a preference in Firestore, global app 'preferences'
            const emailVerified = false;
            const firebaseAuth = this.firebaseInstance.get().auth();

            const result = await firebaseAuth.createUserWithEmailAndPassword(email, password);
            const userId = result.user.uid;
            console.log("Firebase user created: ", userId);

            // Need currentUser to send verification email and update the name
            const currentUser = firebaseAuth.currentUser;

            // Update the displayName for Firebase Auth -- it could be off request too (not await)
            console.log(`User with email ${email} registered. Setting name.`);

            await firebaseAuth.currentUser.updateProfile({ displayName, emailVerified });
            console.log(`User profile with email ${email} name updated. Persisting user.`);

            // Persist in Firestore to control custom workflows, 
            // i.e. won't allow login if user is pending verification
            const userToPersist = {
                userId: userId,
                email: email,
                username: createUserCommand.username,
                firstName: createUserCommand.firstName,
                lastName: createUserCommand.lastName,
                status: UserStatus.PENDING_VERIFICATION
            }

            await this.userRepository.persistUser(userToPersist);

            console.log("Sending email verification -- not awaited");
            currentUser.sendEmailVerification();

            return userToPersist;
        } catch (err) {
            console.log("Error creating user", err)
            if (err instanceof InvalidValueError) {
                throw err;
            } else {
                throw new Error("Error creating user");
            }
        }
    }

    private setDisplayName(userCmd: CreateUserCommand) {
        if (userCmd.firstName && userCmd.lastName) {
            return userCmd.firstName + " " + userCmd.lastName;
        }

        if (userCmd.firstName) {
            return userCmd.firstName;
        }

        return userCmd.username;
    }

    private async firebaseAdminUpdateUser(userId: string, displayName: string, emailVerified: boolean, disabled: boolean): Promise<any> {
        return new Promise((resolve, reject) => {
            return this.firebaseInstance.getAdmin().auth().updateUser(userId, { displayName, disabled, emailVerified })
                .then((updatedUser: any) => {
                    console.log("Success", updatedUser);
                    resolve(updatedUser)
                })
                .catch((err: any) => {
                    if (err.code && err.code === ACCOUNT_DISABLED_ERROR_CODE) {
                        console.log("Ignoring disabled account error -- expected on registration");
                        resolve({});
                    } else {
                        reject(err);
                    }
                });
        });
    }

    private validate(userToCreate: CreateUserCommand) {
        if (!userToCreate.username) {
            throw new InvalidValueError(`Username is invalid ${userToCreate.username}`);
        }

        if (!userToCreate.email) {
            throw new InvalidValueError(`Email is invalid ${userToCreate.email}`);
        }

        if (!userToCreate.password || !userToCreate.repeatedPassword) {
            throw new InvalidValueError(`Password is invalid`);
        }

        if (userToCreate.password !== userToCreate.repeatedPassword) {
            throw new InvalidValueError(`Passwords don't match`);
        }
    }
}

export { FirebaseUserService };