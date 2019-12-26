package com.moneycol.collections.server.application;

import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.base.Id;

import javax.inject.Inject;

public class CollectionApplicationService {

    private CollectionRepository collectionRepository;

    @Inject
    public CollectionApplicationService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public CollectionCreatedResult createCollection(CreateCollectionDTO createCollectionDTO) {

        //TODO: should be inside a domain method in collection itself?
        CollectionId collectionId = CollectionId.of(Id.randomId());
        CollectorId collectorId = CollectorId.of(createCollectionDTO.getCollectorId());
        Collector collector = Collector.of(collectorId);

        Collection collection = Collection.withNameAndDescription(collectionId,
                                        createCollectionDTO.getName(),
                                        createCollectionDTO.getDescription(),
                                        collector);

        Collection createdCollection = collectionRepository.create(collection);

        return new CollectionCreatedResult(createdCollection.id().id(),
                                            createdCollection.name(),
                                            createdCollection.description(),
                                            collector.id().id());
    }
}
