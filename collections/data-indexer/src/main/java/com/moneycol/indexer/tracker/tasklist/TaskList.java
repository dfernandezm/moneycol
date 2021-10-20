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
    private Integer valuesToProcess;

    public static TaskList create(Integer totalNumberOfTasks) {
        TaskList taskList = new TaskList();
        taskList.id = UUID.randomUUID().toString();
        taskList.status = Status.PROCESSING;
        taskList.numberOfTasks = totalNumberOfTasks;
        taskList.completedTasks = 0;
        taskList.valuesToProcess = 0;
        return taskList;
    }

    public boolean allSpawnedTasksCompleted() {
        return numberOfTasks.equals(completedTasks);
    }

    public void completeProcessingStatus() {
        setStatus(Status.PROCESSING_COMPLETED);
    }

    public boolean hasConsolidationCompleted() {
        return status == Status.CONSOLIDATION_COMPLETED;
    }
}
