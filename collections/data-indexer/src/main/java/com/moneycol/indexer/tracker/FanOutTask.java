package com.moneycol.indexer.tracker;

public interface FanOutTask {

    String taskListId();
    Status status();
    Boolean isComplete();
}
