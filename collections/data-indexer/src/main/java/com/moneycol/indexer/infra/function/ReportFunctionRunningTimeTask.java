package com.moneycol.indexer.infra.function;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Singleton
@Slf4j
@RequiredArgsConstructor
@Builder
public class ReportFunctionRunningTimeTask implements Runnable {

    private final Long elapsedTimeInSeconds;

    @Override
    public void run() {
        log.info("Function has been running for {} seconds", elapsedTimeInSeconds);
    }
}
