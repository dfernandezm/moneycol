package com.moneycol.datacollector.colnect;

public interface ColnectCrawlerClient {
    void setupCrawler();
    BanknotesDataSet startCrawler();
}
