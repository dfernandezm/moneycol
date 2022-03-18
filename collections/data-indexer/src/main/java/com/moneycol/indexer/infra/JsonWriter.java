package com.moneycol.indexer.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneycol.indexer.batcher.FilesBatch;
import com.moneycol.indexer.tracker.IntermediateTask;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Singleton
@Builder
@NoArgsConstructor
public class JsonWriter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private void setupMapper() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
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

    public <T> Map<String, String> toMap(T object) {
        return toObject(prettyPrint(object), Map.class);
    }

    public IntermediateTask<FilesBatch> toGenericTask(String jsonString) {
        setupMapper();
        try {
            return objectMapper.readValue(jsonString, new TypeReference<>() { });
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
