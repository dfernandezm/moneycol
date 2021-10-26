package com.moneycol.indexer.hoverfly;

import io.specto.hoverfly.junit.dsl.ResponseBuilder;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.jsonWithSingleQuotes;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.serverError;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.verification.HoverflyVerifications.times;
import static org.assertj.core.api.Assertions.assertThat;

// Examples:
// https://github.com/SpectoLabs/hoverfly-java/blob/master/src/test/java/io/specto/hoverfly/ruletest/HoverflyDslTest.java
public class HoverflyHelperTest {

    private static final String baseUrl = "https://jsonplaceholder.typicode.com";
    private final HttpClient hoverflyCompatibleHttpClient = HttpClientBuilder.create().useSystemProperties().build();

    @ClassRule
    public static final HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode();

    @Before
    public void setUp() {
        hoverflyRule.resetJournal();
    }

    @Test
    public void callsApiGivesErrorTest() throws IOException {
        simulateServerError();

        HttpGet httpGet = new HttpGet(baseUrl + "/todos/1");
        HttpResponse response = hoverflyCompatibleHttpClient.execute(httpGet);
        //String responseString = EntityUtils.toString(response.getEntity());
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(500);
    }

    @Test
    public void callsApiGivesSuccessTest() throws IOException {
        simulateSuccess();
        HttpGet httpGet = new HttpGet(baseUrl + "/todos/1");
        HttpResponse response = hoverflyCompatibleHttpClient.execute(httpGet);
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
    }

    @Test
    public void callsApiWithPayload() throws IOException {
        simulateReturnsPayload();
        HttpGet httpGet = new HttpGet(baseUrl + "/todos/1");
        HttpResponse response = hoverflyCompatibleHttpClient.execute(httpGet);
        String responseString = EntityUtils.toString(response.getEntity());
        assertThat(responseString).isEqualTo("{ \"payload\": \"valid\"}");
        hoverflyRule.verify(
                service(baseUrl)
                        .get("/todos/1"), times(1));
    }

    private void simulateServerError() {
        simulateBasicCall(serverError());
    }

    private void simulateSuccess() {
        simulateBasicCall(success());
    }

    private void simulateReturnsPayload() {
        hoverflyRule.simulate(
                dsl(service(baseUrl)
                        .get("/todos/1")
                        .anyQueryParams()
                        .anyBody()
                        .willReturn(success()
                                .body(jsonWithSingleQuotes(
                                        "{ 'payload': 'valid'}"
                                )))));
    }

    private void simulateBasicCall(ResponseBuilder responseBuilder) {
        hoverflyRule.simulate(
                dsl(service(baseUrl)
                        .get("/todos/1")
                        .anyQueryParams()
                        .anyBody()
                        .willReturn(responseBuilder)));
    }
}