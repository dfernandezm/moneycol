import { FirebaseUserService } from '../../src/infrastructure/users/firebase/FirebaseUserService'
import { UserService, CreateUserCommand, UserRepository, User, UserStatus, EmailService } from '../../src/infrastructure/users/UserService'
import { FirebaseConfig } from "../../src/infrastructure/authentication/firebase/FirebaseConfiguration";
import InvalidValueError from '../../src/infrastructure/users/InvalidValueError';
import FirebaseEmailService from '../../src/infrastructure/users/firebase/FirebaseEmailService';



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
      updateUserData: updateUserDataMock
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

  ["password", "repeatedPassword", "username", "email"].map( field => {

    test(`fails if ${field} field not present`, async () => {
      const createUserCommand: CreateUserCommand = aUserCommand();
      const mutableCreateUserCommand = Object.assign({}, createUserCommand)
      mutableCreateUserCommand[field] = "";
      
      instance = new FirebaseUserService(mockFirebaseConfig, userRepositoryMock, emailServiceMock);
      await expect(instance.signUpWithEmail(mutableCreateUserCommand)).rejects.toThrow(InvalidValueError);
    });

  })

  
});

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
