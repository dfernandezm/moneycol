package com.moneycol.datacollector.colnect.collector;

import com.moneycol.datacollector.colnect.model.BanknotesDataSet;

public interface DataWriter {
    void writeDataBatch(BanknotesDataSet banknotesDataSet);
    void saveState(CrawlingProcessState crawlingProcessState);
    CrawlingProcessState findState();
}
