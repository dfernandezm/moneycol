package com.moneycol.collections.server.infrastructure.api;


import com.moneycol.collections.server.application.AddItemsDTO;
import com.moneycol.collections.server.application.AddItemsToCollectionCommand;
import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionDTO;
import com.moneycol.collections.server.application.CollectorDTO;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Controller("/collections")
public class CollectionController {

    private final CollectionApplicationService collectionApplicationService;

    public CollectionController(CollectionApplicationService collectionApplicationService) {
        this.collectionApplicationService = collectionApplicationService;
    }

    @Get(uri="/collector/{collectorId}", produces = MediaType.APPLICATION_JSON)
    Single<List<CollectionDTO>> collectionsByCollector(@PathVariable String collectorId) {
        log.info("Finding collections for collector with ID: {}", collectorId);
        CollectorDTO collectorDTO = new CollectorDTO(collectorId);
        return Single.just(collectionApplicationService.byCollector(collectorDTO));
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri = "/create")
    Single<CollectionCreatedResult> createCollection(@Body CollectionDTO collectionDTO) {
        log.info("Attempt to create collection: {}", collectionDTO);
        return Single.just(collectionApplicationService.createCollection(collectionDTO));
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Put(uri = "/{collectionId}")
    Single<CollectionCreatedResult> updateCollection(@PathVariable String collectionId, @Body CollectionDTO collectionDTO) {
        log.info("Attempt to update collection with ID: {}, {}", collectionId, collectionDTO);
        collectionDTO.setId(collectionId);
        return Single.just(collectionApplicationService.updateCollection(collectionDTO));
    }

    @Delete(uri="/{collectionId}")
    void deleteCollection(@PathVariable String collectionId) {
        log.info("Deleting collection with ID: {}", collectionId);
        collectionApplicationService.deleteCollection(collectionId);
    }


    //TODO: multiple items
    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri = "/{collectionId}/items")
    void addItemToCollection(@PathVariable String collectionId, @Body AddItemsDTO addItemsDTO) {
        log.info("Adding item to collection with ID: {}", collectionId);
        AddItemsToCollectionCommand addItemToCollectionCommand =
                AddItemsToCollectionCommand.of(collectionId, addItemsDTO.getItems());
        collectionApplicationService.addItemsToCollection(addItemToCollectionCommand);
    }
}
