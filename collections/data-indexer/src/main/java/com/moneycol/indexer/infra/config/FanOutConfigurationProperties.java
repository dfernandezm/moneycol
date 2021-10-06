package com.moneycol.indexer.infra.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

// https://guides.micronaut.io/latest/micronaut-configuration-gradle-java.html

/**
 * Collect properties from environment variables following
 *
 * https://guides.micronaut.io/latest/micronaut-configuration-gradle-java.html
 * https://docs.micronaut.io/latest/guide/index.html#configurationProperties
 *
 * see unit test ConfigurationReadingTest
 *
 */
@ConfigurationProperties("fanout")
@Getter
@Setter
public class FanOutConfigurationProperties {

    private String gcpProjectId;
    private String sourceBucketName;
    private PubSubConfigurationProperties pubSub = new PubSubConfigurationProperties();

    @ConfigurationProperties("pubSub")
    @Getter
    @Setter
    public static class PubSubConfigurationProperties {
        private String triggerTopicName;
        private String sinkTopicName;
        private String doneTopicName;
    }
}
