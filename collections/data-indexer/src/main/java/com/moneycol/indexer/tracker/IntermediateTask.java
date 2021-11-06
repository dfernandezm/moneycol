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
public class IntermediateTask<T> {

    private String taskListId;
    private T content;
    private FanOutProcessStatus status;
}
