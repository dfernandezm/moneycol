package com.moneycol.datacollector.colnect.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneycol.datacollector.exceptions.JsonConversionException;
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
            throw new JsonConversionException("Error writing json");
        }
    }

    public <T> String asJsonString(T object) {
        setupMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            log.error("Error writing json", e);
            throw new JsonConversionException("Error writing json");
        }
    }

    public CrawlingProcessState toObject(String jsonString) {
        setupMapper();
        try {
            return objectMapper.readValue(jsonString, CrawlingProcessState.class);
        } catch (IOException e) {
            log.error("Error writing json", e);
            throw new JsonConversionException("Error writing json");
        }
    }

    public <T> String prettyPrint(T object) {
        setupMapper();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Error printing json", e);
            return "";
        }
    }
}
