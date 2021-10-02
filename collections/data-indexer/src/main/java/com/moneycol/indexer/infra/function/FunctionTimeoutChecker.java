package com.moneycol.indexer.infra.function;


import com.google.common.base.Stopwatch;
import io.micronaut.scheduling.TaskScheduler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
@Slf4j
public class FunctionTimeoutChecker {

    protected final TaskScheduler taskScheduler;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private ScheduledFuture<?> reportTimingScheduledFuture;

    private final Long FUNCTION_TIMEOUT_SECONDS = 540L;
    private final Long TIMEOUT_THRESHOLD_SECONDS = 10L;
    private final Long ELAPSED_TIME_REPORT_FREQUENCY_SECONDS = 30L;

    public FunctionTimeoutChecker(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void stopScheduler() {
        log.info("Stopping scheduler");
        reportTimingScheduledFuture.cancel(true);
    }

    public void startTimer() {
        // It may be running via multiple invocations of the function
        if (stopwatch.isRunning()) {
            log.info("Restart stopWatch as it was running from previous invocation");
            stopwatch.reset();
        }
        log.info("Started function timer at {}", ZonedDateTime.now());
        stopwatch.start();

        ReportFunctionRunningTimeTask task = ReportFunctionRunningTimeTask.builder()
                .stopwatch(stopwatch)
                .build();

        reportTimingScheduledFuture = taskScheduler.scheduleWithFixedDelay(Duration.ofSeconds(0),
                Duration.ofSeconds(ELAPSED_TIME_REPORT_FREQUENCY_SECONDS),
                task);
    }

    public boolean isCloseToTimeout() {
        log.info("Check timeout...");
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
