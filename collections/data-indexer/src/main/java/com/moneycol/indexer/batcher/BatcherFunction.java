package com.moneycol.indexer.batcher;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.moneycol.indexer.tracker.FanOutTracker;
import com.moneycol.indexer.tracker.GenericTask;
import com.moneycol.indexer.tracker.Status;
import com.moneycol.indexer.tracker.tasklist.TaskList;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * This function acts as a batcher/ventilator in a fan-out / fan-in
 * workflow.
 *
 * - It lists the target bucket with all the files
 * - Creates batches of 30 files to process
 * - Publishes the batches for workers to pick up in a topic
 *
 */
@Slf4j
public class BatcherFunction extends GoogleFunctionInitializer
        implements BackgroundFunction<PubSubMessage> {

    @Inject
    private FanOutTracker fanOutTracker;

    @Inject
    private FileBatcher fileBatcher;

    @Override
    public void accept(PubSubMessage message, Context context) {
        log.info("Function called with context {}", context);
        Inventory inventory = fileBatcher.buildInventoryBatched("moneycol", "moneycol-import");
        registerFanOutTaskList(inventory);
    }

    //TODO: this should be encapsulated in 1 tasklist
    private void registerFanOutTaskList(Inventory inventory) {
        log.info("Creating taskList for tracking...");
        TaskList taskList = TaskList.create(inventory.getFilesBatches().size());
        fanOutTracker.createTaskList(taskList);

        log.info("TaskList created with ID: {} and total tasks: {}", taskList.getId(), taskList.getNumberOfTasks());

        inventory.getFilesBatches().forEach(batch -> {
            log.info("Saving and publishing task for batch {}", batch.toString());
            GenericTask<FilesBatch> genericTask = GenericTask.<FilesBatch>builder()
                    .content(batch)
                    .taskListId(taskList.getId())
                    .status(Status.PENDING)
                    .build();
            fanOutTracker.publishTask(genericTask);
            log.info("Published batch in tracker with id {} ", taskList.getId());
        });
    }
}