package com.moneycol.indexer.tracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents completion of a fan-out {@link com.moneycol.indexer.tracker.tasklist.TaskList}
 * with a specific status
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TaskListStatusResult {

    private String taskListId;
    private Status status;
}
