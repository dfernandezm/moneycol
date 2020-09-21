package com.moneycol.collections.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionItem;
import com.moneycol.collections.server.infrastructure.api.dto.AddItemsDTO;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionDTO;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDTO;
import com.moneycol.collections.server.infrastructure.api.dto.UpdateCollectionDataDTO;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
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
import java.util.Map;
import java.util.Optional;
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
import static org.junit.jupiter.api.Assertions.fail;

//https://mfarache.github.io/mfarache/Building-microservices-Micronoaut/
@MicronautTest(environments = "test")
public class CollectionsControllerTest {

    @Inject
    @Client("/")
    private RxHttpClient client;

    private static String accessToken  =  null;

    @Value("${testUser.id}")
    private String testCollectorId;

    @BeforeEach
    public void setup() {
        FirebaseUtil.init();
    }

    @AfterEach
    public void deleteAllCollections() {
        FirebaseUtil.deleteAllCollections();
    }

    @ParameterizedTest
    @CsvSource({"\"A banknote collection\",\"All the banknotes in London\""})
    void testCollectionCreation(String collectionName, String collectionDescription) {
        CollectionDTO collectionDTO = CollectionDTO.builder()
                                        .id("")
                                        .name(collectionName)
                                        .description(collectionDescription)
                                        .items(new ArrayList<>())
                                        .build();

        MutableHttpRequest<CollectionDTO> aRequest = POST("/collections", collectionDTO).contentType(MediaType.APPLICATION_JSON);
        aRequest.bearerAuth(accessToken);
        HttpResponse<CollectionCreatedResult> collectionCreatedResult = client.toBlocking().exchange(aRequest, Argument.of(CollectionCreatedResult.class));

        assertEquals(collectionCreatedResult.getStatus(), HttpStatus.OK, "Status code is not OK");
        assertNotNull(collectionCreatedResult.body(), "Body of response is null");
        assertEquals(collectionCreatedResult.body().getName(), collectionName, "Collection name is not valid");
        assertEquals(collectionCreatedResult.body().getDescription(), collectionDescription, "Collection ID is not valid");
    }

    @Test
    void testUpdateCollectionAttributes() {

        // Given: an existing collection
        String collectorId = testCollectorId;
        String aName = "aCollectionName";
        String aDescription = "aDescription";
        String collectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId, aName, aDescription, collectorId);

        delayMilliseconds(500);

        // When: updating its name/description
        String newName = "newName";
        String newDescription = "newDescription";
        CollectionDTO collectionDTO = CollectionDTO.builder()
                                        .id(collectionId)
                                        .name(newName)
                                        .description(newDescription)
                                        .items(new ArrayList<>())
                                        .build();

        MutableHttpRequest<CollectionDTO> updateCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, collectionDTO);
        updateCollectionEndpoint.bearerAuth(accessToken);

        HttpResponse<CollectionCreatedResult> collectionCreatedResp =
                client.toBlocking().exchange(updateCollectionEndpoint, Argument.of(CollectionCreatedResult.class));

        // Then: name/description should be the new values
        assertEquals(collectionCreatedResp.getStatus(), HttpStatus.OK);
        assertTrue(collectionCreatedResp.getBody().isPresent());
        assertThat(collectionCreatedResp.getBody().get().getName(), is(newName));
        assertThat(collectionCreatedResp.getBody().get().getDescription(), is(newDescription));
    }

    @Test
    void testGetCollectionById() {

        // Given: existing collection
        String collectorId = testCollectorId;
        String aName = "aCollectionName";
        String aDescription = "aDescription";
        String collectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId, aName, aDescription, collectorId);
        delayMilliseconds(600);

        // When: getting it by Id
        MutableHttpRequest<CollectionDTO> getCollectionByIdEndpoint =
                HttpRequest.GET("/collections/" + collectionId);
        getCollectionByIdEndpoint.bearerAuth(accessToken);

        HttpResponse<CollectionDTO> collectionCreatedResp =
                client.toBlocking().exchange(getCollectionByIdEndpoint, Argument.of(CollectionDTO.class));

        // Then: the result is the expected
        assertEquals(collectionCreatedResp.getStatus(), HttpStatus.OK);
        assertTrue(collectionCreatedResp.getBody().isPresent());
        assertThat(collectionCreatedResp.getBody().get().getId(), is(collectionId));
        assertThat(collectionCreatedResp.getBody().get().getName(), is(aName));
        assertThat(collectionCreatedResp.getBody().get().getDescription(), is(aDescription));
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

        // Given: non existing collection ID
        String collectionId = CollectionId.randomId();

        // When: getting by Id using it
        MutableHttpRequest<?> getCollectionByIdEndpoint =
                HttpRequest.GET("/collections/" + collectionId);
        getCollectionByIdEndpoint.bearerAuth(accessToken);

        HttpResponse<JsonNode> getCollectionByIdResponse =
                client.toBlocking().exchange(getCollectionByIdEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        // Then: status code should be NOT FOUND
        assertEquals(getCollectionByIdResponse.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testUpdateCollectionGives404() {

        // Given: an inexistent ID
        String collectionId = CollectionId.randomId();
        CollectionDTO cdto = CollectionDTO.builder()
                                .id("")
                                .name("")
                                .description("")
                                .items(new ArrayList<>())
                                .build();

        // When: updating a collection with it
        MutableHttpRequest<?> updateCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, cdto);
        updateCollectionEndpoint.bearerAuth(accessToken);

        HttpResponse<JsonNode> collectionUpdatedResp =
                client.toBlocking().exchange(updateCollectionEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        // Then: Not found
        assertEquals(collectionUpdatedResp.getStatus(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteCollectionGives404() {

        // Given: an inexistent ID
        String collectionId = CollectionId.randomId();

        // When: deleting a collection with it
        MutableHttpRequest<?> deleteCollectionEndpoint =
                HttpRequest.DELETE("/collections/" + collectionId);
        deleteCollectionEndpoint.bearerAuth(accessToken);

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
        MutableHttpRequest<?> addItemsEndpoint =
                HttpRequest.POST("/collections/" + collectionId + "/items", addItemsDTO);
        addItemsEndpoint.bearerAuth(accessToken);

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
        String collectorId = testCollectorId;
        FirebaseUtil.createCollection(aCollectionId, "aCollection", "desc", collectorId);
        delayMilliseconds(500);

        List<CollectionItemDTO> items = new ArrayList<>();
        items.add(new CollectionItemDTO("itemId1"));
        items.add(new CollectionItemDTO("itemId2"));
        AddItemsDTO addItemsDTO = new AddItemsDTO(items);

        // When: adding items to the collection
        MutableHttpRequest<AddItemsDTO> addItemsToCollectionEndpoint =
                HttpRequest.POST("/collections/" + aCollectionId + "/items", addItemsDTO);
        addItemsToCollectionEndpoint.bearerAuth(accessToken);

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
        String collectorId = testCollectorId;
        FirebaseUtil.createCollection(aCollectionId,
                "aCollection",
                "desc",
                collectorId);

        delayMilliseconds(500);

        // When: it's deleted
        String endpoint = "/collections/" + aCollectionId;
        MutableHttpRequest<?> removeItemEndpoint = HttpRequest.DELETE(endpoint);
        removeItemEndpoint.bearerAuth(accessToken);
        HttpResponse removeItemResponse = client.toBlocking().exchange(removeItemEndpoint);

        // Then: status is ok
        assertThat(removeItemResponse.status(), is(HttpStatus.OK));

        // And: trying to find a collection with that Id
        Executable s = () -> FirebaseUtil.findCollectionById(aCollectionId);
        Exception e = assertThrows(RuntimeException.class, s);
        assertThat(e.getMessage(), containsString("not found"));
    }

    @Test
    void testRemoveItemFromCollectionExisting() {

        // Given: an existing collections with 2 items
        String aCollectionId = CollectionId.randomId();
        String collectorId = testCollectorId;
        CollectionItem item1 = CollectionItem.of("item1");
        CollectionItem item2 = CollectionItem.of("item2");
        List<CollectionItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        FirebaseUtil.createCollectionWithItems(aCollectionId,
                "aCollection",
                "desc",
                collectorId, items);

        // When: deleting one of the items
        String endpoint = "/collections/" + aCollectionId +  "/items/" + item2.getItemId();
        MutableHttpRequest<?> removeItemEndpoint = HttpRequest.DELETE(endpoint);
        removeItemEndpoint.bearerAuth(accessToken);
        HttpResponse<CollectionCreatedResult> removeItemResponse =
                client.toBlocking().exchange(removeItemEndpoint);

        // Then: the deleted item isn't present and the other is
        List<String> itemIds = FirebaseUtil.findItemsForCollection(aCollectionId);
        assertThat(removeItemResponse.getStatus(), is(HttpStatus.OK));
        assertThat(itemIds, hasSize(1));
        assertThat(itemIds.contains("item1"), is(true));
        assertThat(itemIds.contains("item2"), is(false));
    }

    @Test
    void testCollectionWithNoNameReturns400() {

        // Given: a collection with empty name
        CollectionDTO cdto = CollectionDTO.builder()
                                .id("")
                                .name("")
                                .description("")
                                .items(new ArrayList<>())
                                .build();

        // When: trying to create it
        String endpoint = "/collections";
        MutableHttpRequest<CollectionDTO> createCollectionEndpoint = HttpRequest.POST(endpoint, cdto);
        createCollectionEndpoint.bearerAuth(accessToken);

        HttpResponse<JsonNode> createCollectionResp =
                client.toBlocking().exchange(createCollectionEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        // Then: invalid request error is returned
        assertThat(createCollectionResp.getStatus(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void testJustUpdateCollectionDataLeavingItemsUntouched() {

        // Given: existing collection with items
        String collectorId = testCollectorId;
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

        // When: updating the name/description
        String newName = "newName";
        String newDescription = "newDescription";
        CollectionDTO collectionDTO =
                CollectionDTO.builder()
                        .id(collectionId)
                        .name(newName)
                        .description(newDescription)
                        .items(new ArrayList<>())
                        .build();

        MutableHttpRequest<CollectionDTO> updateCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, collectionDTO);
        updateCollectionEndpoint.bearerAuth(accessToken);
        HttpResponse<CollectionCreatedResult> collectionCreatedResp =
                client.toBlocking().exchange(updateCollectionEndpoint, Argument.of(CollectionCreatedResult.class));

        // Then: name/description are changed and items are left untouched
        assertEquals(collectionCreatedResp.getStatus(), HttpStatus.OK);
        assertTrue(collectionCreatedResp.getBody().isPresent());
        assertThat(collectionCreatedResp.getBody().get().getName(), is(newName));
        assertThat(collectionCreatedResp.getBody().get().getDescription(), is(newDescription));
        assertCollectionHasItems(collectionId, items.get(0).getItemId(), items.get(1).getItemId());
    }

    @Test
    public void testNotAllowChangingNameIfThereIsAnotherWithSame() {

        // Given: 2 collections exist with different names
        String collectorId = testCollectorId;
        String aName = "aCollectionName";
        String aDescription = "aDescription";
        String collectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId, aName, aDescription, collectorId);

        String anotherCollectorId = testCollectorId;
        String anotherName = "anotherCollectionName";
        String anotherDescription = "anotherDescription";
        String anotherCollectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(anotherCollectionId, anotherName, anotherDescription, anotherCollectorId);

        delayMilliseconds(500);

        // When: updating one passing a name that is already in use by a different collection (different collectionId)
        String newName = anotherName;

        UpdateCollectionDataDTO updateCollectionDataDTO = UpdateCollectionDataDTO.builder()
                                                            .name(newName)
                                                            .description(aDescription)
                                                            .build();

        HttpRequest<UpdateCollectionDataDTO> updateCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, updateCollectionDataDTO)
                            .bearerAuth(accessToken);

        HttpResponse<JsonNode> collectionUpdatedResp =
                client.toBlocking().exchange(updateCollectionEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        // Then: bad request happens due to duplicated name
        assertThat(collectionUpdatedResp.getStatus(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testAllowChangingNameIfThereIsNotAnotherWithSame() {

        // Given: 2 collections exist with different names
        String collectorId = testCollectorId;
        String aName = "aCollectionName";
        String aDescription = "aDescription";
        String collectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId, aName, aDescription, collectorId);

        String anotherCollectorId = testCollectorId;
        String anotherName = "anotherCollectionName";
        String anotherDescription = "anotherDescription";
        String anotherCollectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(anotherCollectionId, anotherName, anotherDescription, anotherCollectorId);
        delayMilliseconds(600);

        // When: we update one with a name that is already in use by a different collection (different collectionId)
        String newName = "aThirdName";

        UpdateCollectionDataDTO updateCollectionDataDTO = UpdateCollectionDataDTO.builder()
                                                            .name(newName)
                                                            .description(aDescription)
                                                            .build();

        HttpRequest<UpdateCollectionDataDTO> updateCollectionEndpoint =
                HttpRequest.PUT("/collections/" + collectionId, updateCollectionDataDTO)
                            .bearerAuth(accessToken);

        HttpResponse<CollectionCreatedResult> collectionUpdateResp =
                client.toBlocking().exchange(updateCollectionEndpoint, Argument.of(CollectionCreatedResult.class));

        // Then: the update of the name should be successful
        assertEquals(collectionUpdateResp.getStatus(), HttpStatus.OK);
        assertTrue(collectionUpdateResp.getBody().isPresent());
        assertThat(collectionUpdateResp.getBody().get().getName(), is(newName));
        assertThat(collectionUpdateResp.getBody().get().getDescription(), is(aDescription));
    }

    //TODO: this is not passing when running from IDEA but passes from Gradle
    @Test
    public void testForbiddenErrorIfNotCorrectOwner() {
        // Given: a collections exist with a collector
        String collectorId = "aCollectorId";
        String aName = "aCollectionName";
        String aDescription = "aDescription";
        String collectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId, aName, aDescription, collectorId);
        delayMilliseconds(500);

        // When: another collector tries to access it (access token is for another collectorId)
        MutableHttpRequest<CollectionDTO> getCollectionByIdEndpoint =
                HttpRequest.GET("/collections/" + collectionId);
        getCollectionByIdEndpoint.bearerAuth(accessToken);

        HttpResponse<JsonNode> collectionByIdResponse =
                client.toBlocking().exchange(getCollectionByIdEndpoint,
                        Argument.of(JsonNode.class),
                        Argument.of(JsonNode.class));

        // Then: access denied as 'accessToken' is for a different user
        assertThat(collectionByIdResponse.getStatus(), is(HttpStatus.FORBIDDEN));
    }

    private void assertCollectionHasItems(String collectionId, String... expectedItemIds) {
        List<String> itemIds = FirebaseUtil.findItemsForCollection(collectionId);
        assertThat(itemIds, Matchers.contains(expectedItemIds));
    }

    @BeforeEach
    public synchronized void obtainTokenForTestUser() {

        if (accessToken == null) {
            MutableHttpRequest<?> getTokenRequest =
                    HttpRequest.GET("/accessToken");

            //TODO: PUT API KEY
            getTokenRequest.getParameters()
                    .add("apiKey", "API-KEY-HERE")
                    .add("email", "moneycoltest1@mailinator.com");

            HttpResponse<Map> tokenResponse =
                    client.toBlocking().exchange(getTokenRequest, Map.class);

            Optional<Map> tokenBody = tokenResponse.getBody();

            tokenBody
                    .map(token -> accessToken = token.get("token").toString())
                    .orElseGet(() -> fail("Cannot get token for tests"));
        }

    }
}
