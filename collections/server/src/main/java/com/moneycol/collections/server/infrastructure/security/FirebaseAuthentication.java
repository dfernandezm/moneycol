package com.moneycol.collections.server.infrastructure.security;

import com.google.firebase.auth.FirebaseToken;
import io.micronaut.security.authentication.Authentication;

import java.util.Collections;
import java.util.Map;

public class FirebaseAuthentication implements Authentication {

    private FirebaseToken firebaseToken;

    FirebaseAuthentication(FirebaseToken firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(firebaseToken.getClaims());
    }

    //This is the user Id in Firebase
    @Override
    public String getName() {
        return firebaseToken.getUid();
    }

    public String getEmail() {
        return firebaseToken.getEmail();
    }
}
