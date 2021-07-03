package com.moneycol.datacollector.colnect.collector;

import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public class CollectBanknoteDataJob {
    private final ZonedDateTime started;
    private final JobStatus status;
}
