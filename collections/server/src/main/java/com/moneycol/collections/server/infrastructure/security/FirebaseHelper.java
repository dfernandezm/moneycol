package com.moneycol.collections.server.infrastructure.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

/**
 * Helper to setup Firebase Auth access using Google Application Credentials
 *
 */
public class FirebaseHelper {

    private static final String FIREBASE_PROJECT_ID = "moneycol";
    private static final String FIREBASE_DB_URL_TEMPLATE = "https://%s.firebaseio.com/";

    /**
     * Initialize Firebase Auth with default Google Credentials, which are normally picked
     * from GOOGLE_APPLICATION_CREDENTIALS env var.
     *
     * Ensure the Service Account has signBlob and signJwt permission apart from Firebase Auth
     * Admin rights. This way custom tokens can be generated.
     *
     * @return the Firebase Auth Admin instance initialised
     * @throws IOException
     */
    public static FirebaseAuth initializeFirebaseAuth() throws IOException {

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(FIREBASE_PROJECT_ID)
                .setDatabaseUrl(String.format(FIREBASE_DB_URL_TEMPLATE, FIREBASE_PROJECT_ID))
                .build();

        boolean hasBeenInitialized = FirebaseApp.getApps()
                .stream()
                .anyMatch(app -> app.getName().equals(FirebaseApp.DEFAULT_APP_NAME));

        FirebaseApp firebaseApp;

        if (hasBeenInitialized) {
            firebaseApp = FirebaseApp.getInstance(FirebaseApp.DEFAULT_APP_NAME);
        } else {
            firebaseApp = FirebaseApp.initializeApp(firebaseOptions, FirebaseApp.DEFAULT_APP_NAME);
        }

        return FirebaseAuth.getInstance(firebaseApp);
    }
}
