package com.moneycol.indexer.infra.function;


import com.google.common.base.Stopwatch;
import com.moneycol.indexer.infra.config.FanOutConfigurationProperties;
import io.micronaut.scheduling.TaskScheduler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class FunctionTimeoutTracker {

    private final TaskScheduler taskScheduler;
    private final FanOutConfigurationProperties fanOutConfig;

    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private ScheduledFuture<?> reportTimingScheduledFuture;
    private boolean timedOut = false;

    private final Long ELAPSED_TIME_REPORT_FREQUENCY_SECONDS = 30L;

    public FunctionTimeoutTracker(TaskScheduler taskScheduler,
                                  FanOutConfigurationProperties fanOutConfigurationProperties) {
        this.taskScheduler = taskScheduler;
        this.fanOutConfig = fanOutConfigurationProperties;
    }

    public void stopScheduler() {
        log.info("Stopping scheduler");
        if (reportTimingScheduledFuture != null) {
            boolean cancel = reportTimingScheduledFuture.cancel(true);
            if (!cancel) {
                log.warn("Scheduler could not be terminated gracefully");
            }
        }
    }

    public void startTimer() {
        // It may be running already, as subsequent invocations may reuse global variables
        if (stopwatch.isRunning()) {
            log.info("Restarting stopWatch as it was running from previous invocation");
            stopwatch.reset();
            timedOut = false;
            stopScheduler();
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
        log.info("Checking timeout...");
        long runningTime = stopwatch.elapsed(TimeUnit.SECONDS);
        long secondsDiff = fanOutConfig.getConsolidationProcessTimeoutSeconds() - runningTime;

        if (secondsDiff <= fanOutConfig.getConsolidationProcessTimeoutThresholdSeconds()) {
            log.warn("Function is in less than {} seconds of its timeout, has been running for {}, " +
                    "execution will terminate", fanOutConfig.getConsolidationProcessTimeoutThresholdSeconds(), runningTime);
            stopScheduler();
            timedOut = true;
            return true;
        }

        return false;
    }

    public boolean timedOut() {
        return timedOut;
    }
}
