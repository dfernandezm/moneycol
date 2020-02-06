package com.moneycol.collections.server;

import com.moneycol.collections.server.application.AddItemToCollectionCommand;
import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionDTO;
import com.moneycol.collections.server.application.CollectionItemDTO;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.base.Id;
import com.moneycol.collections.server.infrastructure.repository.CollectionNotFoundException;
import com.moneycol.collections.server.infrastructure.repository.EmulatedFirebaseProvider;
import com.moneycol.collections.server.infrastructure.repository.FirebaseCollectionRepository;
import com.moneycol.collections.server.infrastructure.repository.FirebaseProvider;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
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
        String collectorId = UUID.randomUUID().toString();
        CollectionDTO createCollectionDTO = new CollectionDTO("", name, description, collectorId);
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepo);

        // when
        CollectionCreatedResult collectionCreatedResult = cas.createCollection(createCollectionDTO);

        // then
        verify(collectionRepo, times(1)).create(any());
        assertEquals(collectionCreatedResult.getName(), name);
        assertEquals(collectionCreatedResult.getDescription(), description);
        assertEquals(collectionCreatedResult.getCollectorId(), collectorId);
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

        collectionRepository.delete(CollectionId.of(id));
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
        collectionRepository.delete(CollectionId.of(updated.id()));
    }

    @Test
    public void findByCollectorWithMultipleTest() {
        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);

        String name = "Banknotes";
        String description = "A collection for storing my banknotes in London";
        String collectorId = "collectorId1";

        Collection col1 = aCollection(name, description, collectorId);
        collectionRepository.create(col1);

        name = "BanknotesName2";
        description = "A collection in London";

        // same collector Id
        Collection col2 = aCollection(name, description, collectorId);
        collectionRepository.create(col2);

        List<Collection> collectionsForCollector =
                collectionRepository.byCollector(CollectorId.of(collectorId));

        assertEquals(collectionsForCollector.size(), 2);
        assertEquals(collectionsForCollector.get(0).collector().id(), collectorId);
        assertEquals(collectionsForCollector.get(1).collector().id(), collectorId);

        collectionRepository.delete(CollectionId.of(col1.id()));
        collectionRepository.delete(CollectionId.of(col2.id()));
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

        assertThat(e.getMessage(), Matchers.containsString(nonExistingCollectionId));
    }

    @Test
    public void testAddItemToExistingCollection() {

        // Given: a collection
        String aCollectionId = CollectionId.randomId();
        FirebaseUtil.createCollection(aCollectionId, "aCollection", "desc", "colId");

        FirebaseProvider f = new EmulatedFirebaseProvider();
        FirebaseCollectionRepository collectionRepository = new FirebaseCollectionRepository(f);

        // When: adding an item to it
        CollectionApplicationService cas = new CollectionApplicationService(collectionRepository);
        String itemId = "itemId";
        CollectionItemDTO collectionItemDTO = new CollectionItemDTO(itemId);
        AddItemToCollectionCommand addItemToCollectionCommand = AddItemToCollectionCommand.of(aCollectionId, collectionItemDTO);
        cas.addItemToCollection(addItemToCollectionCommand);

        // Then: collection is updated containing the item
        Collection updatedCollection = collectionRepository.byId(CollectionId.of(aCollectionId));
        assertThat(updatedCollection, Matchers.notNullValue());
        assertThat(updatedCollection.items(), Matchers.hasSize(1));
        assertThat(updatedCollection.items().get(0).itemId(), Matchers.is(itemId));

    }

    private CollectionRepository mockRepository() {
        CollectionRepository collectionRepo = Mockito.mock(CollectionRepository.class);
        Mockito.when(collectionRepo.create(any())).thenAnswer((r) -> r.getArgument(0));
        return collectionRepo;
    }

    private Collection aCollection(String name, String description, String collectorId) {
        return Collection.withNameAndDescription(CollectionId.of(Id.randomId()), name,
                description, Collector.of(CollectorId.of(collectorId)));
    }
}
