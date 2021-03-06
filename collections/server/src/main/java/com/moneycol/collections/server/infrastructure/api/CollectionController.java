package com.moneycol.collections.server.infrastructure.api;


import com.google.common.collect.ImmutableMap;
import com.google.firebase.database.annotations.Nullable;
import com.moneycol.collections.server.application.AddItemsToCollectionCommand;
import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionUpdatedResult;
import com.moneycol.collections.server.application.CreateCollectionCommand;
import com.moneycol.collections.server.application.RemoveItemFromCollectionCommand;
import com.moneycol.collections.server.application.UpdateCollectionDataCommand;
import com.moneycol.collections.server.application.exception.DuplicateCollectionNameException;
import com.moneycol.collections.server.domain.InvalidCollectionException;
import com.moneycol.collections.server.infrastructure.api.dto.AddItemsDTO;
import com.moneycol.collections.server.infrastructure.api.dto.CollectionDto;
import com.moneycol.collections.server.infrastructure.api.dto.UpdateCollectionDataDTO;
import com.moneycol.collections.server.infrastructure.repository.CollectionNotFoundException;
import com.moneycol.collections.server.infrastructure.security.InvalidCollectionAccessException;
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
import io.micronaut.security.annotation.Secured;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;


import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller("/collections")
@Secured("isAuthenticated()")
public class CollectionController {

    private final CollectionApplicationService collectionApplicationService;

    public CollectionController(CollectionApplicationService collectionApplicationService) {
        this.collectionApplicationService = collectionApplicationService;
    }

    /**
     *  Get my own collections
     *
     * @param principal the authenticated user
     * @return a List of collections this user owns
     */
    @Get(produces = MediaType.APPLICATION_JSON)
    public Single<List<CollectionDto>> myCollections(@Nullable Principal principal) {
        log.info("Finding collections for user: {}", principal.getName());
        return Single.just(collectionApplicationService.byCollector(principal.getName()));
    }

    @Error(exception = CollectionNotFoundException.class)
    public HttpResponse<Object> onCollectionNotFound(HttpRequest request, CollectionNotFoundException ex) {
        Map<String, Object > map = new LinkedHashMap<>();
        String errorMessage = "Collection not found: " + ex.getMessage();
        log.warn(errorMessage);
        map.put("error", errorMessage);
        return HttpResponse.notFound().body(map);
    }

    @Error(exception = InvalidCollectionException.class)
    public HttpResponse<Object> onInvalidCollection(HttpRequest request, InvalidCollectionException ex) {
        Map<String, Object > map = new LinkedHashMap<>();
        String errorMessage = "Collection is invalid: " + ex.getMessage();
        log.warn(errorMessage);
        map.put("error", errorMessage);
        return HttpResponse.badRequest().body(map);
    }

    @Error(exception = InvalidCollectionAccessException.class)
    public HttpResponse<Object> onInvalidCollectionAccess(HttpRequest request, InvalidCollectionAccessException ex) {
        Map<String, Object > map = new LinkedHashMap<>();
        String errorMessage = ex.getMessage();
        log.warn(errorMessage);
        map.put("error", "Invalid access");
        return HttpResponse.status(HttpStatus.FORBIDDEN).body(map);
    }

    @Error(exception = DuplicateCollectionNameException.class)
    public HttpResponse<Object> onDuplicatedCollectionName(HttpRequest request, DuplicateCollectionNameException ex) {
        Map<String, Object > map = new LinkedHashMap<>();
        String errorMessage = "Collection name duplicated: " + ex.getMessage();
        log.warn(errorMessage);
        map.put("error", errorMessage);
        return HttpResponse.badRequest().body(map);
    }

    /**
     * Get a collection by its ID
     *
     * @param principal the authenticated user
     * @param collectionId the collection Id
     * @return
     */
    @Get(uri="/{collectionId}", produces = MediaType.APPLICATION_JSON)
    public Single<CollectionDto> collectionsById(@Nullable Principal principal, @PathVariable String collectionId) {
        log.info("User Id is: {}", principal.getName());
        log.info("Finding collection for ID: {}", collectionId);
        return Single.just(collectionApplicationService.byId(principal.getName(), collectionId));
    }

    /**
     * Create a collection
     *
     * @param collectionDTO the payload of the collection to create
     * @return
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @Post
    public Single<CollectionCreatedResult> createCollection(@Nullable Principal principal,
                                                            @Body CollectionDto collectionDTO) {
        log.info("Attempt to create collection for user {}: {}", principal.getName(), collectionDTO);
        CreateCollectionCommand createCollectionCommand = CreateCollectionCommand.builder()
                                                            .collectorId(principal.getName())
                                                            .description(collectionDTO.getDescription())
                                                            .name(collectionDTO.getName())
                                                            .build();
        return Single.just(collectionApplicationService.createCollection(createCollectionCommand));
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Put(uri = "/{collectionId}")
    public Single<CollectionUpdatedResult> updateCollection(@Nullable Principal principal,
                                                     @PathVariable String collectionId,
                                                     @Body UpdateCollectionDataDTO collectionDTO) {
        log.info("Attempt to update collection with ID: {}, {}", collectionId, collectionDTO);
        UpdateCollectionDataCommand cmd = UpdateCollectionDataCommand.builder()
                .collectionId(collectionId)
                .name(collectionDTO.getName())
                .description(collectionDTO.getDescription())
                .collectorId(principal.getName())
                .build();
        return Single.just(collectionApplicationService.updateCollectionData(cmd));
    }

    @Delete(uri="/{collectionId}")
    public HttpResponse deleteCollection(@Nullable Principal principal, @PathVariable String collectionId) {
        log.info("Deleting collection with ID: {}", collectionId);
        collectionApplicationService.deleteCollection(principal.getName(), collectionId);
        return HttpResponse.ok();
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri = "/{collectionId}/items")
    HttpResponse addItemToCollection(@Nullable Principal principal, @PathVariable String collectionId, @Body AddItemsDTO addItemsDTO) {
        log.info("Adding item to collection with ID: {}", collectionId);
        AddItemsToCollectionCommand addItemToCollectionCommand = AddItemsToCollectionCommand.builder()
                                                                    .collectionId(collectionId)
                                                                    .items(addItemsDTO.getItems())
                                                                    .collectorId(principal.getName())
                                                                    .build();
        collectionApplicationService.addItemsToCollection(addItemToCollectionCommand);
        return HttpResponse.ok();
    }

    @Delete(uri="/{collectionId}/items/{itemId}")
    MutableHttpResponse<Object> deleteCollectionItem(@Nullable Principal principal,
                                                  @PathVariable String collectionId,
                                                  @PathVariable String itemId) {
        log.info("Deleting collection item with ID: {} in collection with ID: {}", collectionId, itemId);
        RemoveItemFromCollectionCommand removeItemCommand = RemoveItemFromCollectionCommand.builder()
                                                            .collectionId(collectionId)
                                                            .itemId(itemId)
                                                            .collectorId(principal.getName())
                                                            .build();

        collectionApplicationService.removeItemFromCollection(removeItemCommand);
        Map<String, String> resp = ImmutableMap.of("message", "ok");
        return HttpResponse.ok().body(resp);
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
