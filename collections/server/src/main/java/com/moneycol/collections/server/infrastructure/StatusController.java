package com.moneycol.collections.server.infrastructure;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller("/_status")
@Secured("isAnonymous()")
public class StatusController {

    @Get(produces = MediaType.APPLICATION_JSON)
    public Single<String> status() {
        log.info("Calling status endpoint");
        return Single.just("{\"message\": \"alive\"}");
    }
}

