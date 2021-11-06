package com.moneycol.indexer;

import com.moneycol.indexer.infra.JsonWriter;
import com.moneycol.indexer.worker.BanknoteData;
import com.moneycol.indexer.worker.BanknotesDataSet;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class TestHelper {

    private final JsonWriter jsonWriter = JsonWriter.builder().build();

    public File readFile(String testFile) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(testFile).getFile());
        assertThat(file.exists(), is(true));
        return file;
    }

    public String readFileToString(String filePath) {
        try {
            File file = readFile(filePath);
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            fail("Error reading test file " + filePath, e);
            return null;
        }
    }

    public BanknoteData readBanknoteDataFromJsonFile(String filePath) {
        String json = readFileToString(filePath);
        return jsonWriter.toObject(json, BanknoteData.class);
    }

    public Map<String, Object> readBanknoteDataToMap(String filePath) {
        String json = readFileToString(filePath);
        return jsonWriter.toObject(json, Map.class);
    }

    public BanknotesDataSet readBanknoteDataSetFromJsonFile(String filePath) {
        String json = readFileToString(filePath);
        return jsonWriter.toObject(json, BanknotesDataSet.class);
    }
}
