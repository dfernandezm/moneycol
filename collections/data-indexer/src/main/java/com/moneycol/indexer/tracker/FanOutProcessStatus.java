package com.moneycol.indexer.tracker;

public enum FanOutProcessStatus {
    PENDING,
    PROCESSING,
    PROCESSING_COMPLETED,
    CONSOLIDATING,
    CONSOLIDATION_COMPLETED;
}
