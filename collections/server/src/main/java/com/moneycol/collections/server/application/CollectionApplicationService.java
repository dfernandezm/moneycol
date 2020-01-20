package com.moneycol.collections.server.application;

import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.base.Id;

import javax.inject.Inject;

public class CollectionApplicationService {

    private CollectionRepository collectionRepository;

    @Inject
    public CollectionApplicationService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public CollectionCreatedResult createCollection(CreateCollectionDTO createCollectionDTO) {

        CollectionId collectionId = CollectionId.of(Id.randomId());
        Collector collector = Collector.withCollectorId(createCollectionDTO.getCollectorId());

        Collection collection = Collection.withNameAndDescription(collectionId,
                                        createCollectionDTO.getName(),
                                        createCollectionDTO.getDescription(),
                                        collector);

        Collection createdCollection = collectionRepository.create(collection);

        return new CollectionCreatedResult(createdCollection.id(),
                                            createdCollection.name(),
                                            createdCollection.description(),
                                            collector.id());
    }
}
