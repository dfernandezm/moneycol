package com.moneycol.indexer.infra.config;

import io.micronaut.context.annotation.ConfigurationProperties;

// https://guides.micronaut.io/latest/micronaut-configuration-gradle-java.html
@ConfigurationProperties("fanout")
//TODO: Lombok @Getter / @Setter is not correctly picked up when injecting this
public class FanOutConfigurationProperties {
    private String gcpProjectId;
    private String sourceBucketName;
    private String pubsubTriggerTopicName;
    private String pubsubSinkTopicName;
    private String pubsubDoneTopicName;

    public String getGcpProjectId() {
        return this.gcpProjectId;
    }

    public String getSourceBucketName() {
        return this.sourceBucketName;
    }

    public String getPubsubTriggerTopicName() {
        return this.pubsubTriggerTopicName;
    }

    public String getPubsubSinkTopicName() {
        return this.pubsubSinkTopicName;
    }

    public String getPubsubDoneTopicName() {
        return this.pubsubDoneTopicName;
    }

    public void setGcpProjectId(String gcpProjectId) {
        this.gcpProjectId = gcpProjectId;
    }

    public void setSourceBucketName(String sourceBucketName) {
        this.sourceBucketName = sourceBucketName;
    }

    public void setPubsubTriggerTopicName(String pubsubTriggerTopicName) {
        this.pubsubTriggerTopicName = pubsubTriggerTopicName;
    }

    public void setPubsubSinkTopicName(String pubsubSinkTopicName) {
        this.pubsubSinkTopicName = pubsubSinkTopicName;
    }

    public void setPubsubDoneTopicName(String pubsubDoneTopicName) {
        this.pubsubDoneTopicName = pubsubDoneTopicName;
    }
}
