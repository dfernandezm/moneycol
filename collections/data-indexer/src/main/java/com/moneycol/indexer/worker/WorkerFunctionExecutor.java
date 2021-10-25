package com.moneycol.indexer.worker;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.infra.GcsClient;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import com.moneycol.indexer.tracker.TaskListConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class WorkerFunctionExecutor {

    private final FanOutTracker fanOutTracker;
    private final GcsClient gcsClient;
    private final TaskListConverter taskListConverter;
    private final JsonWriter jsonWriter;
    private final FanOutConfigurationProperties fanOutConfigurationProperties;

    public void execute(Message message, Context context) {
        log.info("Worker function called with context {}", context);
        if (message.getData() == null) {
            log.info("No message provided");
            return;
        }

        GenericTask<?> genericTask = taskListConverter.readMessageAsTask(message);
        log.info("Found tasks for taskListId {}", genericTask.getTaskListId());

        FilesBatch batch = (FilesBatch) genericTask.getContent();
        processBatchAsBanknoteDataSetIntoTaskList(batch, genericTask.getTaskListId());

        // This needs to update the status of the overall processing
        // keep count of spawned tasks to see if all has been completed
        fanOutTracker.updateOverallTaskProgressAtomically(genericTask);
    }

    private void processBatchAsBanknoteDataSetIntoTaskList(FilesBatch batch, String taskListId) {
        log.info("Message from taskListId {} contained batch of files {}", taskListId, batch);

        batch.getFilenames().forEach(filename -> {
            BanknotesDataSet banknotesDataSet = readJsonFileToBanknotesDataSet(filename);
            banknotesDataSet.setFilename(filename);
            Integer size = banknotesDataSet.getBanknotes().size();
            fanOutTracker.publishIntermediateResult(banknotesDataSet);
            fanOutTracker.updateValuesToProcessCount(taskListId, size);

            //TODO: make sure there's no negative numbers after this count
            log.info("Published message with {} document from contents of {} as {}", size, filename, banknotesDataSet);
        });
    }

    private BanknotesDataSet readJsonFileToBanknotesDataSet(String jsonFileName) {
        log.info("Reading contents of {}", jsonFileName);
        String jsonContents = gcsClient.readObjectContents(fanOutConfigurationProperties.getSourceBucketName(), jsonFileName);
        return jsonWriter.toObject(jsonContents, BanknotesDataSet.class);
    }
}
