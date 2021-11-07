package com.moneycol.datacollector.crawling;

import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;

/**
 * This is a declarative Pubsub Client created by Micronaut
 * {@see  https://micronaut-projects.github.io/micronaut-gcp/latest/guide/#pubsub}
 *
 * Note: for this class to work in an integration test (the ones annotated with <pre>@MicronautTest</pre>),
 * Micronaut v2.1.3 or superior is required
 */
@PubSubClient
public interface CrawlingProcessReporter {

    @Topic("${crawling.done-topic-name}")
    void sendCrawlingDone(CrawlingDoneResult crawlingDoneResult);
}
