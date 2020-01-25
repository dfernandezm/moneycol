package com.moneycol.collections.server.infrastructure.api;


import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CollectionCreatedResult;
import com.moneycol.collections.server.application.CollectionDTO;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.reactivex.Flowable;
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

    @Get(produces = MediaType.APPLICATION_JSON)
    Flowable<List<CollectionDTO>> getCollections() {
        return null;
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(uri = "/create")
    Single<CollectionCreatedResult> createCollection(@Body CollectionDTO collectionDTO) {
        log.info("Attempt to create collection: {}", collectionDTO);
        return Single.just(collectionApplicationService.createCollection(collectionDTO));
    }
}
