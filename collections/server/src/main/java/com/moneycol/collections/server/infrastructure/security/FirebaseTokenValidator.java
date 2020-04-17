package com.moneycol.collections.server.infrastructure.security;

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

    @Override
    public Publisher<Authentication> validateToken(String token) {
        try {

            FirebaseAuth firebaseAuth = FirebaseHelper.initializeFirebaseAuth();
            FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);

            return Flowable.just(new FirebaseAuthentication(firebaseToken));
        } catch (FirebaseAuthException | IOException ex) {
            return Flowable.empty();
        }
    }
}