package com.moneycol.collections.server.infrastructure.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

public class FirebaseHelper {
    private static final String FIREBASE_PROJECT_ID = "moneycol";

    public static FirebaseAuth initializeFirebaseAuth() throws IOException {
        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(FIREBASE_PROJECT_ID)
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
