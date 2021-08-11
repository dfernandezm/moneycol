package com.moneycol.indexer.tracker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskList {

    private String id;
    private Status status;
    private Integer numberOfTasks;
    private Integer completedTasks;

    public static TaskList create(Integer totalNumberOfTasks) {
        TaskList taskList = new TaskList();
        taskList.id = UUID.randomUUID().toString();
        taskList.status = Status.RUNNING;
        taskList.numberOfTasks = totalNumberOfTasks;
        taskList.completedTasks = 0;
        return taskList;
    }


    public void incrementCompletedBy(Integer quantity) {
        completedTasks += quantity;
    }


    public boolean isDone() {
        return numberOfTasks.equals(completedTasks);
    }
}
