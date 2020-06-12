import { FirebaseUserService } from '../../src/infrastructure/users/firebase/FirebaseUserService'
import { UserService, CreateUserCommand, UserRepository, User, UserStatus, EmailService, UpdateUserProfileCommand, UpdateUserProfileResult } from '../../src/infrastructure/users/UserService'
import { FirebaseConfig } from "../../src/infrastructure/authentication/firebase/FirebaseConfiguration";
import InvalidValueError from '../../src/infrastructure/users/InvalidValueError';
import FirebaseEmailService from '../../src/infrastructure/users/firebase/FirebaseEmailService';
import { updateDecorator } from 'typescript';
import UserInInvalidStateError from '../../src/infrastructure/users/UserInInvalidStateError';



describe('FirebaseUserService', () => {

  let instance: UserService;
  let mockFirebaseConfig: FirebaseConfig;
  let createUserWithEmailAndPasswordMock: jest.Mock;
  let updateProfileMock: jest.Mock;
  let sendEmailVerificationMock: jest.Mock;
  let userRepositoryMock: UserRepository;
  let emailServiceMock: EmailService;
  let persistUserMock: jest.Mock;
  let byEmailMock: jest.Mock;
  let updateUserDataMock: jest.Mock;
  let userId = "randomUid";

  beforeEach(() => {

    jest.clearAllMocks();

    createUserWithEmailAndPasswordMock = setupFirebaseCreateUserEmailMock(userId);
    updateProfileMock = jest.fn();
    sendEmailVerificationMock = jest.fn();
    persistUserMock = jest.fn();
    updateUserDataMock = jest.fn();

    mockFirebaseConfig = setupFirebaseMock(createUserWithEmailAndPasswordMock, updateProfileMock, sendEmailVerificationMock);

    userRepositoryMock = {
      persistUser: persistUserMock,
      byEmail: byEmailMock,
      updateUserData: updateUserDataMock,
      byId: jest.fn()
    }

    emailServiceMock = {
      verifyEmail: jest.fn(),
      generateComebackUrl: jest.fn()
    }

  });

  test('creates user with email and password without error', async () => {

    const createUserCommand: CreateUserCommand = aUserCommand();
    instance = new FirebaseUserService(mockFirebaseConfig, userRepositoryMock, emailServiceMock);

    const userResult = await instance.signUpWithEmail(createUserCommand);

    expect(createUserWithEmailAndPasswordMock).toHaveBeenCalledTimes(1);
    expect(updateProfileMock).toBeCalled();
    expect(persistUserMock).toBeCalled();
    expect(sendEmailVerificationMock).toBeCalled();
    expect(userResult.userId).toEqual(userId);

  });

  test('fails if passwords do not match', async () => {
    const createUserCommand: CreateUserCommand = aUserCommand();
    createUserCommand.repeatedPassword = "differentPassword";

    instance = new FirebaseUserService(mockFirebaseConfig, userRepositoryMock, emailServiceMock);
    await expect(instance.signUpWithEmail(createUserCommand)).rejects.toThrow(InvalidValueError);
  });

  ["password", "repeatedPassword", "username", "email"].map(field => {

    test(`fails if ${field} field not present`, async () => {
      const createUserCommand: CreateUserCommand = aUserCommand();
      const mutableCreateUserCommand = Object.assign({}, createUserCommand)
      mutableCreateUserCommand[field] = "";

      instance = new FirebaseUserService(mockFirebaseConfig, userRepositoryMock, emailServiceMock);
      await expect(instance.signUpWithEmail(mutableCreateUserCommand)).rejects.toThrow(InvalidValueError);
    });

  });

  test('updates user profile data if user is active', async () => {

    // Given an update profile request for an ACTIVE user
    const updateUserCommand = anUpdateUserCommand();
    
    userRepositoryMock.byId = jest.fn((userId: string) => Promise.resolve(aPersistedUser()));
    userRepositoryMock.updateUserData = jest.fn((userData: User) => Promise.resolve({}));
    instance = new FirebaseUserService(mockFirebaseConfig, userRepositoryMock, emailServiceMock);
    
    // When updating user data
    const result: UpdateUserProfileResult = await instance.updateUserProfile(updateUserCommand);

    // Then the values are changed to the expected
    expect(userRepositoryMock.byId).toHaveBeenCalledWith(updateUserCommand.userId);
    expect(result.username).toEqual(updateUserCommand.username);
    expect(result.firstName).toEqual(updateUserCommand.firstName);
    expect(result.lastName).toEqual(updateUserCommand.lastName);

  });

  test('fails to update user profile data if user is NOT active', async () => {

    // Given an update profile request for a non-active user
    const updateUserCommand = anUpdateUserCommand();
    const persistedUser = aPersistedUser();
    persistedUser.status = UserStatus.PENDING_VERIFICATION;
    
    userRepositoryMock.byId = jest.fn((userId: string) => Promise.resolve(persistedUser));
    userRepositoryMock.updateUserData = jest.fn((userData: User) => Promise.resolve({}));
    instance = new FirebaseUserService(mockFirebaseConfig, userRepositoryMock, emailServiceMock);
    
    // When updating user data
    // Then an error is thrown
    await expect(instance.updateUserProfile(updateUserCommand)).rejects.toThrow(UserInInvalidStateError);
  });
});

const aPersistedUser = (): User => {
  return {
    userId: "anUserId",
    username: "anUsername",
    email: "testUser@test.com",
    password: "testPassword",
    firstName: "aFirstName",
    lastName: "aLastName",
    status: "ACTIVE"
  } as User;
}

const anUpdateUserCommand = (): UpdateUserProfileCommand => {
  return {
    userId: "anUserId",
    username: "newUsername",
    firstName: "newFirstname",
    lastName: "newLastname"
  } as UpdateUserProfileCommand;
}

const aUserCommand = () => {
  return {
    username: "testUsername",
    email: "testUser@test.com",
    password: "testPassword",
    repeatedPassword: "testPassword",
    firstName: "aFirstName",
    lastName: "aLastName"
  };
}

const setupFirebaseMock = (createUserWithEmailAndPasswordMock: jest.Mock,
  updateProfileMock: jest.Mock,
  sendEmailVerificationMock: jest.Mock): FirebaseConfig => {
  let mockFirebaseConfig = new FirebaseConfig();
  mockFirebaseConfig.get = () => {
    return {
      auth: () => {
        return {
          createUserWithEmailAndPassword: createUserWithEmailAndPasswordMock,
          currentUser: {
            updateProfile: updateProfileMock,
            sendEmailVerification: sendEmailVerificationMock
          }
        }
      }
    }
  }

  return mockFirebaseConfig;
}

const setupFirebaseCreateUserEmailMock = (userId: string) => jest.fn((email: string, password: string) => Promise.resolve({ user: { uid: userId } }));
