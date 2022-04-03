package com.moneycol.datacollector.crawling;

import lombok.RequiredArgsConstructor;

import jakarta.inject.Singleton;

@Singleton
@RequiredArgsConstructor
public class CrawlerNotifier {

    private static final String DONE_MESSAGE = "crawling-done";
    private final CrawlingProcessReporter crawlingProcessReporter;

    public void notifyDone(String uri) {
        CrawlingDoneResult crawlingDoneResult = CrawlingDoneResult.builder()
                .doneMessage(DONE_MESSAGE)
                .dataUri(uri)
                .build();
        crawlingProcessReporter.sendCrawlingDone(crawlingDoneResult);
    }
}
