package com.moneycol.indexer.worker;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.infra.GcsClient;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class WorkerFunctionExecutor {

    private FanOutTracker fanOutTracker;
    private GcsClient gcsClient;
    private JsonWriter jsonWriter;

    public WorkerFunctionExecutor(FanOutTracker fanOutTracker, GcsClient gcsClient, JsonWriter jsonWriter) {
        this.fanOutTracker = fanOutTracker;
        this.gcsClient = gcsClient;
        this.jsonWriter = jsonWriter;
    }

    public void execute(Message message, Context context) {
        log.info("Function called with context {}", context);
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
