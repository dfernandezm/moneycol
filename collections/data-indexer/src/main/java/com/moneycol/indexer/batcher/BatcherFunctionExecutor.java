package com.moneycol.indexer.batcher;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.tracker.FanOutProcessStatus;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.IntermediateTask;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import jakarta.inject.Singleton;;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Singleton
public class BatcherFunctionExecutor {

    private final FanOutTracker fanOutTracker;
    private final FileBatcher fileBatcher;

    public BatcherFunctionExecutor(FanOutTracker fanOutTracker, FileBatcher fileBatcher) {
        this.fanOutTracker = fanOutTracker;
        this.fileBatcher = fileBatcher;
    }

    public void execute(Message message, Context context) {
        log.info("Invoked batcher function {}, {}", messageToString(message), context);
        DataUri dataUri = dataUriFromMessage(message);
        Inventory inventory = fileBatcher.buildAndStoreInventory(dataUri);
        registerFanOutTaskList(inventory);
    }

    private void registerFanOutTaskList(Inventory inventory) {
        log.info("Creating taskList for tracking...");
        TaskList taskList = TaskList.create(inventory.getFilesBatches().size());
        fanOutTracker.createTaskList(taskList);

        log.info("TaskList created with ID: {} and total tasks: {}", taskList.getId(), taskList.getNumberOfTasks());
        inventory.getFilesBatches().forEach(batch -> {
            log.info("Saving and publishing task for batch {}", batch.toString());
            spawnTaskFor(taskList.getId(), batch);
            log.info("Published batch in tracker with id {} ", taskList.getId());
        });
    }

    private void spawnTaskFor(String taskListId, FilesBatch batch) {
        IntermediateTask<FilesBatch> genericTask = IntermediateTask.<FilesBatch>builder()
                .content(batch)
                .taskListId(taskListId)
                .status(FanOutProcessStatus.PENDING)
                .build();
        fanOutTracker.spawnTask(genericTask);
    }

    public DataUri dataUriFromMessage(Message message) {
        JsonWriter jsonWriter = new JsonWriter();
        String messageDataJson =  messageToString(message);
        return jsonWriter.toObject(messageDataJson, DataUri.class);
    }

    private String messageToString(Message message) {
        return new String(
                Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }
}
