package com.moneycol.datacollector.colnect.collector;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneycol.datacollector.exceptions.JsonWritingException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class JsonWriter {

    private ObjectMapper objectMapper = new ObjectMapper();

    private void setupMapper() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
    }

    public <T> void asJsonFile(String filePath, T object) {
        try {
            objectMapper.writeValue(new File(filePath), object);
        } catch (IOException e) {
            log.error("Error writing json", e);
            throw new JsonWritingException("Error writing json");
        }
    }

    public <T> String asJsonString(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            log.error("Error writing json", e);
            throw new JsonWritingException("Error writing json");
        }
    }
}
