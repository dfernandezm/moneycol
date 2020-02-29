package com.moneycol.collections.server.infrastructure.api;


import com.moneycol.collections.server.application.AddItemsDTO;
import com.moneycol.collections.server.application.AddItemsToCollectionCommand;
import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionDTO;
import com.moneycol.collections.server.application.CollectorDTO;
import com.moneycol.collections.server.domain.InvalidCollectionException;
import com.moneycol.collections.server.infrastructure.repository.CollectionNotFoundException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.hateoas.JsonError;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Error(exception = CollectionNotFoundException.class)
    public MutableHttpResponse<Object> onCollectionNotFound(HttpRequest request, CollectionNotFoundException ex) {
        Map<String, Object > map = new LinkedHashMap<>();
        String errorMessage = "Collection not found: " + ex.getMessage();
        log.warn(errorMessage);
        map.put("error", errorMessage);
        return HttpResponse.notFound().body(map);
    }

    @Error(exception = InvalidCollectionException.class)
    public MutableHttpResponse<Object> onInvalidCollection(HttpRequest request, InvalidCollectionException ex) {
        Map<String, Object > map = new LinkedHashMap<>();
        String errorMessage = "Collection is invalid: " + ex.getMessage();
        log.warn(errorMessage);
        map.put("error", errorMessage);
        return HttpResponse.badRequest().body(map);
    }

    @Get(uri="/{collectionId}", produces = MediaType.APPLICATION_JSON)
    Single<CollectionDTO> collectionsById(@PathVariable String collectionId) {
        log.info("Finding collection for ID: {}", collectionId);
        return Single.just(collectionApplicationService.byId(collectionId));
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post()
    Single<CollectionCreatedResult> createCollection(@Body CollectionDTO collectionDTO) {
        log.info("Attempt to create collection: {}", collectionDTO);
        return Single.just(collectionApplicationService.createCollection(collectionDTO));
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Put(uri = "/{collectionId}")
    Single<CollectionCreatedResult> updateCollection(@PathVariable String collectionId,
                                                     @Body CollectionDTO collectionDTO) {
        log.info("Attempt to update collection with ID: {}, {}", collectionId, collectionDTO);
        collectionDTO.setId(collectionId);
        return Single.just(collectionApplicationService.updateCollection(collectionDTO));
    }

    @Delete(uri="/{collectionId}")
    HttpResponse deleteCollection(@PathVariable String collectionId) {
        log.info("Deleting collection with ID: {}", collectionId);
        collectionApplicationService.deleteCollection(collectionId);
        return HttpResponse.ok();
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri = "/{collectionId}/items")
    HttpResponse addItemToCollection(@PathVariable String collectionId, @Body AddItemsDTO addItemsDTO) {
        log.info("Adding item to collection with ID: {}", collectionId);
        AddItemsToCollectionCommand addItemToCollectionCommand =
                AddItemsToCollectionCommand.of(collectionId, addItemsDTO.getItems());
        collectionApplicationService.addItemsToCollection(addItemToCollectionCommand);
        return HttpResponse.ok();
    }

    @Delete(uri="/{collectionId}/items/{itemId}")
    HttpResponse deleteCollectionItem(@PathVariable String collectionId,
                                      @PathVariable String itemId) {
        log.info("Deleting collection item with ID: {} in collection with ID: {}", collectionId, itemId);
        collectionApplicationService.removeItemFromCollection(collectionId, itemId);
        return HttpResponse.ok();
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    public HttpResponse notFound(HttpRequest request) {
        JsonError error = new JsonError("Element Not Found");

        return HttpResponse.<JsonError>notFound()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    @Error(status = HttpStatus.INTERNAL_SERVER_ERROR, global = true)
    public HttpResponse internalServer(HttpRequest request) {
        JsonError error = new JsonError("Internal Server Error");

        return HttpResponse.<JsonError>notFound()
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}
