package com.moneycol.indexer.tracker.tasklist;

import com.moneycol.indexer.tracker.FanOutProcessStatus;
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
    private FanOutProcessStatus status;
    private Integer numberOfTasks;
    private Integer completedTasks;
    private Integer valuesToProcess;

    public static TaskList create(Integer totalNumberOfTasks) {
        TaskList taskList = new TaskList();
        taskList.id = UUID.randomUUID().toString();
        taskList.status = FanOutProcessStatus.PROCESSING;
        taskList.numberOfTasks = totalNumberOfTasks;
        taskList.completedTasks = 0;
        taskList.valuesToProcess = 0;
        return taskList;
    }

    public boolean allSpawnedTasksCompleted() {
        return numberOfTasks.equals(completedTasks);
    }

    public void completeProcessingStatus() {
        setStatus(FanOutProcessStatus.PROCESSING_COMPLETED);
    }

    public boolean hasConsolidationCompleted() {
        return status == FanOutProcessStatus.CONSOLIDATION_COMPLETED;
    }
}
