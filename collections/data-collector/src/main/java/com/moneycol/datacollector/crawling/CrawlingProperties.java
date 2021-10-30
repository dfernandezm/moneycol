package com.moneycol.datacollector.crawling;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Properties for crawler injected from environment variables into the Micronaut context
 */
@ConfigurationProperties("crawling")
@Getter
@Setter
public class CrawlingProperties {

    /**
     * This is the name of the PubSub Topic notified when the crawling process
     * has completed. From there the FanOut process to index the results is triggered.
     *
     * This value is injected via a environment variable named CRAWLING_DONE_TOPIC_NAME.
     */
    private String doneTopicName;
}
