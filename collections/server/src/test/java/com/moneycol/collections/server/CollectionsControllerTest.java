package com.moneycol.collections.server;

import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionDTO;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.base.Id;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;
import java.util.List;

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

    @BeforeEach
    public void setup() {
        FirebaseUtil.init();
    }

    @AfterEach
    public void deleteAllCollections() {
        FirebaseUtil.deleteAllCollections();
    }

    @ParameterizedTest
    @CsvSource({"\"A banknote collection\",\"All the banknotes in London\", \"collectorId1\""})
    void testCollectionCreation(String collectionName, String collectionDescription, String collectorId) {
        CollectionDTO collectionDTO = new CollectionDTO("", collectionName, collectionDescription, collectorId);

       // client.toBlocking(
        HttpRequest<CollectionDTO> aRequest = POST("/collections/create", collectionDTO).contentType(MediaType.APPLICATION_JSON);
        HttpResponse<CollectionCreatedResult> collectionCreatedResult = client.toBlocking().exchange(aRequest, Argument.of(CollectionCreatedResult.class));

        assertEquals(collectionCreatedResult.getStatus(), HttpStatus.OK, "Status code is not OK");
        assertNotNull(collectionCreatedResult.body(), "Body of response is null");
        assertEquals(collectionCreatedResult.body().getCollectorId(), collectorId, "CollectorID is not valid");
        assertEquals(collectionCreatedResult.body().getName(), collectionName, "Collection name is not valid");
        assertEquals(collectionCreatedResult.body().getDescription(), collectionDescription, "Collection ID is not valid");
    }

    @Test
    void testFindByCollector() {

        // Given
        String collectorId = "aCollectorId";
        FirebaseUtil.createCollection(CollectionId.randomId(), "aCollectionName", "aDescription", collectorId);

        // When
        HttpRequest<?> findByCollectorReq = HttpRequest.GET("/collections/collector/" + collectorId);
        HttpResponse<List<CollectionDTO>> foundCollectionsResp = client.toBlocking().exchange(findByCollectorReq, Argument.listOf(CollectionDTO.class));

        // Then
        assertEquals(foundCollectionsResp.getStatus(), HttpStatus.OK, "Status code is not OK");
        assertNotNull(foundCollectionsResp.body(), "Body of response is null");
        assertCollectionReturnedForCollector(foundCollectionsResp.body(), 1, collectorId);
    }

    @Test
    void testUpdateCollectionsEndpointCanBeInvoked() {
        String collectionId = Id.randomId();
        CollectionDTO collectionDTO =
                new CollectionDTO(collectionId, "aName", "aDesc", "aCollector");
        HttpRequest<CollectionDTO> updateCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, collectionDTO);
        HttpResponse<CollectionCreatedResult> collectionCreatedResp =
                client.toBlocking().exchange(updateCollectionEndpoint, Argument.of(CollectionCreatedResult.class));

        assertEquals(collectionCreatedResp.getStatus(), HttpStatus.OK);

    }



    private void assertCollectionReturnedForCollector(List<CollectionDTO> collectionDTOs,  int expectedSize, String collectorId) {
        assertEquals(collectionDTOs.size(), expectedSize);
        assertEquals(collectionDTOs.get(0).getCollectorId(), collectorId);
    }
}
