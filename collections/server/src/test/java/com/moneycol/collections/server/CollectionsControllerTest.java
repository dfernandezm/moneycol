package com.moneycol.collections.server;

import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionDTO;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;

import static io.micronaut.http.HttpRequest.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//https://mfarache.github.io/mfarache/Building-microservices-Micronoaut/
@MicronautTest(environments = "test")
public class CollectionsControllerTest {

    @Inject
    @Client("/")
    private RxHttpClient client;

// In-place replacement of inner dependency
//    @MockBean(CloudFirebaseProvider.class)
//    FirebaseProvider firebaseService() {
//        return new EmulatedFirebaseProvider();
//    }

    @ParameterizedTest
    @CsvSource({"\"A banknote collection\",\"All the banknotes in London\", \"collectorId1\""})
    void testCollectionCreation(String collectionName, String collectionDescription, String collectorId) {
        CollectionDTO collectionDTO = new CollectionDTO(collectionName, collectionDescription, collectorId);

       // client.toBlocking(
        HttpRequest<CollectionDTO> aRequest = POST("/collections/create", collectionDTO).contentType(MediaType.APPLICATION_JSON);
        HttpResponse<CollectionCreatedResult> collectionCreatedResult = client.toBlocking().exchange(aRequest, Argument.of(CollectionCreatedResult.class));

        assertEquals(collectionCreatedResult.getStatus(), HttpStatus.OK, "Status code is not OK");
        assertNotNull(collectionCreatedResult.body(), "Body of response is null");
        assertEquals(collectionCreatedResult.body().getCollectorId(), collectorId, "CollectorID is not valid");
        assertEquals(collectionCreatedResult.body().getName(), collectionName, "Collection name is not valid");
        assertEquals(collectionCreatedResult.body().getDescription(), collectionDescription, "Collection ID is not valid");

    }
}
