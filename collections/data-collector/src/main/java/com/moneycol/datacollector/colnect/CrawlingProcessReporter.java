package com.moneycol.datacollector.colnect;

// https://micronaut-projects.github.io/micronaut-gcp/latest/guide/#pubsub
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;

// This needs at least Micronaut 2.1.3 to work inside a test
@PubSubClient
public interface CrawlingProcessReporter {

    @Topic("${crawling.crawler-events-topic-name}")
    void sendCrawlingDone(CrawlingDoneResult crawlingDoneResult);
}
