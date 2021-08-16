package com.moneycol.indexer;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Invoke via Http
 *
 * <pre>
 *     curl https://GCF_REGION-GCP_PROJECT_ID.cloudfunctions.net/publish -X POST  -d "{\"topic\": \"PUBSUB_TOPIC\", \"message\":\"YOUR_MESSAGE\"}" -H "Content-Type: application/json"
 *     gcloud functions call publish --data '{"topic":"MY_TOPIC","message":"Hello World!"}'
 * </pre>
 */
@Slf4j
@Builder
public class PubSubClient {

    private static final String PROJECT_ID = "moneycol";

    /**
     * Topic which triggers the execution of the batcher function
     */
    private static final String TRIGGERING_TOPIC_NAME = "{env}.moneycol.indexer.start";

    private final JsonWriter jsonWriter = new JsonWriter();
    private final Map<String, Publisher> topicNameToPublishers = new HashMap<>();

    // Push batches to pubsub topic
    // subscribers read 1 batch, extract filenames, read documents
    // push to another topic (or index directly, depending on time)
    // https://stackoverflow.com/questions/17374743/how-can-i-get-the-memory-that-my-java-program-uses-via-javas-runtime-api
    //TODO: send object message
    public <T> void publishMessage(String topicName, T message) {
        publishMessageWithAttributes(topicName, message, ImmutableMap.of());
    }

    public <T> void publishMessageWithAttributes(String topicName, T message, Map<String, String> messageAttributes) {

        String messageJson = jsonWriter.asJsonString(message);
        PubsubMessage pubsubApiMessage = createPubsubMessage(messageJson, messageAttributes);

        try {

            Publisher publisher = publisherForTopic(topicName);

            // we do .get() to block on the returned Future and ensure the message is sent and avoid
            // premature termination killing messages being sent
            publisher.publish(pubsubApiMessage).get();
        } catch (InterruptedException | ExecutionException | IOException e) {
            log.error("Error publishing Pub/Sub message: " + e.getMessage(), e);
        }
    }

    private PubsubMessage createPubsubMessage(String messageJson, Map<String, String> attributes) {
        ByteString byteStr = ByteString.copyFrom(messageJson, StandardCharsets.UTF_8);
        return PubsubMessage
                .newBuilder()
                .setData(byteStr)
                .putAllAttributes(attributes)
                .build();
    }

    private Publisher publisherForTopic(String topicName) throws IOException {
        Publisher publisher = topicNameToPublishers.get(topicName);
        if (publisher == null) {
            publisher = Publisher
                    .newBuilder(ProjectTopicName.of(PROJECT_ID, topicName))
                    .build();
            topicNameToPublishers.put(topicName, publisher);
        }
        return publisher;
    }

    // sink topic, use synchronous pull https://cloud.google.com/pubsub/docs/pull#synchronous_pull
    // to get them 100 by 100. Needs a single subscription created to the sink topic and 1 subscriber
}
