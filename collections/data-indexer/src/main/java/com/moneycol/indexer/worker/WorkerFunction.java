package com.moneycol.indexer.worker;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.infra.GcsClient;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

//TODO: https://cloud.google.com/functions/docs/testing/test-background
/**
 * Reads message with batch of files, process them and writes result to sink topic
 *
 * - Subscribes to topic
 * - Extracts files to read
 * - Reads each file to a list of documents
 * - Publishes the list to a sink topic moneycol.indexer.banknotes.sink
 */
@Slf4j
public class WorkerFunction extends GoogleFunctionInitializer implements BackgroundFunction<Message> {

    private final JsonWriter jsonWriter = new JsonWriter();
    private final GcsClient gcsClient = new GcsClient();

    @Inject
    public FanOutTracker fanOutTracker;

    @Override
    public void accept(Message message, Context context) {
        if (message.getData() == null) {
            log.info("No message provided");
            return;
        }

        GenericTask<?> genericTask = fanOutTracker.readMessageAsTask(message);
        log.info("Found tasks for taskListId {}", genericTask.getTaskListId());

        FilesBatch batch = (FilesBatch) genericTask.getContent();
        extractBanknoteDataFromFilesBatch(batch);

        fanOutTracker.updateTracking(genericTask);
    }

    private void extractBanknoteDataFromFilesBatch(FilesBatch batch) {
        log.info("Message contained batch of files {}", batch);

        batch.getFilenames().forEach(filename -> {
            BanknotesDataSet banknotesDataSet = readJsonFileToBanknotesDataSet(filename);
            fanOutTracker.publishIntermediateResult(banknotesDataSet);
            log.info("Published message with contents of {} as {}", filename, banknotesDataSet);
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