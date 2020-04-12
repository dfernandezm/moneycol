package com.moneycol.collections.server.infrastructure.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import io.micronaut.context.annotation.Value;
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

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Controller("/accessToken")
@Secured("isAnonymous()")
public class TokenController {

    @Value("${authentication.firebase.signInWithCustomTokenEndpoint}")
    private String signInWithCustomTokenEndpoint;

    @Client("https://identitytoolkit.googleapis.com/v1")
    @Inject
    RxHttpClient client;

    @Get
    Single<Map> getAccessToken(@QueryValue("userId") String userId, @QueryValue("apiKey") String apiKey) {
        log.info("Getting token for Test User ID: {}, {}", userId, apiKey.hashCode());

        try {
            FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId("moneycol")
                    .build();

            FirebaseApp firebaseApp = FirebaseApp.initializeApp(firebaseOptions);
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
            String token = firebaseAuth.createCustomToken(userId);
            log.info("Custom token: {}", token);
            return signInWithCustomToken(token, apiKey).map(idToken -> ImmutableMap.of("token", idToken));

        } catch (FirebaseAuthException | IOException e) {
            //TODO: throw custom exception
            log.error("Error generating token for testUid: {}", userId, e);
            return Single.just(ImmutableMap.of("error", "Error generating token for testUser"));
        }
    }

    private Single<String> signInWithCustomToken(String customToken, String apikey) {
        signInWithCustomTokenEndpoint = "/accounts:signInWithCustomToken";
        String endpoint = signInWithCustomTokenEndpoint + "?key=" + apikey;
        Map<String, Object> payload = ImmutableMap.of("token", customToken, "returnSecureToken", true);
        MutableHttpRequest<?> request = HttpRequest.POST(endpoint, payload);
        Flowable<HttpResponse<Map>> response = client.exchange(request, Map.class);
        return response
                .firstOrError()
                .doAfterSuccess(r -> log.info("Success exchanging custom token: {}", r.body()))
                .doOnError(error -> log.error("Error exchanging custom token", error))
                .map(resp -> resp.body().get("idToken").toString());
    }
}
