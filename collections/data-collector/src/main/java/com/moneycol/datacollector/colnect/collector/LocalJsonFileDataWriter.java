package com.moneycol.datacollector.colnect.collector;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneycol.datacollector.colnect.model.BanknotesDataSet;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class LocalJsonFileDataWriter implements DataWriter {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void writeDataBatch(BanknotesDataSet banknotesDataSet) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

        String country = banknotesDataSet.getCountry();
        String filePath = String.format("/tmp/%s.json", country);

        writeJsonToFile(banknotesDataSet, country, filePath);
    }

    @Override
    public void saveState(CrawlingProcessState crawlingProcessState) {

    }

    @Override
    public CrawlingProcessState findState() {
        return null;
    }

    @Override
    public void deleteState() {

    }

    private void writeJsonToFile(BanknotesDataSet banknotesDataSet, String country, String filePath) {
        try {
            objectMapper.writeValue(new File(filePath), banknotesDataSet);
        } catch (IOException e) {
            log.error("Error writing batch of data for {}", country, e);
        }
    }
}
