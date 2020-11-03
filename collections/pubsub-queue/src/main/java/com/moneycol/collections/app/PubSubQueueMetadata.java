package com.moneycol.collections.app;

import lombok.Data;

@Data
public class PubSubQueueMetadata {
    private final String topicId;
    private final String subscriptionId;
}
