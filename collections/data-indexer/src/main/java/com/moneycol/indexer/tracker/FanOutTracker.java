package com.moneycol.indexer.tracker;

/**
 * Interface of the tracking process for spawned tasks. With this
 * it is possible to check when the overall process has completed fully
 * and consolidation can happen
 */
public interface FanOutTracker {
    String createTaskList(TaskList taskList);
    boolean isDone(String taskListId);
    void incrementCompletedCount(String taskListId, Integer quantity);
}
