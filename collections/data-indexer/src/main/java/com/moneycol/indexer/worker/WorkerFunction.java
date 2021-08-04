package com.moneycol.indexer.worker;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Reads message with batch of files, process them and writes result to sink topic
 *
 * - Subscribes to topic moneycol.
 * - Extracts files to read
 * - Reads each file to a list of documents
 * - Publishes the list to a sink topic moneycol.indexer.banknotes.sink
 */
@Slf4j
public class WorkerFunction implements BackgroundFunction<Message> {

    @Override
    public void accept(Message message, Context context) {
        if (message.getData() == null) {
            log.info("No message provided");
            return;
        }

        String messageString = new String(
                Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
        log.info("Received message with batch");
        log.info(messageString);

        log.info("Pending publish to sink topic");
    }
}
