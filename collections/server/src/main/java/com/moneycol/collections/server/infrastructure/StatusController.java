package com.moneycol.collections.server.infrastructure;

import com.moneycol.collections.server.application.CollectionApplicationService;
import com.moneycol.collections.server.application.CreateCollectionDTO;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.validation.Validated;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
@Controller("/_status")
@Validated
public class StatusController {

    @Inject
    private CollectionApplicationService collectionApplicationService;

    @Get(produces = MediaType.APPLICATION_JSON)
    public Single<String> status() {
        CreateCollectionDTO createCollectionDTO = new CreateCollectionDTO("aC", "desc", "id");
        collectionApplicationService.createCollection(createCollectionDTO);
        log.info("Calling status endpoint");
        return Single.just("{\"message\": \"alive\"}");
    }
}

