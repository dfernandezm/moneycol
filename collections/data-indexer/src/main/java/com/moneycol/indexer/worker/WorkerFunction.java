package com.moneycol.indexer.worker;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

/**
 * This function gets triggered of a result of a PubSub published message containing a batch
 * of files to process.
 *
 * It reads the message with a batch of files, process them and writes result to destination
 * (sink topic).
 *
 * - Each batch is a function invocation
 * - Extracts files to read
 * - Reads each file to a list of documents to process
 * - Publishes the list to a sink topic
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