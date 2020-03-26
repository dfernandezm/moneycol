package com.moneycol.collections.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.moneycol.collections.server.application.AddItemsDTO;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionDTO;
import com.moneycol.collections.server.application.CollectionItemDTO;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionItem;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.micronaut.http.HttpRequest.POST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

//https://mfarache.github.io/mfarache/Building-microservices-Micronoaut/
@MicronautTest(environments = "test")
public class CollectionsControllerTest {

    @Inject
    @Client("/")
    private RxHttpClient client;

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
        CollectionDTO collectionDTO = new CollectionDTO("", collectionName, collectionDescription, collectorId, new ArrayList<>());

        HttpRequest<CollectionDTO> aRequest = POST("/collections", collectionDTO).contentType(MediaType.APPLICATION_JSON);
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
        delayMilliseconds(500);
        // When
        HttpRequest<?> findByCollectorReq = HttpRequest.GET("/collections/collector/" + collectorId);
        HttpResponse<List<CollectionDTO>> foundCollectionsResp = client.toBlocking().exchange(findByCollectorReq, Argument.listOf(CollectionDTO.class));

        // Then
        assertEquals(foundCollectionsResp.getStatus(), HttpStatus.OK, "Status code is not OK");
        assertNotNull(foundCollectionsResp.body(), "Body of response is null");
        assertCollectionReturnedForCollector(foundCollectionsResp.body(), 1, collectorId);
    }

    @Test
    void testUpdateCollectionAttributes() {

        // Given: existing collection
        String collectorId = "aCollectorId";
        String aName = "aCollectionName";
        String aDescription = "aDescription";
        String collectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId, aName, aDescription, collectorId);

        delayMilliseconds(500);
        // When: updating its name/description

        String newName = "newName";
        String newDescription = "newDescription";
        CollectionDTO collectionDTO =
                new CollectionDTO(collectionId, newName, newDescription, collectorId, new ArrayList<>());

        HttpRequest<CollectionDTO> updateCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, collectionDTO);
        HttpResponse<CollectionCreatedResult> collectionCreatedResp =
                client.toBlocking().exchange(updateCollectionEndpoint, Argument.of(CollectionCreatedResult.class));

        assertEquals(collectionCreatedResp.getStatus(), HttpStatus.OK);
        assertTrue(collectionCreatedResp.getBody().isPresent());
        assertThat(collectionCreatedResp.getBody().get().getName(), is(newName));
        assertThat(collectionCreatedResp.getBody().get().getDescription(), is(newDescription));
    }

    @Test
    void testGetCollectionById() {

        // Given: existing collection
        String collectorId = "aCollectorId";
        String aName = "aCollectionName";
        String aDescription = "aDescription";
        String collectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId, aName, aDescription, collectorId);
        delaySecond(1);
        // When: getting it by Id
        HttpRequest<CollectionDTO> getCollectionByIdEndpoint =
                HttpRequest.GET("/collections/" + collectionId);
        HttpResponse<CollectionDTO> collectionCreatedResp =
                client.toBlocking().exchange(getCollectionByIdEndpoint, Argument.of(CollectionDTO.class));

        assertEquals(collectionCreatedResp.getStatus(), HttpStatus.OK);
        assertTrue(collectionCreatedResp.getBody().isPresent());
        assertThat(collectionCreatedResp.getBody().get().getId(), is(collectionId));
        assertThat(collectionCreatedResp.getBody().get().getName(), is(aName));
        assertThat(collectionCreatedResp.getBody().get().getDescription(), is(aDescription));

        //TODO: missing test for collection with Items
    }

    void delaySecond(int second) {
       delayMilliseconds(second * 1000);
    }

    void delayMilliseconds(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * This test proves that Micronaut won't throw an exception when there is an error around the HTTPClient
     * which is the default behaviour. Instead we prefer checking the status code, especially for 404 and similar
     *
     * In order to enable this, the following needs to be added to application.yml:
     *
     * micronaut:
     *   http:
     *     client:
     *       exception-on-error-status: false
     *
     * Compatible responses are detailed here:
     * https://github.com/micronaut-projects/micronaut-core/pull/2372#issuecomment-569520454
     */
    @Test
    void testGetCollectionByIdGives404() {

        // Given: existing collection
        String collectionId = CollectionId.randomId();

        // When: getting it by Id
        HttpRequest<?> getCollectionByIdEndpoint =
                HttpRequest.GET("/collections/" + collectionId);
        HttpResponse<JsonNode> collectionCreatedResp =
                client.toBlocking().exchange(getCollectionByIdEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        assertEquals(collectionCreatedResp.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateCollectionGives404() {

        // Given: an inexistent ID
        String collectionId = CollectionId.randomId();
        CollectionDTO cdto = new CollectionDTO("","","", "", new ArrayList<>());

        // When: updating a collection with it
        HttpRequest<?> updatCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, cdto);
        HttpResponse<JsonNode> collectionUpdatedResp =
                client.toBlocking().exchange(updatCollectionEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        // Then: Not found
        assertEquals(collectionUpdatedResp.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteCollectionGives404() {

        // Given: an inexistent ID
        String collectionId = CollectionId.randomId();

        // When: delete a collection with it
        HttpRequest<?> deleteCollectionEndpoint =
                HttpRequest.DELETE("/collections/" + collectionId);
        HttpResponse<JsonNode> deleteCollectionResp =
                client.toBlocking().exchange(deleteCollectionEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        // Then: Not found
        assertEquals(deleteCollectionResp.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testAddItemsToCollectionGives404() {

        // Given: an inexistent ID
        String collectionId = CollectionId.randomId();
        AddItemsDTO addItemsDTO = new AddItemsDTO(new ArrayList<>());

        // When: adding items to a collection with it
        HttpRequest<?> addItemsEndpoint =
                HttpRequest.POST("/collections/" + collectionId + "/items", addItemsDTO);
        HttpResponse<JsonNode> addItemsEndpointResp =
                client.toBlocking().exchange(addItemsEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        // Then: Not found
        assertEquals(addItemsEndpointResp.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testAddItemsToCollection() {
        // Given: a collection exists
        String aCollectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(aCollectionId, "aCollection", "desc", "colId");
        delayMilliseconds(500);

        List<CollectionItemDTO> items = new ArrayList<>();
        items.add(new CollectionItemDTO("itemId1"));
        items.add(new CollectionItemDTO("itemId2"));
        AddItemsDTO addItemsDTO = new AddItemsDTO(items);

        // When: adding items to the collection
        HttpRequest<AddItemsDTO> addItemsToCollectionEndpoint =
                HttpRequest.POST("/collections/" + aCollectionId + "/items", addItemsDTO);
        HttpResponse<?> addItemsToCollectionResponse =
                client.toBlocking().exchange(addItemsToCollectionEndpoint);

        // Then: result is ok, and collection has the added items
        assertEquals(addItemsToCollectionResponse.getStatus(), HttpStatus.OK);
        assertCollectionHasItems(aCollectionId, items.get(0).getItemId(), items.get(1).getItemId());
    }

    @Test
    void testRemoveCollectionExisting() {

        // Given: an existing collection
        String aCollectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(aCollectionId,
                "aCollection",
                "desc",
                "colId");

        delayMilliseconds(500);

        // When: it's deleted
        String endpoint = "/collections/" + aCollectionId;
        HttpRequest<?> removeItemEndpoint = HttpRequest.DELETE(endpoint);
        HttpResponse<CollectionCreatedResult> removeItemResponse = client.toBlocking().exchange(removeItemEndpoint);

        // Then: status is ok
        assertThat(removeItemResponse.status(), is(HttpStatus.OK));

        // And: trying to find a collection with that Id
        Executable s = () -> FirebaseUtil.findCollectionById(aCollectionId);
        Exception e = assertThrows(RuntimeException.class, s);
        assertThat(e.getMessage(), containsString("not found"));
    }

    @Test
    void testRemoveItemFromCollectionExisting() {
        // Given: an existing collections with items collection

        String aCollectionId = CollectionId.randomId();
        CollectionItem item1 = CollectionItem.of("item1");
        CollectionItem item2 = CollectionItem.of("item2");
        List<CollectionItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        FirebaseUtil.createCollectionWithItems(aCollectionId,
                "aCollection",
                "desc",
                "colId", items);

        String endpoint = "/collections/" + aCollectionId +  "/items/" + item2.getItemId();
        HttpRequest<?> removeItemEndpoint = HttpRequest.DELETE(endpoint);
        HttpResponse<CollectionCreatedResult> removeItemResponse =
                client.toBlocking().exchange(removeItemEndpoint);

        List<String> itemIds = FirebaseUtil.findItemsForCollection(aCollectionId);
        assertThat(removeItemResponse.getStatus(), is(HttpStatus.OK));
        assertThat(itemIds, hasSize(1));
        assertThat(itemIds.contains("item1"), is(true));
        assertThat(itemIds.contains("item2"), is(false));
    }

    @Test
    void testCollectionWithNoNameReturns400() {
        CollectionDTO cdto = new CollectionDTO("","","", "", new ArrayList<>());
        String endpoint = "/collections";
        HttpRequest<CollectionDTO> createCollectionEndpoint = HttpRequest.POST(endpoint, cdto);

        HttpResponse<JsonNode> createCollectionResp =
                client.toBlocking().exchange(createCollectionEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        assertThat(createCollectionResp.getStatus(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void shouldJustUpdateCollectionAttributesLeavingItemsUntouched() {

        // Given: existing collection
        String collectorId = "aCollectorId";
        String aName = "aCollectionName";
        String aDescription = "aDescription";
        String collectionId = CollectionId.randomId();
        CollectionItem item1 = CollectionItem.of("item1");
        CollectionItem item2 = CollectionItem.of("item2");
        List<CollectionItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        FirebaseUtil.createCollectionWithItems(collectionId, aName, aDescription, collectorId, items);

        delayMilliseconds(500);
        // When: updating its name/description

        String newName = "newName";
        String newDescription = "newDescription";
        CollectionDTO collectionDTO =
                new CollectionDTO(collectionId, newName, newDescription, collectorId, new ArrayList<>());

        HttpRequest<CollectionDTO> updateCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, collectionDTO);
        HttpResponse<CollectionCreatedResult> collectionCreatedResp =
                client.toBlocking().exchange(updateCollectionEndpoint, Argument.of(CollectionCreatedResult.class));

        assertEquals(collectionCreatedResp.getStatus(), HttpStatus.OK);
        assertTrue(collectionCreatedResp.getBody().isPresent());
        assertThat(collectionCreatedResp.getBody().get().getName(), is(newName));
        assertThat(collectionCreatedResp.getBody().get().getDescription(), is(newDescription));
        assertCollectionHasItems(collectionId, items.get(0).getItemId(), items.get(1).getItemId());
    }

    @Test
    public void shouldAllowChangingNameOnlyIfThereIsntAnotherWithSameTest() {
        //TODO: existsWithName: check if any other collection has the name taken
        // updating: check if any other collection different than this one has the new name
    }

    private void assertCollectionHasItems(String collectionId, String... expectedItemIds) {
        List<String> itemIds = FirebaseUtil.findItemsForCollection(collectionId);
        assertThat(itemIds, Matchers.contains(expectedItemIds));
    }

    private void assertCollectionReturnedForCollector(List<CollectionDTO> collectionDTOs,  int expectedSize, String collectorId) {
        assertEquals(collectionDTOs.size(), expectedSize);
        assertEquals(collectionDTOs.get(0).getCollectorId(), collectorId);
    }
}
