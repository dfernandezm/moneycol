package com.moneycol.indexer.batcher;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import com.moneycol.indexer.tracker.Status;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class BatcherFunctionExecutor {

    private FanOutTracker fanOutTracker;
    private FileBatcher fileBatcher;

    public BatcherFunctionExecutor(FanOutTracker fanOutTracker, FileBatcher fileBatcher) {
        this.fanOutTracker = fanOutTracker;
        this.fileBatcher = fileBatcher;
    }

    public void execute(Message message, Context context) {
        Inventory inventory = fileBatcher.buildAndStoreInventory();
        registerFanOutTaskList(inventory);
    }

    private void registerFanOutTaskList(Inventory inventory) {
        log.info("Creating taskList for tracking...");
        TaskList taskList = TaskList.create(inventory.getFilesBatches().size());
        fanOutTracker.createTaskList(taskList);

        log.info("TaskList created with ID: {} and total tasks: {}", taskList.getId(), taskList.getNumberOfTasks());

        inventory.getFilesBatches().forEach(batch -> {
            log.info("Saving and publishing task for batch {}", batch.toString());
            forkWorkerTaskFor(taskList.getId(), batch);
            log.info("Published batch in tracker with id {} ", taskList.getId());
        });
    }

    private void forkWorkerTaskFor(String taskListId, FilesBatch batch) {
        GenericTask<FilesBatch> genericTask = GenericTask.<FilesBatch>builder()
                .content(batch)
                .taskListId(taskListId)
                .status(Status.PENDING)
                .build();
        fanOutTracker.publishWorkerTask(genericTask);
    }
}
