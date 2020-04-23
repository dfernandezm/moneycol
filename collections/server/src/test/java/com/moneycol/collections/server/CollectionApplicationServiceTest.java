package com.moneycol.collections.server;

import com.moneycol.collections.server.application.AddItemsToCollectionCommand;
import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CreateCollectionCommand;
import com.moneycol.collections.server.application.UpdateCollectionDataCommand;
import com.moneycol.collections.server.application.exception.DuplicateCollectionNameException;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionItem;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.InvalidCollectionException;
import com.moneycol.collections.server.domain.base.Id;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDTO;
import com.moneycol.collections.server.infrastructure.repository.CollectionNotFoundException;
import com.moneycol.collections.server.infrastructure.repository.EmulatedFirebaseProvider;
import com.moneycol.collections.server.infrastructure.repository.FirebaseCollectionRepository;
import com.moneycol.collections.server.infrastructure.repository.FirebaseProvider;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Use emulator support for Firebase:
 * Frontend folder has firebase.json, that needs to be around
 * firebase emulators:start
 *
 */

public class CollectionApplicationServiceTest {

    @BeforeEach
    public void setup() {
        FirebaseUtil.init();
    }

    @AfterEach
    public void cleanup() {
        FirebaseUtil.deleteAllCollections();
    }

    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\"",
                "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\""})
    public void shouldCreateBasicCollection(String name, String description) {

       CollectionRepository collectionRepo = mockRepository();

        // Given
        CreateCollectionCommand createCollectionCommand = CreateCollectionCommand.builder()
                                            .name(name)
                                            .description(description)
                                            .items(new ArrayList<>())
                                            .collectorId("aCollectorId")
                                            .build();

        CollectionApplicationService cas = new CollectionApplicationService(collectionRepo);

        // When
        CollectionCreatedResult collectionCreatedResult = cas.createCollection(createCollectionCommand);

        // Then
        verify(collectionRepo, times(1)).create(any());
        assertEquals(collectionCreatedResult.getName(), name);
        assertEquals(collectionCreatedResult.getDescription(), description);
        assertNotNull(collectionCreatedResult.getCollectionId());
    }


    @Test
    public void firebaseCollectionShouldntExist() {
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        collectionRepository.firebaseCollectionExistsSingle("collections").subscribe(e -> {
            System.out.print("Result: " + e);

        }, error -> {
            System.out.print("Error: " + error);
        });
    }

    //TODO: this test actually creates collections in firestore, so needs to mock the Firestore
    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\"",
            "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\""})
    public void testCreateFirebaseCollection(String name, String description) {

        FirebaseProvider f = new EmulatedFirebaseProvider();

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        String collectorId = UUID.randomUUID().toString();

        Collection col =  Collection.withNameAndDescription(CollectionId.of(Id.randomId()), name,
                description, Collector.of(CollectorId.of(collectorId)));

        collectionRepository.create(col);
        String id = col.id();

        Collection res = collectionRepository.byId(CollectionId.of(id));

        assertEquals(id, res.id());
    }

    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\",BanknotesUpdated",
            "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\",\"Banknotes only\""})
    public void updateCollectionTest(String name, String description, String newName) {

        FirebaseProvider f = new EmulatedFirebaseProvider();

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        String collectorId = UUID.randomUUID().toString();

        Collection col =  Collection.withNameAndDescription(CollectionId.of(Id.randomId()), name,
                description, Collector.of(CollectorId.of(collectorId)));

        collectionRepository.create(col);

        Collection toUpdate =  Collection.withNameAndDescription(CollectionId.of(col.id()), newName,
                description, Collector.of(CollectorId.of(collectorId)));

        Collection updated = collectionRepository.update(toUpdate);

        assertEquals(updated.name(), newName);
    }

    @Test
    public void findByCollectorWithMultipleTest() {
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);

        String name = "Banknotes";
        String description = "A collection for storing my banknotes in London";
        String collectorId = "collectorId1";

        Collection col1 = aCollection(CollectionId.randomId(), name, description, collectorId);
        collectionRepository.create(col1);

        name = "BanknotesName2";
        description = "A collection in London";

        // same collector Id
        Collection col2 = aCollection(CollectionId.randomId(), name, description, collectorId);
        collectionRepository.create(col2);

        List<Collection> collectionsForCollector =
                collectionRepository.byCollector(CollectorId.of(collectorId));

        assertEquals(collectionsForCollector.size(), 2);
        assertEquals(collectionsForCollector.get(0).collector().id(), collectorId);
        assertEquals(collectionsForCollector.get(1).collector().id(), collectorId);
    }

    @Test
    public void findByCollectorWithEmptySetTest() {
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);

        String inexistentCollectorId = "collectorId1";

        List<Collection> collectionsForCollector =
                collectionRepository.byCollector(CollectorId.of(inexistentCollectorId));

        assertTrue(collectionsForCollector.isEmpty());
    }

    @Test
    public void findByIdNotFound() {
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);

        String nonExistingCollectionId = "nonExistingId";

        Executable s = () -> collectionRepository.byId(CollectionId.of(nonExistingCollectionId));

        Exception e = assertThrows(CollectionNotFoundException.class, s);

        assertThat(e.getMessage(), containsString(nonExistingCollectionId));
    }

    @Test
    public void updateCollectionByAddingItemsTest() {
        // Given: a collection
        String aCollectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(aCollectionId, "aCollection", "desc", "colId");
        delaySecond(1);
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        Collection collection = collectionWithItemsToUpdate(aCollectionId);
        Collection updated = collectionRepository.update(collection);

        assertThat(updated.items(), hasSize(2));
    }

    @Test
    public void testFailUpdatingCollectionWithExistingName() {
        // Given: a collection exists with a name
        String collectionName = "collectionName1";
        String collectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId, collectionName, "desc", "colId");

        // And: another collection exists with a different name
        String collectionName2 = "collectionName2";
        String collectionId2 = CollectionId.randomId();
        FirebaseUtil.createCollection(collectionId2, collectionName2, "desc", "colId");
        delaySecond(1);

        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository);

        // When: updating the first collection providing name2 instead of name1
        UpdateCollectionDataCommand updateCollectionDataCommand =
                UpdateCollectionDataCommand.builder()
                    .id(collectionId)
                    .name(collectionName2)
                    .description("aDescription")
                    .build();


        Executable updateExec = () -> cas.updateCollectionData(updateCollectionDataCommand);
        delaySecond(1);

        // Then: it fails with DuplicateCollectionName, as the name is already taken
        assertThrows(DuplicateCollectionNameException.class, updateExec);
    }

    @Test
    public void testFailCreatingCollectionWithExistingName() {

        // Given: an existing collection with name
        String collectionName = "aColName";
        String collectionId = CollectionId.randomId();
        String collectorId = UUID.randomUUID().toString();

        FirebaseUtil.createCollection(collectionId, collectionName, "desc", collectorId);
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);

        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository);
        CreateCollectionCommand createCollectionCommand = CreateCollectionCommand.builder()
                                                            .name(collectionName)
                                                            .description("newDesc")
                                                            .build();

        // When: creating it
        Executable updateExec = () ->  cas.createCollection(createCollectionCommand);

        delaySecond(1);

        // Then: error should occur
        assertThrows(DuplicateCollectionNameException.class, updateExec);

    }

    void delaySecond(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Collection collectionWithItemsToUpdate(String id) {
        Collection collection = aCollection(id,"aCollection", "desc", "colId");
        List<CollectionItem> items = new ArrayList<>();
        items.add(CollectionItem.of("itemId1"));
        items.add(CollectionItem.of("itemId2"));
        collection.addItems(items);
        return collection;
    }

    @Test
    public void testAddItemToExistingCollection() {

        // Given: a collection
        String aCollectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(aCollectionId, "aCollection", "desc", "colId");
        delaySecond(1);
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);

        // When: adding an item to it
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository);
        String itemId = "itemId";
        CollectionItemDTO collectionItemDTO = new CollectionItemDTO(itemId);
        List<CollectionItemDTO> items = new ArrayList<>();
        items.add(collectionItemDTO);
        AddItemsToCollectionCommand addItemToCollectionCommand = AddItemsToCollectionCommand.of(aCollectionId, items);
        cas.addItemsToCollection(addItemToCollectionCommand);

        // Then: collection is updated containing the item
        Collection updatedCollection = collectionRepository.byId(CollectionId.of(aCollectionId));
        assertThat(updatedCollection, Matchers.notNullValue());
        assertThat(updatedCollection.items(), hasSize(1));
        assertThat(updatedCollection.items().get(0).getItemId(), is(itemId));

    }

    @Test
    public void testDeleteCollectionWithoutItems() {
        // Given: a collection exists with known id
        String aCollectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(aCollectionId, "aCollection", "desc", "colId");
        delaySecond(1);

        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);

        // When: deleting it
        collectionRepository.delete(CollectionId.of(aCollectionId));

        // Then: it won't exist
        Executable e = () -> collectionRepository.byId(CollectionId.of(aCollectionId));
        assertThrows(CollectionNotFoundException.class, e);
    }

    @Ignore
    @Test
    public void testDeleteCollectionWithItems() {
        // Given: a collection exists with items
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

        // when: deleting it
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        collectionRepository.delete(CollectionId.of(aCollectionId));

        // Then: no documents should exist in the subcollection
        assertThat(FirebaseUtil.findItemsForCollection(aCollectionId).size(), equalTo(0));
    }

    @Test
    public void testDeleteItemFromCollection() {
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

        // when: deleting an item from the collection
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository);

        cas.removeItemFromCollection(aCollectionId, "item1");

        List<String> itemsInCollection = FirebaseUtil.findItemsForCollection(aCollectionId);

        assertThat(itemsInCollection, hasSize(1));
        assertThat(itemsInCollection.contains("item1"), is(false));
        assertThat(itemsInCollection.contains("item2"), is(true));
    }

    @Test
    public void testAddAndRemoveDifferentItemsFromCollection() {
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

        // when: deleting an item from the collection
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository);


        CollectionItemDTO item1Dto = new CollectionItemDTO("item3");
        CollectionItemDTO item2Dto = new CollectionItemDTO("item4");
        List<CollectionItemDTO> collectionDTOS = new ArrayList<>();

        collectionDTOS.add(item1Dto);
        collectionDTOS.add(item2Dto);

        AddItemsToCollectionCommand addItemsToCollectionCommand = AddItemsToCollectionCommand.builder()
                                                                    .collectionId(aCollectionId)
                                                                    .items(collectionDTOS)
                                                                    .build();

        cas.addItemsToCollection(addItemsToCollectionCommand);
        cas.removeItemFromCollection(aCollectionId, "item1");

        List<String> itemsInCollection = FirebaseUtil.findItemsForCollection(aCollectionId);

        assertThat(itemsInCollection, hasSize(3));
        assertThat(itemsInCollection.contains("item1"), is(false));
        assertThat(itemsInCollection.contains("item4"), is(true));
        assertThat(itemsInCollection.contains("item3"), is(true));
        assertThat(itemsInCollection.contains("item2"), is(true));
    }

    @Test
    public void shouldNotCreateCollectionWithEmptyName() {

        CollectionRepository collectionRepo = mockRepository();

        // Given: a collection with empty name
        String collectionName = "";
        String description = "A description";
        String aCollectorId = "aCollectorId";

        CreateCollectionCommand createCollectionCommand = CreateCollectionCommand.builder()
                                                            .name(collectionName)
                                                            .description(description)
                                                            .items(new ArrayList<>())
                                                            .collectorId(aCollectorId)
                                                            .build();

        CollectionApplicationService cas = new CollectionApplicationService(collectionRepo);

        // When: creating it
        Executable s = () ->  cas.createCollection(createCollectionCommand);

        // Then: an error is thrown
        Exception e = assertThrows(InvalidCollectionException.class, s);
        assertThat(e.getMessage(), containsString("collection cannot have an empty name"));
    }

    private CollectionRepository mockRepository() {
        CollectionRepository collectionRepo = Mockito.mock(CollectionRepository.class);
        Mockito.when(collectionRepo.create(any())).thenAnswer((r) -> r.getArgument(0));
        return collectionRepo;
    }

    private Collection aCollection(String id, String name, String description, String collectorId) {
        return Collection.withNameAndDescription(CollectionId.of(id), name,
                description, Collector.of(CollectorId.of(collectorId)));
    }
}
