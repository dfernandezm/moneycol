package com.moneycol.datacollector.colnect.collector;

import com.moneycol.datacollector.colnect.BanknotesDataSet;

public interface DataWriter {
    void writeDataBatch(BanknotesDataSet banknotesDataSet);
}
