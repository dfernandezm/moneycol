package com.moneycol.indexer.tracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a status lifecycle within a fan-out
 * {@link com.moneycol.indexer.tracker.tasklist.TaskList}
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TaskListStatusReport {

    private String taskListId;
    private FanOutProcessStatus status;
}
