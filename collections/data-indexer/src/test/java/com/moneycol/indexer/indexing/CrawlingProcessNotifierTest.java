package com.moneycol.indexer.indexing;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;

@MicronautTest(packages="com.moneycol.datacollector", environments = {"test", "picocli"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CrawlingProcessNotifierTest {

    @Inject
    private IndexingDataReader crawlerNotifier;


    @Test
    public void testSendDone() {

//        CrawlingDoneResult crawlingDoneResult = CrawlingDoneResult.builder()
//                .doneMessage("done")
//                .build();

       // crawlerNotifier.notifyDone();


        String a = "";
    }
}
