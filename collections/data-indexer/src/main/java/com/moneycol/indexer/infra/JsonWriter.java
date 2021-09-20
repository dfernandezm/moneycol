package com.moneycol.indexer.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.tracker.GenericTask;
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

    public <T> T toObject(String jsonString, Class<T> clazz) {
        setupMapper();
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.error("Error writing json", e);
            throw new JsonConversionException("Error writing json");
        }
    }

    public GenericTask<FilesBatch> toGenericTask(String jsonString) {

        setupMapper();
        try {
            return objectMapper.readValue(jsonString, new TypeReference<GenericTask<FilesBatch>>() {
            });
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
