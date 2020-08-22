import { FirebaseConfig } from './FirebaseConfiguration';
import EmailVerificationError from '../EmailVerificationError';
import { EmailService, EmailVerificationResult } from '../UserService';

export default class FirebaseEmailService implements EmailService {

    private firebaseInstance: FirebaseConfig;
    private BASE_DOMAIN = process.env.BASE_DOMAIN || "http://localhost:3000";

    constructor(firebaseInstance: FirebaseConfig) {
        this.firebaseInstance = firebaseInstance;
    }

    /**
     * Verification of email as a response to a handler in client side that contains a generated Firebase actionCode.
     * 
     * The UI displays a confirmation message to the user. You could also provide the user with a link back to the app (continueUrl).
     * If a continue URL is available, the UI should display a button which on click redirects the user back to the app via continueUrl with
     * additional state determined from that URL's parameters.
     * 
     * @param actionCode the Firebase Auth generated actionCode, appended to the actionURL present in the verification email sent to the user
     * @param continueUrl optional, the url to get back to a different app screen once the verification is complete
     * @param lang optional, the configured language of the email template in Firebase Auth console
     * 
     */
    async verifyEmail(actionCode: string, continueUrl?: string, lang?: string): Promise<EmailVerificationResult> {
        
        const auth = this.firebaseInstance.get().auth();

        try {

            console.log(`About to verify email: ${actionCode}, ${continueUrl}, ${lang}`);
            const checkResponse = await auth.checkActionCode(actionCode);
            console.log("Check response", checkResponse);
            const email = checkResponse.data.email as string;
            const response = await auth.applyActionCode(actionCode);
            console.log("Email successfully verified", response);

            return {
                email: email,
                result: "ok"
            };
        } catch (err) {
            // Code is invalid or expired. Ask the user to verify their email address again.
            console.log("Error verifying email, invalid parameters in handler", err);
            throw new EmailVerificationError("Email verification error: " + err.message);
        }
    }

    /**
     * Generates the link that is included as 'continueUrl' in the email sent to new users
     * or when an email needs to be verified
     * 
     * See: https://firebase.google.com/docs/auth/web/passing-state-in-email-actions
     */
    generateComebackUrl(email: string): string {
        return `${this.BASE_DOMAIN}/login?email=${email}`;
    }
}