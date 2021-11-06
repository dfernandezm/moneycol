package com.moneycol.indexer.tracker;

import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.JsonWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class TaskListConverter {

    private final JsonWriter jsonWriter;

    public TaskListStatusReport readMessageAsTaskListStatus(Message message) {
        String messageString = messageToString(message);
        log.info("De serializing message...");
        return jsonWriter.toObject(messageString, TaskListStatusReport.class);
    }

    public IntermediateTask<?> readMessageAsTask(Message message) {
        String messageString = messageToString(message);
        log.info("De serializing message...");
        return jsonWriter.toGenericTask(messageString);
    }

    private String messageToString(Message message) {
        return new String(
                Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }
}
