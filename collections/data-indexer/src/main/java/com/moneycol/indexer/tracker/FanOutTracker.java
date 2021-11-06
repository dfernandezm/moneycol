package com.moneycol.indexer.tracker;

import com.moneycol.indexer.tracker.tasklist.TaskList;

/**
 * Interface of the tracking process for spawned tasks in a fan-in/fan-out setup.
 *
 * With this it is possible the currently processing outstanding elements,
 * if consolidation started and when it's done
 *
 */
public interface FanOutTracker {

    String createTaskList(TaskList taskList);
    boolean areAllTasksCompleted(String taskListId);
    void incrementCompletedCount(String taskListId, Integer quantity);
    void completeProcessing(String taskList);
    void updateOverallTaskProgressAtomically(String taskListId);
    void spawnTask(IntermediateTask<?> genericTask);
    <T> void publishIntermediateResult(T resultData);
    void notifyProcessingCompleted(String taskListId);
    void updateTaskListStatus(String taskListId, FanOutProcessStatus status);

    void updatePendingItemsToProcessCount(String taskListId, Integer count);

    boolean isConsolidationCompleted(String taskListId);
}
