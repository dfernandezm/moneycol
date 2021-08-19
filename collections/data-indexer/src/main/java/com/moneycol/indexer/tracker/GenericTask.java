package com.moneycol.indexer.tracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
