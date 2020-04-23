package com.moneycol.collections.server.infrastructure.api;

import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.moneycol.collections.server.infrastructure.security.FirebaseHelper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.annotation.Secured;
import io.reactivex.Flowable;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller("/accessToken")
@Secured("isAnonymous()")
public class TokenController {

    private static final String DEFAULT_FIREBASE_APP_NAME = "[DEFAULT]";

    @Client("${authentication.firebase.signInWithCustomTokenEndpoint}")
    @Inject
    RxHttpClient client;

    @Get
    public Single<Map> getAccessToken(@Nullable @QueryValue("userId") final String userId,
                               @QueryValue("apiKey") final String apiKey,
                               @Nullable @QueryValue("email") final String email) {
        log.info("Getting token for Test User ID: {}, {}, {}", userId, email, apiKey.hashCode());

        try {

            FirebaseAuth firebaseAuth = FirebaseHelper.initializeFirebaseAuth();
            String uid = "";

            if (email != null) {
                UserRecord userRecord = firebaseAuth.getUserByEmail(email);
                log.info("User for email: {}", userRecord.getUid());
                uid = userRecord.getUid();
            } else if (userId != null) {
                uid = userId;
            }

            String token = firebaseAuth.createCustomToken(uid);
            log.info("Custom token: {}", token);
            return signInWithCustomToken(token, apiKey).map(idToken -> ImmutableMap.of("token", idToken));

        } catch (FirebaseAuthException | IOException e) {
            log.error("Error generating token for test user: {}, {}", userId, email, e);
            return Single.just(ImmutableMap.of("error", "Error generating token for testUser"));
        }
    }

    private Single<String> signInWithCustomToken(String customToken, String apikey) {
        Map<String, Object> payload = ImmutableMap.of("token", customToken, "returnSecureToken", true);
        MutableHttpRequest<?> request = HttpRequest.POST("?key=" + apikey, payload);

        Flowable<HttpResponse<Map>> response = client.exchange(request, Map.class);
        return response
                .firstOrError()
                .doAfterSuccess(r -> log.info("Success exchanging custom token: {}", r.body()))
                .doOnError(error -> log.error("Error exchanging custom token", error))
                .map(resp -> resp.body().get("idToken").toString());
    }
}
