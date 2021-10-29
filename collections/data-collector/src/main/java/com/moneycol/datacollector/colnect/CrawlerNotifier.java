package com.moneycol.datacollector.colnect;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CrawlerNotifier {

    @Inject
    private CrawlingProcessReporter crawlingProcessReporter;

//    public CrawlerNotifier(CrawlingProcessReporter crawlingProcessReporter) {
//        this.crawlingProcessReporter = crawlingProcessReporter;
//    }

    public void notifyDone() {
        CrawlingDoneResult crawlingDoneResult = CrawlingDoneResult.builder()
                .doneMessage("well-done-twice-from-test")
                .build();
        crawlingProcessReporter.sendCrawlingDone(crawlingDoneResult);
    }
}
