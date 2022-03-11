package com.moneycol.collections.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.moneycol.collections.server.domain.CollectionId;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.rxjava2.http.client.RxHttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

//https://guides.micronaut.io/micronaut-token-propagation/guide/index.html
@MicronautTest(environments = "test")
public class CollectionsControllerSecuredTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Value("${testUser.email}")
    String testUserEmail;

    private static final String FAKE_TOKEN = "fakeToken";
    private static final String FAKE_UID = "fakeUid";

    @Test
    public void securityIsEnabled() {
        ApplicationContext applicationContext = ApplicationContext.run( "test");
        Map<String, Object> map  = applicationContext.getProperties("micronaut.security");
        assertNotNull(map);
        assertEquals(map.get("enabled"), "true");
        assertEquals(map.get("token.jwt.enabled"), "true");
    }

    @Test
    public void testCollectionsByIdEndpointIsSecured() {

        String collectionsId = CollectionId.randomId();
        String collectionsByIdEndpoint = "/collections/" + collectionsId;
        HttpRequest<?> getRequestWithBearerToken = HttpRequest.GET(collectionsByIdEndpoint).bearerAuth(FAKE_TOKEN);

        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () ->
                client.toBlocking().exchange(getRequestWithBearerToken)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getResponse().getStatus());
    }

    @Test
    public void canCallStatusEndointWithoutSecurity() {
        String statusEndpoint = "/_status";
        HttpRequest<?> getStatus = HttpRequest.GET(statusEndpoint);
        HttpResponse<?> response = client.toBlocking().exchange(getStatus, Argument.of(JsonNode.class), Argument.of(JsonNode.class));
        assertNotEquals(HttpStatus.OK, response);
    }

//    @Replaces(FirebaseTokenValidator.class)
//    @Singleton
//    public static class FakeFirebaseTokenValidator extends FirebaseTokenValidator {
//        @Override
//        public Publisher<Authentication> validateToken(String token) {
//            assertEquals(token, FAKE_TOKEN);
//            return Flowable.just(new Authentication() {
//                @Nonnull
//                @Override
//                public Map<String, Object> getAttributes() {
//                    return new HashMap<>();
//                }
//
//                @Override
//                public String getName() {
//                    return FAKE_UID;
//                }
//            });
//        }
//    }
}
