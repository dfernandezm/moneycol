package com.moneycol.datacollector.colnect.collector;

import com.moneycol.datacollector.BanknoteDataCollector;
import com.moneycol.datacollector.colnect.ColnectCrawlerClient;

public class ColnectDataCollector implements BanknoteDataCollector {

    private ColnectCrawlerClient crawlerClient;
    private DataWriter dataWriter;

    @Override
    public void collectBanknoteData() {
        // start process to collect data
    }

    public void readBanknoteData() {

    }
}
