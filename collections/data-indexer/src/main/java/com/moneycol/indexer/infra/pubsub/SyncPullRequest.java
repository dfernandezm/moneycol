package com.moneycol.indexer.infra.pubsub;

import com.google.pubsub.v1.PubsubMessage;
import lombok.Value;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Value
public class SyncPullRequest {

    private String gcpProjectId;
    private String subscriptionId;
    private Integer numOfMessages;
    Consumer<PubsubMessage> messageHandler;
    Supplier<Boolean> stopCondition;
}
