package com.moneycol.indexer.tracker.tasklist;

import com.moneycol.indexer.tracker.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Data
@Getter
@Setter
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

    public boolean hasCompleted() {
        return numberOfTasks.equals(completedTasks);
    }

    public void complete() {
        setStatus(Status.COMPLETED);
    }

}
