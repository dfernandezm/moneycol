package com.moneycol.indexer.indexing;

import com.google.events.cloud.pubsub.v1.Message;
import com.google.pubsub.v1.PubsubMessage;
import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.infra.PubSubClient;
import com.moneycol.indexer.tracker.TaskListResult;
import com.moneycol.indexer.worker.BanknotesDataSet;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class IndexingDataReader {

    private JsonWriter jsonWriter = new JsonWriter();

    @Inject
    private PubSubClient pubSubClient;

    public BanknotesDataSet readBanknotesDataSet(PubsubMessage pubsubMessage) {
        String banknotesDataSetJson = pubSubClient.readMessageToString(pubsubMessage);
        return jsonWriter.toObject(banknotesDataSetJson, BanknotesDataSet.class);
    }

    public void logTriggeringMessage(Message payload) {
        String messagePayload = pubSubClient.readMessageFromEventToString(payload);
        log.info("Received payload to start indexing {}", messagePayload);

        // should validate if it's a valid taskList / discard if not
        TaskListResult taskListDoneResult =
                jsonWriter.toObject(messagePayload, TaskListResult.class);
        log.info("Start indexing after completion of taskList {}", taskListDoneResult);
    }

    //TODO: in messagePullProcessor (template method?)
    public void processMessages() {

        // List<Message> messages = receiveBatch(30)
        // loop:
        // processMessage(message)
        // ackMessage(message)
        // checkTimeout
        //    nack()
        //    exit(0)
        // endloop
    }
}
