package com.moneycol.indexer.infra.function;


import com.google.common.base.Stopwatch;
import io.micronaut.scheduling.TaskScheduler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@Singleton
@Slf4j
public class FunctionTimeoutChecker {

    protected final TaskScheduler taskScheduler;
    private final ReportFunctionRunningTimeTask reportFunctionRunningTimeTask;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    private final Long FUNCTION_TIMEOUT_SECONDS = 540L;
    private final Long TIMEOUT_THRESHOLD_SECONDS = 10L;
    private final Long ELAPSED_TIME_REPORT_FREQUENCY_SECONDS = 30L;

    public FunctionTimeoutChecker(TaskScheduler taskScheduler, ReportFunctionRunningTimeTask task) {
        this.taskScheduler = taskScheduler;
        this.reportFunctionRunningTimeTask = task;
    }

    public void startTimer() {
        log.info("Started function timer at {}", ZonedDateTime.now());
        stopwatch.start();
        taskScheduler.schedule(
                Duration.ofSeconds(ELAPSED_TIME_REPORT_FREQUENCY_SECONDS),
                reportFunctionRunningTimeTask);
    }

    public boolean isCloseToTimeout() {
        long runningTime = stopwatch.elapsed(TimeUnit.SECONDS);
        long secondsDiff = FUNCTION_TIMEOUT_SECONDS - runningTime;

        if (secondsDiff <= TIMEOUT_THRESHOLD_SECONDS) {
            log.warn("Function is in less than {} to timeout, has been running for {}" +
                    "execution will terminate soon", TIMEOUT_THRESHOLD_SECONDS, runningTime);
            return true;
        }

        return false;
    }

}
