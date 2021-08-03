package com.moneycol.indexer;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.moneycol.indexer.batcher.FilesBatch;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
public class PubSubClient {

    private static final String PROJECT_ID = "moneycol";
    private static final String DEFAULT_ENV = "dev";

    /**
     * Topic which triggers the execution of the batcher function
     */
    private static final String TRIGGERING_TOPIC_NAME = "{env}.moneycol.indexer.start";

    /**
     * Topic on which batches of files are pushed
     */
    private static final String BATCHES_TOPIC_NAME = "{env}.moneycol.indexer.batches";

    /**
     * Topic on which documents to index are pushed
     */
    private static final String SINK_TOPIC_NAME = "{env}.moneycol.indexer.sink";


    // Push batches to pubsub topic
    // subscribers read 1 batch, extract filenames, read documents
    // push to another topic (or index directly, depending on time)
    public void publishBatch(FilesBatch fileBatch) throws IOException {
        String topicName = BATCHES_TOPIC_NAME;
        createTopicIfNotExists(topicName);
        log.info("Publishing message to topic: " + topicName);

        // Create the PubsubMessage object
        ByteString byteStr = ByteString.copyFrom("data", StandardCharsets.UTF_8);
        PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();


        //TODO: create or update topic

        Publisher publisher = Publisher.newBuilder(
                ProjectTopicName.of(PROJECT_ID, topicName))
                .build();

        // Attempt to publish the message
        String responseMessage;
        try {
            publisher.publish(pubsubApiMessage).get();
            responseMessage = "Message published.";
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error publishing Pub/Sub message: " + e.getMessage(), e);
            responseMessage = "Error publishing Pub/Sub message; see logs for more info.";
        }
    }

    //TODO: do this as part of infra preparation
    private void createTopicIfNotExists(String topicName) {

    }
}
