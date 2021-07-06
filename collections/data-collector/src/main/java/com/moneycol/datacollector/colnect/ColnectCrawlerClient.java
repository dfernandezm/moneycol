package com.moneycol.datacollector.colnect;

public interface ColnectCrawlerClient {
    void setupCrawler();
    ColnectBanknotesDataSet startCrawler();
}
