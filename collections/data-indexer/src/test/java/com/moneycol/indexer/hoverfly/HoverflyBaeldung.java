package com.moneycol.indexer.hoverfly;

import io.micronaut.http.HttpStatus;
import io.specto.hoverfly.junit.core.SimulationSource;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.jsonWithSingleQuotes;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.junit.Assert.assertEquals;

public class HoverflyBaeldung {

    private static final SimulationSource source =
            dsl(service("http://www.baeldung.com")
                    .get("/api/courses/1").willReturn(success()
                    .body(jsonWithSingleQuotes("{'id':'1','name':'HCI'}")))
                    .post("/api/courses").willReturn(success()));
                    //.andDelay(3, TimeUnit.SECONDS).forMethod("POST"));

    @ClassRule
    public static final HoverflyRule rule = HoverflyRule.inSimulationMode(source);


    @Test
    public void givenGetCourseById_whenRequestSimulated_thenAPICalledSuccessfully() throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().useSystemProperties().build();
        HttpGet httpGet = new HttpGet("http://www.baeldung.com/api/courses/1");

        HttpResponse collectionCreatedResp = httpClient.execute(httpGet);
        System.out.println("Result " + EntityUtils.toString(collectionCreatedResp.getEntity()));
        String a = "";
        assertEquals(HttpStatus.OK.getCode(), collectionCreatedResp.getStatusLine().getStatusCode());
        assertEquals("{\"id\":\"1\",\"name\":\"HCI\"}",
                EntityUtils.toString(collectionCreatedResp.getEntity()));
    }


}
