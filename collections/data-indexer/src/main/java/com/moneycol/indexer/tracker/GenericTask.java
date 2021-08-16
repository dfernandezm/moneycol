package com.moneycol.indexer.tracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenericTask<T> implements FanOutTask {

    private String taskListId;
    private T content;
    private Status status;

    @Override
    public String taskListId() {
        return null;
    }

    @Override
    public Status status() {
        return status;
    }

    @Override
    public Boolean isComplete() {
        return status == Status.COMPLETED;
    }
}
