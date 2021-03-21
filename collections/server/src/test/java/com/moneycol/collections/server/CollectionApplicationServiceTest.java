package com.moneycol.collections.server;

import com.moneycol.collections.server.application.AddItemsToCollectionCommand;
import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CreateCollectionCommand;
import com.moneycol.collections.server.application.RemoveItemFromCollectionCommand;
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
import com.moneycol.collections.server.domain.events.DomainEventStoringSubscriber;
import com.moneycol.collections.server.domain.events.core.DomainEvent;
import com.moneycol.collections.server.domain.events.core.LocalEventPublisher;
import com.moneycol.collections.server.domain.events.core.LocalEventSubscriber;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDto;
import com.moneycol.collections.server.infrastructure.event.DomainEventRegistry;
import com.moneycol.collections.server.infrastructure.event.FirestoreEventStore;
import com.moneycol.collections.server.infrastructure.repository.CollectionNotFoundException;
import com.moneycol.collections.server.infrastructure.repository.FirebaseCollectionRepository;
import com.moneycol.collections.server.infrastructure.repository.FirestoreProvider;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Use emulator support for Firebase:
 * Frontend folder has firebase.json, that needs to be around
 * firebase emulators:start
 *
 */
@Slf4j
public class CollectionApplicationServiceTest {

    private static FirestoreProvider firestoreProvider;
    private static DomainEventRegistry eventBusRegistry;

    @BeforeAll
    public static void setup() {
        firestoreProvider = FirestoreHelper.initContainer();
        eventBusRegistry = Mockito.mock(DomainEventRegistry.class);
    }

    @AfterEach
    public void cleanup() {
        FirestoreHelper.deleteAllCollections();
        FirestoreHelper.deleteAllEvents();
    }

    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\"",
            "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\""})
    public void shouldCreateBasicCollection(String name, String description) {

        // Given
        CollectionRepository collectionRepo = new FirebaseCollectionRepository(firestoreProvider);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepo, eventBusRegistry);

        // When
        CreateCollectionCommand createCollectionCommand = CreateCollectionCommand.builder()
                .name(name)
                .description(description)
                .items(new ArrayList<>())
                .collectorId("aCollectorId")
                .build();
        CollectionCreatedResult collectionCreatedResult = cas.createCollection(createCollectionCommand);

        // Then
        assertEquals(collectionCreatedResult.getName(), name);
        assertEquals(collectionCreatedResult.getDescription(), description);
        assertNotNull(collectionCreatedResult.getCollectionId());
    }


    //TODO: this test actually creates collections in firestore, so needs to mock the Firestore
    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\"",
            "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\""})
    public void testCreateFirebaseCollection(String name, String description) {

        // Given
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);
        String collectorId = UUID.randomUUID().toString();

        Collection col =  Collection.withNameAndDescription(CollectionId.of(Id.randomId()), name,
                description, Collector.withStringCollectorId(collectorId));

        String id = col.id();

        // When
        collectionRepository.create(col);

        // Then
        Collection res = collectionRepository.byId(CollectionId.of(id));
        assertEquals(id, res.id());
    }

    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\",BanknotesUpdated",
            "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\",\"Banknotes only\""})
    public void updateCollectionTest(String name, String description, String newName) {

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);
        String collectorId = UUID.randomUUID().toString();

        Collection col =  Collection.withNameAndDescription(CollectionId.fromRandomId(), name,
                description, Collector.withStringCollectorId(collectorId));

        collectionRepository.create(col);

        Collection toUpdate =  Collection.withNameAndDescription(CollectionId.of(col.id()), newName,
                description, Collector.withStringCollectorId(collectorId));

        Collection updated = collectionRepository.update(toUpdate);

        assertEquals(updated.name(), newName);
    }

    @Test
    public void findByCollectorWithMultipleTest() {

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);

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

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);

        String nonExistentCollectorId = "collectorId1";

        List<Collection> collectionsForCollector =
                collectionRepository.byCollector(CollectorId.of(nonExistentCollectorId));

        assertTrue(collectionsForCollector.isEmpty());
    }

    @Test
    public void findByIdNotFound() {

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);

        String nonExistingCollectionId = "nonExistingId";

        Executable s = () -> collectionRepository.byId(CollectionId.of(nonExistingCollectionId));

        Exception e = assertThrows(CollectionNotFoundException.class, s);

        assertThat(e.getMessage()).contains(nonExistingCollectionId);
    }

    @Test
    public void updateCollectionByAddingItemsTest() {
        // Given: a collection
        String aCollectionId = CollectionId.randomId();
        FirestoreHelper.createCollection(aCollectionId, "aCollection", "desc", "colId");
        delaySecond(1);

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);

        // When updating it by adding another item
        Collection collection = collectionWithItemsToUpdate(aCollectionId);
        Collection updated = collectionRepository.update(collection);

        // Then the collection size is increased to 2
        assertThat(updated.items()).hasSize(2);
    }

    @Test
    public void testFailUpdatingCollectionWithExistingName() {
        // Given: a collection exists with a name
        String collectionName = "collectionName1";
        String collectorId = "colId";
        String collectionId = CollectionId.randomId();
        FirestoreHelper.createCollection(collectionId, collectionName, "desc", collectorId);

        // And: another collection exists with a different name
        String collectionName2 = "collectionName2";
        String collectionId2 = CollectionId.randomId();
        FirestoreHelper.createCollection(collectionId2, collectionName2, "desc", collectorId);

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository, eventBusRegistry);

        // When: updating the first collection providing name2 instead of name1
        UpdateCollectionDataCommand updateCollectionDataCommand =
                UpdateCollectionDataCommand.builder()
                        .collectionId(collectionId)
                        .name(collectionName2)
                        .collectorId(collectorId)
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

        FirestoreHelper.createCollection(collectionId, collectionName, "desc", collectorId);
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);

        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository, eventBusRegistry);
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

    /**
     * Given an existing empty collection
     * When adding an item to it
     * Then the collection gets updated correctly
     * And contains the single item added
     */
    @Test
    public void testAddItemToExistingCollection() {

        // Given
        String aCollectionId = CollectionId.randomId();
        String collectorId = "colId";
        String collectionName = "aCollection";
        String collectionDescription = "desc";

        FirestoreHelper.createCollection(aCollectionId, collectionName, collectionDescription, collectorId);

        // This is a big delay, but without it the collection added is not found when finding it
        delaySecond(3);
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);

        // When: adding an item to it
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository, eventBusRegistry);
        String itemId = "itemId";
        CollectionItemDto collectionItemDTO = new CollectionItemDto(itemId);
        List<CollectionItemDto> items = new ArrayList<>();
        items.add(collectionItemDTO);
        AddItemsToCollectionCommand addItemToCollectionCommand = AddItemsToCollectionCommand.builder()
                                                                    .collectionId(aCollectionId)
                                                                    .items(items)
                                                                    .collectorId(collectorId)
                                                                    .build();
        cas.addItemsToCollection(addItemToCollectionCommand);

        // Then: collection is updated containing the item
        Collection updatedCollection = collectionRepository.byId(CollectionId.of(aCollectionId));
        assertThat(updatedCollection).isNotNull();
        assertThat(updatedCollection.items()).hasSize(1);
        assertThat(updatedCollection.items()).startsWith(CollectionItem.of(itemId));

    }

    @Test
    public void testDeleteCollectionWithoutItems() {
        // Given: a collection exists with known collectionId
        String aCollectionId = CollectionId.randomId();
        FirestoreHelper.createCollection(aCollectionId, "aCollection", "desc", "colId");
        delaySecond(1);

        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);

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
        FirestoreHelper.createCollectionWithItems(aCollectionId,
                "aCollection",
                "desc",
                "colId", items);

        // when: deleting it
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);
        collectionRepository.delete(CollectionId.of(aCollectionId));

        // Then: no documents should exist in the subcollection
        assertThat(FirestoreHelper.findItemsForCollection(aCollectionId)).isEmpty();
    }

    @Test
    public void testDeleteItemFromCollection() {

        // Given: a collection with items
        String aCollectionId = CollectionId.randomId();
        String collectorId = "colId";
        CollectionItem item1 = CollectionItem.of("item1");
        CollectionItem item2 = CollectionItem.of("item2");
        List<CollectionItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        FirestoreHelper.createCollectionWithItems(aCollectionId,
                "aCollection",
                "desc",
                "colId", items);

        // When: Deleting an item from the collection
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository, eventBusRegistry);
        RemoveItemFromCollectionCommand removeItemFromCollectionCommand = RemoveItemFromCollectionCommand.builder()
                                                                            .collectorId(collectorId)
                                                                            .collectionId(aCollectionId)
                                                                            .itemId("item1")
                                                                            .build();
        cas.removeItemFromCollection(removeItemFromCollectionCommand);

        // Then: the deleted item is not present and the other is
        List<String> itemsInCollection = FirestoreHelper.findItemsForCollection(aCollectionId);
        assertThat(itemsInCollection).hasSize(1);
        assertThat(itemsInCollection).doesNotContain("item1");
        assertThat(itemsInCollection).contains("item2");
    }

    @Test
    public void testAddAndRemoveDifferentItemsFromCollection() {
        String aCollectionId = CollectionId.randomId();
        String collectorId = "colId";
        CollectionItem item1 = CollectionItem.of("item1");
        CollectionItem item2 = CollectionItem.of("item2");
        List<CollectionItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        FirestoreHelper.createCollectionWithItems(aCollectionId,
                "aCollection",
                "desc",
                collectorId, items);

        // when: deleting an item from the collection
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(firestoreProvider);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository, eventBusRegistry);

        CollectionItemDto item1Dto = new CollectionItemDto("item3");
        CollectionItemDto item2Dto = new CollectionItemDto("item4");
        List<CollectionItemDto> collectionDTOS = new ArrayList<>();

        collectionDTOS.add(item1Dto);
        collectionDTOS.add(item2Dto);

        AddItemsToCollectionCommand addItemsToCollectionCommand = AddItemsToCollectionCommand.builder()
                                                                    .collectorId(collectorId)
                                                                    .collectionId(aCollectionId)
                                                                    .items(collectionDTOS)
                                                                    .build();

        cas.addItemsToCollection(addItemsToCollectionCommand);

        RemoveItemFromCollectionCommand removeItemFromCollectionCommand = RemoveItemFromCollectionCommand.builder()
                                                                            .collectionId(aCollectionId)
                                                                            .collectorId(collectorId)
                                                                            .itemId("item1")
                                                                            .build();
        cas.removeItemFromCollection(removeItemFromCollectionCommand);

        List<String> itemsInCollection = FirestoreHelper.findItemsForCollection(aCollectionId);

        assertThat(itemsInCollection).hasSize(3);
        assertThat(itemsInCollection).doesNotContain("item1");
        assertThat(itemsInCollection).contains("item4");
        assertThat(itemsInCollection).contains("item3");
        assertThat(itemsInCollection).contains("item2");
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

        CollectionApplicationService cas = new CollectionApplicationService(collectionRepo, eventBusRegistry);

        // When: creating it
        Executable s = () ->  cas.createCollection(createCollectionCommand);

        // Then: an error is thrown
        Exception e = assertThrows(InvalidCollectionException.class, s);
        assertThat(e.getMessage()).contains("collection cannot have an empty name");
    }

    @Test
    public void publishesEventOnCollectionCreation() {

        // Given
        CollectionRepository collectionRepo = new FirebaseCollectionRepository(firestoreProvider);
        List<DomainEvent> publishedEvents = new ArrayList<>();
        eventBusRegistry = new DomainEventRegistry(new LocalEventPublisher<>(),
                new LocalEventSubscriber<DomainEvent>() {
                    @Override
                    public void handleEvent(DomainEvent domainEvent) {
                        publishedEvents.add(domainEvent);
                    }

                    @Override
                    public Class<DomainEvent> subscribedToEventType() {
                        return DomainEvent.class;
                    }
                }, new DomainEventStoringSubscriber(new FirestoreEventStore(firestoreProvider)));

        CollectionApplicationService cas = new CollectionApplicationService(collectionRepo, eventBusRegistry);
        String collectionName = "aCollectionName";
        String collectionDescription = "someDescription";
        String collectorId = "aCollectorId";

        // When
        CreateCollectionCommand createCollectionCommand = CreateCollectionCommand.builder()
                .name(collectionName)
                .description(collectionDescription)
                .items(new ArrayList<>())
                .collectorId(collectorId)
                .build();

        cas.createCollection(createCollectionCommand);

        // Then
        assertThat(publishedEvents).hasSize(1);

    }

    @Test
    public void publishesEventInFirestoreOnCollectionCreation() {

        // Given
        CollectionRepository collectionRepo = new FirebaseCollectionRepository(firestoreProvider);
        List<DomainEvent> publishedEvents = new ArrayList<>();
        eventBusRegistry = new DomainEventRegistry(
                new LocalEventPublisher<>(),
                new LocalEventSubscriber<DomainEvent>() {
                    @Override
                    public void handleEvent(DomainEvent domainEvent) {
                        publishedEvents.add(domainEvent);
                    }

                    @Override
                    public Class<DomainEvent> subscribedToEventType() {
                        return DomainEvent.class;
                    }
                },
                new DomainEventStoringSubscriber(new FirestoreEventStore(firestoreProvider)));

        CollectionApplicationService cas = new CollectionApplicationService(collectionRepo, eventBusRegistry);
        String collectionName = "aCollectionName";
        String collectionDescription = "someDescription";
        String collectorId = "aCollectorId";

        // When
        CreateCollectionCommand createCollectionCommand = CreateCollectionCommand.builder()
                .name(collectionName)
                .description(collectionDescription)
                .items(new ArrayList<>())
                .collectorId(collectorId)
                .build();

        cas.createCollection(createCollectionCommand);
        delaySecond(1);

        // Then
        List<Map<String, Object>> allEvents = FirestoreHelper.findAllEvents();
        assertThat(allEvents).hasSize(1);

    }

    private CollectionRepository mockRepository() {
        CollectionRepository collectionRepo = Mockito.mock(CollectionRepository.class);
        when(collectionRepo.create(any())).thenAnswer((r) -> r.getArgument(0));
        return collectionRepo;
    }

    private Collection aCollection(String id, String name, String description, String collectorId) {
        return Collection.withNameAndDescription(CollectionId.of(id), name,
                description, Collector.withStringCollectorId(collectorId));
    }
}
