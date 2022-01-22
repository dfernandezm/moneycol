package com.moneycol.indexer.indexing;

import com.google.events.cloud.pubsub.v1.Message;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.infra.pubsub.PubSubClient;
import com.moneycol.indexer.tracker.TaskListStatusReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class IndexingDataReader {

    private final JsonWriter jsonWriter;
    private final PubSubClient pubSubClient;

    public void logTriggeringMessage(Message payload) {
        String messagePayload = pubSubClient.readMessageFromEventToString(payload);
        log.info("Received payload to start indexing {}", messagePayload);

        // should validate if it's a valid taskList / discard if not
        TaskListStatusReport taskListDoneResult =
                jsonWriter.toObject(messagePayload, TaskListStatusReport.class);
        log.info("Start indexing after completion of taskList {}", taskListDoneResult);
    }
}
