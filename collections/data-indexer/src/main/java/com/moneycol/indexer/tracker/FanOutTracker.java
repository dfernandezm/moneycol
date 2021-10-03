package com.moneycol.indexer.tracker;

import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.tracker.tasklist.TaskList;

/**
 * Interface of the tracking process for spawned tasks. With this
 * it is possible to check when the overall process has completed fully
 * and consolidation can happen
 */
public interface FanOutTracker {

    String DEFAULT_ENV = "dev";

    String createTaskList(TaskList taskList);
    boolean hasCompletedProcessing(String taskListId);
    void incrementCompletedCount(String taskListId, Integer quantity);
    void completeProcessing(String taskList);
    void updateProcessingFor(GenericTask<?> genericTask);
    void publishWorkerTask(GenericTask<?> genericTask);
    <T> void publishIntermediateResult(T resultData);
    void publishProcessingDone(String taskListId);
    void updateTaskListStatus(String taskListId, Status status);
    boolean hasConsolidationCompleted(String taskListId);
    GenericTask<?> readMessageAsTask(Message message);
    TaskListStatusResult readMessageAsTaskListStatus(Message message);
}
