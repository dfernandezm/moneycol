package com.moneycol.indexer.infra.function;

import com.google.common.base.Stopwatch;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@Builder
public class ReportFunctionRunningTimeTask implements Runnable {

    private final Stopwatch stopwatch;

    @Override
    public void run() {
        //TODO: this elapsed time is unreliable, runs in different threads
        // reporting different duration.
        long runningTime = stopwatch.elapsed(TimeUnit.SECONDS);
        log.info("Function has been running for {} seconds", runningTime);
    }
}
