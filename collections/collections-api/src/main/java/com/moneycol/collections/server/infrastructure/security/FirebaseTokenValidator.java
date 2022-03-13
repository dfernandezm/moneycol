package com.moneycol.collections.server.infrastructure.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.validator.TokenValidator;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import jakarta.inject.Singleton;
import java.io.IOException;

/**
 * This is the hook into Micronaut Token validation, which allows a custom validation of Firebase Auth
 * generated tokens. Agnostic validation could be performed, but we know it's a Firebase Auth token.
 *
 * Also, caching should be added here as {@code verifyIdToken} in Firebase an expensive operation.
  */
@Slf4j
@Singleton
public class FirebaseTokenValidator implements TokenValidator {

    @Override
    public Publisher<Authentication> validateToken(String token, HttpRequest<?> request) {
        try {
            FirebaseAuth firebaseAuth = FirebaseHelper.initializeFirebaseAuth();
            FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);
            return Flowable.just(new FirebaseAuthentication(firebaseToken));
        } catch (FirebaseAuthException | IOException ex) {
            log.debug("Error validating token {} - sending empty authentication, error: {}", token, ex);
            return Flowable.empty();
        }
    }
}