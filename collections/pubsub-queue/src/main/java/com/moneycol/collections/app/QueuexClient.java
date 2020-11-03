package com.moneycol.collections.app;

import lombok.Builder;

@Builder
public class QueuexClient {
    private final String projectId;
    public void createQueue(String queueName) {
        try  {
            String topicId = "t_" + queueName;
            String subscriptionId = "s_" + queueName;
            PubSubPrimitives pubSubPrimitives = PubSubPrimitives.builder().projectId(projectId).build();
            pubSubPrimitives.createTopic(topicId);
            pubSubPrimitives.createPullSubscription(topicId, subscriptionId);
            //Setup this metadata in the queue so it can publish and receive
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // send message
    // receive message/s
}
