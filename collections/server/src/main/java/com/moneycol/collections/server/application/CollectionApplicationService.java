package com.moneycol.collections.server.application;

import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.base.Id;

import javax.inject.Inject;
import java.util.List;

public class CollectionApplicationService {

    private CollectionRepository collectionRepository;

    @Inject
    public CollectionApplicationService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public CollectionCreatedResult createCollection(CollectionDTO createCollectionDTO) {

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

    //TODO: for collector
    public List<CollectionDTO> byCollector(CollectorDTO collectorDTO) {
        return null;
    }

    public CollectionDTO updateCollection(CollectorDTO collectorDTO) {
        return null;
    }
}
