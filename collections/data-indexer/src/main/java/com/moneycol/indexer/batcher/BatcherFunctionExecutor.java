package com.moneycol.indexer.batcher;

import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.IntermediateTask;
import com.moneycol.indexer.tracker.FanOutProcessStatus;
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
        log.info("Invoked batcher function {}, {}", message, context);
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
        IntermediateTask<FilesBatch> intermediateTask = IntermediateTask.<FilesBatch>builder()
                .content(batch)
                .taskListId(taskListId)
                .status(FanOutProcessStatus.PENDING)
                .build();
        fanOutTracker.spawnTask(intermediateTask);
    }
}
