package com.moneycol.collections.server.application;

import com.moneycol.collections.server.application.exception.DuplicateCollectionNameException;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionItem;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.base.Id;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionDTO;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDTO;
import io.micronaut.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import sun.plugin.dom.exception.InvalidStateException;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

//TODO: when with auth, check the owner of the collection
@Slf4j
public class CollectionApplicationService {

    private CollectionRepository collectionRepository;

    @Inject
    public CollectionApplicationService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public CollectionCreatedResult createCollection(CreateCollectionCommand createCollectionCommand) {

        if (collectionRepository.existsWithName(null, createCollectionCommand.getName())) {
            throw new DuplicateCollectionNameException("Collection already exists with name " +
                    createCollectionCommand.getName());
        }

        if (StringUtils.isEmpty(createCollectionCommand.getCollectorId())) {
            throw new InvalidStateException("Cannot create a collection without collector");
        }

        log.info("Creating collection for user {}", createCollectionCommand.getCollectorId());

        CollectionId collectionId = CollectionId.of(Id.randomId());
        Collector collector = Collector.withCollectorId(createCollectionCommand.getCollectorId());

        Collection collection = Collection.withNameAndDescription(collectionId,
                                        createCollectionCommand.getName(),
                                        createCollectionCommand.getDescription(),
                                        collector);

        Collection createdCollection = collectionRepository.create(collection);

        return CollectionCreatedResult.builder()
                                    .collectionId(createdCollection.id())
                                    .name(createdCollection.name())
                                    .description(createdCollection.description())
                                    .build();
    }

    public List<CollectionDTO> byCollector(String collectorId) {
        Collector collector = Collector.withCollectorId(collectorId);

        List<Collection> collections = collectionRepository.byCollector(CollectorId.of(collector.id()));

        return collections
                    .stream()
                    .map(collection ->
                            CollectionDTO.builder()
                            .id(collection.id())
                            .name(collection.name())
                            .description(collection.description())
                            .items(toCollectionItemDTOs(collection))
                            .build())
                    .collect(Collectors.toList());
    }

    public CollectionDTO byId(String collectionId) {
        Collection collection = collectionRepository.byId(CollectionId.of(collectionId));
        List<CollectionItemDTO> collectionItemDTOS = toCollectionItemDTOs(collection);

        return CollectionDTO.builder()
                .id(collection.id())
                .name(collection.name())
                .description(collection.description())
                .items(collectionItemDTOS)
                .build();
    }

    public void deleteCollection(String collectionId) {
        collectionRepository.delete(CollectionId.of(collectionId));
    }

//    public CollectionCreatedResult updateCollection(UpdateColle collectionDTO) {
//
//        if (collectionRepository.existsWithName(collectionDTO.getId(), collectionDTO.getName())) {
//            throw new DuplicateCollectionNameException("Collection already exists with name " + collectionDTO.getName());
//        }
//
//        Collection collection = Collection.withNameAndDescription(
//                CollectionId.of(collectionDTO.getId()),
//                collectionDTO.getName(),
//                collectionDTO.getDescription(),
//                Collector.withCollectorId(collectionDTO.getCollectorId()));
//
//        collectionRepository.update(collection);
//        return new CollectionCreatedResult(collection.id(), collection.name(),
//                                           collection.description(), collection.collector().id());
//    }

    public CollectionUpdatedResult updateCollectionData(UpdateCollectionDataCommand collectionDataCommand) {
        if (collectionRepository.existsWithName(collectionDataCommand.getId(), collectionDataCommand.getName())) {
            throw new DuplicateCollectionNameException("Collection already exists with name " + collectionDataCommand.getName());
        }

        Collection collection = collectionRepository.byId(CollectionId.of(collectionDataCommand.getId()));
        collection.name(collectionDataCommand.getName());
        collection.description(collectionDataCommand.getDescription());
        collectionRepository.update(collection);
        return CollectionUpdatedResult.builder()
                .collectionId(collection.id())
                .name(collection.name())
                .description(collection.description())
                .build();
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
