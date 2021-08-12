package com.moneycol.indexer.worker;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.GcsClient;
import com.moneycol.indexer.JsonWriter;
import com.moneycol.indexer.PubSubClient;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.tracker.FanOutTracker;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Reads message with batch of files, process them and writes result to sink topic
 *
 * - Subscribes to topic
 * - Extracts files to read
 * - Reads each file to a list of documents
 * - Publishes the list to a sink topic moneycol.indexer.banknotes.sink
 */
@Slf4j
public class BatchWorker extends GoogleFunctionInitializer implements BackgroundFunction<Message> {

    /**
     * Topic on which documents to index are pushed
     */
    private static final String SINK_TOPIC_NAME = "%s.moneycol.indexer.sink";
    private static final String DEFAULT_ENV = "dev";

    private final JsonWriter jsonWriter = new JsonWriter();
    private final GcsClient gcsClient = new GcsClient();
    private final PubSubClient pubSubClient = PubSubClient.builder().build();

    @Inject
    public FanOutTracker fanOutTracker;

    @Override
    public void accept(Message message, Context context) {
        if (message.getData() == null) {
            log.info("No message provided");
            return;
        }

        String messageString = new String(
                Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);

        log.info("De serializing message to files batch...");
        FilesBatch batch = jsonWriter.toObject(messageString, FilesBatch.class);
        log.info("Message contained batch of files {}", batch);

        String sinkTopicName = String.format(SINK_TOPIC_NAME, DEFAULT_ENV);

        batch.getFilenames().forEach(filename -> {
            BanknotesDataSet banknotesDataSet = readJsonFileToBanknotesDataSet(filename);
            pubSubClient.publishMessage(sinkTopicName, banknotesDataSet);
            log.info("Published message with contents of {} as {}", filename, banknotesDataSet);
            String taskListId = "";
            fanOutTracker.incrementCompletedCount(taskListId, 1);
            fanOutTracker.isDone(taskListId);
            // could index here in bulk, but it's too concurrent for the basic Elasticsearch
            // cluster in GKE at the moment
        });
    }

    private BanknotesDataSet readJsonFileToBanknotesDataSet(String jsonFileName) {
        log.info("Reading contents of {}", jsonFileName);
        String jsonContents = gcsClient.readObjectContents("moneycol-import", jsonFileName);
        return jsonWriter.toObject(jsonContents, BanknotesDataSet.class);
    }
}

/**
 * Idea for sink processing:
 *
 * - indexer to ES starts - uses Sync Pull to get 100 messages
 * - bulk inserts into ES
 * - keeps track of time, when it reaches 8 min 30 secs it switches off and publishes
 * again to the trigger topic and continue
 */