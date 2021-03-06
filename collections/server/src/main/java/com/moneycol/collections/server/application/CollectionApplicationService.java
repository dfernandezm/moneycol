package com.moneycol.collections.server.application;

import com.moneycol.collections.server.application.exception.DuplicateCollectionNameException;
import com.moneycol.collections.server.domain.Collection;
import com.moneycol.collections.server.domain.CollectionId;
import com.moneycol.collections.server.domain.CollectionItem;
import com.moneycol.collections.server.domain.CollectionRepository;
import com.moneycol.collections.server.domain.Collector;
import com.moneycol.collections.server.domain.CollectorId;
import com.moneycol.collections.server.domain.base.Id;
import com.moneycol.collections.server.domain.events.CollectionCreatedEvent;
import com.moneycol.collections.server.infrastructure.event.DomainEventRegistry;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionDto;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionItemDto;
import com.moneycol.collections.server.infrastructure.security.InvalidCollectionAccessException;
import io.micronaut.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class CollectionApplicationService {

    private CollectionRepository collectionRepository;
    private DomainEventRegistry eventBusRegistry;

    @Inject
    public CollectionApplicationService(CollectionRepository collectionRepository,
                                        DomainEventRegistry eventBusRegistry) {
        this.collectionRepository = collectionRepository;
        this.eventBusRegistry = eventBusRegistry;
    }

    public CollectionCreatedResult createCollection(CreateCollectionCommand createCollectionCommand) {

        if (collectionRepository.existsWithName(null, createCollectionCommand.getName())) {
            throw new DuplicateCollectionNameException("Collection already exists with name " +
                    createCollectionCommand.getName());
        }

        if (StringUtils.isEmpty(createCollectionCommand.getCollectorId())) {
            throw new IllegalStateException("Cannot create a collection without collector");
        }

        log.info("Creating collection for user {}", createCollectionCommand.getCollectorId());

        CollectionId collectionId = CollectionId.fromRandomId();
        Collector collector = Collector.withStringCollectorId(createCollectionCommand.getCollectorId());

        Collection collection = Collection.withNameAndDescription(collectionId,
                                        createCollectionCommand.getName(),
                                        createCollectionCommand.getDescription(),
                                        collector);

        Collection createdCollection = collectionRepository.create(collection);

        eventBusRegistry.publish(
                CollectionCreatedEvent.builder()
                .collectionId(createdCollection.id())
                .name(createdCollection.name())
                .description(createdCollection.description())
                .build());

        return CollectionCreatedResult.builder()
                                    .collectionId(createdCollection.id())
                                    .name(createdCollection.name())
                                    .description(createdCollection.description())
                                    .build();
    }

    public List<CollectionDto> byCollector(String collectorId) {
        Collector collector = Collector.withCollectorId(collectorId);

        List<Collection> collections = collectionRepository.byCollector(CollectorId.of(collector.id()));

        return collections
                    .stream()
                    .map(collection ->
                            CollectionDto.builder()
                            .collectionId(collection.id())
                            .name(collection.name())
                            .description(collection.description())
                            .items(toCollectionItemDTOs(collection))
                            .build())
                    .collect(Collectors.toList());
    }

    public CollectionDto byId(String userId, String collectionId) {
        checkCollectionOwnership(userId, collectionId);

        Collection collection = collectionRepository.byId(CollectionId.of(collectionId));
        List<CollectionItemDto> collectionItemDTOS = toCollectionItemDTOs(collection);

        return CollectionDto.builder()
                .collectionId(collection.id())
                .name(collection.name())
                .description(collection.description())
                .items(collectionItemDTOS)
                .build();
    }

    public void deleteCollection(String userId, String collectionId) {
        checkCollectionOwnership(userId, collectionId);
        collectionRepository.delete(CollectionId.of(collectionId));
    }

    public CollectionUpdatedResult updateCollectionData(UpdateCollectionDataCommand collectionDataCommand) {
        checkCollectionOwnership(collectionDataCommand.getCollectorId(), collectionDataCommand.getCollectionId());
        if (collectionRepository.existsWithName(collectionDataCommand.getCollectionId(), collectionDataCommand.getName())) {
            throw new DuplicateCollectionNameException("Collection already exists with name " + collectionDataCommand.getName());
        }

        Collection collection = collectionRepository.byId(CollectionId.of(collectionDataCommand.getCollectionId()));
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
        checkCollectionOwnership(addItemsToCollectionCommand.getCollectorId(), addItemsToCollectionCommand.getCollectionId());
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

    public void removeItemFromCollection(RemoveItemFromCollectionCommand removeItemCommand) {
        checkCollectionOwnership(removeItemCommand.getCollectorId(), removeItemCommand.getCollectionId());
        CollectionId collectionIdObj = CollectionId.of(removeItemCommand.getCollectionId());
        Collection collection = collectionRepository.byId(collectionIdObj);
        CollectionItem collectionItem = CollectionItem.of(removeItemCommand.getItemId());

        collection.removeItem(collectionItem);
        collectionRepository.update(collection);
    }

    private void checkCollectionOwnership(String userId, String collectionId) {
        Collection collection = collectionRepository.byId(CollectionId.of(collectionId));
        Collector collector = Collector.withStringCollectorId(userId);

        if (!collection.isOwnedBy(collector)) {
            throw new InvalidCollectionAccessException("Collection with ID " + collectionId + " is not owned by " + userId);
        }
    }

    private List<CollectionItemDto> toCollectionItemDTOs(Collection collection) {
        return collection.items().stream()
                .map(item -> new CollectionItemDto(item.getItemId()))
                .collect(Collectors.toList());
    }
}
