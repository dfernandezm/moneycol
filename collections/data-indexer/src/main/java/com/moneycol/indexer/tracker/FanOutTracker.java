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
    boolean hasCompleted(String taskListId);
    void incrementCompletedCount(String taskListId, Integer quantity);
    void complete(String taskList);
    void updateTracking(GenericTask<?> genericTask);

    void publishWorkerTask(GenericTask<?> genericTask);
    <T> void publishIntermediateResult(T resultData);
    GenericTask<?> readMessageAsTask(Message message);
}
