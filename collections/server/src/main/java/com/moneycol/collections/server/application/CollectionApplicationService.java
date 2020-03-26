package com.moneycol.collections.server.application;

import com.moneycol.collections.server.application.exception.DuplicateCollectionNameException;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionItem;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.base.Id;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

//TODO: when with auth, check the owner of the collection
public class CollectionApplicationService {

    private CollectionRepository collectionRepository;

    @Inject
    public CollectionApplicationService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public CollectionCreatedResult createCollection(CollectionDTO createCollectionDTO) {

        if (collectionRepository.existsWithName(null, createCollectionDTO.getName())) {
            throw new DuplicateCollectionNameException("Collection already exists with name " +
                    createCollectionDTO.getName());
        }

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
                            new CollectionDTO(
                                    collection.id(), collection.name(),
                                    collection.description(), collection.collector().id(),
                                    toCollectionItemDTOs(collection)))
                    .collect(Collectors.toList());
    }

    public CollectionDTO byId(String collectionId) {
        Collection collection = collectionRepository.byId(CollectionId.of(collectionId));

        List<CollectionItemDTO> collectionItemDTOS = toCollectionItemDTOs(collection);

        CollectionDTO collectionDTO = new CollectionDTO(collection.id(), collection.name(), collection.description(),
                collection.collector().id(), collectionItemDTOS);
        return collectionDTO;
    }

    public void deleteCollection(String collectionId) {
        collectionRepository.delete(CollectionId.of(collectionId));
    }

    public CollectionCreatedResult updateCollection(CollectionDTO collectionDTO) {

        if (collectionRepository.existsWithName(collectionDTO.getId(), collectionDTO.getName())) {
            throw new DuplicateCollectionNameException("Collection already exists with name " + collectionDTO.getName());
        }

        Collection collection = Collection.withNameAndDescription(
                CollectionId.of(collectionDTO.getId()),
                collectionDTO.getName(),
                collectionDTO.getDescription(),
                Collector.withCollectorId(collectionDTO.getCollectorId()));

        collectionRepository.update(collection);
        return new CollectionCreatedResult(collection.id(), collection.name(),
                                           collection.description(), collection.collector().id());
    }

    public CollectionCreatedResult updateCollectionData(UpdateCollectionDataCommand collectionDataCommand) {
        if (collectionRepository.existsWithName(collectionDataCommand.getId(), collectionDataCommand.getName())) {
            throw new DuplicateCollectionNameException("Collection already exists with name " + collectionDataCommand.getName());
        }

        Collection collection = collectionRepository.byId(CollectionId.of(collectionDataCommand.getId()));
        collection.name(collectionDataCommand.getName());
        collection.description(collectionDataCommand.getDescription());
        collectionRepository.update(collection);
        return new CollectionCreatedResult(collection.id(), collection.name(),
                collection.description(), collection.collector().id());
    }

    public void addItemsToCollection(AddItemsToCollectionCommand addItemsToCollectionCommand) {
        CollectionId collectionId = CollectionId.of(addItemsToCollectionCommand.getCollectionId());
        Collection collection = collectionRepository.byId(collectionId);
        List<CollectionItem> collectionItems = addItemsToCollectionCommand
                                                .getItems()
                                                .stream()
                                                .map(item -> CollectionItem.of(item.getItemId()))
                                                .collect(Collectors.toList());
        collection.addItems(collectionItems);
        collectionRepository.update(collection);
    }

    public void removeItemFromCollection(String collectionId, String itemId) {
        CollectionId collectionIdObj = CollectionId.of(collectionId);
        Collection collection = collectionRepository.byId(collectionIdObj);
        CollectionItem collectionItem = CollectionItem.of(itemId);

        collection.removeItem(collectionItem);
        collectionRepository.update(collection);
    }

    private List<CollectionItemDTO> toCollectionItemDTOs(Collection collection) {
        return collection.items().stream()
                .map(item -> new CollectionItemDTO(item.getItemId()))
                .collect(Collectors.toList());
    }
}
