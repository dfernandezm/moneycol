package com.moneycol.indexer.worker;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * Reads message with batch of files, process them and writes result to sink destination
 *
 * - Each batch is a function invocation
 * - Extracts files to read
 * - Reads each file to a list of documents
 * - Publishes the list to a sink topic {env}.indexer.banknotes.sink
 *
 * Write tests for functions: https://cloud.google.com/functions/docs/testing/test-background
 */
@Slf4j
public class WorkerFunction extends GoogleFunctionInitializer implements BackgroundFunction<Message> {

    @Inject
    private WorkerFunctionExecutor workerFunctionExecutor;

    @Override
    public void accept(Message message, Context context) {
        workerFunctionExecutor.execute(message, context);
    }
}

/**
 * Idea for sink processing:
 *
 * - indexer to ES starts - uses Sync Pull to get 100 messages
 * - bulk inserts into ES
 * - keeps track of time, when it reaches 8 min 30 secs it switches off and publishes
 * again to the trigger topic and continue
 */