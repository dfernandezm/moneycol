package com.moneycol.indexer.tracker;

import com.moneycol.indexer.tracker.tasklist.TaskList;
import com.moneycol.indexer.tracker.tasklist.TaskListRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Singleton
public class DefaultFanOutTracker implements FanOutTracker {

    // Constructor injection does not seem to work with functions
    @Inject
    private TaskListRepository taskListRepo;

    @Override
    public String createTaskList(TaskList taskList) {
        return taskListRepo.createTaskList(taskList);
    }

    @Override
    public boolean hasCompleted(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        return taskList.hasCompleted();
    }

    @Override
    public void incrementCompletedCount(String taskListId, Integer quantity) {
        taskListRepo.updateFieldAtomically(taskListId, "completedTasks", 1);
    }

    @Override
    public void complete(String taskListId) {
        TaskList taskList = taskListRepo.findById(taskListId);
        taskList.complete();
        taskListRepo.update(taskList);
    }
}
