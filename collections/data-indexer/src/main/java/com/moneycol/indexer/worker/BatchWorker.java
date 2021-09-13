package com.moneycol.indexer.worker;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.GcsClient;
import com.moneycol.indexer.JsonWriter;
import com.moneycol.indexer.PubSubClient;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import com.moneycol.indexer.tracker.Status;
import com.moneycol.indexer.tracker.TaskListDoneResult;
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
    private static final String DONE_TOPIC_NAME = "%s.moneycol.indexer.batching.done";
    private static final String DEFAULT_ENV = "dev";

    private final JsonWriter jsonWriter = new JsonWriter();
    private final GcsClient gcsClient = new GcsClient();

    @Inject
    private PubSubClient pubSubClient;

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

        log.info("De serializing message...");
        GenericTask<FilesBatch> genericTask = jsonWriter.toGenericTask(messageString);
        FilesBatch batch = genericTask.getContent();
        log.info("Found tasks for taskListId {}", genericTask.getTaskListId());
        log.info("Message contained batch of files {}", batch);

        String sinkTopicName = String.format(SINK_TOPIC_NAME, DEFAULT_ENV);

        batch.getFilenames().forEach(filename -> {
            BanknotesDataSet banknotesDataSet = readJsonFileToBanknotesDataSet(filename);
            pubSubClient.publishMessage(sinkTopicName, banknotesDataSet);
            log.info("Published message with contents of {} as {}", filename, banknotesDataSet);
            // could index here in bulk, but it's too concurrent for the basic Elasticsearch
            // cluster in GKE at the moment
        });

        updateTracking(genericTask);
    }

    // should be in separate service (the fanOutTracker itself)
    private void updateTracking(GenericTask<FilesBatch> genericTask) {
        String taskListId = genericTask.getTaskListId();
        fanOutTracker.incrementCompletedCount(taskListId, 1);
        log.info("Incrementing task count completion for taskListId {}", taskListId);

        if (fanOutTracker.hasCompleted(taskListId)) {
            log.info("Completed FULL set of tasks for taskListId {}", taskListId);
            log.info("Indexing/collecting function can now be invoked");
            fanOutTracker.complete(taskListId);
            publishDoneStatus(taskListId);
        }
    }

    private void publishDoneStatus(String taskListId) {
        String doneTopicName = String.format(DONE_TOPIC_NAME, DEFAULT_ENV);
        TaskListDoneResult taskListDoneResult = TaskListDoneResult.builder()
                                                .taskListId(taskListId)
                                                .status(Status.COMPLETED)
                                                .build();
        log.info("Publishing DONE status to pubsub {}", taskListDoneResult);
        pubSubClient.publishMessage(doneTopicName, taskListDoneResult);
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