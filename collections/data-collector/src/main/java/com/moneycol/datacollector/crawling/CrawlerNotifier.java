package com.moneycol.datacollector.crawling;

import lombok.RequiredArgsConstructor;

import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor
public class CrawlerNotifier {

    private static final String DONE_MESSAGE = "crawling-done";
    private final CrawlingProcessReporter crawlingProcessReporter;

    public void notifyDone() {
        CrawlingDoneResult crawlingDoneResult = CrawlingDoneResult.builder()
                .doneMessage(DONE_MESSAGE)
                .build();
        crawlingProcessReporter.sendCrawlingDone(crawlingDoneResult);
    }
}
