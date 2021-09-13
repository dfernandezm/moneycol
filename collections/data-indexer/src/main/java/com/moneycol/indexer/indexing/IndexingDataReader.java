package com.moneycol.indexer.indexing;

import com.google.pubsub.v1.PubsubMessage;
import com.moneycol.indexer.JsonWriter;
import com.moneycol.indexer.PubSubClient;
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
}
