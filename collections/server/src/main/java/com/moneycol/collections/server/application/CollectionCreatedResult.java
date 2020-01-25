package com.moneycol.collections.server.application;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectionCreatedResult {
    private final String collectionId;
    private final String name;
    private final String description;
    private final String collectorId;

    @JsonCreator
    public CollectionCreatedResult(@JsonProperty("collectionId")  String collectionId,
                                   @JsonProperty("name") String name,
                                   @JsonProperty("description") String description,
                                   @JsonProperty("collectorId") String collectorId) {
        this.collectionId = collectionId;
        this.name = name;
        this.description = description;
        this.collectorId = collectorId;
    }
}
