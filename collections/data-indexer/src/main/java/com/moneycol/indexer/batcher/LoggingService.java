package com.moneycol.indexer.batcher;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
class LoggingService {

    void logMessage(PubSubMessage message) {
        log.info("Received message {}", message);
    }
}
