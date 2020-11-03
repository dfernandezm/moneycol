package com.moneycol.queuex.queue;

import com.moneycol.collections.app.QueuexClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueueCrudTest {


    @BeforeEach
    public void setup() {

    }

    @Test
    public void testCreatesQueue() {
        String queueName = "test-queue";
        QueuexClient queuexClient = QueuexClient
                .builder()
                .projectId("moneycol").build();
        queuexClient.createQueue("testingQueue");

    }


}
