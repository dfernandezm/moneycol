package com.moneycol.indexer.batcher;

import java.util.Map;

class PubSubMessage {

    String data;
    Map<String, String> attributes;
    String messageId;
    String publishTime;
}
