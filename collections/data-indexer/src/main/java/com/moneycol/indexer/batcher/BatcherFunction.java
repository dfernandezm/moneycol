package com.moneycol.indexer.batcher;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * This function acts as a batcher/ventilator in a fan-out / fan-in
 * workflow.
 *
 * - It lists the target bucket with all the files
 * - Creates batches of 30 files to process
 * - Publishes the batches for workers to pick up in a topic
 *
 */
//TODO: test - https://cloud.google.com/functions/docs/testing/test-background
@Slf4j
public class BatcherFunction extends GoogleFunctionInitializer
        implements BackgroundFunction<Message> {

    @Inject
    private BatcherFunctionExecutor batcherFunctionExecutor;

    @Override
    public void accept(Message message, Context context) {
        log.info("Function called with context {}", context);
        batcherFunctionExecutor.execute(message, context);
    }
}