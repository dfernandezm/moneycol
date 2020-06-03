import { FirebaseConfig } from '../../authentication/firebase/FirebaseConfiguration';
import EmailVerificationError from '../EmailVerificationError';
import { EmailService } from '../UserService';

export default class FirebaseEmailService implements EmailService {

    private firebaseInstance: FirebaseConfig;
    private BASE_DOMAIN = "http://localhost:4000";

    constructor(firebaseInstance: FirebaseConfig) {
        this.firebaseInstance = firebaseInstance;
    }

    /**
     * Verification of a given email link
     */

    //TODO: in the UI component
    // Get the action to complete.
    //   var mode = getParameterByName('mode');
    //   // Get the one-time code from the query parameter.
    //   var actionCode = getParameterByName('oobCode');
    //   // (Optional) Get the continue URL from the query parameter if available.
    //   var continueUrl = getParameterByName('continueUrl');
    //   // (Optional) Get the language code if available.
    //   var lang = getParameterByName('lang') || 'en';
    async verifyEmail(actionCode: string, continueUrl: string, lang: string) {
        
        const auth = this.firebaseInstance.get().auth();

        try {

            console.log(`About to verify email: ${actionCode}, ${continueUrl}, ${lang}`);
            const response = await auth.applyActionCode(actionCode);
            console.log("Email successfully verified", response);

            // Email address has been verified.

            // TODO: Display a confirmation message to the user.
            // You could also provide the user with a link back to the app.

            // TODO: If a continue URL is available, display a button which on
            // click redirects the user back to the app via continueUrl with
            // additional state determined from that URL's parameters.
            return {
                result: "ok"
            };
        } catch (err) {
            // Code is invalid or expired. Ask the user to verify their email address
            // again.
            console.log("Error verifying email", err);
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