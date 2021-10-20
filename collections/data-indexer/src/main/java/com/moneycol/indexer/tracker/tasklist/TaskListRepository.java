package com.moneycol.indexer.tracker.tasklist;

import java.util.function.Consumer;

public interface TaskListRepository {

    String createTaskList(TaskList taskList);
    TaskList findById(String taskListId);
    void update(TaskList toUpdate);
    void updateFieldAtomically(String taskListId, String fieldName, Integer quantity);
    void updateTaskListProcessCompletionInTransaction(String taskListId, Consumer<String> doneConsumer);
}
