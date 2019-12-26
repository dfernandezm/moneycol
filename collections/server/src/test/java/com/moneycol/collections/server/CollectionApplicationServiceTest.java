package com.moneycol.collections.server;

import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CreateCollectionDTO;
import com.moneycol.collections.server.domain.CollectionRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class CollectionApplicationServiceTest {

    @ParameterizedTest
    @CsvSource({"Banknotes, \"A collection for storing my banknotes in London\"",
                "\"Bankotes of the world\", \"A collection for storing my banknotes in the world\""})
    public void shouldCreateBasicCollection(String name, String description) {

       CollectionRepository collectionRepo = mockRepository();

        // Given
        String collectorId = UUID.randomUUID().toString();
        CreateCollectionDTO createCollectionDTO = new CreateCollectionDTO(name, description, collectorId);
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



    private CollectionRepository mockRepository() {
        CollectionRepository collectionRepo = Mockito.mock(CollectionRepository.class);
        Mockito.when(collectionRepo.create(any())).thenAnswer((r) -> r.getArgument(0));
        return collectionRepo;
    }
}
