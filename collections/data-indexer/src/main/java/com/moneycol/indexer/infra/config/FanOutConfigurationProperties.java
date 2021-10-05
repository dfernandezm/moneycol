package com.moneycol.indexer.infra.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;

// https://guides.micronaut.io/latest/micronaut-configuration-gradle-java.html
@ConfigurationProperties("fanout")
@Getter
public class FanOutConfigurationProperties {
    private String gcpProjectId;
    private String sourceBucketName;
    private String pubsubTriggerTopicName;
    private String pubsubSinkTopicName;
    private String pubsubDoneTopicName;
}
