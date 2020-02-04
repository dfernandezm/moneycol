package com.moneycol.collections.server.application;

import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.base.Id;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<CollectionDTO> byCollector(CollectorDTO collectorDTO) {
        Collector collector = Collector.withCollectorId(collectorDTO.collectorId());

        List<Collection> collections = collectionRepository.byCollector(CollectorId.of(collector.id()));

        return collections
                    .stream()
                    .map(collection ->
                            new CollectionDTO(collection.id(), collection.name(),
                                            collection.description(), collection.collector().id()))
                    .collect(Collectors.toList());
    }

    public void deleteCollection(String collectionId) {
        collectionRepository.delete(CollectionId.of(collectionId));
    }

    public CollectionDTO updateCollection(CollectorDTO collectorDTO) {
        return null;
    }
}
