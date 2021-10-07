package com.moneycol.indexer.worker;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.infra.GcsClient;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class WorkerFunctionExecutor {

    private final FanOutTracker fanOutTracker;
    private final GcsClient gcsClient;
    private final JsonWriter jsonWriter;
    private final FanOutConfigurationProperties fanOutConfigurationProperties;

//    public WorkerFunctionExecutor(FanOutTracker fanOutTracker, GcsClient gcsClient, JsonWriter jsonWriter) {
//        this.fanOutTracker = fanOutTracker;
//        this.gcsClient = gcsClient;
//        this.jsonWriter = jsonWriter;
//    }

    public void execute(Message message, Context context) {
        log.info("Worker function called with context {}", context);
        if (message.getData() == null) {
            log.info("No message provided");
            return;
        }

        GenericTask<?> genericTask = fanOutTracker.readMessageAsTask(message);
        log.info("Found tasks for taskListId {}", genericTask.getTaskListId());

        FilesBatch batch = (FilesBatch) genericTask.getContent();
        extractBanknoteDataFromFilesBatch(batch);

        fanOutTracker.updateTaskProgress(genericTask);
    }

    private void extractBanknoteDataFromFilesBatch(FilesBatch batch) {
        log.info("Message contained batch of files {}", batch);

        batch.getFilenames().forEach(filename -> {
            BanknotesDataSet banknotesDataSet = readJsonFileToBanknotesDataSet(filename);
            fanOutTracker.publishIntermediateResult(banknotesDataSet);
            log.info("Published message with contents of {} as {}", filename, banknotesDataSet);

            // could index here in bulk, but it's too concurrent for the basic Elasticsearch
            // cluster in GKE at the moment. The result is published to a sink topic, from where
            // it will be pulled in small batches
        });
    }

    private BanknotesDataSet readJsonFileToBanknotesDataSet(String jsonFileName) {
        log.info("Reading contents of {}", jsonFileName);
        String jsonContents = gcsClient.readObjectContents(fanOutConfigurationProperties.getSourceBucketName(), jsonFileName);
        return jsonWriter.toObject(jsonContents, BanknotesDataSet.class);
    }
}
