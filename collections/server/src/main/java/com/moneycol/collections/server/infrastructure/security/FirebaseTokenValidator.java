package com.moneycol.collections.server.infrastructure.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.validator.TokenValidator;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class FirebaseTokenValidator implements TokenValidator {

    private static final String FIREBASE_PROJECT_ID = "moneycol";

    @Override
    public Publisher<Authentication> validateToken(String token) {
        try {

            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .build();

            FirebaseApp firebaseApp = FirebaseApp.initializeApp(firebaseOptions);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
            FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);

            return Flowable.just(new FirebaseAuthentication(firebaseToken));
        } catch (FirebaseAuthException | IOException ex) {
            return Flowable.empty();
        }
    }
}